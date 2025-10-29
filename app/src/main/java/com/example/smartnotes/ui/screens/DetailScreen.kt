package com.example.smartnotes.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.smartnotes.R
import com.example.smartnotes.ui.viewmodels.ItemsListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: ItemsListViewModel,
    itemId: String,
    onBack: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val items by viewModel.itemsUiState.collectAsState()
    val item = items.find { it.id == itemId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.title ?: "") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button_description))
                    }
                },
                actions = {
                    //BOTÓN DE EDITAR
                    IconButton(onClick = { onEditClick(itemId) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_button_description))
                    }
                }
            )
        }
    ) { padding ->
        if (item != null) {
            ItemDetailView(
                item = item,
                viewModel = viewModel,
                onTaskCompleted = onBack, // Navigate back after completing a task
                modifier = Modifier.padding(padding)
            )
        }
    }
}
