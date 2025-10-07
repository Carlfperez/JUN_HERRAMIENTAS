package com.example.jun.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jun.data.Task
import com.example.jun.data.TaskDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// SOLO UNA VEZ esta declaración - ELIMINA CUALQUIER OTRA TaskListUiState
data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true
)

// SOLO UNA VEZ esta declaración - ELIMINA CUALQUIER OTRA TaskViewModel
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDao = TaskDatabase.getInstance(application).taskDao()

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            taskDao.getAllTasks().collect { tasks ->
                _uiState.value = TaskListUiState(
                    tasks = tasks,
                    isLoading = false
                )
            }
        }
    }
}