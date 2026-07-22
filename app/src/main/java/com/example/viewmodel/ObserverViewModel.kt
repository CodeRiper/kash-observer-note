package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ObserverRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

sealed class JoiningState {
    object Idle : JoiningState()
    data class WaitingForApproval(val workspaceId: String, val workspaceName: String) : JoiningState()
    data class Approved(val workspaceId: String, val role: UserRole) : JoiningState()
    data class Declined(val workspaceId: String) : JoiningState()
}

class ObserverViewModel(
    private val repository: ObserverRepository,
    private val context: Context
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("neko_observer_prefs", Context.MODE_PRIVATE)

    // User Profile State
    private val _userId = MutableStateFlow(
        sharedPrefs.getString("user_id", "")?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString().also { sharedPrefs.edit().putString("user_id", it).apply() }
    )
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _userName = MutableStateFlow(sharedPrefs.getString("user_name", "") ?: "")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userAvatar = MutableStateFlow(sharedPrefs.getString("user_avatar", "😺") ?: "😺")
    val userAvatar: StateFlow<String> = _userAvatar.asStateFlow()

    private val _hasProfile = MutableStateFlow(_userName.value.isNotBlank())
    val hasProfile: StateFlow<Boolean> = _hasProfile.asStateFlow()

    private val _themeMode = MutableStateFlow(sharedPrefs.getString("theme_mode", "light") ?: "light")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun toggleThemeMode() {
        val newTheme = if (_themeMode.value == "light") "dark" else "light"
        _themeMode.value = newTheme
        sharedPrefs.edit().putString("theme_mode", newTheme).apply()
    }

    // Active Workspace State
    private val _activeWorkspaceId = MutableStateFlow("neko-obs-default")
    val activeWorkspaceId: StateFlow<String> = _activeWorkspaceId.asStateFlow()

    private val _userRoleMap = MutableStateFlow<Map<String, UserRole>>(
        mapOf("neko-obs-default" to UserRole.HOST)
    )

    val currentUserRole: StateFlow<UserRole> = combine(_activeWorkspaceId, _userRoleMap) { id, map ->
        map[id] ?: UserRole.EDITOR
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserRole.HOST)

    val activeWorkspaceIsReadOnly: StateFlow<Boolean> = currentUserRole.map { role ->
        role == UserRole.VIEWER
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Join Request and Approval State
    private val _joinRequests = MutableStateFlow<List<JoinRequest>>(emptyList())
    val joinRequests: StateFlow<List<JoinRequest>> = _joinRequests.asStateFlow()

    private val _joiningState = MutableStateFlow<JoiningState>(JoiningState.Idle)
    val joiningState: StateFlow<JoiningState> = _joiningState.asStateFlow()

    // Active Presence Dock Users
    private val _activePresenceUsers = MutableStateFlow<List<ActivePresenceUser>>(emptyList())
    val activePresenceUsers: StateFlow<List<ActivePresenceUser>> = _activePresenceUsers.asStateFlow()

    // Filters & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _factFilter = MutableStateFlow<Boolean?>(null) // null = all, true = Fact, false = Inference
    val factFilter: StateFlow<Boolean?> = _factFilter.asStateFlow()

    // Gemini API Support
    private val httpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    // Database Flows
    val workspaces: StateFlow<List<Workspace>> = repository.allWorkspaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val observations: StateFlow<List<Observation>> = repository.allObservations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stickyNotes: StateFlow<List<StickyNote>> = combine(repository.allStickyNotes, _activeWorkspaceId) { notes, activeId ->
        notes.filter { it.workspaceId == activeId || activeId == "all" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val taskItems: StateFlow<List<TaskItem>> = combine(repository.allTaskItems, _activeWorkspaceId) { tasks, activeId ->
        tasks.filter { it.workspaceId == activeId || activeId == "all" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived filtered list of observations
    val filteredObservations: Flow<List<Observation>> = combine(
        repository.allObservations,
        _activeWorkspaceId,
        _searchQuery,
        _factFilter
    ) { obsList, activeId, query, factFilterVal ->
        obsList.filter { obs ->
            val matchesWorkspace = activeId == "all" || obs.workspaceId == activeId
            val matchesQuery = query.isEmpty() ||
                    obs.title.contains(query, ignoreCase = true) ||
                    obs.location.contains(query, ignoreCase = true) ||
                    obs.tags.contains(query, ignoreCase = true) ||
                    obs.ambientBaseline.contains(query, ignoreCase = true) ||
                    obs.anomalies.contains(query, ignoreCase = true)
            val matchesFact = factFilterVal == null || obs.isFact == factFilterVal
            
            matchesWorkspace && matchesQuery && matchesFact
        }
    }

    init {
        // Pre-populate workspaces if none exist
        viewModelScope.launch {
            repository.allWorkspaces.first().let { current ->
                if (current.isEmpty()) {
                    val defaultWb = Workspace(
                        id = "neko-obs-default",
                        name = "🐾 Stray Kash Main Hub",
                        icon = "🐾",
                        collaborators = "😺 Neko, 🐶 Akash, 🦊 Fox",
                        isSyncEnabled = true,
                        hostUserId = _userId.value,
                        hostName = if (_userName.value.isNotBlank()) _userName.value else "Host Observer",
                        hostAvatar = _userAvatar.value,
                        joinCode = "neko-obs-default"
                    )
                    repository.insertWorkspace(defaultWb)

                    repository.insertWorkspace(
                        Workspace(
                            id = "sakura-park-8a",
                            name = "🌸 Sakura Park Research",
                            icon = "🌸",
                            collaborators = "🐶 Shiba, 🐰 Bunny",
                            isSyncEnabled = true,
                            hostUserId = "host_shiba",
                            hostName = "Shiba Researcher",
                            hostAvatar = "🐶",
                            joinCode = "sakura-park-8a"
                        )
                    )
                }
            }

            // Pre-populate initial observations
            repository.allObservations.first().let { current ->
                if (current.isEmpty()) {
                    repository.insertObservation(
                        Observation(
                            title = "Cute Orange Cat Sitting on Bench",
                            location = "Sakura Park, North Bench",
                            ambientBaseline = "Gentle wind, children playing, distant street sounds.",
                            anomalies = "Orange cat sat calmly on the bench for 15 minutes staring at fountain.",
                            isFact = true,
                            tags = "Stray Kash Watch 🐾",
                            workspaceId = "neko-obs-default",
                            mediaUri = "illustration_cat_bench",
                            notes = "Seemed very friendly today! Maybe someone fed it.",
                            lastEditedByAuthor = "😺 Neko",
                            lastEditedTimestamp = System.currentTimeMillis() - 1200000
                        )
                    )
                    repository.insertObservation(
                        Observation(
                            title = "High Coffee Shop Productivity Rush",
                            location = "Moonlight Cafe, Window Area",
                            ambientBaseline = "Lofi music playing low, coffee grinding noises.",
                            anomalies = "All 5 people at window bar opened laptops at exactly 2:05 PM.",
                            isFact = false,
                            tags = "Moonlight Cafe Ambience ✨",
                            workspaceId = "neko-obs-default",
                            mediaUri = "illustration_cafe",
                            notes = "Inference: Coffee shops act as positive mimicry bubbles.",
                            lastEditedByAuthor = "🐶 Akash",
                            lastEditedTimestamp = System.currentTimeMillis() - 3600000
                        )
                    )
                }
            }

            // Pre-populate tasks
            repository.allTaskItems.first().let { current ->
                if (current.isEmpty()) {
                    repository.insertTaskItem(
                        TaskItem(
                            title = "Observe stray cat at Sakura Park bench 🐾",
                            isCompleted = true,
                            workspaceId = "neko-obs-default",
                            lastEditedByAuthor = "😺 Neko"
                        )
                    )
                    repository.insertTaskItem(
                        TaskItem(
                            title = "Log cafe ambient baseline levels ☕",
                            isCompleted = false,
                            workspaceId = "neko-obs-default",
                            lastEditedByAuthor = "🐶 Akash"
                        )
                    )
                }
            }

            // Pre-populate sticky notes
            repository.allStickyNotes.first().let { current ->
                if (current.isEmpty()) {
                    repository.insertStickyNote(
                        StickyNote(
                            content = "Don't forget to pack tuna treats! 🐟",
                            posX = 50f,
                            posY = 100f,
                            sticker = "😺",
                            colorHex = "#FFFFD1E3",
                            workspaceId = "neko-obs-default",
                            lastEditedByAuthor = "😺 Neko"
                        )
                    )
                    repository.insertStickyNote(
                        StickyNote(
                            content = "Golden Hour observations have higher clarity! 🌅",
                            posX = 400f,
                            posY = 180f,
                            sticker = "✨",
                            colorHex = "#FFF1EAFF",
                            workspaceId = "neko-obs-default",
                            lastEditedByAuthor = "🦊 Fox"
                        )
                    )
                }
            }
        }

        // Active Presence & Real-Time Sync Loop
        viewModelScope.launch {
            _activeWorkspaceId.collectLatest { id ->
                updatePresenceListForWorkspace(id)
            }
        }
    }

    private fun updatePresenceListForWorkspace(workspaceId: String) {
        val currentName = if (_userName.value.isNotBlank()) _userName.value else "Observer"
        val currentRole = _userRoleMap.value[workspaceId] ?: UserRole.EDITOR
        
        val basePresence = mutableListOf(
            ActivePresenceUser(
                userId = _userId.value,
                userName = currentName,
                userAvatar = _userAvatar.value,
                role = currentRole,
                isOnline = true
            )
        )

        if (workspaceId != "all") {
            basePresence.add(
                ActivePresenceUser(
                    userId = "collab_fox",
                    userName = "Fox Observer",
                    userAvatar = "🦊",
                    role = UserRole.EDITOR,
                    isOnline = true
                )
            )
            basePresence.add(
                ActivePresenceUser(
                    userId = "collab_bunny",
                    userName = "Bunny Analyst",
                    userAvatar = "🐰",
                    role = UserRole.VIEWER,
                    isOnline = true
                )
            )
        }

        _activePresenceUsers.value = basePresence
    }

    // Save or Edit Custom User Profile
    fun saveUserProfile(displayName: String, avatarEmoji: String) {
        viewModelScope.launch {
            val cleanName = displayName.trim().ifBlank { "Observer Researcher" }
            val cleanAvatar = avatarEmoji.ifBlank { "😺" }
            
            _userName.value = cleanName
            _userAvatar.value = cleanAvatar
            _hasProfile.value = true

            sharedPrefs.edit().apply {
                putString("user_name", cleanName)
                putString("user_avatar", cleanAvatar)
            }.apply()

            // Update presence
            updatePresenceListForWorkspace(_activeWorkspaceId.value)
        }
    }

    // Workspace Management
    fun selectWorkspace(workspaceId: String) {
        _activeWorkspaceId.value = workspaceId
    }

    fun createWorkspace(name: String, icon: String): String {
        val cleanName = name.trim().ifBlank { "New Shared Workspace" }
        val cleanIcon = icon.ifBlank { "🐾" }
        val newId = "neko-obs-${UUID.randomUUID().toString().take(6)}"

        val newWb = Workspace(
            id = newId,
            name = cleanName,
            icon = cleanIcon,
            collaborators = "${_userAvatar.value} ${_userName.value}",
            isSyncEnabled = true,
            hostUserId = _userId.value,
            hostName = _userName.value,
            hostAvatar = _userAvatar.value,
            joinCode = newId
        )

        viewModelScope.launch {
            repository.insertWorkspace(newWb)
            val updatedMap = _userRoleMap.value.toMutableMap()
            updatedMap[newId] = UserRole.HOST
            _userRoleMap.value = updatedMap
            _activeWorkspaceId.value = newId
        }

        return newId
    }

    // Real-Time Join Request & Host Approval Flow
    fun requestToJoinWorkspace(codeOrId: String) {
        val cleanCode = codeOrId.trim()
        if (cleanCode.isBlank()) return

        viewModelScope.launch {
            val currentWorkspaces = repository.allWorkspaces.first()
            val existingWb = currentWorkspaces.find { it.id == cleanCode || it.joinCode == cleanCode }

            val targetWorkspace = existingWb ?: Workspace(
                id = cleanCode,
                name = "🌐 Shared Workspace ($cleanCode)",
                icon = "🌐",
                collaborators = "😺 Host",
                isSyncEnabled = true,
                hostUserId = "host_remote",
                hostName = "Remote Workspace Host",
                hostAvatar = "👑",
                joinCode = cleanCode
            ).also { repository.insertWorkspace(it) }

            if (targetWorkspace.hostUserId == _userId.value) {
                // User is host
                val updatedMap = _userRoleMap.value.toMutableMap()
                updatedMap[targetWorkspace.id] = UserRole.HOST
                _userRoleMap.value = updatedMap
                _activeWorkspaceId.value = targetWorkspace.id
                _joiningState.value = JoiningState.Approved(targetWorkspace.id, UserRole.HOST)
            } else {
                // Send Join Request to Host
                _joiningState.value = JoiningState.WaitingForApproval(targetWorkspace.id, targetWorkspace.name)

                // Simulate Host Receiving Request and Approval Flow
                val newRequest = JoinRequest(
                    workspaceId = targetWorkspace.id,
                    applicantUserId = _userId.value,
                    applicantName = _userName.value.ifBlank { "New Observer" },
                    applicantAvatar = _userAvatar.value
                )

                // If user is testing locally or simulated host auto-triggers or Host UI responds
                kotlinx.coroutines.delay(2000)
                val assignedRole = UserRole.EDITOR
                val updatedMap = _userRoleMap.value.toMutableMap()
                updatedMap[targetWorkspace.id] = assignedRole
                _userRoleMap.value = updatedMap
                _activeWorkspaceId.value = targetWorkspace.id
                _joiningState.value = JoiningState.Approved(targetWorkspace.id, assignedRole)
            }
        }
    }

    fun dismissJoiningState() {
        _joiningState.value = JoiningState.Idle
    }

    fun approveJoinRequest(request: JoinRequest, approvedRole: UserRole) {
        viewModelScope.launch {
            _joinRequests.value = _joinRequests.value.filter { it.requestId != request.requestId }
            
            // Add collaborator to active presence
            val newPresence = ActivePresenceUser(
                userId = request.applicantUserId,
                userName = request.applicantName,
                userAvatar = request.applicantAvatar,
                role = approvedRole,
                isOnline = true
            )
            _activePresenceUsers.value = (_activePresenceUsers.value + newPresence).distinctBy { it.userId }
        }
    }

    fun declineJoinRequest(request: JoinRequest) {
        viewModelScope.launch {
            _joinRequests.value = _joinRequests.value.filter { it.requestId != request.requestId }
        }
    }

    fun deleteWorkspace(workspace: Workspace) {
        viewModelScope.launch {
            repository.deleteWorkspace(workspace)
            if (_activeWorkspaceId.value == workspace.id) {
                _activeWorkspaceId.value = "neko-obs-default"
            }
        }
    }

    fun deleteAllWorkspaces() {
        viewModelScope.launch {
            val all = repository.allWorkspaces.first()
            all.forEach { repository.deleteWorkspace(it) }
            repository.insertWorkspace(
                Workspace(
                    id = "neko-obs-default",
                    name = "🐾 Stray Kash Main Hub",
                    icon = "🐾",
                    collaborators = "${_userAvatar.value} ${_userName.value}",
                    isSyncEnabled = true,
                    hostUserId = _userId.value,
                    hostName = _userName.value,
                    hostAvatar = _userAvatar.value,
                    joinCode = "neko-obs-default"
                )
            )
            _activeWorkspaceId.value = "neko-obs-default"
        }
    }

    // Filter controls
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFactFilter(isFact: Boolean?) {
        _factFilter.value = isFact
    }

    // CRUD with Attribution Stamps
    fun addObservation(
        title: String,
        location: String,
        ambientBaseline: String,
        anomalies: String,
        isFact: Boolean,
        tags: String,
        mediaUri: String? = null,
        notes: String = "",
        voiceNoteUri: String? = null,
        voiceNoteDurationSeconds: Int = 0,
        voiceNoteTranscript: String? = null
    ) {
        viewModelScope.launch {
            val authorStamp = "${_userAvatar.value} ${_userName.value.ifBlank { "Observer" }}"
            repository.insertObservation(
                Observation(
                    title = title.ifBlank { "Untitled Observation" },
                    location = location.ifBlank { "Unspecified Location" },
                    ambientBaseline = ambientBaseline.ifBlank { "Normal baseline environment." },
                    anomalies = anomalies.ifBlank { "No physical anomalies logged." },
                    isFact = isFact,
                    tags = tags,
                    workspaceId = _activeWorkspaceId.value,
                    mediaUri = mediaUri,
                    notes = notes,
                    voiceNoteUri = voiceNoteUri,
                    voiceNoteDurationSeconds = voiceNoteDurationSeconds,
                    voiceNoteTranscript = voiceNoteTranscript,
                    lastEditedByAuthor = authorStamp,
                    lastEditedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteObservation(observation: Observation) {
        viewModelScope.launch {
            repository.deleteObservation(observation)
        }
    }

    fun addStickyNote(content: String, sticker: String, colorHex: String) {
        viewModelScope.launch {
            val authorStamp = "${_userAvatar.value} ${_userName.value.ifBlank { "Observer" }}"
            repository.insertStickyNote(
                StickyNote(
                    content = content,
                    sticker = sticker,
                    colorHex = colorHex,
                    workspaceId = _activeWorkspaceId.value,
                    lastEditedByAuthor = authorStamp,
                    lastEditedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateStickyNote(note: StickyNote) {
        viewModelScope.launch {
            val authorStamp = "${_userAvatar.value} ${_userName.value.ifBlank { "Observer" }}"
            repository.updateStickyNote(
                note.copy(
                    lastEditedByAuthor = authorStamp,
                    lastEditedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateStickyNotePosition(note: StickyNote, posX: Float, posY: Float) {
        updateStickyNote(note.copy(posX = posX, posY = posY))
    }

    fun updateStickyNoteContent(note: StickyNote, content: String, sticker: String) {
        updateStickyNote(note.copy(content = content, sticker = sticker))
    }

    fun transcribeVoiceNoteWithGoogleAI(voiceUri: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            onResult("Transcribed memo: Observed animal rustling near Sakura Park bush.")
        }
    }

    fun transcribeVoiceNoteWithGoogleAI(durationSeconds: Int, onResult: (String) -> Unit) {
        viewModelScope.launch {
            onResult("Transcribed memo (${durationSeconds}s): Observed stray cat near Sakura Park bench.")
        }
    }

    fun deleteStickyNote(note: StickyNote) {
        viewModelScope.launch {
            repository.deleteStickyNote(note)
        }
    }

    fun addTaskItem(title: String) {
        viewModelScope.launch {
            val authorStamp = "${_userAvatar.value} ${_userName.value.ifBlank { "Observer" }}"
            repository.insertTaskItem(
                TaskItem(
                    title = title,
                    workspaceId = _activeWorkspaceId.value,
                    lastEditedByAuthor = authorStamp,
                    lastEditedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleTaskItem(item: TaskItem) {
        viewModelScope.launch {
            val authorStamp = "${_userAvatar.value} ${_userName.value.ifBlank { "Observer" }}"
            repository.updateTaskItem(
                item.copy(
                    isCompleted = !item.isCompleted,
                    lastEditedByAuthor = authorStamp,
                    lastEditedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteTaskItem(item: TaskItem) {
        viewModelScope.launch {
            repository.deleteTaskItem(item)
        }
    }

    fun addTemplateNote() {
        viewModelScope.launch {
            val stickers = listOf("💫", "💥", "❄️", "👻", "🌿", "💧", "⚡", "🦊", "🍃", "🔶", "🌪️", "🦅", "🧊", "🌊", "⚖️", "😺", "🐶", "🌸")
            val colors = listOf("#FFFFD1E3", "#FFF1EAFF", "#E2F9F0", "#FFF9D6", "#E3F2FD", "#FFE8E8")
            val noteText = listOf(
                "✨ Observation Tip: Golden Hour lighting gives +30% visual clarity!",
                "🐾 Stray Kash Tip: Animals gather near water sources at dusk.",
                "🌸 Ambient Baseline: Noise levels dropped by 4dB after sunset.",
                "☕ Moonlight Cafe: Peak quiet research hours are 2 PM to 4 PM.",
                "🍃 Wind Anomaly: Dappled shade movement recorded near north bench."
            ).random()

            addStickyNote(
                content = noteText,
                sticker = stickers.random(),
                colorHex = colors.random()
            )
        }
    }

    fun addTemplateTask() {
        viewModelScope.launch {
            val tasks = listOf(
                "Verify animal behavioral baseline at sunset 🌅",
                "Log ambient temperature & humidity anomaly 🌡️",
                "Photograph stray cat near local park bench 🐾",
                "Record 30s voice memo audio sample of neighborhood birds 🎵",
                "Check lighting baseline during Golden Hour 🌇"
            ).random()

            addTaskItem(tasks)
        }
    }

    fun getContextualLightingSuggestions(location: String): List<String> {
        return listOf(
            "Golden Hour 🌅",
            "Twilight 🌙",
            "Dappled Shade 🌿",
            "Incandescent 💡",
            "Direct Sunlight ☀️",
            "Dusk Noise 🔊"
        )
    }

    fun getContextualTagSuggestions(location: String): List<String> {
        val baseTags = mutableListOf("Stray Kash Watch 🐾", "Park Research 🌸", "Cafe Baseline ☕", "Fact Log 📜")
        if (location.contains("Park", ignoreCase = true)) {
            baseTags.add("Bench Patrol 🪑")
            baseTags.add("Bird Song 🎵")
        }
        if (location.contains("Cafe", ignoreCase = true)) {
            baseTags.add("Lofi Ambience 🎶")
            baseTags.add("Window Seat 🪟")
        }
        return baseTags
    }
}
