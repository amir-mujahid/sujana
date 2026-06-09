package com.sujana.backend.feature.request

import com.sujana.backend.db.AssignmentsTable
import com.sujana.backend.db.RequestsTable
import com.sujana.backend.db.SchoolsTable
import com.sujana.backend.db.UsersTable
import com.sujana.backend.plugins.UserPrincipal
import com.sujana.shared.AssignmentStatus
import com.sujana.shared.RequestStatus
import com.sujana.shared.RequestType
import com.sujana.shared.Role
import com.sujana.shared.dto.CreateRequestRequest
import com.sujana.shared.dto.RequestDto
import com.sujana.shared.dto.SchoolDto
import java.time.OffsetDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object RequestService {

    fun createRequest(principal: UserPrincipal, body: CreateRequestRequest): RequestDto = transaction {
        val user = UsersTable.selectAll()
            .where { UsersTable.firebaseUid eq principal.uid }
            .singleOrNull() ?: error("User not found")

        val newId = RequestsTable.insert {
            it[type]            = body.type.name
            it[requesterId]     = user[UsersTable.id]
            it[status]          = RequestStatus.PENDING.name
            it[pickupLat]       = body.pickupLat
            it[pickupLng]       = body.pickupLng
            it[pickupAddress]   = body.pickupAddress
            it[dropoffSchoolId] = body.dropoffSchoolId?.let { sid -> UUID.fromString(sid) }
            it[notes]           = body.notes
            it[photoUrl]        = body.photoUrl
        }[RequestsTable.id]

        val row = RequestsTable.selectAll().where { RequestsTable.id eq newId }.single()
        row.toRequestDto(schoolInfoFor(row[RequestsTable.dropoffSchoolId]))
    }

    fun listRequests(principal: UserPrincipal): List<RequestDto> = transaction {
        val user = UsersTable.selectAll()
            .where { UsersTable.firebaseUid eq principal.uid }
            .singleOrNull() ?: error("User not found")

        val role = Role.valueOf(user[UsersTable.role])
        val query = when (role) {
            Role.MPS_DISPATCHER, Role.MPS_ADMIN, Role.SUPER_ADMIN ->
                RequestsTable.selectAll()
            else ->
                RequestsTable.selectAll().where { RequestsTable.requesterId eq user[UsersTable.id] }
        }.orderBy(RequestsTable.createdAt, SortOrder.DESC)

        query.map { row -> row.toRequestDto(schoolInfoFor(row[RequestsTable.dropoffSchoolId])) }
    }

    fun getRequest(principal: UserPrincipal, requestId: String): RequestDto = transaction {
        val user = UsersTable.selectAll()
            .where { UsersTable.firebaseUid eq principal.uid }
            .singleOrNull() ?: error("User not found")

        val role = Role.valueOf(user[UsersTable.role])
        val uuid = UUID.fromString(requestId)
        val row = RequestsTable.selectAll()
            .where { RequestsTable.id eq uuid }
            .singleOrNull() ?: throw NoSuchElementException("Request not found")

        val isOwner = row[RequestsTable.requesterId] == user[UsersTable.id]
        val canViewAll = role == Role.MPS_DISPATCHER || role == Role.MPS_ADMIN ||
                role == Role.SUPER_ADMIN || role == Role.RIDER
        if (!isOwner && !canViewAll) throw SecurityException("Access denied")

        row.toRequestDto(
            school       = schoolInfoFor(row[RequestsTable.dropoffSchoolId]),
            assignmentId = assignmentIdFor(row[RequestsTable.id]),
        )
    }

    fun cancelRequest(principal: UserPrincipal, requestId: String): RequestDto = transaction {
        val user = UsersTable.selectAll()
            .where { UsersTable.firebaseUid eq principal.uid }
            .singleOrNull() ?: error("User not found")

        val role = Role.valueOf(user[UsersTable.role])
        val uuid = UUID.fromString(requestId)
        val row = RequestsTable.selectAll()
            .where { RequestsTable.id eq uuid }
            .singleOrNull() ?: throw NoSuchElementException("Request not found")

        val isOwner = row[RequestsTable.requesterId] == user[UsersTable.id]
        val canCancel = isOwner || role == Role.MPS_DISPATCHER || role == Role.SUPER_ADMIN
        if (!canCancel) throw SecurityException("Access denied")

        val currentStatus = RequestStatus.valueOf(row[RequestsTable.status])
        if (currentStatus == RequestStatus.COMPLETED || currentStatus == RequestStatus.CANCELLED) {
            throw IllegalArgumentException("Cannot cancel a request with status $currentStatus")
        }

        RequestsTable.update({ RequestsTable.id eq uuid }) {
            it[status]    = RequestStatus.CANCELLED.name
            it[updatedAt] = OffsetDateTime.now()
        }

        val updated = RequestsTable.selectAll().where { RequestsTable.id eq uuid }.single()
        updated.toRequestDto(schoolInfoFor(updated[RequestsTable.dropoffSchoolId]))
    }

    fun listAvailableRequests(principal: UserPrincipal): List<RequestDto> = transaction {
        val user = UsersTable.selectAll()
            .where { UsersTable.firebaseUid eq principal.uid }
            .singleOrNull() ?: error("User not found")

        if (Role.valueOf(user[UsersTable.role]) != Role.RIDER) {
            throw SecurityException("Only riders may browse available requests")
        }

        RequestsTable.selectAll()
            .where {
                (RequestsTable.type eq RequestType.CONTRIBUTOR.name) and
                (RequestsTable.status eq RequestStatus.PENDING.name)
            }
            .orderBy(RequestsTable.createdAt, SortOrder.DESC)
            .map { row -> row.toRequestDto(schoolInfoFor(row[RequestsTable.dropoffSchoolId])) }
    }

    fun listNearbyRequests(
        principal: UserPrincipal,
        lat: Double,
        lng: Double,
        radiusMetres: Double,
    ): List<RequestDto> = transaction {
        val user = UsersTable.selectAll()
            .where { UsersTable.firebaseUid eq principal.uid }
            .singleOrNull() ?: error("User not found")

        if (Role.valueOf(user[UsersTable.role]) != Role.RIDER) {
            throw SecurityException("Only riders may browse nearby requests")
        }

        RequestsTable.selectAll()
            .where {
                (RequestsTable.type eq RequestType.CONTRIBUTOR.name) and
                (RequestsTable.status eq RequestStatus.PENDING.name)
            }
            .orderBy(RequestsTable.createdAt, SortOrder.DESC)
            .filter { row ->
                haversineMetres(lat, lng, row[RequestsTable.pickupLat], row[RequestsTable.pickupLng]) <= radiusMetres
            }
            .map { row -> row.toRequestDto(schoolInfoFor(row[RequestsTable.dropoffSchoolId])) }
    }

    fun acceptRequest(principal: UserPrincipal, requestId: String): RequestDto = transaction {
        val rider = UsersTable.selectAll()
            .where { UsersTable.firebaseUid eq principal.uid }
            .singleOrNull() ?: error("User not found")

        if (Role.valueOf(rider[UsersTable.role]) != Role.RIDER) {
            throw SecurityException("Only riders may accept requests")
        }

        val uuid = UUID.fromString(requestId)
        val requestRow = RequestsTable.selectAll()
            .where { RequestsTable.id eq uuid }
            .singleOrNull() ?: throw NoSuchElementException("Request not found")

        if (RequestType.valueOf(requestRow[RequestsTable.type]) != RequestType.CONTRIBUTOR) {
            throw IllegalArgumentException("Only CONTRIBUTOR requests can be self-assigned; SCHOOL requests require a dispatcher")
        }

        if (RequestStatus.valueOf(requestRow[RequestsTable.status]) != RequestStatus.PENDING) {
            throw IllegalArgumentException("Request is no longer available (status: ${requestRow[RequestsTable.status]})")
        }

        val now = OffsetDateTime.now()
        AssignmentsTable.insert {
            it[AssignmentsTable.requestId]  = uuid
            it[AssignmentsTable.riderId]    = rider[UsersTable.id]
            it[AssignmentsTable.status]     = AssignmentStatus.ASSIGNED.name
            it[AssignmentsTable.assignedAt] = now
        }

        RequestsTable.update({ RequestsTable.id eq uuid }) {
            it[RequestsTable.status]    = RequestStatus.ASSIGNED.name
            it[RequestsTable.updatedAt] = now
        }

        val updated = RequestsTable.selectAll().where { RequestsTable.id eq uuid }.single()
        updated.toRequestDto(schoolInfoFor(updated[RequestsTable.dropoffSchoolId]))
    }

    fun listSchools(): List<SchoolDto> = transaction {
        SchoolsTable.selectAll()
            .orderBy(SchoolsTable.name, SortOrder.ASC)
            .map { row ->
                SchoolDto(
                    id   = row[SchoolsTable.id].toString(),
                    name = row[SchoolsTable.name],
                    lat  = row[SchoolsTable.lat],
                    lng  = row[SchoolsTable.lng],
                )
            }
    }

    private fun schoolInfoFor(schoolId: java.util.UUID?): SchoolInfo? =
        schoolId?.let { sid ->
            SchoolsTable.selectAll().where { SchoolsTable.id eq sid }
                .singleOrNull()?.let { row ->
                    SchoolInfo(
                        name = row[SchoolsTable.name],
                        lat  = row[SchoolsTable.lat],
                        lng  = row[SchoolsTable.lng],
                    )
                }
        }

    private fun assignmentIdFor(requestId: java.util.UUID): String? =
        AssignmentsTable.selectAll()
            .where { AssignmentsTable.requestId eq requestId }
            .orderBy(AssignmentsTable.assignedAt, SortOrder.DESC)
            .firstOrNull()
            ?.get(AssignmentsTable.id)
            ?.toString()
}

private fun haversineMetres(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val R = 6_371_000.0
    val dLat = (lat2 - lat1) * PI / 180.0
    val dLng = (lng2 - lng1) * PI / 180.0
    val a = sin(dLat / 2).pow(2) +
            cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) * sin(dLng / 2).pow(2)
    return 2 * R * asin(sqrt(a))
}
