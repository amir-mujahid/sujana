package com.sujana.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sujana.domain.model.User
import com.sujana.shared.Role
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "session")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val FIREBASE_UID = stringPreferencesKey("firebase_uid")
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
        val ROLE = stringPreferencesKey("role")
        val TENANT_ID = stringPreferencesKey("tenant_id")
        val PHONE = stringPreferencesKey("phone")
    }

    val currentUser: Flow<User?> = context.dataStore.data.map { prefs ->
        val id = prefs[Keys.USER_ID] ?: return@map null
        val uid = prefs[Keys.FIREBASE_UID] ?: return@map null
        val name = prefs[Keys.NAME] ?: return@map null
        val email = prefs[Keys.EMAIL] ?: return@map null
        val roleStr = prefs[Keys.ROLE] ?: return@map null
        User(
            id = id,
            firebaseUid = uid,
            name = name,
            email = email,
            role = Role.valueOf(roleStr),
            tenantId = prefs[Keys.TENANT_ID],
            phone = prefs[Keys.PHONE],
        )
    }

    suspend fun save(user: User) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_ID] = user.id
            prefs[Keys.FIREBASE_UID] = user.firebaseUid
            prefs[Keys.NAME] = user.name
            prefs[Keys.EMAIL] = user.email
            prefs[Keys.ROLE] = user.role.name
            if (user.tenantId != null) prefs[Keys.TENANT_ID] = user.tenantId
            else prefs.remove(Keys.TENANT_ID)
            if (user.phone != null) prefs[Keys.PHONE] = user.phone
            else prefs.remove(Keys.PHONE)
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
