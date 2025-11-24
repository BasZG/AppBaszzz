package com.example.appbasz.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbasz.data.local.PreferencesManager
import com.example.appbasz.data.model.ProductModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchHistory = MutableStateFlow<Set<String>>(emptySet())
    val searchHistory: StateFlow<Set<String>> = _searchHistory.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    init {
        loadSearchHistory()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) {
            _searchQuery.value = ""
        }
    }

    fun addToSearchHistory(query: String) {
        viewModelScope.launch {
            if (query.isNotBlank()) {
                preferencesManager.addToSearchHistory(query)
            }
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            preferencesManager.clearSearchHistory()
        }
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            preferencesManager.getSearchHistory().collect { history ->
                _searchHistory.value = history
            }
        }
    }

    // Función para filtrar productos
    fun filterProducts(products: List<ProductModel>, query: String): List<ProductModel> {
        return if (query.isBlank()) {
            products
        } else {
            products.filter { product ->
                product.name.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true)
            }
        }
    }
}