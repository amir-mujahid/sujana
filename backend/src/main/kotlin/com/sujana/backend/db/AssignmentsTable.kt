package com.sujana.backend.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone

object AssignmentsTable : Table("assignments") {
    val id           = uuid("id").autoGenerate()
    val requestId    = uuid("request_id").references(RequestsTable.id, onDelete = ReferenceOption.CASCADE)
    val riderId      = uuid("rider_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val dispatcherId = uuid("dispatcher_id").references(UsersTable.id).nullable()
    val status       = text("status").default("ASSIGNED")
    val assignedAt   = timestampWithTimeZone("assigned_at")
    val acceptedAt   = timestampWithTimeZone("accepted_at").nullable()
    val collectedAt  = timestampWithTimeZone("collected_at").nullable()
    val deliveredAt  = timestampWithTimeZone("delivered_at").nullable()
    val completedAt  = timestampWithTimeZone("completed_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
