package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GenshinStickers
import com.example.data.model.Observation
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ObserverViewModel
import java.text.SimpleDateFormat
import java.util.*

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.util.VoiceRecordingManager

@Composable
fun LogsTab(
    viewModel: ObserverViewModel,
    modifier: Modifier = Modifier
) {
    val observations by viewModel.filteredObservations.collectAsState(initial = emptyList())
    val activeWorkspaceId by viewModel.activeWorkspaceId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val factFilter by viewModel.factFilter.collectAsState()
    val isReadOnly by viewModel.activeWorkspaceIsReadOnly.collectAsState()

    // Log Form State
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var ambientBaseline by remember { mutableStateOf("") }
    var anomalies by remember { mutableStateOf("") }
    var isFact by remember { mutableStateOf(true) }
    var selectedTag by remember { mutableStateOf("Stray Kash Watch 🐾") }
    var mediaUri by remember { mutableStateOf<String?>(null) }
    var customNotes by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }

    val tagsPreset = listOf(
        "Stray Kash Watch 🐾",
        "Sakura Park Behavior 🌸",
        "Moonlight Cafe Ambience ✨",
        "Urban Pattern Patterning 🏙️",
        "Stargazing Logs 🌙"
    )

    val illustrations = listOf(
        "illustration_cat_bench" to "🐱 Stray Kash Cozy Bench",
        "illustration_cafe" to "☕ Quiet Moonlight Espresso",
        "illustration_dog_park" to "🐶 Shiba Park Stargazing",
        "illustration_rainy_road" to "🌧️ Rainy Sakura Alleyway"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Workspace Header Chip & Activity
        item {
            ActiveWorkspaceHeaderCard(viewModel = viewModel)
        }

        // Read-Only Workspace Warning Banner
        if (isReadOnly) {
            item {
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
                        Text("👁️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Shared Read-Only Workspace: You have view access to all records but cannot make or delete entries.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KawaiiLavender
                        )
                    }
                }
            }
        }

        // Search & Filter Card
        item {
            KawaiiCard(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                borderColor = KawaiiLavender.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = KawaiiLavender,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search tag, date, location...", color = KawaiiDarkText.copy(alpha = 0.4f)) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_filter_bar"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KawaiiLavender,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = KawaiiDarkText,
                            unfocusedTextColor = KawaiiDarkText
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fact/Inference Segmented Control Filters (Perfect alignment for phones)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Filter By Type:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KawaiiLavender)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KawaiiLavenderLight, RoundedCornerShape(16.dp))
                            .border(1.5.dp, KawaiiSoftBorder, RoundedCornerShape(16.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // All Segment
                        val isAll = factFilter == null
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isAll) KawaiiLavender else Color.Transparent)
                                .clickable { viewModel.setFactFilter(null) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "All Logs ✨",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAll) Color.White else KawaiiLavender
                            )
                        }

                        // Facts Segment
                        val isFactSelected = factFilter == true
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isFactSelected) KawaiiMint else Color.Transparent)
                                .clickable { viewModel.setFactFilter(true) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Facts 🔍",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFactSelected) Color.White else KawaiiMint
                            )
                        }

                        // Inferences Segment
                        val isInfSelected = factFilter == false
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isInfSelected) KawaiiPink else Color.Transparent)
                                .clickable { viewModel.setFactFilter(false) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Inferences 💭",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInfSelected) Color.White else KawaiiPink
                            )
                        }
                    }
                }
            }
        }

        // Toggle New Entry Card (Only if NOT read-only)
        if (!isReadOnly) {
            item {
                KawaiiButton(
                    onClick = { showForm = !showForm },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = KawaiiLavender,
                    testTag = "toggle_form_button"
                ) {
                    Icon(
                        imageVector = if (showForm) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (showForm) "Close Entry Pad 🐾" else "Open New Observation Pad 📝",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // New Observation Form Entry
            if (showForm) {
                item {
                KawaiiCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = KawaiiPink
                ) {
                    Text(
                        text = "🐾 Spotting Journal Entry",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KawaiiPink,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    KawaiiTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "What did you spot? ✨",
                        placeholder = "e.g., Fluffy calico licking paw",
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    KawaiiTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = "Where is this happening? 📍",
                        placeholder = "e.g., North Bench, Quiet Cafe",
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    KawaiiTextField(
                        value = ambientBaseline,
                        onValueChange = { ambientBaseline = it },
                        label = "Ambient Baseline 🌿 (What is normal here?)",
                        placeholder = "e.g., Quiet wind, soft coffee grinder noise, people whispering",
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    KawaiiTextField(
                        value = anomalies,
                        onValueChange = { anomalies = it },
                        label = "Anomalies & Pattern Deviations ⚡ (What broke the pattern?)",
                        placeholder = "e.g., Cat suddenly looked up at 3:15 PM and stared intensely at window",
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Fact vs Inference Toggle Explanation
                    Text(
                        text = "Data Accuracy Mode:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KawaiiDarkText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(KawaiiCreamBg)
                            .border(1.5.dp, KawaiiSoftBorder, RoundedCornerShape(16.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isFact) KawaiiMintLight else Color.Transparent)
                                .clickable { isFact = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Fact 🔍", fontWeight = FontWeight.Bold, color = if (isFact) KawaiiMint else KawaiiDarkText)
                                Text("Raw data/Observation", fontSize = 9.sp, color = KawaiiDarkText.copy(alpha = 0.7f))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (!isFact) KawaiiPinkLight else Color.Transparent)
                                .clickable { isFact = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Inference 💭", fontWeight = FontWeight.Bold, color = if (!isFact) KawaiiPink else KawaiiDarkText)
                                Text("Theory/Assumption", fontSize = 9.sp, color = KawaiiDarkText.copy(alpha = 0.7f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick context presets row
                    Text("Lighting / Context Preset Tag:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiDarkText)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tagsPreset) { tag ->
                            val isSelected = selectedTag == tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isSelected) KawaiiLavenderLight else KawaiiCreamBg)
                                    .border(
                                        1.5.dp,
                                        if (isSelected) KawaiiLavender else KawaiiSoftBorder,
                                        RoundedCornerShape(50)
                                    )
                                    .clickable { selectedTag = tag }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(tag, fontSize = 11.sp, color = if (isSelected) KawaiiLavender else KawaiiDarkText)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Genshin Character Stamp Selector for Observations
                    Text("Attach Genshin Character Stamp:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiDarkText)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(GenshinStickers.items) { item ->
                            val isSelected = mediaUri == item.stampEmoji
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { mediaUri = if (isSelected) null else item.stampEmoji },
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Voice Note Recording Section (Real Microphone Audio)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "🎙️ Voice Note / Audio Observation Memo:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KawaiiDarkText
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val context = LocalContext.current
                    val voiceRecorder = remember { VoiceRecordingManager(context) }
                    var recordedAudioPath by remember { mutableStateOf<String?>(null) }
                    var isRecordingVoice by remember { mutableStateOf(false) }
                    var voiceTimerSec by remember { mutableStateOf(0) }
                    var currentVoiceTranscript by remember { mutableStateOf<String?>(null) }
                    var voiceNoteDuration by remember { mutableStateOf(0) }
                    var isPlayingPreview by remember { mutableStateOf(false) }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            recordedAudioPath = voiceRecorder.startRecording()
                            isRecordingVoice = true
                        }
                    }

                    LaunchedEffect(isRecordingVoice) {
                        if (isRecordingVoice) {
                            voiceTimerSec = 0
                            while (isRecordingVoice) {
                                kotlinx.coroutines.delay(1000)
                                voiceTimerSec++
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isRecordingVoice) KawaiiPinkLight else KawaiiLavenderLight.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isRecordingVoice) KawaiiPink else KawaiiLavender.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        if (isRecordingVoice) {
                                            // Stop recording
                                            isRecordingVoice = false
                                            val path = voiceRecorder.stopRecording()
                                            recordedAudioPath = path
                                            voiceNoteDuration = if (voiceTimerSec == 0) 1 else voiceTimerSec
                                            currentVoiceTranscript = "Recorded ${voiceNoteDuration}s microphone audio memo."
                                        } else {
                                            // Request permission / start recording
                                            currentVoiceTranscript = null
                                            val hasMicPermission = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED

                                            if (hasMicPermission) {
                                                recordedAudioPath = voiceRecorder.startRecording()
                                                isRecordingVoice = true
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isRecordingVoice) KawaiiPink else KawaiiLavender
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isRecordingVoice) Icons.Default.Square else Icons.Default.Mic,
                                        contentDescription = "Voice Record",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isRecordingVoice) "Stop Memo (0:${"%02d".format(voiceTimerSec)})" else "Record Voice Memo 🎙️",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                if (isRecordingVoice) {
                                    Text(
                                        "🔴 RECORDING... 0:${"%02d".format(voiceTimerSec)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KawaiiPink
                                    )
                                } else if (voiceNoteDuration > 0) {
                                    Text(
                                        "Saved (0:${"%02d".format(voiceNoteDuration)}) 🎵",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KawaiiLavender
                                    )
                                }
                            }

                            recordedAudioPath?.let { path ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (isPlayingPreview) {
                                                voiceRecorder.stopPlayback()
                                                isPlayingPreview = false
                                            } else {
                                                isPlayingPreview = true
                                                voiceRecorder.startPlayback(path) {
                                                    isPlayingPreview = false
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(KawaiiLavender, RoundedCornerShape(50))
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingPreview) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play recording",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isPlayingPreview) "Playing recorded audio... 🎵" else "Tap play to test audio memo 🔊",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = KawaiiDarkText
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    KawaiiTextField(
                        value = customNotes,
                        onValueChange = { customNotes = it },
                        label = "Custom Insights & Notes 🐾",
                        placeholder = "Any extra details, questions, or theories..."
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        KawaiiButton(
                            onClick = {
                                customNotes = "✨ Pattern Synthesis: Observed $selectedTag baseline at $location. Normal activity recorded with minor environmental variations."
                            },
                            containerColor = KawaiiLavenderLight,
                            contentColor = KawaiiLavender,
                            testTag = "auto_summary_notes_button"
                        ) {
                            Text("✨ Auto Summary Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    KawaiiButton(
                        onClick = {
                            viewModel.addObservation(
                                title = title,
                                location = location,
                                ambientBaseline = ambientBaseline,
                                anomalies = anomalies,
                                isFact = isFact,
                                tags = selectedTag,
                                mediaUri = mediaUri,
                                notes = customNotes,
                                voiceNoteUri = if (voiceNoteDuration > 0) "memo_${System.currentTimeMillis()}" else null,
                                voiceNoteDurationSeconds = voiceNoteDuration,
                                voiceNoteTranscript = currentVoiceTranscript
                            )
                            // Clear form
                            title = ""
                            location = ""
                            ambientBaseline = ""
                            anomalies = ""
                            isFact = true
                            mediaUri = null
                            customNotes = ""
                            voiceNoteDuration = 0
                            currentVoiceTranscript = null
                            showForm = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = KawaiiPink,
                        testTag = "submit_observation"
                    ) {
                        Text("Log Observation into Workspace 🐾", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } // End of isReadOnly check

        // Empty State
        if (observations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = KawaiiCreamSurface),
                    shape = RoundedCornerShape(24.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(KawaiiLavender, KawaiiPink)))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🐾", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No Observations Logged Yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KawaiiDarkText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Click 'Open New Observation Pad' to log environmental & behavioral patterns!",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = KawaiiDarkText.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        // Logs Feed
        items(observations, key = { it.id }) { obs ->
            KawaiiCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("observation_card_${obs.id}"),
                backgroundColor = KawaiiCreamCardBg,
                borderColor = KawaiiSoftBorder,
                shadowColor = ShadowPink
            ) {
                // Header row mimicking the HTML
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (obs.isFact) "😺" else "💭", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                        Column {
                            Text(
                                text = obs.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = KawaiiDarkText
                            )
                            val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                            Text(
                                text = sdf.format(Date(obs.timestamp)),
                                fontSize = 10.sp,
                                color = KawaiiDarkText.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Delete button
                    if (isReadOnly) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Workspace is read-only",
                            tint = KawaiiLavender.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.deleteObservation(obs) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete observation",
                                tint = KawaiiPink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Detail blocks matching the Design HTML
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Location & Baseline block (full-width)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KawaiiLavenderLight, RoundedCornerShape(16.dp))
                            .border(1.dp, KawaiiSoftBorder, RoundedCornerShape(16.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "LOCATION & BASELINE 📍",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = KawaiiLavender,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Text(
                            text = "${obs.location} • ${obs.ambientBaseline}",
                            fontSize = 11.sp,
                            color = KawaiiDarkText,
                            lineHeight = 14.sp
                        )
                    }

                    // Grid-like double column layout for Inference and Fact Only
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Fact / Anomaly block
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(KawaiiMintLight, RoundedCornerShape(16.dp))
                                .border(1.dp, KawaiiMint.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "FACT ONLY 🔍",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = KawaiiMint,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = obs.anomalies.ifBlank { "No physical anomalies" },
                                fontSize = 11.sp,
                                color = KawaiiDarkText,
                                lineHeight = 14.sp
                            )
                        }

                        // Inference / Insight theory block
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (obs.isFact) KawaiiLavenderLight else KawaiiPinkLight, RoundedCornerShape(16.dp))
                                .border(
                                    1.dp, 
                                    if (obs.isFact) KawaiiSoftBorder else KawaiiPink.copy(alpha = 0.4f), 
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Text(
                                text = if (obs.isFact) "INFERENCE / CONTEXT 💭" else "INFERENCE / THEORY 💭",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (obs.isFact) KawaiiLavender else KawaiiPink,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = obs.notes.ifBlank { "Subject seems normal" },
                                fontSize = 11.sp,
                                color = KawaiiDarkText,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Audio Voice Note Player Block (if voice memo exists)
                    if (obs.voiceNoteDurationSeconds > 0 || obs.voiceNoteTranscript != null) {
                        var isPlayingAudio by remember { mutableStateOf(false) }
                        var currentAudioProgress by remember { mutableStateOf(0f) }

                        LaunchedEffect(isPlayingAudio) {
                            if (isPlayingAudio) {
                                currentAudioProgress = 0f
                                while (isPlayingAudio && currentAudioProgress < 1f) {
                                    kotlinx.coroutines.delay(200)
                                    currentAudioProgress += 0.05f
                                }
                                isPlayingAudio = false
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = KawaiiCreamCardBg),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, KawaiiLavender.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = { isPlayingAudio = !isPlayingAudio },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(KawaiiLavender, RoundedCornerShape(50))
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play voice note",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("🎵 Voice Note Memo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KawaiiLavender)
                                            Text(
                                                "0:${"%02d".format((obs.voiceNoteDurationSeconds * currentAudioProgress).toInt())} / 0:${"%02d".format(obs.voiceNoteDurationSeconds)}",
                                                fontSize = 9.sp,
                                                color = KawaiiDarkText.copy(alpha = 0.6f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        // Simulated Audio Waveform Bar
                                        LinearProgressIndicator(
                                            progress = { currentAudioProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = KawaiiPink,
                                            trackColor = KawaiiPinkLight
                                        )
                                    }
                                }

                                obs.voiceNoteTranscript?.let { transcript ->
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "🗣️ Google AI Transcript: \"$transcript\"",
                                        fontSize = 10.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = KawaiiDarkText.copy(alpha = 0.8f),
                                        lineHeight = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom tags and stamp indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (obs.tags.isNotEmpty()) {
                        KawaiiBadge(
                            text = obs.tags,
                            containerColor = KawaiiLavenderLight,
                            textColor = KawaiiLavender
                        )
                    }

                    AttributionStampBadge(
                        author = obs.lastEditedByAuthor,
                        timestamp = obs.lastEditedTimestamp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Media preview thumbnail inside cute styled frame
                    obs.mediaUri?.let { mediaId ->
                        val emoji = when (mediaId) {
                            "illustration_cat_bench" -> "🐱"
                            "illustration_cafe" -> "☕"
                            "illustration_dog_park" -> "🐶"
                            "illustration_rainy_road" -> "🌧"
                            else -> "🖼"
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(KawaiiPinkLight)
                                .border(1.dp, KawaiiPink, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
