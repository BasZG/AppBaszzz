package com.example.appbasz.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val total: Double = 0.0,
    val items: List<OrderItem> = emptyList()
) : Parcelable

@Parcelize
data class OrderItem(
    val productId: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String = ""
) : Parcelable