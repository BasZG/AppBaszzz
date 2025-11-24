package com.example.appbasz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbasz.data.model.ProductModel
import com.example.appbasz.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel : ViewModel() {
    private val repository = ProductRepository()

    private val _product = MutableStateFlow<ProductModel?>(null)
    val product: StateFlow<ProductModel?> = _product.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadProduct(productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getProducts().collect { products ->
                    val foundProduct = products.find { it.id == productId }
                    _product.value = foundProduct
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
}