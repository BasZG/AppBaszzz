package com.example.appbasz.data.repository

import com.example.appbasz.data.model.CartItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CartRepository {
    private val database = FirebaseDatabase.getInstance()
    private val cartRef = database.getReference("cart")

    fun getCartItems(userId: String): Flow<List<CartItem>> = callbackFlow {
        val userCartRef = cartRef.child(userId)
        println("Buscando carrito para usuario: $userId")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                println("Datos recibidos del carrito: ${snapshot.exists()}")
                val cartItems = mutableListOf<CartItem>()

                for (itemSnapshot in snapshot.children) {
                    val cartItem = itemSnapshot.getValue(CartItem::class.java)
                    println("Item encontrado: ${cartItem?.name}")
                    cartItem?.let {
                        cartItems.add(it.copy(id = itemSnapshot.key ?: ""))
                    }
                }

                println("Total items en carrito: ${cartItems.size}")
                trySend(cartItems)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error cargando carrito: ${error.message}")
                close(error.toException())
            }
        }

        userCartRef.addValueEventListener(listener)

        awaitClose {
            userCartRef.removeEventListener(listener)
        }
    }

    suspend fun addToCart(cartItem: CartItem): Result<Boolean> {
        return try {
            val userCartRef = cartRef.child(cartItem.userId)
            val newItemRef = userCartRef.push()
            newItemRef.setValue(cartItem.copy(id = newItemRef.key ?: "")).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCartItemQuantity(itemId: String, userId: String, newQuantity: Int): Result<Boolean> {
        return try {
            if (newQuantity <= 0) {
                removeFromCart(itemId, userId)
            } else {
                val itemRef = cartRef.child(userId).child(itemId)
                itemRef.child("quantity").setValue(newQuantity).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(itemId: String, userId: String): Result<Boolean> {
        return try {
            val itemRef = cartRef.child(userId).child(itemId)
            itemRef.removeValue().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearCart(userId: String): Result<Boolean> {
        return try {
            val userCartRef = cartRef.child(userId)
            userCartRef.removeValue().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}