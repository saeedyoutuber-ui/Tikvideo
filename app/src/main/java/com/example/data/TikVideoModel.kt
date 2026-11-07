package com.example.data

enum class ScreenTab(val label: String) {
    HOME("Home"),
    CHAT("Chat"),
    UPLOAD(""),
    ALERTS("Alerts"),
    PROFILE("Profile")
}

enum class HomeFeedTab(val label: String) {
    FRIENDS("Friends"),
    FOLLOWING("Following"),
    FOR_YOU("For You")
}

enum class MessageFilterTab(val label: String) {
    ALL("All"),
    UNREAD("Unread"),
    FRIENDS("Friends")
}

enum class NotificationFilterTab(val label: String) {
    ALL("All"),
    LIKES("Likes"),
    COMMENTS("Comments"),
    FOLLOWERS("Followers")
}

data class VideoItem(
    val id: String,
    val creatorName: String,
    val creatorHandle: String,
    val caption: String,
    val soundTitle: String,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val bookmarksCount: Int,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val isFollowing: Boolean = false,
    val bgStartColor: Long = 0xFF0B192C,
    val bgEndColor: Long = 0xFF1E3E62,
    val category: HomeFeedTab = HomeFeedTab.FOR_YOU
)

data class CommentItem(
    val id: String,
    val author: String,
    val authorHandle: String,
    val text: String,
    val timeAgo: String,
    val likes: Int,
    val isLiked: Boolean = false
)

data class ChatConversation(
    val id: String,
    val name: String,
    val handle: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val isFriend: Boolean = false
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: NotificationFilterTab,
    val isRead: Boolean = false
)

data class UserProfile(
    val name: String = "Hamza Khan",
    val handle: String = "@hamza_creatives",
    val bio: String = "🎥 Content Creator | Tech & Travel 🇵🇰\n✨ Daily shorts & cinematic vibes",
    val followingCount: Int = 142,
    val followersCount: String = "12.4K",
    val likesCount: String = "89.2K",
    val isLoggedIn: Boolean = false
)
