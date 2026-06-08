package com.sujana.backend.feature.assignment

import com.sujana.backend.db.AssignmentsTable
import com.sujana.backend.db.RequestsTable
import com.sujana.backend.db.SchoolsTable
import com.sujana.backend.db.UsersTable
import com.sujana.backend.feature.request.toRequestDto
import com.sujana.backend.plugins.UserPrincipal
import com.sujana.shared.AssignmentStatus
import com.sujana.shared.RequestStatus
import com.sujana.shared.Role
import com.sujana.shared.dto.AssignmentDto
import com.sujana.shared.dto.CreateAssignmentRequest
import com.sujana.shared.dto.RequestDto
import com.sujana.shared.dto.TransitionRequest
import com.sujana.shared.dto.UserDto
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.OffsetDateTime
import java.util.UUID

object AssignmentService {

    // --------------- Dispatcher: create assignment ---------------

    fun createAssignment(principal: UserPrincipal, body: CreateAssignmentRequest): AssignmentDto = transaction {
        val dispatcher = userByUid(principal.uid)
        val dispRole = Role.valueOf(dispatcher[UsersTable.role])
        if (dispRole != Role.MPS_DISPATCHER && dispRole != Role.MPS_ADMIN && dispRole != Role.SUPER_ADMIN) {
            throw SecurityException("Only dispatchers may create assignments")
        }

        val requestUuid = UUID.fromString(body.requestId)
        val riderUuid = UUID.fromString(body.riderId)

        val requestRow = RequestsTable.selectAll()
            .where { RequestsTable.id eq requestUuid }
            .singleOrNull() ?: throw NoSuchElementException("Request not found")

        if (RequestStatus.valueOf(requestRow[RequestsTable.status]) != RequestStatus.PENDING) {
            throw IllegalArgumentException("Only PENDING requests can be assigned")
        }

        val riderRow = UsersTable.selectAll()
            .where { UsersTable.id eq riderUuid }
            .singleOrNull() ?: throw NoSuchElementException("Rider not found")

        if (Role.valueOf(riderRow[UsersTable.role]) != Role.RIDER) {
            throw IllegalArgumentException("Target user is not a RIDER")
        }

        val now = OffsetDateTime.now()
        val newId = AssignmentsTable.insert {
            it[AssignmentsTable.requestId]    = requestUuid
            it[AssignmentsTable.riderId]      = riderUuid
            it[AssignmentsTable.dispatcherId] = dispatcher[UsersTable.id]
            it[AssignmentsTable.status]       = AssignmentStatus.ASSIGNED.name
            it[AssignmentsTable.assignedAt]   = now
        }[AssignmentsTable.id]

        RequestsTable.update({ RequestsTable.id eq requestUuid }) {
            it[RequestsTable.status]    = RequestStatus.ASSIGNED.name
            it[RequestsTable.updatedAt] = now
        }

        val row = AssignmentsTable.selectAll().where { AssignmentsTable.id eq newId }.single()
        row.toDto()
    }

    // --------------- List assignments (role-scoped) ---------------

    fun listAssignments(principal: UserPrincipal): List<AssignmentDto> = transaction {
        val user = userByUid(principal.uid)
        val role = Role.valueOf(user[UsersTable.role])

        val rows = when (role) {
            Role.RIDER -> AssignmentsTable.selectAll()
                .where { AssignmentsTable.riderId eq user[UsersTable.id] }
                .orderBy(AssignmentsTable.assignedAt, SortOrder.DESC)

            Role.MPS_DISPATCHER, Role.MPS_ADMIN, Role.SUPER_ADMIN ->
                AssignmentsTable.selectAll()
                    .orderBy(AssignmentsTable.assignedAt, SortOrder.DESC)

            else -> throw SecurityException("Access denied")
        }

        rows.map { it.toDto() }
    }

    // --------------- Rider: transition assignment ---------------

    fun transitionAssignment(
        principal: UserPrincipal,
        assignmentId: String,
        body: TransitionRequest,
    ): AssignmentDto = transaction {
        val user = userByUid(principal.uid)
        val role = Role.valueOf(user[UsersTable.role])
        val uuid = UUID.fromString(assignmentId)

        val row = AssignmentsTable.selectAll()
            .where { AssignmentsTable.id eq uuid }
            .singleOrNull() ?: throw NoSuchElementException("Assignment not found")

        val current = AssignmentStatus.valueOf(row[AssignmentsTable.status])
        val next = body.status

        validateTransition(current, next, role, row[AssignmentsTable.riderId], user[UsersTable.id])

        val now = OffsetDateTime.now()
        AssignmentsTable.update({ AssignmentsTable.id eq uuid }) { upd ->
            upd[AssignmentsTable.status] = next.name
            when (next) {
                AssignmentStatus.ACCEPTED  -> upd[AssignmentsTable.acceptedAt]  = now
                AssignmentStatus.COLLECTED -> upd[AssignmentsTable.collectedAt] = now
                AssignmentStatus.DELIVERED -> upd[AssignmentsTable.deliveredAt] = now
                AssignmentStatus.COMPLETED -> upd[AssignmentsTable.completedAt] = now
                else -> Unit
            }
        }

        // Mirror to request status
        val requestUuid = row[AssignmentsTable.requestId]
        val mirroredRequestStatus = mirrorStatus(next)
        RequestsTable.update({ RequestsTable.id eq requestUuid }) {
            it[RequestsTable.status]    = mirroredRequestStatus.name
            it[RequestsTable.updatedAt] = now
        }

        AssignmentsTable.selectAll().where { AssignmentsTable.id eq uuid }.single().toDto()
    }

    // --------------- Riders list (for dispatcher picker) ---------------

    fun listRiders(): List<UserDto> = transaction {
        UsersTable.selectAll()
            .where { UsersTable.role eq Role.RIDER.name }
            .orderBy(UsersTable.name, SortOrder.ASC)
            .map { row ->
                UserDto(
                    id    = row[UsersTable.id].toString(),
                    name  = row[UsersTable.name],
                    email = row[UsersTable.email],
                    role  = Role.RIDER,
                )
            }
    }

    // --------------- Helpers ---------------

    private fun validateTransition(
        current: AssignmentStatus,
        next: AssignmentStatus,
        role: Role,
        riderId: java.util.UUID,
        actorId: java.util.UUID,
    ) {
        val allowed: Map<AssignmentStatus, Set<AssignmentStatus>> = mapOf(
            AssignmentStatus.ASSIGNED  to setOf(AssignmentStatus.ACCEPTED, AssignmentStatus.CANCELLED),
            AssignmentStatus.ACCEPTED  to setOf(AssignmentStatus.COLLECTED, AssignmentStatus.CANCELLED),
            AssignmentStatus.COLLECTED to setOf(AssignmentStatus.DELIVERED),
            AssignmentStatus.DELIVERED to setOf(AssignmentStatus.COMPLETED),
            AssignmentStatus.COMPLETED to emptySet(),
            AssignmentStatus.CANCELLED to emptySet(),
        )

        if (next !in (allowed[current] ?: emptySet())) {
            throw IllegalArgumentException("Transition $current → $next is not allowed")
        }

        when (next) {
            AssignmentStatus.CANCELLED -> {
                val canCancel = role == Role.MPS_DISPATCHER || role == Role.MPS_ADMIN ||
                        role == Role.SUPER_ADMIN || (role == Role.RIDER && actorId == riderId)
                if (!canCancel) throw SecurityException("You may not cancel this assignment")
            }
            else -> {
                // Only the assigned rider may advance status
                if (role != Role.RIDER || actorId != riderId) {
                    throw SecurityException("Only the assigned rider may advance this assignment")
                }
            }
        }
    }

    private fun mirrorStatus(assignmentStatus: AssignmentStatus): RequestStatus = when (assignmentStatus) {
        AssignmentStatus.ASSIGNED,
        AssignmentStatus.ACCEPTED  -> RequestStatus.ASSIGNED
        AssignmentStatus.COLLECTED -> RequestStatus.COLLECTED
        AssignmentStatus.DELIVERED -> RequestStatus.DELIVERED
        AssignmentStatus.COMPLETED -> RequestStatus.COMPLETED
        AssignmentStatus.CANCELLED -> RequestStatus.PENDING
    }

    private fun userByUid(uid: String) =
        UsersTable.selectAll()
            .where { UsersTable.firebaseUid eq uid }
            .singleOrNull() ?: error("User not found")

    private fun ResultRow.toDto(): AssignmentDto {
        val requestRow = RequestsTable.selectAll()
            .where { RequestsTable.id eq this[AssignmentsTable.requestId] }
            .single()
        val schoolName = requestRow[RequestsTable.dropoffSchoolId]?.let { sid ->
            SchoolsTable.selectAll().where { SchoolsTable.id eq sid }.singleOrNull()?.get(SchoolsTable.name)
        }
        val riderRow = UsersTable.selectAll()
            .where { UsersTable.id eq this[AssignmentsTable.riderId] }
            .single()

        return AssignmentDto(
            id           = this[AssignmentsTable.id].toString(),
            requestId    = this[AssignmentsTable.requestId].toString(),
            riderId      = this[AssignmentsTable.riderId].toString(),
            riderName    = riderRow[UsersTable.name],
            dispatcherId = this[AssignmentsTable.dispatcherId]?.toString(),
            status       = AssignmentStatus.valueOf(this[AssignmentsTable.status]),
            request      = requestRow.toRequestDto(schoolName),
            assignedAt   = this[AssignmentsTable.assignedAt].toString(),
            acceptedAt   = this[AssignmentsTable.acceptedAt]?.toString(),
            collectedAt  = this[AssignmentsTable.collectedAt]?.toString(),
            deliveredAt  = this[AssignmentsTable.deliveredAt]?.toString(),
            completedAt  = this[AssignmentsTable.completedAt]?.toString(),
        )
    }
}
