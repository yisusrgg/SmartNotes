package com.example.smartnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.smartnotes.ui.theme.SmartNotesTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
//import androidx.navigation.compose.navArgument
import androidx.navigation.navArgument
import com.example.smartnotes.iu.screens.AddEditTaskScreen
import com.example.smartnotes.iu.screens.TasksScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartNotesTheme {
                App()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun App() {
    val navController = rememberNavController()
    val vm: com.example.smartnotes.viewmodel.TaskViewModel = viewModel()

    NavHost(navController = navController, startDestination = "tasks") {
        composable("tasks") {
            TasksScreen(
                viewModel = vm,
                onAddClick = { navController.navigate("add") },
                onEditClick = { taskId -> navController.navigate("edit/$taskId") }
            )
        }
        composable("add") {
            AddEditTaskScreen(
                viewModel = vm,
                onDone = { navController.popBackStack() }
            )
        }
        composable(
            "edit/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            AddEditTaskScreen(
                viewModel = vm,
                editTaskId = taskId,
                onDone = { navController.popBackStack() }
            )
        }
    }
}


