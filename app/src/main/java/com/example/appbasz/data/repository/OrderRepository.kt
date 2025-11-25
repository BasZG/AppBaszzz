package com.example.appbasz.data.repository

import com.example.appbasz.data.model.Order
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class OrderRepository {
    private val database = FirebaseDatabase.getInstance()

    fun getOrdersByUserId(userId: String): Flow<List<Order>> = callbackFlow {
        val ordersRef = database.getReference("users").child(userId).child("orders")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<Order>()

                if (snapshot.exists()) {
                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(Order::class.java)
                        order?.let { orders.add(it) }
                    }
                }

                trySend(orders)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ordersRef.addValueEventListener(listener)

        awaitClose {
            ordersRef.removeEventListener(listener)
        }
    }

    suspend fun createOrder(order: Order) {
        val orderRef = database.getReference("users")
            .child(order.userId)
            .child("orders")
            .child(order.id)

        orderRef.setValue(order)
    }
}