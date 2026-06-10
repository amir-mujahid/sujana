package com.sujana.backend.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone

object RequestsTable : Table("requests") {
    val id              = uuid("id").autoGenerate()
    val type            = text("type").default("CONTRIBUTOR")
    val requesterId     = uuid("requester_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val status          = text("status").default("PENDING")
    val pickupLat       = double("pickup_lat")
    val pickupLng       = double("pickup_lng")
    val pickupAddress   = text("pickup_address").default("")
    val dropoffSchoolId = uuid("dropoff_school_id").references(SchoolsTable.id).nullable()
    val notes             = text("notes").nullable()
    val photoUrl          = text("photo_url").nullable()
    val scheduledFor      = timestampWithTimeZone("scheduled_for").nullable()
    val requesterSchoolId = uuid("requester_school_id").references(SchoolsTable.id).nullable()
    val createdAt         = timestampWithTimeZone("created_at")
    val updatedAt         = timestampWithTimeZone("updated_at")

    override val primaryKey = PrimaryKey(id)
}
