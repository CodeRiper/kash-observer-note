package com.example.data.database

import androidx.room.*
import com.example.data.model.Observation
import com.example.data.model.StickyNote
import com.example.data.model.TaskItem
import com.example.data.model.Workspace
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {
    @Query("SELECT * FROM observations ORDER BY timestamp DESC")
    fun getAllObservations(): Flow<List<Observation>>

    @Query("SELECT * FROM observations WHERE workspaceId = :workspaceId ORDER BY timestamp DESC")
    fun getObservationsByWorkspace(workspaceId: String): Flow<List<Observation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: Observation)

    @Delete
    suspend fun deleteObservation(observation: Observation)
}

@Dao
interface StickyNoteDao {
    @Query("SELECT * FROM sticky_notes ORDER BY id ASC")
    fun getAllStickyNotes(): Flow<List<StickyNote>>

    @Query("SELECT * FROM sticky_notes WHERE workspaceId = :workspaceId ORDER BY id ASC")
    fun getStickyNotesByWorkspace(workspaceId: String): Flow<List<StickyNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStickyNote(note: StickyNote)

    @Update
    suspend fun updateStickyNote(note: StickyNote)

    @Delete
    suspend fun deleteStickyNote(note: StickyNote)
}

@Dao
interface TaskItemDao {
    @Query("SELECT * FROM task_items ORDER BY timestamp DESC")
    fun getAllTaskItems(): Flow<List<TaskItem>>

    @Query("SELECT * FROM task_items WHERE workspaceId = :workspaceId ORDER BY timestamp DESC")
    fun getTaskItemsByWorkspace(workspaceId: String): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskItem(item: TaskItem)

    @Update
    suspend fun updateTaskItem(item: TaskItem)

    @Delete
    suspend fun deleteTaskItem(item: TaskItem)
}

@Dao
interface WorkspaceDao {
    @Query("SELECT * FROM workspaces")
    fun getAllWorkspaces(): Flow<List<Workspace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkspace(workspace: Workspace)

    @Delete
    suspend fun deleteWorkspace(workspace: Workspace)
}
