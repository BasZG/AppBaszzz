package com.example.appbasz.data.repository

import com.example.appbasz.data.model.ProductModel
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun getFavorites(userId: String): Flow<List<ProductModel>>
    suspend fun addToFavorites(userId: String, product: ProductModel)
    suspend fun removeFromFavorites(userId: String, productId: Int)
    suspend fun isProductInFavorites(userId: String, productId: Int): Boolean
    suspend fun clearFavorites(userId: String)
}