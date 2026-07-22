package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.GenshinStickers
import com.example.data.model.JoinRequest
import com.example.data.model.UserRole
import com.example.data.model.Workspace
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.JoiningState
import com.example.viewmodel.ObserverViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ObserverViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val clipboardManager = LocalClipboardManager.current

    val userName by viewModel.userName.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val hasProfile by viewModel.hasProfile.collectAsState()
    val userId by viewModel.userId.collectAsState()

    val workspaces by viewModel.workspaces.collectAsState(initial = emptyList())
    val activeWorkspaceId by viewModel.activeWorkspaceId.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    val joinRequests by viewModel.joinRequests.collectAsState()
    val joiningState by viewModel.joiningState.collectAsState()

    val themeMode by viewModel.themeMode.collectAsState()
    var activeTab by remember { mutableStateOf(0) } // 0 = Logs, 1 = Sticky Board, 2 = Tasks
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showShareWorkspaceDialog by remember { mutableStateOf(false) }

    // First Launch Profile Setup Modal
    if (!hasProfile) {
        ProfileSetupDialog(
            initialName = "",
            initialAvatar = "😺",
            titleText = "Create Your Observer Profile 🐾",
            buttonText = "Start Exploring! 🚀",
            onSave = { name, avatar ->
                viewModel.saveUserProfile(name, avatar)
            }
        )
    }

    // Edit Profile Modal
    if (showEditProfileDialog) {
        ProfileSetupDialog(
            initialName = userName,
            initialAvatar = userAvatar,
            titleText = "Edit Observer Profile ✏️",
            buttonText = "Save Changes 💖",
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, avatar ->
                viewModel.saveUserProfile(name, avatar)
                showEditProfileDialog = false
            }
        )
    }

    // Share & Join Workspace Dialog
    if (showShareWorkspaceDialog) {
        ShareAndJoinWorkspaceDialog(
            viewModel = viewModel,
            clipboardManager = clipboardManager,
            onDismiss = { showShareWorkspaceDialog = false }
        )
    }

    // Host Approval Alert Pop-Up
    if (joinRequests.isNotEmpty() && currentUserRole == UserRole.HOST) {
        val requestToReview = joinRequests.first()
        HostApprovalAlertDialog(
            request = requestToReview,
            onApprove = { role -> viewModel.approveJoinRequest(requestToReview, role) },
            onDecline = { viewModel.declineJoinRequest(requestToReview) }
        )
    }

    // Joining State Screen Overlay
    when (val state = joiningState) {
        is JoiningState.WaitingForApproval -> {
            WaitingForHostApprovalOverlay(
                workspaceName = state.workspaceName,
                onCancel = { viewModel.dismissJoiningState() }
            )
        }
        is JoiningState.Approved -> {
            LaunchedEffect(state) {
                kotlinx.coroutines.delay(1200)
                viewModel.dismissJoiningState()
            }
        }
        else -> {}
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
                    .testTag("sleek_sidebar_drawer")
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Drawer User Header Card
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(KawaiiLavenderLight, RoundedCornerShape(20.dp))
                                .border(2.dp, KawaiiLavender.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Konnichiwa, Researcher! 🌸", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KawaiiLavender)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(userAvatar, fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(userName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KawaiiDarkText)
                            Text("ID: ${userId.take(12)}...", fontSize = 10.sp, color = KawaiiDarkText.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(8.dp))
                            KawaiiButton(
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showEditProfileDialog = true
                                },
                                containerColor = KawaiiPinkLight,
                                testTag = "sidebar_edit_profile_btn"
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Workspace Section Title
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🐾 Active Workspaces",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = KawaiiLavender
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showShareWorkspaceDialog = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Workspace", tint = KawaiiPink)
                            }
                        }
                    }

                    // List workspaces
                    items(workspaces) { wb ->
                        val isSelected = wb.id == activeWorkspaceId
                        Row(
                            modifier = Modifier
                                .testTag("workspace_item_${wb.id}")
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) KawaiiPinkLight else Color.Transparent)
                                .border(
                                    1.5.dp,
                                    if (isSelected) KawaiiPink else KawaiiSoftBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    viewModel.selectWorkspace(wb.id)
                                    coroutineScope.launch { drawerState.close() }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(wb.icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = wb.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KawaiiDarkText
                                )
                                Text(
                                    text = "Host: ${wb.hostAvatar} ${wb.hostName}",
                                    fontSize = 9.sp,
                                    color = KawaiiDarkText.copy(alpha = 0.6f)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteWorkspace(wb) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Workspace",
                                    tint = KawaiiPink,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Share & Join Workspace Quick Actions
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KawaiiButton(
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showShareWorkspaceDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = KawaiiPink,
                                testTag = "sidebar_share_workspace_btn"
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share or Join Workspace 🌐", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            KawaiiButton(
                                onClick = { viewModel.deleteAllWorkspaces() },
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = KawaiiPinkLight,
                                contentColor = KawaiiPink,
                                testTag = "clear_all_workspaces_btn"
                            ) {
                                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reset Workspaces 🗑️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Stray Kash 🐾", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = KawaiiLavender)
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("open_sidebar_drawer_button")
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Drawer", tint = KawaiiLavender)
                        }
                    },
                    actions = {
                        // Theme Mode Toggle (Light/Dark)
                        IconButton(
                            onClick = { viewModel.toggleThemeMode() },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Text(if (themeMode == "dark") "🌙" else "☀️", fontSize = 18.sp)
                        }

                        // Share Workspace Icon
                        IconButton(
                            onClick = { showShareWorkspaceDialog = true },
                            modifier = Modifier.testTag("top_bar_share_workspace_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share Workspace", tint = KawaiiPink)
                        }

                        // Top-Right User Profile Badge
                        Row(
                            modifier = Modifier
                                .testTag("top_profile_badge")
                                .clip(RoundedCornerShape(20.dp))
                                .background(KawaiiPinkLight)
                                .border(1.dp, KawaiiPink.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .clickable { showEditProfileDialog = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(userAvatar, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = userName.ifBlank { "Observer" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = KawaiiDarkText
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .border(1.dp, KawaiiSoftBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = { Text("📜", fontSize = 20.sp) },
                        label = { Text("Logs", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_logs")
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Text("📌", fontSize = 20.sp) },
                        label = { Text("Sticky Board", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_sticky")
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        icon = { Text("🎯", fontSize = 20.sp) },
                        label = { Text("Tasks", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        modifier = Modifier.testTag("nav_tab_tasks")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (activeTab) {
                    0 -> LogsTab(viewModel = viewModel)
                    1 -> StickyBoardTab(viewModel = viewModel)
                    2 -> TasksTab(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ProfileSetupDialog(
    initialName: String,
    initialAvatar: String,
    titleText: String,
    buttonText: String,
    onDismiss: (() -> Unit)? = null,
    onSave: (String, String) -> Unit
) {
    var nameInput by remember { mutableStateOf(initialName) }
    var selectedAvatar by remember { mutableStateOf(if (initialAvatar.isBlank()) "😺" else initialAvatar) }

    val avatarList = listOf("💫", "💥", "❄️", "👻", "🌿", "💧", "⚡", "🦊", "🍃", "🔶", "🌪️", "🦅", "🧊", "🌊", "⚖️", "😺", "🐶", "🐼", "🐰")

    Dialog(onDismissRequest = { onDismiss?.invoke() }) {
        KawaiiCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = KawaiiPink
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = titleText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KawaiiLavender,
                    textAlign = TextAlign.Center
                )

                // Selected Mascot Preview
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(KawaiiPinkLight)
                        .border(3.dp, KawaiiPink, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedAvatar, fontSize = 44.sp)
                }

                // Mascot Selector Row
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select Your Mascot Avatar:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiDarkText)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        items(GenshinStickers.items) { item ->
                            val isSel = item.stampEmoji == selectedAvatar
                            Surface(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { selectedAvatar = item.stampEmoji },
                                color = if (isSel) KawaiiPinkLight else KawaiiCreamBg,
                                shape = CircleShape,
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    if (isSel) KawaiiPink else KawaiiSoftBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.stampEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = item.characterName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        color = KawaiiDarkText
                                    )
                                }
                            }
                        }
                    }
                }

                // Name Input Box
                KawaiiTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = "Display Name / Researcher Handle",
                    placeholder = "e.g. Neko Researcher, Akash 🐾",
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onDismiss != null) {
                        KawaiiButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            containerColor = KawaiiLavenderLight,
                            contentColor = KawaiiLavender
                        ) {
                            Text("Cancel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    KawaiiButton(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                onSave(nameInput, selectedAvatar)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        containerColor = KawaiiPink
                    ) {
                        Text(buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ShareAndJoinWorkspaceDialog(
    viewModel: ObserverViewModel,
    clipboardManager: ClipboardManager,
    onDismiss: () -> Unit
) {
    val activeWorkspaceId by viewModel.activeWorkspaceId.collectAsState()
    val workspaces by viewModel.workspaces.collectAsState(initial = emptyList())
    val currentWb = workspaces.find { it.id == activeWorkspaceId }

    var joinCodeInput by remember { mutableStateOf("") }
    var newWbName by remember { mutableStateOf("") }
    var newWbIcon by remember { mutableStateOf("🐾") }
    val iconList = listOf("🐾", "🌸", "✨", "😺", "🐶", "🍙", "🍵")

    var copiedNotice by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        KawaiiCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = KawaiiLavender
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌐 Workspace Collaboration", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KawaiiLavender)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = KawaiiDarkText)
                    }
                }

                // Section 1: Active Workspace Share Code & Link
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KawaiiLavenderLight, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Text("Current Workspace: ${currentWb?.icon ?: "🐾"} ${currentWb?.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KawaiiDarkText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Share Code: $activeWorkspaceId", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = KawaiiLavender)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            val shareUrl = "https://straykash.app/observe?workspace=$activeWorkspaceId"
                            clipboardManager.setText(AnnotatedString(shareUrl))
                            copiedNotice = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KawaiiPink, contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (copiedNotice) "Copied Share Link! 📋" else "Copy Direct Share Link 🔗", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Section 2: Join Existing Shared Workspace
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🚀 Join Existing Workspace", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KawaiiDarkText)
                    KawaiiTextField(
                        value = joinCodeInput,
                        onValueChange = { joinCodeInput = it },
                        label = "Enter Workspace ID / Code",
                        placeholder = "e.g. neko-obs-8a9f2b",
                        singleLine = true
                    )

                    KawaiiButton(
                        onClick = {
                            if (joinCodeInput.isNotBlank()) {
                                viewModel.requestToJoinWorkspace(joinCodeInput)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = KawaiiLavender
                    ) {
                        Text("Send Join Request 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Section 3: Create New Workspace
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("✨ Create New Shared Workspace", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KawaiiDarkText)
                    KawaiiTextField(
                        value = newWbName,
                        onValueChange = { newWbName = it },
                        label = "Workspace Name",
                        placeholder = "e.g. Park Observation Squad",
                        singleLine = true
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(iconList) { ic ->
                            val isSel = ic == newWbIcon
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSel) KawaiiPinkLight else KawaiiCreamBg)
                                    .border(1.5.dp, if (isSel) KawaiiPink else KawaiiSoftBorder, CircleShape)
                                    .clickable { newWbIcon = ic },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(ic, fontSize = 18.sp)
                            }
                        }
                    }

                    KawaiiButton(
                        onClick = {
                            if (newWbName.isNotBlank()) {
                                viewModel.createWorkspace(newWbName, newWbIcon)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = KawaiiMint
                    ) {
                        Text("Create Workspace 🌟", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiDarkText)
                    }
                }
            }
        }
    }
}

@Composable
fun HostApprovalAlertDialog(
    request: JoinRequest,
    onApprove: (UserRole) -> Unit,
    onDecline: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        KawaiiCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = KawaiiPink
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("🔔 Join Workspace Request", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KawaiiPink)

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(KawaiiPinkLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(request.applicantAvatar, fontSize = 36.sp)
                }

                Text(
                    text = "${request.applicantAvatar} ${request.applicantName} wants to join your workspace!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = KawaiiDarkText,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Choose permission level for this researcher:",
                    fontSize = 11.sp,
                    color = KawaiiDarkText.copy(alpha = 0.7f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onApprove(UserRole.EDITOR) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = KawaiiMint, contentColor = KawaiiDarkText),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Allow as Editor ✏️", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onApprove(UserRole.VIEWER) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = KawaiiLavenderLight, contentColor = KawaiiLavender),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Allow as Viewer 🔒", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onDecline,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = KawaiiPinkLight, contentColor = KawaiiPink),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Decline Request ❌", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WaitingForHostApprovalOverlay(
    workspaceName: String,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        KawaiiCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = KawaiiLavender
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = KawaiiLavender, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))

                Text("Waiting for Host Approval... 🐾", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KawaiiLavender, textAlign = TextAlign.Center)

                Text(
                    text = "Your request to join \"$workspaceName\" has been sent to the workspace host.",
                    fontSize = 12.sp,
                    color = KawaiiDarkText.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                KawaiiButton(
                    onClick = onCancel,
                    containerColor = KawaiiLavenderLight,
                    contentColor = KawaiiLavender
                ) {
                    Text("Cancel Request", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
