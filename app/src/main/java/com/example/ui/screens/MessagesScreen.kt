package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ChatConversation
import com.example.data.MessageFilterTab
import com.example.ui.theme.TikActiveGreen
import com.example.ui.theme.TikBackground
import com.example.ui.theme.TikChipInactive
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikGradientBrush
import com.example.ui.theme.TikSearchInput
import com.example.ui.theme.TikSurfaceElevated
import com.example.ui.theme.TikTextPrimary
import com.example.ui.theme.TikTextSecondary
import com.example.ui.theme.TikTextTertiary
import com.example.viewmodel.TikVideoViewModel

@Composable
fun MessagesScreen(
    viewModel: TikVideoViewModel,
    filterTab: MessageFilterTab,
    searchQuery: String,
    chats: List<ChatConversation>,
    modifier: Modifier = Modifier
) {
    var selectedChatForDialog by remember { mutableStateOf<ChatConversation?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TikBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Top Header matching image_3.png
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Messages",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TikTextPrimary
            )
            IconButton(
                onClick = { /* Search shortcut */ },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("messages_search_icon")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search messages",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Search Input Bar matching image_3.png
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setMessageSearchQuery(it) },
            placeholder = {
                Text(
                    text = "Search messages",
                    color = TikTextTertiary,
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TikTextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setMessageSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TikTextTertiary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(26.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = TikSearchInput,
                unfocusedContainerColor = TikSearchInput,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = TikCyan
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("messages_search_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Tabs matching image_3.png: All | Unread | Friends
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MessageFilterTab.values().forEach { tab ->
                val isSelected = filterTab == tab
                Box(
                    modifier = Modifier
                        .testTag("message_tab_${tab.name.lowercase()}")
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) TikActiveGreen else TikChipInactive
                        )
                        .clickable { viewModel.setMessageFilterTab(tab) }
                        .padding(horizontal = 22.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        color = if (isSelected) Color.Black else TikTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Body Content: filtered chats or "No chats found"
        val filteredChats = chats.filter { chat ->
            val matchesTab = when (filterTab) {
                MessageFilterTab.ALL -> true
                MessageFilterTab.UNREAD -> chat.unreadCount > 0
                MessageFilterTab.FRIENDS -> chat.isFriend
            }
            val matchesSearch = searchQuery.isBlank() ||
                chat.name.contains(searchQuery, ignoreCase = true) ||
                chat.handle.contains(searchQuery, ignoreCase = true) ||
                chat.lastMessage.contains(searchQuery, ignoreCase = true)
            matchesTab && matchesSearch
        }

        if (filteredChats.isEmpty()) {
            // Exactly matching screenshot image_3.png: "No chats found"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No chats found",
                        fontSize = 17.sp,
                        color = TikTextTertiary,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.testTag("no_chats_placeholder")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Optional interactive button to demo chat features
                    Button(
                        onClick = { viewModel.populateSampleChatsForTesting() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TikChipInactive,
                            contentColor = TikCyan
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("load_sample_chats_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Message,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Try Demo Conversations", fontSize = 13.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredChats) { chat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(TikChipInactive.copy(alpha = 0.5f))
                            .clickable { selectedChatForDialog = chat }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(TikGradientBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.name.take(1),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = chat.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = chat.time,
                                    color = TikTextTertiary,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = chat.lastMessage,
                                color = TikTextSecondary,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    // Interactive Chat Message Dialog
    selectedChatForDialog?.let { chat ->
        var replyText by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { selectedChatForDialog = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(TikSurfaceElevated)
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(TikGradientBrush),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(chat.name.take(1), color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(chat.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(chat.handle, color = TikCyan, fontSize = 12.sp)
                            }
                        }
                        IconButton(onClick = { selectedChatForDialog = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TikChipInactive)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "${chat.name}: ${chat.lastMessage}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Write a message...", color = TikTextTertiary) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = TikCyan,
                                unfocusedBorderColor = Color(0xFF2B3548)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    viewModel.clearUiMessage()
                                    selectedChatForDialog = null
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(TikGradientBrush)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
