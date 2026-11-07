package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val handle: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val followingCount: Long = 0L,
    val followersCount: Long = 0L,
    val likesCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreVideo(
    @DocumentId val id: String = "",
    val userId: String = "",
    val creatorName: String = "",
    val creatorHandle: String = "",
    val caption: String = "",
    val soundTitle: String = "",
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    val sharesCount: Long = 0L,
    val bookmarksCount: Long = 0L,
    val bgStartColor: Long = 0xFF0D2538,
    val bgEndColor: Long = 0xFF1B4965,
    val category: String = "FOR_YOU",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreComment(
    @DocumentId val id: String = "",
    val videoId: String = "",
    val userId: String = "",
    val author: String = "",
    val authorHandle: String = "",
    val text: String = "",
    val likes: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreChat(
    @DocumentId val id: String = "",
    val userId: String = "",
    val recipientId: String = "",
    val name: String = "",
    val handle: String = "",
    val lastMessage: String = "",
    val unreadCount: Long = 0L,
    val isFriend: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "ALL",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)
