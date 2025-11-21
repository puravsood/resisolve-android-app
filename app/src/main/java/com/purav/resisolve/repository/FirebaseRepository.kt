package com.purav.resisolve.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.purav.resisolve.models.AppNotification
import com.purav.resisolve.models.Issue
import com.purav.resisolve.models.TimelineEvent
import com.purav.resisolve.models.User
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Authentication
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    // User Operations
    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.uid)
                .set(user.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(uid: String): Result<User> {
        return try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .get()
                .await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(uid)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(uid: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(uid)
                .delete()
                .await()
            auth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Issue Operations
    suspend fun createIssue(issue: Issue): Result<String> {
        return try {
            val docRef = firestore.collection("issues").document()
            val issueWithId = issue.copy(id = docRef.id)
            docRef.set(issueWithId.toMap()).await()

            // Create initial timeline event
            addTimelineEvent(
                issueWithId.id,
                TimelineEvent("Received", "Issue reported", System.currentTimeMillis(), issue.reportedByName)
            )

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getIssuesByUser(userId: String): Result<List<Issue>> {
        return try {
            val snapshot = firestore.collection("issues")
                .whereEqualTo("reportedBy", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val issues = snapshot.documents.mapNotNull { it.toObject(Issue::class.java) }
            Result.success(issues)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getIssuesBySociety(societyName: String): Result<List<Issue>> {
        return try {
            val snapshot = firestore.collection("issues")
                .whereEqualTo("societyName", societyName)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val issues = snapshot.documents.mapNotNull { it.toObject(Issue::class.java) }
            Result.success(issues)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getIssue(issueId: String): Result<Issue> {
        return try {
            val snapshot = firestore.collection("issues")
                .document(issueId)
                .get()
                .await()
            val issue = snapshot.toObject(Issue::class.java)
            if (issue != null) {
                Result.success(issue)
            } else {
                Result.failure(Exception("Issue not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateIssue(issueId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updateMap = updates.toMutableMap()
            updateMap["updatedAt"] = System.currentTimeMillis()

            firestore.collection("issues")
                .document(issueId)
                .update(updateMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTimelineEvent(issueId: String, event: TimelineEvent): Result<Unit> {
        return try {
            val issueRef = firestore.collection("issues").document(issueId)
            val issue = issueRef.get().await().toObject(Issue::class.java)

            if (issue != null) {
                val timeline = issue.timeline.toMutableList()
                timeline.add(event)
                issueRef.update("timeline", timeline.map { it.toMap() }).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteIssue(issueId: String): Result<Unit> {
        return try {
            firestore.collection("issues")
                .document(issueId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Notification Operations
    suspend fun createNotification(notification: AppNotification): Result<String> {
        return try {
            val docRef = firestore.collection("notifications").document()
            val notificationWithId = notification.copy(id = docRef.id)
            docRef.set(notificationWithId.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotifications(userId: String): Result<List<AppNotification>> {
        return try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val notifications = snapshot.documents.mapNotNull { it.toObject(AppNotification::class.java) }
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
