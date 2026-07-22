package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskItem
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ObserverViewModel

@Composable
fun TasksTab(
    viewModel: ObserverViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.taskItems.collectAsState(initial = emptyList())
    val isReadOnly by viewModel.activeWorkspaceIsReadOnly.collectAsState()
    var newTaskTitle by remember { mutableStateOf("") }

    val completedCount = tasks.count { it.isCompleted }
    val progress = if (tasks.isNotEmpty()) completedCount.toFloat() / tasks.size else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Workspace Header Chip & Activity
        item {
            ActiveWorkspaceHeaderCard(viewModel = viewModel)
        }

        // Read-Only Warning Banner
        if (isReadOnly) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
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
                            text = "Shared Read-Only Workspace: You have view access to all objectives but cannot add, toggle, or delete tasks.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KawaiiLavender
                        )
                    }
                }
            }
        }

        // Progress Card
        item {
            KawaiiCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = KawaiiLavender
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🐾 Observer Training Progress",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KawaiiLavender
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Completed $completedCount of ${tasks.size} studio tasks!",
                            fontSize = 12.sp,
                            color = KawaiiDarkText.copy(alpha = 0.7f)
                        )
                    }
                    
                    Text(
                        text = if (progress == 1f) "👑 Elite Kash Coordinator" else "🌱 Novice Observer",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KawaiiPink,
                        modifier = Modifier
                            .background(KawaiiPinkLight, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = KawaiiPink,
                    trackColor = KawaiiLavenderLight
                )
            }
        }

        // Add Task card (Only if NOT read-only)
        if (!isReadOnly) {
            item {
                KawaiiCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = KawaiiPink,
                    shadowColor = ShadowPink
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = newTaskTitle,
                                onValueChange = { newTaskTitle = it },
                                placeholder = { Text("Add daily observer objective...", fontSize = 11.sp, color = KawaiiDarkText.copy(alpha = 0.5f)) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("task_input_field"),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = KawaiiPink,
                                    unfocusedBorderColor = KawaiiPink.copy(alpha = 0.4f),
                                    focusedContainerColor = KawaiiCreamCardBg,
                                    unfocusedContainerColor = KawaiiCreamCardBg,
                                    focusedTextColor = KawaiiDarkText,
                                    unfocusedTextColor = KawaiiDarkText
                                )
                            )

                            IconButton(
                                onClick = {
                                    viewModel.addTaskItem(newTaskTitle)
                                    newTaskTitle = ""
                                },
                                modifier = Modifier
                                    .testTag("add_task_button")
                                    .size(48.dp)
                                    .background(KawaiiPink, RoundedCornerShape(14.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add task",
                                    tint = Color.White
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            KawaiiButton(
                                onClick = {
                                    viewModel.addTemplateTask()
                                },
                                containerColor = KawaiiLavenderLight,
                                contentColor = KawaiiLavender,
                                testTag = "template_task_button"
                            ) {
                                Text("✨ Quick Objective", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Tasks List Items
        items(tasks, key = { it.id }) { task ->
            Box(
                modifier = Modifier
                    .testTag("task_item_${task.id}")
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                val shadowCol = if (task.isCompleted) ShadowPink.copy(alpha = 0.3f) else ShadowPink
                // Solid flat bottom shadow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(y = 6.dp)
                        .background(shadowCol, RoundedCornerShape(18.dp))
                )
                // Content row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (task.isCompleted) KawaiiPinkLight.copy(alpha = 0.35f) else KawaiiCreamSurface,
                            RoundedCornerShape(18.dp)
                        )
                        .border(
                            1.5.dp,
                            if (task.isCompleted) KawaiiPink.copy(alpha = 0.4f) else KawaiiLavender.copy(alpha = 0.5f),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox
                    KawaiiCheckbox(
                        checked = task.isCompleted,
                        onCheckedChange = { if (!isReadOnly) viewModel.toggleTaskItem(task) },
                        testTag = "task_checkbox_${task.id}"
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Text & Attribution
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            fontSize = 14.sp,
                            fontWeight = if (task.isCompleted) FontWeight.Medium else FontWeight.Bold,
                            color = if (task.isCompleted) KawaiiDarkText.copy(alpha = 0.5f) else KawaiiDarkText,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        AttributionStampBadge(
                            author = task.lastEditedByAuthor,
                            timestamp = task.lastEditedTimestamp
                        )
                    }

                    // Delete
                    if (isReadOnly) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Workspace is read-only",
                            tint = KawaiiLavender.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp).padding(end = 8.dp)
                        )
                    } else {
                        IconButton(onClick = { viewModel.deleteTaskItem(task) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete task",
                                tint = KawaiiPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
