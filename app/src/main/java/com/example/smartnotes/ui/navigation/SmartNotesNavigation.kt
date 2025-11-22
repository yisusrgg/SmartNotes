package com.example.smartnotes.ui.navigation

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartnotes.ui.AppViewModelProvider
import com.example.smartnotes.ui.screens.AddNoteTaskScreen
import com.example.smartnotes.ui.screens.DetailScreen
import com.example.smartnotes.ui.screens.TasksScreen
import com.example.smartnotes.ui.viewmodels.AddNoteTaskViewModel
import com.example.smartnotes.ui.viewmodels.ItemsListViewModel


sealed class Screen(val route: String) {
    object Tasks : Screen("tasks")
    object Add : Screen("add/{type}") {
        fun createRoute(type: String) = "add/$type"
    }
    object Detail : Screen("detail/{itemId}") {
        fun createRoute(id: String) = "detail/$id"
    }
    object Edit : Screen("edit/{itemId}") {
        fun createRoute(itemId: String) = "edit/$itemId"
    }
}

@Composable
fun SmartNotesNavHost(navController: NavHostController, windowSizeClass: WindowWidthSizeClass) {
    //val vm: ItemViewModel = viewModel()
    val vmIL: ItemsListViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val vmAdd: AddNoteTaskViewModel = viewModel(factory = AppViewModelProvider.Factory)
    //val vmEdit: EditNoteTaskViewModel = viewModel(factory = AppViewModelProvider.Factory)
    // val vmDetail: DetailScreenViewModel = viewModel(factory = AppViewModelProvider.Factory) // Si existe

    //Determinar el tipo de layout a usar en toda la app
    val layoutType = when (windowSizeClass) {
        WindowWidthSizeClass.Compact -> LayoutType.COMPACT
        WindowWidthSizeClass.Medium -> LayoutType.MEDIUM
        WindowWidthSizeClass.Expanded -> LayoutType.EXPANDED
        else -> LayoutType.COMPACT
    }

    NavHost(navController = navController, startDestination = Screen.Tasks.route) {
        composable(Screen.Tasks.route) {
            TasksScreen(
                viewModel = vmIL,
                onAddClick = { type -> navController.navigate(Screen.Add.createRoute(type)) },
                onDetailClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                layoutType = layoutType
            )
        }

        composable(
            route = Screen.Add.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "task"
            AddNoteTaskScreen(
                viewModel = vmAdd,
                type = type,
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                layoutType = layoutType
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            DetailScreen(
                viewModel = vmIL,
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onEditClick = {}
            )
        }

        /*composable(
            route = Screen.Edit.route,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType }) // Usar IntType
        ) {
            AddNoteTaskScreen( // Reutilizar la pantalla de formulario
                viewModel = vmEdit,
                onBack = { navController.popBackStack() }
            )
        }*/
    }
}

enum class LayoutType { COMPACT, MEDIUM, EXPANDED }