package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GenshinStickers
import com.example.data.model.StickyNote
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ObserverViewModel

@Composable
fun getStickyNoteBgColor(hex: String): Color {
    val isDark = isAppInDarkTheme()
    return if (isDark) {
        when (hex.uppercase()) {
            "#FFFFD1E3" -> Color(0xFF3F2130) // Dark Sakura
            "#FFF1EAFF" -> Color(0xFF302442) // Dark Lavender
            "#E2F9F0" -> Color(0xFF1E3A35)   // Dark Mint
            "#FFF9D6" -> Color(0xFF38301D)   // Dark Gold
            "#E3F2FD" -> Color(0xFF1E2E42)   // Dark Sky
            "#FFE8E8" -> Color(0xFF3D2229)   // Dark Coral
            else -> try {
                val parsed = Color(android.graphics.Color.parseColor(hex))
                Color(
                    red = parsed.red * 0.35f,
                    green = parsed.green * 0.35f,
                    blue = parsed.blue * 0.35f,
                    alpha = 1f
                )
            } catch (e: Exception) {
                Color(0xFF2E243A)
            }
        }
    } else {
        try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            StickyYellowBg
        }
    }
}

@Composable
fun StickyBoardTab(
    viewModel: ObserverViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.stickyNotes.collectAsState(initial = emptyList())
    val isReadOnly by viewModel.activeWorkspaceIsReadOnly.collectAsState()

    var content by remember { mutableStateOf("") }
    var selectedSticker by remember { mutableStateOf("💧") }
    var selectedColor by remember { mutableStateOf("#FFFFD1E3") } // Sakura Pink default
    var editingNote by remember { mutableStateOf<StickyNote?>(null) }

    val stickers = listOf("💫", "💥", "❄️", "👻", "🌿", "💧", "⚡", "🦊", "🍃", "🔶", "🌪️", "🦅", "🧊", "🌊", "⚖️", "😺", "🐶", "🌸")
    
    val pastelColors = listOf(
        "#FFFFD1E3" to "Sakura Pink",
        "#FFF1EAFF" to "Lavender",
        "#E2F9F0" to "Mint Soft",
        "#FFF9D6" to "Lemon Yellow",
        "#E3F2FD" to "Sky Blue",
        "#FFE8E8" to "Sakura Coral"
    )

    // Layout representation
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Workspace Header Chip & Activity
        ActiveWorkspaceHeaderCard(viewModel = viewModel)

        // Read-Only Banner
        if (isReadOnly) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = KawaiiPinkLight.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, KawaiiPink.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👁️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Shared Read-Only Workspace: You have view access to all sticky notes but cannot add, edit, or delete notes.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KawaiiLavender
                    )
                }
            }
        }

        // Sticky Note Creation Card (Only if NOT read-only)
        if (!isReadOnly) {
            KawaiiCard(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                borderColor = KawaiiPink
            ) {
            Text(
                text = if (editingNote == null) "📌 Stick a New Snapshot" else "✏️ Edit Sticky Note",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = KawaiiLavender,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            KawaiiTextField(
                value = content,
                onValueChange = { content = it },
                label = "Jot down a fast pattern note...",
                placeholder = "e.g., Cat wagging tail twice before jumping.",
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sticker selector row
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Genshin Character Stamp (18 Available):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = KawaiiDarkText
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(GenshinStickers.items) { item ->
                        val isSelected = selectedSticker == item.stampEmoji
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedSticker = item.stampEmoji },
                            color = if (isSelected) KawaiiPinkLight else KawaiiCreamBg,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) KawaiiPink else KawaiiSoftBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.stampEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.characterName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = KawaiiDarkText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Color Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Note Color:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiDarkText)
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pastelColors.forEach { (colorHex, name) ->
                        val color = getStickyNoteBgColor(colorHex)
                        val isSelected = selectedColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(50))
                                .background(color)
                                .border(
                                    2.dp,
                                    if (isSelected) KawaiiPink else KawaiiSoftBorder,
                                    RoundedCornerShape(50)
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Position adjustments for the selected note if editing
            editingNote?.let { note ->
                Text(
                    text = "Adjust Position on Canvas (X: ${note.posX.toInt()}, Y: ${note.posY.toInt()}):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = KawaiiDarkText
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val newX = (note.posX - 20).coerceAtLeast(0f)
                        viewModel.updateStickyNotePosition(note, newX, note.posY)
                        editingNote = note.copy(posX = newX)
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Left")
                    }
                    IconButton(onClick = {
                        val newY = (note.posY - 20).coerceAtLeast(0f)
                        viewModel.updateStickyNotePosition(note, note.posX, newY)
                        editingNote = note.copy(posY = newY)
                    }) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Up")
                    }
                    IconButton(onClick = {
                        val newY = note.posY + 20
                        viewModel.updateStickyNotePosition(note, note.posX, newY)
                        editingNote = note.copy(posY = newY)
                    }) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Down")
                    }
                    IconButton(onClick = {
                        val newX = note.posX + 20
                        viewModel.updateStickyNotePosition(note, newX, note.posY)
                        editingNote = note.copy(posX = newX)
                    }) {
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Right")
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    KawaiiButton(
                        onClick = {
                            editingNote = null
                            content = ""
                        },
                        containerColor = KawaiiLavenderLight,
                        contentColor = KawaiiLavender
                    ) {
                        Text("Done", fontSize = 11.sp)
                    }
                }
            }

            if (editingNote == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KawaiiButton(
                        onClick = {
                            viewModel.addStickyNote(
                                content = content,
                                sticker = selectedSticker,
                                colorHex = selectedColor
                            )
                            content = ""
                        },
                        modifier = Modifier.weight(1.2f),
                        containerColor = KawaiiPink,
                        testTag = "add_sticky_note"
                    ) {
                        Text("Pin Note 📌", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    KawaiiButton(
                        onClick = {
                            viewModel.addTemplateNote()
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = KawaiiLavenderLight,
                        contentColor = KawaiiLavender,
                        testTag = "template_sticky_button"
                    ) {
                        Text("✨ Quick Note", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KawaiiButton(
                        onClick = {
                            editingNote?.let {
                                viewModel.updateStickyNoteContent(it, content, selectedSticker)
                            }
                            editingNote = null
                            content = ""
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = KawaiiMint
                    ) {
                        Text("Save Changes 💖", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        } // End of isReadOnly check

        // Section Title: Dashboard Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📍 Your Sticky Board Snapshot", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KawaiiDarkText)
            Spacer(modifier = Modifier.weight(1f))
            Text("${notes.size} notes", fontSize = 12.sp, color = KawaiiDarkText.copy(alpha = 0.5f))
        }

        // Drag/Grid rendering (Expanded single-column layout for maximum notes clarity!)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            notes.forEach { note ->
                val bgCol = getStickyNoteBgColor(note.colorHex)

                // Beautiful sticky note card with a rotation angle
                val angle = remember(note.id) { (-2..2).random().toFloat() }
                val shadowCol = if (isAppInDarkTheme()) Color(0xFF0F0A16) else (if (note.colorHex == "#FFFFD1E3") ShadowPink else ShadowYellow)

                Box(
                    modifier = Modifier
                        .testTag("sticky_note_${note.id}")
                        .rotate(angle)
                        .padding(bottom = 8.dp)
                ) {
                    // Solid flat bottom shadow
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(y = 6.dp)
                            .background(shadowCol, RoundedCornerShape(24.dp))
                    )
                    // Note content card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgCol, RoundedCornerShape(24.dp))
                            .border(1.5.dp, KawaiiSoftBorder, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(enabled = !isReadOnly) {
                                editingNote = note
                                content = note.content
                                selectedSticker = note.sticker
                                selectedColor = note.colorHex
                            }
                            .padding(16.dp)
                            .defaultMinSize(minHeight = 130.dp)
                    ) {
                        // Content text
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = note.content,
                                fontSize = 14.sp,
                                color = KawaiiDarkText,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            AttributionStampBadge(
                                author = note.lastEditedByAuthor,
                                timestamp = note.lastEditedTimestamp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Space Coordinates • X: ${note.posX.toInt()} Y: ${note.posY.toInt()}",
                                    fontSize = 9.sp,
                                    color = KawaiiDarkText.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                if (isReadOnly) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Workspace is read-only",
                                        tint = KawaiiDarkText.copy(alpha = 0.3f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete note",
                                        tint = KawaiiDarkText.copy(alpha = 0.4f),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { viewModel.deleteStickyNote(note) }
                                    )
                                }
                            }
                        }

                        // Stamped pet sticker at top-right corner - larger size!
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                        ) {
                            Text(note.sticker, fontSize = 28.sp)
                        }
                    }
                }
            }
        }
    }
}
