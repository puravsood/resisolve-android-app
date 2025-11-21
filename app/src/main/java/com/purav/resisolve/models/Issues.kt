package com.purav.resisolve.models

data class Issue(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val priority: String = "Medium",
    val status: String = "Received",
    val reportedBy: String = "", // User UID
    val reportedByName: String = "",
    val reportedByFlat: String = "",
    val societyName: String = "",
    val location: String = "",
    val assignedTo: String = "",
    val adminNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val timeline: List<TimelineEvent> = emptyList()
) {
    constructor() : this("", "", "", "", "Medium", "Received", "", "", "", "", "", "", "", 0L, 0L, emptyList())

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "category" to category,
            "priority" to priority,
            "status" to status,
            "reportedBy" to reportedBy,
            "reportedByName" to reportedByName,
            "reportedByFlat" to reportedByFlat,
            "societyName" to societyName,
            "location" to location,
            "assignedTo" to assignedTo,
            "adminNotes" to adminNotes,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "timeline" to timeline.map { it.toMap() }
        )
    }
}

data class TimelineEvent(
    val status: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val by: String = ""
) {
    constructor() : this("", "", 0L, "")

    fun toMap(): Map<String, Any> {
        return mapOf(
            "status" to status,
            "description" to description,
            "timestamp" to timestamp,
            "by" to by
        )
    }
}
