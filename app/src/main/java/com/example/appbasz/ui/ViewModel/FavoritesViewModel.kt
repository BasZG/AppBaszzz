package com.example.appbasz.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbasz.data.local.PreferencesManager
import com.example.appbasz.data.model.ProductModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<ProductModel>>(emptyList())
    val favorites: StateFlow<List<ProductModel>> = _favorites.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadFavoriteIds()
    }

    private fun loadFavoriteIds() {
        viewModelScope.launch {
            preferencesManager.getFavorites().collect { favoriteIdsSet ->
                _favoriteIds.value = favoriteIdsSet
            }
        }
    }

    fun addToFavorites(product: ProductModel) {
        viewModelScope.launch {
            preferencesManager.addToFavorites(product.id.toString())
        }
    }

    fun removeFromFavorites(product: ProductModel) {
        viewModelScope.launch {
            preferencesManager.removeFromFavorites(product.id.toString())
        }
    }

    fun toggleFavorite(product: ProductModel) {
        viewModelScope.launch {
            val currentIds = _favoriteIds.value
            if (currentIds.contains(product.id.toString())) {
                removeFromFavorites(product)
            } else {
                addToFavorites(product)
            }
        }
    }

    // Esta función es crucial para mostrar los favoritos en FavoritesScreen
    fun loadFavoritesWithProducts(allProducts: List<ProductModel>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val favoriteIdsSet = _favoriteIds.value
                val favoriteProducts = allProducts.filter { product ->
                    favoriteIdsSet.contains(product.id.toString())
                }
                _favorites.value = favoriteProducts
            } catch (e: Exception) {
                _favorites.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isProductFavorite(productId: Int): Boolean {
        return _favoriteIds.value.contains(productId.toString())
    }

    // Mantenemos esta función por compatibilidad, pero ahora usa PreferencesManager
    fun loadFavorites(userId: String, allProducts: List<ProductModel>? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (allProducts != null) {
                    loadFavoritesWithProducts(allProducts)
                } else {
                    // Si no tenemos productos, al menos cargamos los IDs
                    val favoriteIdsSet = _favoriteIds.value
                    _favorites.value = emptyList() // Limpiamos hasta tener productos
                }
            } catch (e: Exception) {
                _favorites.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Función de compatibilidad para el toggle antiguo
    fun toggleFavorite(userId: String, product: ProductModel, isCurrentlyFavorite: Boolean) {
        toggleFavorite(product)
    }

    fun clearFavorites(userId: String) {
        viewModelScope.launch {
            // Para limpiar favoritos, necesitaríamos una función en PreferencesManager
            // Por ahora, individualmente removemos cada uno
            val idsToRemove = _favoriteIds.value.toList()
            idsToRemove.forEach { id ->
                preferencesManager.removeFromFavorites(id)
            }
        }
    }
}