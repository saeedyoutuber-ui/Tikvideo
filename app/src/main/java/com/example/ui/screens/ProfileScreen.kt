package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.auth.onGoogleSignInClicked
import com.example.auth.signOut
import com.example.data.UserProfile
import com.example.ui.components.TikVideoLogo
import com.example.ui.theme.TikActiveGreen
import com.example.ui.theme.TikBackground
import com.example.ui.theme.TikChipInactive
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikGradientBrush
import com.example.ui.theme.TikSurfaceElevated
import com.example.ui.theme.TikTextPrimary
import com.example.ui.theme.TikTextSecondary
import com.example.ui.theme.TikTextTertiary
import com.example.viewmodel.TikVideoViewModel

@Composable
fun ProfileScreen(
    viewModel: TikVideoViewModel,
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isSigningIn by remember { mutableStateOf(false) }

    if (!userProfile.isLoggedIn) {
        // Exactly matches screenshot image_0.png
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(TikBackground)
                .statusBarsPadding()
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Gradient TikVideo Logo
                TikVideoLogo(
                    fontSize = 42.sp,
                    modifier = Modifier.testTag("profile_logo_title")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Text matching screenshot image_0.png
                Text(
                    text = "Apna profile banane ke liye login ya sign up karein",
                    fontSize = 17.sp,
                    color = TikTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.testTag("profile_login_prompt_text")
                )

                Spacer(modifier = Modifier.height(34.dp))

                // Gradient Button: "Login / Sign up"
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .height(54.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(27.dp),
                            spotColor = TikCyan,
                            ambientColor = TikCyan
                        )
                        .clip(RoundedCornerShape(27.dp))
                        .background(TikGradientBrush)
                        .clickable(enabled = !isSigningIn) {
                            isSigningIn = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isSigningIn = false
                                    viewModel.setUiMessage("Welcome to TikVideo!")
                                },
                                onAuthError = { err ->
                                    isSigningIn = false
                                    viewModel.setUiMessage("Sign in error: $err")
                                },
                                scope = coroutineScope,
                                onAuthCancelled = {
                                    isSigningIn = false
                                }
                            )
                        }
                        .testTag("login_signup_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Login / Sign up",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    } else {
        // Authenticated Profile View
        LoggedInProfileView(
            userProfile = userProfile,
            onLogout = {
                signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {
                        viewModel.setUiMessage("Signed out")
                    },
                    scope = coroutineScope
                )
            }
        )
    }
}

@Composable
private fun LoggedInProfileView(
    userProfile: UserProfile,
    onLogout: () -> Unit
) {
    var selectedProfileTab by remember { mutableStateOf(0) }
    val profileTabs = listOf("Videos", "Liked", "Saved")

    val sampleMyVideos = listOf(
        Pair("38.4K", 0xFF0D2538),
        Pair("125K", 0xFF2A1B0E),
        Pair("19.2K", 0xFF112233),
        Pair("54.1K", 0xFF1E3E62),
        Pair("8.9K", 0xFF4A154B),
        Pair("92.3K", 0xFF003049)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TikBackground)
            .statusBarsPadding()
    ) {
        // Profile Top Bar: Handle & Settings/Logout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(36.dp))
            Text(
                text = userProfile.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            IconButton(onClick = onLogout, modifier = Modifier.testTag("profile_logout_button")) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = TikTextSecondary
                )
            }
        }

        // Avatar + Edit badge
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .border(2.dp, TikCyan, CircleShape)
                        .background(TikGradientBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile.name.take(1),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(TikSurfaceElevated)
                        .border(1.dp, TikCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Avatar",
                        tint = TikCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = userProfile.handle,
                fontSize = 14.sp,
                color = TikCyan,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats: Following | Followers | Likes
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProfileStatColumn(value = userProfile.followingCount.toString(), label = "Following")
                ProfileStatColumn(value = userProfile.followersCount, label = "Followers")
                ProfileStatColumn(value = userProfile.likesCount, label = "Likes")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Buttons: Edit Profile | Share Profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TikChipInactive,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit Profile", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TikChipInactive,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Share Profile", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bio
            Text(
                text = userProfile.bio,
                color = TikTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs: Videos | Liked | Saved
        TabRow(
            selectedTabIndex = selectedProfileTab,
            containerColor = TikBackground,
            contentColor = TikCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedProfileTab]),
                    color = TikActiveGreen,
                    height = 2.dp
                )
            }
        ) {
            profileTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedProfileTab == index,
                    onClick = { selectedProfileTab = index },
                    icon = {
                        val icon = when (index) {
                            0 -> Icons.Default.GridView
                            1 -> Icons.Default.Favorite
                            else -> Icons.Default.Bookmark
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (selectedProfileTab == index) TikCyan else TikTextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }

        // Video Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(sampleMyVideos) { video ->
                Box(
                    modifier = Modifier
                        .aspectRatio(0.75f)
                        .background(Color(video.second)),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = video.first,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TikTextTertiary
        )
    }
}
