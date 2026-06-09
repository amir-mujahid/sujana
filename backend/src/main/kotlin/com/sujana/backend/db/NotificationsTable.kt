package com.sujana.backend.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object NotificationsTable : Table("notifications") {
    val id        = uuid("id").autoGenerate()
    val userId    = uuid("user_id").references(UsersTable.id)
    val category  = text("category")
    val title     = text("title")
    val body      = text("body")
    val deeplink  = text("deeplink").nullable()
    val dataJson  = text("data_json").nullable()
    val readAt    = timestamp("read_at").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}

object NotificationPrefsTable : Table("notification_prefs") {
    val userId   = uuid("user_id").references(UsersTable.id)
    val category = text("category")
    val muted    = bool("muted").default(false)

    override val primaryKey = PrimaryKey(userId, category)
}

object DeviceTokensTable : Table("device_tokens") {
    val id        = uuid("id").autoGenerate()
    val userId    = uuid("user_id").references(UsersTable.id)
    val token     = text("token").uniqueIndex()
    val platform  = text("platform").default("android")
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}
