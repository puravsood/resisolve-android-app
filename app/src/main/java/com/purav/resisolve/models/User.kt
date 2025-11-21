package com.purav.resisolve.models

data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val societyName: String = "",
    val flatNumber: String = "",
    val emergencyContact: String = "",
    val userType: String = "resident", // "resident" or "admin"
    val createdAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", "", "", "", "resident", 0L)

    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "fullName" to fullName,
            "phoneNumber" to phoneNumber,
            "address" to address,
            "societyName" to societyName,
            "flatNumber" to flatNumber,
            "emergencyContact" to emergencyContact,
            "userType" to userType,
            "createdAt" to createdAt
        )
    }
}
