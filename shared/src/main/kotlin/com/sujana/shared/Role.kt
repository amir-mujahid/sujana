package com.sujana.shared

import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    SUPER_ADMIN,
    MPS_ADMIN,
    MPS_DISPATCHER,
    SCHOOL_ADMIN,
    SCHOOL_STAFF,
    RIDER,
    CONTRIBUTOR,
}
