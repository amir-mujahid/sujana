package com.sujana.backend.feature.auth

import com.google.firebase.auth.FirebaseAuth
import com.sujana.backend.db.UsersTable
import com.sujana.backend.plugins.UserPrincipal
import com.sujana.shared.Role
import com.sujana.shared.dto.MeResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object AuthService {

    fun upsertAndGetMe(principal: UserPrincipal): MeResponse = transaction {
        val superAdminEmail = System.getenv("SUPER_ADMIN_EMAIL")

        // Get or create the user row
        val existing = UsersTable.selectAll()
            .where { UsersTable.firebaseUid eq principal.uid }
            .firstOrNull()

        val row = if (existing == null) {
            UsersTable.insert {
                it[firebaseUid] = principal.uid
                it[email] = principal.email ?: ""
                it[name] = principal.name ?: principal.email?.substringBefore('@') ?: principal.uid
                it[role] = Role.CONTRIBUTOR.name
            }
            UsersTable.selectAll()
                .where { UsersTable.firebaseUid eq principal.uid }
                .single()
        } else {
            existing
        }

        // Bootstrap check runs on every /auth/me while the user is still CONTRIBUTOR.
        // Promotion requires ALL of:
        //   (a) SUPER_ADMIN_EMAIL env var is configured
        //   (b) email is verified in Firebase (not just claimed)
        //   (c) the current user's email matches the bootstrap address
        //   (d) no SUPER_ADMIN row exists yet (single-use)
        val currentRole = row[UsersTable.role]
        val resolvedRole = if (
            currentRole == Role.CONTRIBUTOR.name &&
            superAdminEmail != null &&
            principal.emailVerified &&
            principal.email.equals(superAdminEmail, ignoreCase = true) &&
            UsersTable.selectAll()
                .where { UsersTable.role eq Role.SUPER_ADMIN.name }
                .count() == 0L
        ) {
            UsersTable.update({ UsersTable.firebaseUid eq principal.uid }) {
                it[role] = Role.SUPER_ADMIN.name
            }
            Role.SUPER_ADMIN.name
        } else {
            currentRole
        }

        setCustomClaims(principal.uid, resolvedRole, row[UsersTable.tenantId]?.toString())

        MeResponse(
            id = row[UsersTable.id].toString(),
            firebaseUid = row[UsersTable.firebaseUid],
            name = row[UsersTable.name],
            email = row[UsersTable.email],
            role = Role.valueOf(resolvedRole),
            tenantId = row[UsersTable.tenantId]?.toString(),
            phone = row[UsersTable.phone],
        )
    }

    private fun setCustomClaims(uid: String, role: String, tenantId: String?) {
        if (com.google.firebase.FirebaseApp.getApps().isEmpty()) return
        val claims = mutableMapOf<String, Any>("role" to role)
        if (tenantId != null) claims["tenantId"] = tenantId
        FirebaseAuth.getInstance().setCustomUserClaims(uid, claims)
    }
}
