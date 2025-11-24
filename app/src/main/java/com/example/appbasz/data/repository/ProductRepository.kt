package com.example.appbasz.data.repository

import android.util.Log
import com.example.appbasz.data.model.ProductModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ProductRepository {
    private val database = FirebaseDatabase.getInstance()
    private val productsRef = database.getReference("products")

    fun getProducts(): Flow<List<ProductModel>> = callbackFlow {
        println("Repository: Conectando con Firebase...")
        Log.d("DEBUG_APP", "ProductRepository: Conectando con Firebase")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                println("Repository: Datos recibidos - existe: ${snapshot.exists()}")
                Log.d("DEBUG_APP", "Datos recibidos - existe: ${snapshot.exists()}")

                if (!snapshot.exists()) {
                    println("Repository: Snapshot NO EXISTE - la base de datos está vacía")
                    Log.w("DEBUG_APP", "Snapshot NO EXISTE")
                    trySend(emptyList())
                    return
                }

                println("Repository: Número de productos: ${snapshot.childrenCount}")
                Log.d("DEBUG_APP", "Número de productos: ${snapshot.childrenCount}")

                val products = mutableListOf<ProductModel>()

                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(ProductModel::class.java)
                    println("Producto: ${product?.name ?: "NULL"} - ID: ${productSnapshot.key}")
                    Log.d("DEBUG_APP", "Producto: ${product?.name ?: "NULL"}")
                    product?.let {
                        products.add(it)
                    }
                }

                println("Repository: Total productos procesados: ${products.size}")
                Log.d("DEBUG_APP", "Total productos: ${products.size}")
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Repository: ERROR Firebase: ${error.message}")
                Log.e("DEBUG_APP", "ERROR Firebase: ${error.message}")
                close(error.toException())
            }
        }

        productsRef.addValueEventListener(listener)

        awaitClose {
            println("Repository: Cerrando conexión")
            productsRef.removeEventListener(listener)
        }
    }
}