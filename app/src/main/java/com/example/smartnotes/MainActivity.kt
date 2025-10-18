package com.example.smartnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartnotes.ui.screens.AddEditItemScreen
import com.example.smartnotes.ui.screens.DetailScreen
import com.example.smartnotes.ui.screens.TasksScreen
import com.example.smartnotes.ui.viewmodels.ItemViewModel
import com.example.smartnotes.ui.theme.SmartNotesTheme as MisTareasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MisTareasTheme {
                App()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun App() {
    val navController = rememberNavController()
    val vm: ItemViewModel = viewModel()

    NavHost(navController = navController, startDestination = "tasks") {
        composable("tasks") {
            TasksScreen(
                viewModel = vm,
                onAddClick = { type -> navController.navigate("add/$type") },
                onDetailClick = { id -> navController.navigate("detail/$id") }
            )
        }
        composable(
            "add/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "task"
            AddEditItemScreen(
                viewModel = vm,
                type = type,
                onDone = { navController.popBackStack() }
            )
        }
        composable("detail/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            DetailScreen(
                viewModel = vm,
                itemId = itemId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

