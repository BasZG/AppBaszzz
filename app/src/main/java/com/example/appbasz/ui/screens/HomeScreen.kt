package com.example.appbasz.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appbasz.ui.components.BottomNavigationBar
import com.example.appbasz.ui.components.ProductGrid
import com.example.appbasz.viewmodel.AuthViewModel
import com.example.appbasz.viewmodel.CartViewModel
import com.example.appbasz.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (Int) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val viewModel: ProductViewModel = viewModel()
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    val cartViewModel: CartViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ruñau´s Store") })
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen = "home",
                isUserLoggedIn = isAuthenticated,
                onHomeClick = {  },
                onCartClick = onNavigateToCart,
                onProfileClick = onNavigateToProfile,
                onLoginClick = onNavigateToLogin
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (currentUser != null) {
                Text(
                    text = "Bienvenido, ${currentUser?.displayName ?: currentUser?.email}",
                    modifier = Modifier.padding(16.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }

            ProductGrid(
                products = products,
                isLoading = isLoading,
                onProductClick = onProductClick,
                onAddToCart = { product ->
                    if (currentUser != null) {
                        cartViewModel.addToCart(product, currentUser!!.uid)
                        println("Añadido al carrito: ${product.name}")
                    } else {
                        println("Usuario no logueado - navegando a login")
                        onNavigateToLogin()
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}