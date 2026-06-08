package com.sujana.backend.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone

object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val firebaseUid = text("firebase_uid")
    val name = text("name")
    val email = text("email")
    val role = text("role").default("CONTRIBUTOR")
    val tenantId = uuid("tenant_id").nullable()
    val phone = text("phone").nullable()
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}
