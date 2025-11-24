package com.example.appbasz.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val id: String = "",
    val productId: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val quantity: Int = 1,
    val userId: String = "" // Para vincular al usuario
) : Parcelable {

    fun totalPrice(): Double {
        return price * quantity
    }
}