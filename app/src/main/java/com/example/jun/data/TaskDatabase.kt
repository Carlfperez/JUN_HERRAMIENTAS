package com.example.jun.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Database(entities = [Task::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: android.content.Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY
            CASE priority WHEN 'HIGH' THEN 1 WHEN 'NORMAL' THEN 2 WHEN 'LOW' THEN 3 ELSE 4 END")
        fun getAllTasks(): Flow<List<Task>>

        @Query("SELECT * FROM tasks WHERE status = :status")
        fun getTasksByStatus(status: TaskStatus): Flow<List<Task>>

        @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%'")
        fun searchTasks(query: String): Flow<List<Task>>

        @Insert
        suspend fun insertTask(task: Task): Long

        @Update
        suspend fun updateTask(task: Task)

        @Delete
        suspend fun deleteTask(task: Task)
}