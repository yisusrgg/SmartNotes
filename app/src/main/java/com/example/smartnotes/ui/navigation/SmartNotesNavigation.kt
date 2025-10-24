package com.example.smartnotes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartnotes.ui.screens.AddEditItemScreen
import com.example.smartnotes.ui.screens.DetailScreen
import com.example.smartnotes.ui.screens.TasksScreen
import com.example.smartnotes.ui.viewmodels.ItemViewModel


sealed class Screen(val route: String) {
    object Tasks : Screen("tasks")
    object Add : Screen("add/{type}") {
        fun createRoute(type: String) = "add/$type"
    }
    object Detail : Screen("detail/{itemId}") {
        fun createRoute(id: String) = "detail/$id"
    }
}

@Composable
fun SmartNotesNavHost(navController: NavHostController) {
    val vm: ItemViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Tasks.route) {
        composable(Screen.Tasks.route) {
            TasksScreen(
                viewModel = vm,
                onAddClick = { type -> navController.navigate(Screen.Add.createRoute(type)) },
                onDetailClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }

        composable(
            route = Screen.Add.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "task"
            AddEditItemScreen(
                viewModel = vm,
                type = type,
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() } // Aquí se pasa la acción de retroceso
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            DetailScreen(
                viewModel = vm,
                itemId = itemId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
