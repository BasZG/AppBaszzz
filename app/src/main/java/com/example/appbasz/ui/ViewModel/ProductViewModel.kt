package com.example.appbasz.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbasz.data.model.ProductModel
import com.example.appbasz.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()

    private val _products = MutableStateFlow<List<ProductModel>>(emptyList())
    val products: StateFlow<List<ProductModel>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        println("ViewModel INICIADO")
        Log.d("DEBUG_APP", "ProductViewModel iniciado")
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            println("Cargando productos...")
            Log.d("DEBUG_APP", "Iniciando carga de productos")

            try {
                repository.getProducts().collect { productsList ->
                    println("Productos recibidos: ${productsList.size}")
                    Log.d("DEBUG_APP", "Productos recibidos: ${productsList.size}")
                    _products.value = productsList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                println("ERROR en ViewModel: ${e.message}")
                Log.e("DEBUG_APP", "ERROR: ${e.message}", e)
                _isLoading.value = false
            }
        }
    }
}