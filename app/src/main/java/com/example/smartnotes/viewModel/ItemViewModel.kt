package com.example.smartnotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartnotes.data.Item
import com.example.smartnotes.data.ItemRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class ItemViewModel : ViewModel() {
    open val items: StateFlow<List<Item>> = ItemRepository.items
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
