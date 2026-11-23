package com.collector.coinsstamps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.collector.coinsstamps.data.AppDatabase
import com.collector.coinsstamps.data.ItemEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CollectorViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).itemDao()

    val allItems: StateFlow<List<ItemEntity>> = dao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coins: StateFlow<List<ItemEntity>> = dao.getItemsByCategory("Coin")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stamps: StateFlow<List<ItemEntity>> = dao.getItemsByCategory("Stamp")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchResults: StateFlow<List<ItemEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) dao.getAllItems()
            else dao.searchItems(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insert(item: ItemEntity) {
        viewModelScope.launch {
            dao.insertItem(item)
        }
    }

    fun update(item: ItemEntity) {
        viewModelScope.launch {
            dao.updateItem(item)
        }
    }

    fun delete(item: ItemEntity) {
        viewModelScope.launch {
            dao.deleteItem(item)
        }
    }

    suspend fun getItem(id: Long): ItemEntity? {
        return dao.getItemById(id)
    }
}
