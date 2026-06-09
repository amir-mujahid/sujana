package com.sujana.shared

enum class NotificationCategory {
    REQUEST_UPDATE,    // Contributor: pickup request lifecycle
    ASSIGNMENT_UPDATE, // Rider: assignment changes
    SCHOOL_COLLECTION, // School Admin/Staff: incoming collections
    DISPATCH_ALERT,    // MPS Dispatcher: new requests, SLA/overdue, rider issues
    ADMIN_SUMMARY,     // MPS Admin: operational summary, escalations
    SYSTEM,            // Super Admin: system/tenant-level alerts
}
