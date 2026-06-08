package com.sujana.backend.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestampWithTimeZone

object SchoolsTable : Table("schools") {
    val id        = uuid("id").autoGenerate()
    val name      = text("name")
    val lat       = double("lat")
    val lng       = double("lng")
    val tenantId  = uuid("tenant_id").nullable()
    val createdAt = timestampWithTimeZone("created_at")

    override val primaryKey = PrimaryKey(id)
}
