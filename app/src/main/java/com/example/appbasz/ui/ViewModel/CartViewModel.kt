package com.example.appbasz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appbasz.data.model.CartItem
import com.example.appbasz.data.model.Order
import com.example.appbasz.data.model.OrderItem
import com.example.appbasz.data.model.ProductModel
import com.example.appbasz.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CartViewModel : ViewModel() {
    private val cartRepository = CartRepository()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _cartError = MutableStateFlow<String?>(null)
    val cartError: StateFlow<String?> = _cartError.asStateFlow()

    fun createOrderFromCart(userId: String): Order {
        val cartItems = _cartItems.value
        val total = getTotalPrice()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        return Order(
            id = System.currentTimeMillis().toString(),
            userId = userId,
            date = currentDate,
            total = total,
            items = cartItems.map { cartItem ->
                OrderItem(
                    productId = cartItem.productId,
                    name = cartItem.name,
                    price = cartItem.price,
                    quantity = cartItem.quantity,
                    imageUrl = cartItem.imageUrl
                )
            }
        )
    }
    fun loadCartItems(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                cartRepository.getCartItems(userId).collect { items ->
                    _cartItems.value = items
                    _isLoading.value = false
                    println("Carrito cargado: ${items.size} items")
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _cartError.value = "Error al cargar carrito"
                println("Error cargando carrito: ${e.message}")
            }
        }
    }

    fun addToCart(product: ProductModel, userId: String) {
        viewModelScope.launch {
            val cartItem = CartItem(
                productId = product.id,
                name = product.name,
                price = product.price,
                imageUrl = product.imageUrl,
                quantity = 1,
                userId = userId
            )

            val result = cartRepository.addToCart(cartItem)
            if (result.isFailure) {
                _cartError.value = "Error al añadir al carrito"
            }
        }
    }

    fun updateQuantity(itemId: String, userId: String, newQuantity: Int) {
        viewModelScope.launch {
            val result = cartRepository.updateCartItemQuantity(itemId, userId, newQuantity)
            if (result.isFailure) {
                _cartError.value = "Error al actualizar cantidad"
            }
        }
    }

    fun removeFromCart(itemId: String, userId: String) {
        viewModelScope.launch {
            val result = cartRepository.removeFromCart(itemId, userId)
            if (result.isFailure) {
                _cartError.value = "Error al eliminar del carrito"
            }
        }
    }

    fun clearCart(userId: String) {
        viewModelScope.launch {
            val result = cartRepository.clearCart(userId)
            if (result.isFailure) {
                _cartError.value = "Error al vaciar carrito"
            }
        }
    }

    fun getTotalPrice(): Double {
        return _cartItems.value.sumOf { it.totalPrice() }
    }

    fun getTotalItems(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }

    fun clearError() {
        _cartError.value = null
    }
}