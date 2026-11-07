package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TikVideoRepository(
    private val db: FirebaseFirestore
) {
    // Resolves named database ID from string resources
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before performing write operations.")
    }

    // 1. Observe Videos Feed in real-time
    fun observeVideos(): Flow<List<FirestoreVideo>> {
        val path = "videos"
        return db.collection(path)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FirestoreVideo::class.java)
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }
    }

    // 2. Create Video
    suspend fun createVideo(video: FirestoreVideo): Result<String> {
        val uid = requireUserId()
        val path = "videos"
        return try {
            val docRef = db.collection(path).document()
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "creatorName" to video.creatorName,
                "creatorHandle" to video.creatorHandle,
                "caption" to video.caption,
                "soundTitle" to video.soundTitle,
                "likesCount" to 0L,
                "commentsCount" to 0L,
                "sharesCount" to 0L,
                "bookmarksCount" to 0L,
                "bgStartColor" to video.bgStartColor,
                "bgEndColor" to video.bgEndColor,
                "category" to video.category,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // 3. Toggle Like on Video
    suspend fun toggleLike(videoId: String, isCurrentlyLiked: Boolean): Result<Boolean> {
        val uid = requireUserId()
        val userLikeRef = db.collection("users").document(uid).collection("likes").document(videoId)
        val videoRef = db.collection("videos").document(videoId)
        return try {
            if (isCurrentlyLiked) {
                userLikeRef.delete().await()
                videoRef.update("likesCount", FieldValue.increment(-1L)).await()
                Result.success(false)
            } else {
                val likePayload = mapOf(
                    "videoId" to videoId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                userLikeRef.set(likePayload).await()
                videoRef.update("likesCount", FieldValue.increment(1L)).await()
                Result.success(true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "videos/$videoId")
            Result.failure(e)
        }
    }

    // 4. Toggle Save / Bookmark Video
    suspend fun toggleSave(videoId: String, isCurrentlySaved: Boolean): Result<Boolean> {
        val uid = requireUserId()
        val userSaveRef = db.collection("users").document(uid).collection("saved").document(videoId)
        val videoRef = db.collection("videos").document(videoId)
        return try {
            if (isCurrentlySaved) {
                userSaveRef.delete().await()
                videoRef.update("bookmarksCount", FieldValue.increment(-1L)).await()
                Result.success(false)
            } else {
                val savePayload = mapOf(
                    "videoId" to videoId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                userSaveRef.set(savePayload).await()
                videoRef.update("bookmarksCount", FieldValue.increment(1L)).await()
                Result.success(true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "videos/$videoId")
            Result.failure(e)
        }
    }

    // 5. Observe Comments for a video
    fun observeComments(videoId: String): Flow<List<FirestoreComment>> {
        val path = "videos/$videoId/comments"
        return db.collection("videos").document(videoId).collection("comments")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FirestoreComment::class.java)
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }
    }

    // 6. Add Comment
    suspend fun addComment(videoId: String, author: String, authorHandle: String, text: String): Result<String> {
        val uid = requireUserId()
        val path = "videos/$videoId/comments"
        return try {
            val commentRef = db.collection("videos").document(videoId).collection("comments").document()
            val commentPayload = hashMapOf<String, Any>(
                "videoId" to videoId,
                "userId" to uid,
                "author" to author,
                "authorHandle" to authorHandle,
                "text" to text,
                "likes" to 0L,
                "createdAt" to FieldValue.serverTimestamp()
            )
            commentRef.set(commentPayload).await()
            db.collection("videos").document(videoId).update("commentsCount", FieldValue.increment(1L)).await()
            Result.success(commentRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // 7. Observe Chats
    fun observeChats(userId: String): Flow<List<FirestoreChat>> {
        val path = "users/$userId/chats"
        return db.collection("users").document(userId).collection("chats")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FirestoreChat::class.java)
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }
    }

    // 8. Observe Notifications
    fun observeNotifications(userId: String): Flow<List<FirestoreNotification>> {
        val path = "users/$userId/notifications"
        return db.collection("users").document(userId).collection("notifications")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FirestoreNotification::class.java)
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.LIST, path)
                }
                throw error
            }
    }

    // 9. Mark All Notifications Read
    suspend fun markAllNotificationsRead(userId: String) {
        val path = "users/$userId/notifications"
        try {
            val snapshot = db.collection("users").document(userId).collection("notifications").get().await()
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    // 10. Get User Profile
    suspend fun getUserProfile(userId: String): FirestoreUserProfile? {
        val path = "users/$userId"
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(FirestoreUserProfile::class.java)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }

    // 11. Save User Profile
    suspend fun saveUserProfile(profile: FirestoreUserProfile): Result<Unit> {
        val uid = requireUserId()
        val path = "users/$uid"
        return try {
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "name" to profile.name,
                "handle" to profile.handle,
                "bio" to profile.bio,
                "followingCount" to profile.followingCount,
                "followersCount" to profile.followersCount,
                "likesCount" to profile.likesCount,
                "createdAt" to (profile.createdAt ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // 12. Add Demo Chat
    suspend fun addDemoChat(chat: FirestoreChat) {
        val uid = requireUserId()
        val path = "users/$uid/chats"
        try {
            val docRef = db.collection("users").document(uid).collection("chats").document()
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "recipientId" to chat.recipientId,
                "name" to chat.name,
                "handle" to chat.handle,
                "lastMessage" to chat.lastMessage,
                "unreadCount" to chat.unreadCount,
                "isFriend" to chat.isFriend,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
        }
    }

    // 13. Add Demo Notification
    suspend fun addDemoNotification(notification: FirestoreNotification) {
        val uid = requireUserId()
        val path = "users/$uid/notifications"
        try {
            val docRef = db.collection("users").document(uid).collection("notifications").document()
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "title" to notification.title,
                "message" to notification.message,
                "type" to notification.type,
                "isRead" to notification.isRead,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
        }
    }

    // 14. Seed initial public videos if empty
    suspend fun seedInitialVideosIfEmpty() {
        try {
            val existingVideos = db.collection("videos").limit(1).get().await()
            if (existingVideos.isEmpty) {
                val demoVideos = listOf(
                    FirestoreVideo(
                        userId = "creator_tikvideo_official",
                        creatorName = "Hamza Travel Logs",
                        creatorHandle = "@hamza_vids",
                        caption = "Sunset at Hunza Valley! The mountain reflection in Attabad Lake is pure magic 🏔️✨ #foryou #hunza #pakistan #travel #nature",
                        soundTitle = "Original Audio - Strings • Sar Kiye",
                        likesCount = 38420L,
                        commentsCount = 1420L,
                        sharesCount = 680L,
                        bookmarksCount = 3120L,
                        bgStartColor = 0xFF0D2538,
                        bgEndColor = 0xFF1B4965,
                        category = "FOR_YOU"
                    ),
                    FirestoreVideo(
                        userId = "creator_tikvideo_official",
                        creatorName = "Street Foodies Lahore",
                        creatorHandle = "@foodies_lahore",
                        caption = "Crispy Gol Gappay with spicy tangy khatta pani in Anarkali! Who is craving this right now? 🔥😋 #streetfood #lahore #desi #foodie",
                        soundTitle = "Trending Beat - Coke Studio Fusion",
                        likesCount = 52100L,
                        commentsCount = 2890L,
                        sharesCount = 1350L,
                        bookmarksCount = 4510L,
                        bgStartColor = 0xFF2A1B0E,
                        bgEndColor = 0xFF5C3D2E,
                        category = "FOR_YOU"
                    ),
                    FirestoreVideo(
                        userId = "creator_tikvideo_official",
                        creatorName = "Tech Guru Ali",
                        creatorHandle = "@tech_ali",
                        caption = "Top 5 Android features you probably missed in 2026! Check #3 out 📱⚡ #android #tech #tips #gadgets",
                        soundTitle = "Tech Soundwaves - Modern Synth",
                        likesCount = 19400L,
                        commentsCount = 672L,
                        sharesCount = 412L,
                        bookmarksCount = 2100L,
                        bgStartColor = 0xFF112233,
                        bgEndColor = 0xFF19376D,
                        category = "FOR_YOU"
                    )
                )
                for (v in demoVideos) {
                    db.collection("videos").add(v).await()
                }
            }
        } catch (e: Exception) {
            // Seeding will safely fail or succeed depending on initial collection state
        }
    }
}
