package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.viewmodel.ObserverViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun KawaiiCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = Color(0xFFFFD6E0).copy(alpha = 0.6f),
    shadowColor: Color = ShadowPink,
    shadowOffset: androidx.compose.ui.unit.Dp = 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var cardWidth by remember { mutableStateOf(1f) }
    var cardHeight by remember { mutableStateOf(1f) }

    // Smooth physics spring animation for 3D depth and tilt transition
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cardScale"
    )

    val animatedPressOffset by animateDpAsState(
        targetValue = if (isPressed) shadowOffset / 2 else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardPressOffset"
    )

    val shadowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.55f else 0.85f,
        animationSpec = tween(200),
        label = "shadowAlpha"
    )

    Box(
        modifier = modifier
            .padding(bottom = shadowOffset, end = shadowOffset / 2)
            .onGloballyPositioned { coordinates ->
                cardWidth = coordinates.size.width.toFloat()
                cardHeight = coordinates.size.height.toFloat()
            }
    ) {
        // Shadow layer with depth transition
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(
                    x = shadowOffset / 2,
                    y = shadowOffset - animatedPressOffset
                )
                .background(shadowColor.copy(alpha = shadowAlpha), RoundedCornerShape(32.dp))
        )

        // Main Card Layer with 3D Perspective GraphicsLayer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = animatedPressOffset)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                    cameraDistance = 14f * density
                }
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onClick() }
                    } else Modifier
                )
                .background(backgroundColor, RoundedCornerShape(32.dp))
                .border(1.5.dp, borderColor, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .padding(20.dp),
            content = content
        )
    }
}

@Composable
fun KawaiiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = KawaiiPinkLight,
    contentColor: Color = KawaiiDarkText,
    testTag: String = "",
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .testTag(testTag)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(1.dp, KawaiiPink.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        content = content
    )
}

@Composable
fun KawaiiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE
) {
    val fieldBg = if (isAppInDarkTheme()) Color(0xFF332A42) else Color(0xFFFFFFFF)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontWeight = FontWeight.Medium, color = KawaiiDarkText) },
        placeholder = { Text(placeholder, color = KawaiiDarkText.copy(alpha = 0.6f)) },
        singleLine = singleLine,
        maxLines = maxLines,
        modifier = modifier
            .fillMaxWidth()
            .background(fieldBg, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = KawaiiPink,
            unfocusedBorderColor = KawaiiSoftBorder,
            focusedContainerColor = fieldBg,
            unfocusedContainerColor = fieldBg,
            focusedTextColor = KawaiiDarkText,
            unfocusedTextColor = KawaiiDarkText
        )
    )
}

@Composable
fun KawaiiBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = KawaiiLavenderLight,
    textColor: Color = KawaiiLavender,
    icon: String? = null
) {
    Row(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(50))
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Text(icon, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp))
        }
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun KawaiiCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val scale by animateFloatAsState(
        targetValue = if (checked) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium)
    )

    val rotation by animateFloatAsState(
        targetValue = if (checked) 15f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy)
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .scale(scale)
            .rotate(rotation)
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (checked) KawaiiPinkLight else KawaiiLavenderLight,
                RoundedCornerShape(12.dp)
            )
            .border(
                2.dp,
                if (checked) KawaiiPink else KawaiiLavender.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (checked) "😸" else "💤",
            fontSize = 18.sp
        )
    }
}

@Composable
fun KawaiiStatusFrame(
    avatar: String,
    modifier: Modifier = Modifier,
    pulse: Boolean = true,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (pulse) 1.1f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                1.0f at 0
                1.1f at 750
                1.0f at 1500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .scale(borderPulse)
            .size(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(listOf(KawaiiPink, KawaiiLavender, KawaiiMint)),
                RoundedCornerShape(18.dp)
            )
            .padding(2.5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(avatar, fontSize = 28.sp)
    }
}

@Composable
fun ActiveWorkspaceHeaderCard(
    viewModel: ObserverViewModel,
    onOpenShareDialog: () -> Unit = {}
) {
    val activeWorkspaceId by viewModel.activeWorkspaceId.collectAsState()
    val workspaces by viewModel.workspaces.collectAsState(initial = emptyList())
    val role by viewModel.currentUserRole.collectAsState()
    val activePresence by viewModel.activePresenceUsers.collectAsState()

    val currentWb = workspaces.find { it.id == activeWorkspaceId }

    KawaiiCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_workspace_header_card"),
        borderColor = KawaiiLavender,
        shadowColor = ShadowPink
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(KawaiiLavenderLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(currentWb?.icon ?: "🐾", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentWb?.name ?: "Stray Kash Workspace",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = KawaiiDarkText,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Role Badge
                        val roleLabel = when (role) {
                            UserRole.HOST -> "👑 Host"
                            UserRole.EDITOR -> "✏️ Editor"
                            UserRole.VIEWER -> "🔒 Viewer"
                        }
                        val roleBg = when (role) {
                            UserRole.HOST -> KawaiiPinkLight
                            UserRole.EDITOR -> KawaiiMintLight
                            UserRole.VIEWER -> KawaiiLavenderLight
                        }
                        val roleColor = when (role) {
                            UserRole.HOST -> KawaiiPink
                            UserRole.EDITOR -> KawaiiMint
                            UserRole.VIEWER -> KawaiiLavender
                        }
                        KawaiiBadge(text = roleLabel, containerColor = roleBg, textColor = roleColor)

                        Text("• Code: ${currentWb?.joinCode?.take(10)}", fontSize = 10.sp, color = KawaiiLavender)
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Share Workspace Button
                Button(
                    onClick = onOpenShareDialog,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KawaiiPink, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share Workspace", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            // Live Presence Dock Users Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KawaiiLavenderLight.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Live Presence 🟢 ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KawaiiLavender)
                Spacer(modifier = Modifier.width(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(activePresence) { u ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(KawaiiCreamCardBg, RoundedCornerShape(12.dp))
                                .border(1.dp, KawaiiLavender.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(u.userAvatar, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(u.userName, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = KawaiiDarkText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttributionStampBadge(
    author: String,
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = sdf.format(Date(timestamp))

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KawaiiLavenderLight.copy(alpha = 0.8f))
            .border(1.dp, KawaiiLavender.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "✏️ Edited by $author • $formattedTime",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = KawaiiDarkText.copy(alpha = 0.8f)
        )
    }
}
