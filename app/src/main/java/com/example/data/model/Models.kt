package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "observations")
data class Observation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val location: String,
    val ambientBaseline: String,
    val anomalies: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFact: Boolean = true, // true for Fact, false for Inference
    val tags: String = "", // comma-separated tags
    val workspaceId: String = "local_default",
    val mediaUri: String? = null,
    val notes: String = "",
    val voiceNoteUri: String? = null,
    val voiceNoteDurationSeconds: Int = 0,
    val voiceNoteTranscript: String? = null,
    val lastEditedByAuthor: String = "😺 Neko Observer",
    val lastEditedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sticky_notes")
data class StickyNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val posX: Float = 100f,
    val posY: Float = 100f,
    val sticker: String = "😺", // Cute stickers: 😺 🐶 🐾 ✨ 🌸 🍙
    val colorHex: String = "#FFF1EAFF", // Default soft pastel color
    val workspaceId: String = "local_default",
    val lastEditedByAuthor: String = "😺 Neko Observer",
    val lastEditedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "task_items")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val workspaceId: String = "local_default",
    val lastEditedByAuthor: String = "😺 Neko Observer",
    val lastEditedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "workspaces")
data class Workspace(
    @PrimaryKey val id: String,
    val name: String,
    val icon: String = "🐾",
    val collaborators: String = "", // Comma-separated emails or handles
    val isSyncEnabled: Boolean = false,
    val hostUserId: String = "",
    val hostName: String = "Observer Host",
    val hostAvatar: String = "😺",
    val joinCode: String = id
)

enum class UserRole {
    HOST,
    EDITOR,
    VIEWER
}

data class UserProfile(
    val userId: String,
    val displayName: String,
    val avatarEmoji: String
)

data class JoinRequest(
    val requestId: String = java.util.UUID.randomUUID().toString(),
    val workspaceId: String,
    val applicantUserId: String,
    val applicantName: String,
    val applicantAvatar: String,
    val status: String = "PENDING" // "PENDING", "APPROVED_EDITOR", "APPROVED_VIEWER", "DECLINED"
)

data class ActivePresenceUser(
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val role: UserRole,
    val isOnline: Boolean = true
)
