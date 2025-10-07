package com.example.jun

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jun.data.Task
import com.example.jun.data.TaskStatus
import com.example.jun.ui.theme.JUNTheme
import com.example.jun.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JUNTheme {
                val navController = rememberNavController()
                val taskViewModel: TaskViewModel = viewModel(
                    factory = TaskViewModelFactory(application)
                )

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(
                            onNavigateToTasks = { navController.navigate("tasks") },
                            onNavigateToAbout = { navController.navigate("about") }
                        )
                    }
                    composable("tasks") {
                        TaskListScreen(
                            viewModel = taskViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("about") {
                        AboutScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

class TaskViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Pantalla de Inicio
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTasks: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "JUN KANBAN",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
        )
        Text(
            text = "Bienvenido a JUN",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Tu organizador de tareas personal",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Botón para ir a Tareas
        Button(
            onClick = onNavigateToTasks,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Mis Tareas")
        }

        // Botón para ir a Acerca de
        OutlinedButton(
            onClick = onNavigateToAbout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Acerca de")
        }
    }
}

// TaskListScreen actualizado con ViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onBack: () -> Unit
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    // Observar cambios en búsqueda
    LaunchedEffect(searchText) {
        viewModel.setSearchQuery(searchText)
    }

    // Recoger datos del ViewModel
    val todoTasks by viewModel.todoTasks.collectAsState(initial = emptyList())
    val inProgressTasks by viewModel.inProgressTasks.collectAsState(initial = emptyList())
    val doneTasks by viewModel.doneTasks.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onBack) {
                Text("← Inicio")
            }

            Button(
                onClick = { showAddTaskDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
                Text("Nueva")
            }
        }

        // Barra de búsqueda
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Buscar tareas...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { searchText = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                    }
                }
            }
        )

        Text(
            text = if (searchText.isEmpty()) "Tablero Kanban" else "Resultados: \"$searchText\"",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Mostrar resultados de búsqueda o columnas normales
        if (searchText.isNotEmpty()) {
            val searchedTasks by viewModel.searchedTasks.collectAsState(initial = emptyList())
            SearchResultsSection(
                tasks = searchedTasks,
                onTaskClick = { task ->
                    when (task.status) {
                        TaskStatus.TODO -> viewModel.moveTask(task, TaskStatus.IN_PROGRESS)
                        TaskStatus.IN_PROGRESS -> viewModel.moveTask(task, TaskStatus.DONE)
                        TaskStatus.DONE -> viewModel.deleteTask(task)
                    }
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            // Columnas Kanban normales
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                KanbanColumn(
                    title = "Por Hacer",
                    tasks = todoTasks,
                    onTaskClick = { task ->
                        viewModel.moveTask(task, TaskStatus.IN_PROGRESS)
                    },
                    modifier = Modifier.weight(1f),
                    columnColor = MaterialTheme.colorScheme.surfaceVariant
                )

                KanbanColumn(
                    title = "En Progreso",
                    tasks = inProgressTasks,
                    onTaskClick = { task ->
                        viewModel.moveTask(task, TaskStatus.DONE)
                    },
                    modifier = Modifier.weight(1f),
                    columnColor = MaterialTheme.colorScheme.primaryContainer
                )

                KanbanColumn(
                    title = "Completadas",
                    tasks = doneTasks,
                    onTaskClick = { task ->
                        viewModel.deleteTask(task)
                    },
                    modifier = Modifier.weight(1f),
                    columnColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }

        // Contador
        val totalTasks = todoTasks.size + inProgressTasks.size + doneTasks.size
        Text(
            text = "Total: $totalTasks tareas",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )

        // Diálogo para agregar tarea
        if (showAddTaskDialog) {
            SimpleAddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onAddTask = { title, description ->
                    if (title.isNotBlank()) {
                        viewModel.addTask(title, description)
                    }
                }
            )
        }
    }
}

// Sección de resultados de búsqueda
@Composable
fun SearchResultsSection(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (tasks.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "No se encontraron tareas",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(tasks) { task ->
                    KanbanTaskCard(
                        task = task,
                        onTaskClick = { onTaskClick(task) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// Composable para cada columna Kanban
@Composable
fun KanbanColumn(
    title: String,
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
    columnColor: androidx.compose.ui.graphics.Color
) {
    Column(
        modifier = modifier.padding(4.dp)
    ) {
        // Header de la columna
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            colors = CardDefaults.cardColors(containerColor = columnColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "${tasks.size} tareas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Lista de tareas en la columna
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {
            items(tasks) { task ->
                KanbanTaskCard(
                    task = task,
                    onTaskClick = { onTaskClick(task) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Espacio vacío si no hay tareas
            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay tareas",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// Composable para cada tarjeta de tarea
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanTaskCard(
    task: Task,
    onTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onTaskClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// Diálogo simple para agregar tareas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleAddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Tarea") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTask(title, description)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Pantalla Acerca de
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text("← Volver")
        }

        Text(
            text = "Acerca de JUN",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "JUN App",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Aplicación móvil para organización de tareas usando metodología Kanban.",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Desarrollada con Jetpack Compose y Kotlin.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Características",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text("• Gestión visual de tareas")
                Text("• Interfaz intuitiva")
                Text("• Método Kanban")
                Text("• Desarrollada en Android Studio")
            }
        }
    }
}

// Previsualizaciones
@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    JUNTheme {
        HomeScreen(
            onNavigateToTasks = { },
            onNavigateToAbout = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTaskListScreen() {
    JUNTheme {
        // Preview con datos de ejemplo
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Tablero Kanban (Preview)")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAboutScreen() {
    JUNTheme {
        AboutScreen(onBack = { })
    }
}