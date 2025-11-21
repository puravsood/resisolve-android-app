package com.purav.resisolve.models

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // "issue_update", "community_announcement", "admin_comment"
    val issueId: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", "", false, 0L)

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "title" to title,
            "message" to message,
            "type" to type,
            "issueId" to issueId,
            "isRead" to isRead,
            "timestamp" to timestamp
        )
    }
}
