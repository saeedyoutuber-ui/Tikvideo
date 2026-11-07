package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScreenTab
import com.example.ui.theme.TikBackground
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikDivider
import com.example.ui.theme.TikGradientBrush
import com.example.ui.theme.TikNavInactive

@Composable
fun TikVideoBottomNav(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(TikBackground)
            .border(width = 0.8.dp, color = TikDivider)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home Tab
            BottomNavItem(
                label = "Home",
                isSelected = currentTab == ScreenTab.HOME,
                onClick = { onTabSelected(ScreenTab.HOME) },
                icon = { isSelected ->
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(24.dp)
                    )
                },
                testTag = "nav_home"
            )

            // 2. Chat Tab
            BottomNavItem(
                label = "Chat",
                isSelected = currentTab == ScreenTab.CHAT,
                onClick = { onTabSelected(ScreenTab.CHAT) },
                icon = { isSelected ->
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Chat",
                        modifier = Modifier.size(24.dp)
                    )
                },
                testTag = "nav_chat"
            )

            // 3. Central Glowing Teal '+' Button
            Box(
                modifier = Modifier
                    .testTag("nav_upload")
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onTabSelected(ScreenTab.UPLOAD) }
                    )
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 46.dp, height = 30.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = TikCyan,
                            spotColor = TikCyan
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .background(TikGradientBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Upload Video",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 4. Alerts Tab
            BottomNavItem(
                label = "Alerts",
                isSelected = currentTab == ScreenTab.ALERTS,
                onClick = { onTabSelected(ScreenTab.ALERTS) },
                icon = { isSelected ->
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                        contentDescription = "Alerts",
                        modifier = Modifier.size(24.dp)
                    )
                },
                testTag = "nav_alerts"
            )

            // 5. Profile Tab
            BottomNavItem(
                label = "Profile",
                isSelected = currentTab == ScreenTab.PROFILE,
                onClick = { onTabSelected(ScreenTab.PROFILE) },
                icon = { isSelected ->
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Person else Icons.Outlined.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(24.dp)
                    )
                },
                testTag = "nav_profile"
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Boolean) -> Unit,
    testTag: String
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) TikCyan else TikNavInactive,
        animationSpec = tween(durationMillis = 200),
        label = "navColor"
    )

    Column(
        modifier = Modifier
            .testTag(testTag)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides contentColor
            ) {
                icon(isSelected)
            }
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
