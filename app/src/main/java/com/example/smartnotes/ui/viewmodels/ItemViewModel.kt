package com.example.smartnotes.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.smartnotes.data.Item
import com.example.smartnotes.data.ItemRepository

open class ItemViewModel : ViewModel() {
    open val items: StateFlow<List<Item>> = ItemRepository.items
        .stateIn(viewModelScope, SharingStarted.Companion.Lazily, emptyList())

    fun addItem(item: Item) {
        viewModelScope.launch {
            ItemRepository.addItem(item)
        }
    }

    open fun updateItem(item: Item) {
        viewModelScope.launch {
            ItemRepository.updateItem(item)
        }
    }

    fun removeItem(id: String) {
        viewModelScope.launch {
            ItemRepository.removeItem(id)
        }
    }

    open fun getItemById(id: String): Item? {
        TODO("Not yet implemented")
    }
}