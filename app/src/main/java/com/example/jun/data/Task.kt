package com.example.jun.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDate: String? = null,
    val priority: Priority = Priority.NORMAL,
    val status: TaskStatus
) {
    fun isOverdue(): Boolean {
        return dueDate?.toLocalDate()?.let { it < LocalDate.now() } ?: false
    }
}

enum class Priority { LOW, NORMAL, HIGH }
enum class TaskStatus { TODO, IN_PROGRESS, DONE }

class Converters {
    @TypeConverter fun fromPriority(priority: Priority): String = priority.name
    @TypeConverter fun toPriority(priority: String): Priority = Priority.valueOf(priority)
    @TypeConverter fun fromTaskStatus(status: TaskStatus): String = status.name
    @TypeConverter fun toTaskStatus(status: String): TaskStatus = TaskStatus.valueOf(status)
}