package com.example.data.repository

import com.example.data.database.ObservationDao
import com.example.data.database.StickyNoteDao
import com.example.data.database.TaskItemDao
import com.example.data.database.WorkspaceDao
import com.example.data.model.Observation
import com.example.data.model.StickyNote
import com.example.data.model.TaskItem
import com.example.data.model.Workspace
import kotlinx.coroutines.flow.Flow

class ObserverRepository(
    private val observationDao: ObservationDao,
    private val stickyNoteDao: StickyNoteDao,
    private val taskItemDao: TaskItemDao,
    private val workspaceDao: WorkspaceDao
) {
    val allObservations: Flow<List<Observation>> = observationDao.getAllObservations()

    fun getObservationsByWorkspace(workspaceId: String): Flow<List<Observation>> =
        observationDao.getObservationsByWorkspace(workspaceId)

    suspend fun insertObservation(observation: Observation) =
        observationDao.insertObservation(observation)

    suspend fun deleteObservation(observation: Observation) =
        observationDao.deleteObservation(observation)

    val allStickyNotes: Flow<List<StickyNote>> = stickyNoteDao.getAllStickyNotes()

    fun getStickyNotesByWorkspace(workspaceId: String): Flow<List<StickyNote>> =
        stickyNoteDao.getStickyNotesByWorkspace(workspaceId)

    suspend fun insertStickyNote(note: StickyNote) =
        stickyNoteDao.insertStickyNote(note)

    suspend fun updateStickyNote(note: StickyNote) =
        stickyNoteDao.updateStickyNote(note)

    suspend fun deleteStickyNote(note: StickyNote) =
        stickyNoteDao.deleteStickyNote(note)

    val allTaskItems: Flow<List<TaskItem>> = taskItemDao.getAllTaskItems()

    fun getTaskItemsByWorkspace(workspaceId: String): Flow<List<TaskItem>> =
        taskItemDao.getTaskItemsByWorkspace(workspaceId)

    suspend fun insertTaskItem(item: TaskItem) =
        taskItemDao.insertTaskItem(item)

    suspend fun updateTaskItem(item: TaskItem) =
        taskItemDao.updateTaskItem(item)

    suspend fun deleteTaskItem(item: TaskItem) =
        taskItemDao.deleteTaskItem(item)

    val allWorkspaces: Flow<List<Workspace>> = workspaceDao.getAllWorkspaces()

    suspend fun insertWorkspace(workspace: Workspace) =
        workspaceDao.insertWorkspace(workspace)

    suspend fun deleteWorkspace(workspace: Workspace) =
        workspaceDao.deleteWorkspace(workspace)
}
