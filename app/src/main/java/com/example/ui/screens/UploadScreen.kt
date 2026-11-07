package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.auth.onGoogleSignInClicked
import com.example.data.UserProfile
import com.example.ui.components.TikVideoLogo
import com.example.ui.theme.TikBackground
import com.example.ui.theme.TikChipInactive
import com.example.ui.theme.TikCyan
import com.example.ui.theme.TikGradientBrush
import com.example.ui.theme.TikHeartRed
import com.example.ui.theme.TikSurfaceElevated
import com.example.ui.theme.TikTextPrimary
import com.example.ui.theme.TikTextSecondary
import com.example.viewmodel.TikVideoViewModel

@Composable
fun UploadScreen(
    viewModel: TikVideoViewModel,
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isSigningIn by remember { mutableStateOf(false) }

    if (!userProfile.isLoggedIn) {
        // Exactly matches image_2.png
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
                // Gradient TikVideo Title
                TikVideoLogo(
                    fontSize = 42.sp,
                    modifier = Modifier.testTag("upload_logo_title")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Roman Urdu/Hindi text matching screenshot
                Text(
                    text = "Video upload karne ke liye login karein",
                    fontSize = 17.sp,
                    color = TikTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.testTag("upload_prompt_text")
                )

                Spacer(modifier = Modifier.height(34.dp))

                // Gradient Button: "Continue with Google"
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
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
                                    viewModel.setUiMessage("Signed in successfully with Google!")
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
                        .testTag("continue_with_google_button"),
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
                            text = "Continue with Google",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    } else {
        // Logged-in / Creator Camera Studio
        TikVideoCreatorStudio(
            onPostVideo = { caption, sound ->
                viewModel.postNewVideo(caption, sound)
            },
            onClose = {
                viewModel.switchTab(com.example.data.ScreenTab.HOME)
            }
        )
    }
}

@Composable
fun TikVideoCreatorStudio(
    onPostVideo: (caption: String, sound: String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRecording by remember { mutableStateOf(false) }
    var captionText by remember { mutableStateOf("") }
    var selectedSound by remember { mutableStateOf("Original Sound - Trending Beats") }
    var speedLevel by remember { mutableStateOf("1x") }
    var timerActive by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07090E))
            .statusBarsPadding()
    ) {
        // Camera Viewfinder Simulation with Guide Grid
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(2.dp, TikCyan.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Camera Viewfinder",
                        tint = TikCyan,
                        modifier = Modifier.size(54.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (isRecording) "● Recording 00:08 / 00:60" else "TikVideo HD Camera Ready",
                    color = if (isRecording) TikHeartRed else Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Top Bar Controls: Close, Add Sound, Flip Camera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close Studio", tint = Color.White)
            }

            // Sound Selector Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, TikCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .clickable { selectedSound = "🎵 Viral Fusion - Coke Studio Mix" }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = TikCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedSound.take(22) + "...",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            IconButton(
                onClick = { /* Flip camera */ },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.Cameraswitch, contentDescription = "Flip Camera", tint = Color.White)
            }
        }

        // Right side camera adjustments: Flash, Speed, Timer
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CameraToolButton(
                icon = Icons.Default.FlashOn,
                label = "Flash",
                onClick = {}
            )
            CameraToolButton(
                icon = Icons.Default.Speed,
                label = speedLevel,
                onClick = {
                    speedLevel = when (speedLevel) {
                        "1x" -> "2x"
                        "2x" -> "0.5x"
                        else -> "1x"
                    }
                }
            )
            CameraToolButton(
                icon = Icons.Default.Timer,
                label = if (timerActive) "3s" else "Timer",
                onClick = { timerActive = !timerActive }
            )
        }

        // Bottom Controls: Caption Input & Record Button & Post
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF07090E))))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = captionText,
                onValueChange = { captionText = it },
                placeholder = { Text("Write video description... #tikvideo #viral", color = Color(0xFF64748B), fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TikSurfaceElevated,
                    unfocusedContainerColor = TikSurfaceElevated,
                    focusedBorderColor = TikCyan,
                    unfocusedBorderColor = Color(0xFF232D3F),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Effects / Filter button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TikChipInactive),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 18.sp)
                    }
                    Text("Effects", color = TikTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                }

                // Central Record Button
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .border(4.dp, if (isRecording) TikHeartRed else TikCyan, CircleShape)
                        .scale(if (isRecording) pulseScale else 1f)
                        .clickable { isRecording = !isRecording },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) TikHeartRed else Color.White)
                    )
                }

                // Post / Publish Button
                Button(
                    onClick = {
                        onPostVideo(captionText, selectedSound)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TikCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("publish_video_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Post", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CameraToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
