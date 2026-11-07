package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.authStateFlow
import com.example.data.ChatConversation
import com.example.data.CommentItem
import com.example.data.FirestoreChat
import com.example.data.FirestoreComment
import com.example.data.FirestoreNotification
import com.example.data.FirestoreUserProfile
import com.example.data.FirestoreVideo
import com.example.data.HomeFeedTab
import com.example.data.MessageFilterTab
import com.example.data.NotificationFilterTab
import com.example.data.NotificationItem
import com.example.data.ScreenTab
import com.example.data.TikVideoRepository
import com.example.data.UserProfile
import com.example.data.VideoItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TikVideoViewModel(
    private val repository: TikVideoRepository
) : ViewModel() {

    // Secondary constructor to allow ViewModel resolution from Application context
    constructor(context: Context) : this(TikVideoRepository(context))

    private val auth = Firebase.auth

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _homeFeedTab = MutableStateFlow(HomeFeedTab.FOR_YOU)
    val homeFeedTab: StateFlow<HomeFeedTab> = _homeFeedTab.asStateFlow()

    private val _messageFilterTab = MutableStateFlow(MessageFilterTab.ALL)
    val messageFilterTab: StateFlow<MessageFilterTab> = _messageFilterTab.asStateFlow()

    private val _messageSearchQuery = MutableStateFlow("")
    val messageSearchQuery: StateFlow<String> = _messageSearchQuery.asStateFlow()

    private val _notificationFilterTab = MutableStateFlow(NotificationFilterTab.ALL)
    val notificationFilterTab: StateFlow<NotificationFilterTab> = _notificationFilterTab.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile(isLoggedIn = auth.currentUser != null))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _activeCommentsVideoId = MutableStateFlow<String?>(null)
    val activeCommentsVideoId: StateFlow<String?> = _activeCommentsVideoId.asStateFlow()

    private val _comments = MutableStateFlow<List<CommentItem>>(emptyList())
    val comments: StateFlow<List<CommentItem>> = _comments.asStateFlow()

    // Local set of liked video IDs and saved video IDs by current user
    private val _likedVideoIds = MutableStateFlow<Set<String>>(emptySet())
    private val _savedVideoIds = MutableStateFlow<Set<String>>(emptySet())

    private val _videos = MutableStateFlow<List<VideoItem>>(emptyList())
    val videos: StateFlow<List<VideoItem>> = _videos.asStateFlow()

    private val _chats = MutableStateFlow<List<ChatConversation>>(emptyList())
    val chats: StateFlow<List<ChatConversation>> = _chats.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private var userChatsJob: Job? = null
    private var userNotificationsJob: Job? = null
    private var activeCommentsJob: Job? = null

    init {
        // Observe Firebase Auth state
        viewModelScope.launch {
            auth.authStateFlow().collectLatest { user ->
                _currentUser.value = user
                if (user != null) {
                    onUserSignedIn(user)
                } else {
                    onUserSignedOut()
                }
            }
        }

        // Observe Firestore Videos feed
        viewModelScope.launch {
            repository.observeVideos()
                .catch {
                    // Fall back to empty or keep current on stream error
                }
                .collectLatest { firestoreVideos ->
                    if (firestoreVideos.isEmpty()) {
                        // Seed initial videos if collection is empty
                        repository.seedInitialVideosIfEmpty()
                    } else {
                        mapAndEmitVideos(firestoreVideos)
                    }
                }
        }
    }

    private fun mapAndEmitVideos(firestoreVideos: List<FirestoreVideo>) {
        val liked = _likedVideoIds.value
        val saved = _savedVideoIds.value
        _videos.value = firestoreVideos.map { fv ->
            VideoItem(
                id = fv.id,
                creatorName = fv.creatorName,
                creatorHandle = fv.creatorHandle,
                caption = fv.caption,
                soundTitle = fv.soundTitle,
                likesCount = fv.likesCount.toInt(),
                commentsCount = fv.commentsCount.toInt(),
                sharesCount = fv.sharesCount.toInt(),
                bookmarksCount = fv.bookmarksCount.toInt(),
                isLiked = liked.contains(fv.id),
                isSaved = saved.contains(fv.id),
                isFollowing = false,
                bgStartColor = fv.bgStartColor,
                bgEndColor = fv.bgEndColor,
                category = when (fv.category) {
                    "FOLLOWING" -> HomeFeedTab.FOLLOWING
                    "FRIENDS" -> HomeFeedTab.FRIENDS
                    else -> HomeFeedTab.FOR_YOU
                }
            )
        }
    }

    private fun onUserSignedIn(user: FirebaseUser) {
        val displayName = user.displayName ?: "TikVideo Creator"
        val emailPrefix = user.email?.substringBefore("@") ?: "creator"
        val handle = "@$emailPrefix"

        viewModelScope.launch {
            val existingProfile = repository.getUserProfile(user.uid)
            if (existingProfile != null) {
                _userProfile.value = UserProfile(
                    name = existingProfile.name,
                    handle = existingProfile.handle,
                    bio = existingProfile.bio,
                    followingCount = existingProfile.followingCount.toInt(),
                    followersCount = "${existingProfile.followersCount}",
                    likesCount = "${existingProfile.likesCount}",
                    isLoggedIn = true
                )
            } else {
                val newProfile = FirestoreUserProfile(
                    userId = user.uid,
                    name = displayName,
                    handle = handle,
                    bio = "🎥 Content Creator on TikVideo ✨",
                    followingCount = 12L,
                    followersCount = 0L,
                    likesCount = 0L
                )
                repository.saveUserProfile(newProfile)
                _userProfile.value = UserProfile(
                    name = displayName,
                    handle = handle,
                    bio = "🎥 Content Creator on TikVideo ✨",
                    followingCount = 12,
                    followersCount = "0",
                    likesCount = "0",
                    isLoggedIn = true
                )
            }

            // Observe User Chats
            userChatsJob?.cancel()
            userChatsJob = launch {
                repository.observeChats(user.uid)
                    .catch { }
                    .collectLatest { chatList ->
                        _chats.value = chatList.map { fc ->
                            ChatConversation(
                                id = fc.id,
                                name = fc.name,
                                handle = fc.handle,
                                lastMessage = fc.lastMessage,
                                time = "Recent",
                                unreadCount = fc.unreadCount.toInt(),
                                isFriend = fc.isFriend
                            )
                        }
                    }
            }

            // Observe User Notifications
            userNotificationsJob?.cancel()
            userNotificationsJob = launch {
                repository.observeNotifications(user.uid)
                    .catch { }
                    .collectLatest { notifList ->
                        _notifications.value = notifList.map { fn ->
                            NotificationItem(
                                id = fn.id,
                                title = fn.title,
                                message = fn.message,
                                time = "Just now",
                                type = when (fn.type) {
                                    "LIKES" -> NotificationFilterTab.LIKES
                                    "COMMENTS" -> NotificationFilterTab.COMMENTS
                                    "FOLLOWERS" -> NotificationFilterTab.FOLLOWERS
                                    else -> NotificationFilterTab.ALL
                                },
                                isRead = fn.isRead
                            )
                        }
                    }
            }
        }
    }

    private fun onUserSignedOut() {
        userChatsJob?.cancel()
        userNotificationsJob?.cancel()
        _chats.value = emptyList()
        _notifications.value = emptyList()
        _likedVideoIds.value = emptySet()
        _savedVideoIds.value = emptySet()
        _userProfile.value = UserProfile(isLoggedIn = false)
    }

    fun switchTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun setHomeFeedTab(tab: HomeFeedTab) {
        _homeFeedTab.value = tab
    }

    fun setMessageFilterTab(tab: MessageFilterTab) {
        _messageFilterTab.value = tab
    }

    fun setMessageSearchQuery(query: String) {
        _messageSearchQuery.value = query
    }

    fun setNotificationFilterTab(tab: NotificationFilterTab) {
        _notificationFilterTab.value = tab
    }

    fun toggleLike(videoId: String) {
        if (auth.currentUser == null) {
            _currentTab.value = ScreenTab.PROFILE
            _uiMessage.value = "Please sign in to like videos"
            return
        }
        val isCurrentlyLiked = _likedVideoIds.value.contains(videoId)
        viewModelScope.launch {
            val result = repository.toggleLike(videoId, isCurrentlyLiked)
            if (result.isSuccess) {
                val newLikedState = result.getOrNull() ?: !isCurrentlyLiked
                _likedVideoIds.update { set ->
                    if (newLikedState) set + videoId else set - videoId
                }
                _videos.update { list ->
                    list.map { item ->
                        if (item.id == videoId) {
                            item.copy(
                                isLiked = newLikedState,
                                likesCount = if (newLikedState) item.likesCount + 1 else (item.likesCount - 1).coerceAtLeast(0)
                            )
                        } else item
                    }
                }
            }
        }
    }

    fun toggleSave(videoId: String) {
        if (auth.currentUser == null) {
            _currentTab.value = ScreenTab.PROFILE
            _uiMessage.value = "Please sign in to save videos"
            return
        }
        val isCurrentlySaved = _savedVideoIds.value.contains(videoId)
        viewModelScope.launch {
            val result = repository.toggleSave(videoId, isCurrentlySaved)
            if (result.isSuccess) {
                val newSavedState = result.getOrNull() ?: !isCurrentlySaved
                _savedVideoIds.update { set ->
                    if (newSavedState) set + videoId else set - videoId
                }
                _videos.update { list ->
                    list.map { item ->
                        if (item.id == videoId) {
                            item.copy(
                                isSaved = newSavedState,
                                bookmarksCount = if (newSavedState) item.bookmarksCount + 1 else (item.bookmarksCount - 1).coerceAtLeast(0)
                            )
                        } else item
                    }
                }
            }
        }
    }

    fun toggleFollow(videoId: String) {
        _videos.update { list ->
            list.map { item ->
                if (item.id == videoId) {
                    item.copy(isFollowing = !item.isFollowing)
                } else item
            }
        }
    }

    fun openComments(videoId: String) {
        _activeCommentsVideoId.value = videoId
        activeCommentsJob?.cancel()
        activeCommentsJob = viewModelScope.launch {
            repository.observeComments(videoId)
                .catch { }
                .collectLatest { list ->
                    _comments.value = list.map { fc ->
                        CommentItem(
                            id = fc.id,
                            author = fc.author,
                            authorHandle = fc.authorHandle,
                            text = fc.text,
                            timeAgo = "Recent",
                            likes = fc.likes.toInt()
                        )
                    }
                }
        }
    }

    fun closeComments() {
        _activeCommentsVideoId.value = null
        activeCommentsJob?.cancel()
    }

    fun addComment(videoId: String, text: String) {
        if (text.isBlank()) return
        val user = auth.currentUser
        if (user == null) {
            _currentTab.value = ScreenTab.PROFILE
            _uiMessage.value = "Please sign in to comment"
            return
        }
        val authorName = user.displayName ?: _userProfile.value.name
        val authorHandle = _userProfile.value.handle
        viewModelScope.launch {
            val result = repository.addComment(videoId, authorName, authorHandle, text)
            if (result.isSuccess) {
                _uiMessage.value = "Comment posted!"
            } else {
                _uiMessage.value = "Could not post comment"
            }
        }
    }

    fun postNewVideo(caption: String, soundTitle: String) {
        val user = auth.currentUser
        if (user == null) {
            _currentTab.value = ScreenTab.PROFILE
            _uiMessage.value = "Please sign in to post video"
            return
        }
        viewModelScope.launch {
            val video = FirestoreVideo(
                userId = user.uid,
                creatorName = user.displayName ?: _userProfile.value.name,
                creatorHandle = _userProfile.value.handle,
                caption = caption.ifBlank { "New video on #TikVideo 🚀🔥" },
                soundTitle = soundTitle.ifBlank { "Original Sound - TikVideo Creator" },
                category = "FOR_YOU"
            )
            val result = repository.createVideo(video)
            if (result.isSuccess) {
                _currentTab.value = ScreenTab.HOME
                _uiMessage.value = "Video published to TikVideo! 🎉"
            } else {
                _uiMessage.value = "Failed to upload video"
            }
        }
    }

    fun markAllNotificationsRead() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            repository.markAllNotificationsRead(user.uid)
            _uiMessage.value = "All notifications marked as read"
        }
    }

    fun populateSampleChatsForTesting() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                repository.addDemoChat(
                    FirestoreChat(
                        userId = user.uid,
                        recipientId = "sara_k",
                        name = "Sara Khan",
                        handle = "@sarak",
                        lastMessage = "Check out that latest viral video! 🎬",
                        unreadCount = 2L,
                        isFriend = true
                    )
                )
                repository.addDemoChat(
                    FirestoreChat(
                        userId = user.uid,
                        recipientId = "dan_vlog",
                        name = "Daniyal Vlog",
                        handle = "@dan_vlog",
                        lastMessage = "Collab this weekend in Islamabad?",
                        unreadCount = 0L,
                        isFriend = true
                    )
                )
            }
            _uiMessage.value = "Sample chats added to Firestore"
        } else {
            _currentTab.value = ScreenTab.PROFILE
            _uiMessage.value = "Sign in to add sample chats"
        }
    }

    fun populateSampleNotificationsForTesting() {
        val user = auth.currentUser
        if (user != null) {
            viewModelScope.launch {
                repository.addDemoNotification(
                    FirestoreNotification(
                        userId = user.uid,
                        title = "New Follower",
                        message = "@ayesha_n started following you",
                        type = "FOLLOWERS",
                        isRead = false
                    )
                )
                repository.addDemoNotification(
                    FirestoreNotification(
                        userId = user.uid,
                        title = "Liked your video",
                        message = "@bilal_ahmed liked 'Sunset at Hunza'",
                        type = "LIKES",
                        isRead = false
                    )
                )
            }
            _uiMessage.value = "Sample alerts added to Firestore"
        } else {
            _currentTab.value = ScreenTab.PROFILE
            _uiMessage.value = "Sign in to add sample alerts"
        }
    }

    fun setUiMessage(message: String) {
        _uiMessage.value = message
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }
}
