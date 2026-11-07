package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NotificationFilterTab
import com.example.data.NotificationItem
import com.example.ui.theme.TikActiveGreen
import com.example.ui.theme.TikBackground
import com.example.ui.theme.TikChipInactive
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikGradientBrush
import com.example.ui.theme.TikTextPrimary
import com.example.ui.theme.TikTextSecondary
import com.example.ui.theme.TikTextTertiary
import com.example.viewmodel.TikVideoViewModel

@Composable
fun NotificationsScreen(
    viewModel: TikVideoViewModel,
    filterTab: NotificationFilterTab,
    notifications: List<NotificationItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TikBackground)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Header Row matching image_1.png: "Notifications" on left, "Mark all read" chip on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notifications",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TikTextPrimary
            )

            // "Mark all read" Chip
            Box(
                modifier = Modifier
                    .testTag("mark_all_read_chip")
                    .clip(RoundedCornerShape(20.dp))
                    .background(TikChipInactive)
                    .clickable { viewModel.markAllNotificationsRead() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Check",
                        tint = TikTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mark all read",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TikTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Filter Tabs matching image_1.png: All | Likes | Comments | Followers
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(NotificationFilterTab.values()) { tab ->
                val isSelected = filterTab == tab
                Box(
                    modifier = Modifier
                        .testTag("notification_tab_${tab.name.lowercase()}")
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) TikActiveGreen else TikChipInactive
                        )
                        .clickable { viewModel.setNotificationFilterTab(tab) }
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

        // Filter notifications
        val filteredList = notifications.filter { item ->
            if (filterTab == NotificationFilterTab.ALL) true else item.type == filterTab
        }

        if (filteredList.isEmpty()) {
            // Exactly matching screenshot image_1.png: "Nothing here yet"
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Nothing here yet",
                        fontSize = 17.sp,
                        color = TikTextTertiary,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.testTag("notifications_empty_placeholder")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Optional demo trigger button
                    Button(
                        onClick = { viewModel.populateSampleNotificationsForTesting() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TikChipInactive,
                            contentColor = TikCyan
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("load_sample_notifications_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Try Demo Alerts", fontSize = 13.sp)
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
                items(filteredList) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (item.isRead) TikChipInactive.copy(alpha = 0.4f)
                                else TikChipInactive.copy(alpha = 0.8f)
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(TikGradientBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.title.take(1),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.message,
                                color = TikTextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.time,
                                color = TikTextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
