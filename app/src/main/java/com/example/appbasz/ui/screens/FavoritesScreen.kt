package com.example.appbasz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appbasz.ViewModel.FavoritesViewModel
import com.example.appbasz.ViewModel.FavoritesViewModelFactory
import com.example.appbasz.data.local.PreferencesManager
import com.example.appbasz.ui.components.ProductGridItem
import com.example.appbasz.viewmodel.AuthViewModel
import com.example.appbasz.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    onProductClick: (Int) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    // OBTENER PRODUCTOS PARA CONVERTIR IDs A PRODUCTOS COMPLETOS
    val productViewModel: ProductViewModel = viewModel()
    val allProducts by productViewModel.products.collectAsState()

    // OBTENER EL CONTEXTO PARA PREFERENCES MANAGER
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }

    // USAR EL NUEVO FAVORITES VIEWMODEL
    val favoritesViewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModelFactory(preferencesManager)
    )

    val favorites by favoritesViewModel.favorites.collectAsState()
    val favoriteIds by favoritesViewModel.favoriteIds.collectAsState()
    val isLoading by favoritesViewModel.isLoading.collectAsState()

    // CARGAR FAVORITOS CUANDO CAMBIEN LOS PRODUCTOS O LOS IDs
    LaunchedEffect(key1 = allProducts, key2 = favoriteIds) {
        if (allProducts.isNotEmpty()) {
            favoritesViewModel.loadFavoritesWithProducts(allProducts)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Favoritos") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (!isAuthenticated || currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favoritos",
                        modifier = Modifier.padding(16.dp)
                    )
                    Text("Inicia sesión para ver tus favoritos")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (favorites.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Sin favoritos",
                                modifier = Modifier.padding(16.dp)
                            )
                            Text("No tienes productos favoritos")
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(favorites, key = { it.id }) { product ->
                            Card(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                ProductGridItem(
                                    product = product,
                                    onProductClick = { onProductClick(product.id) },
                                    onAddToCart = { /* Puedes añadir funcionalidad aquí */ },
                                    onToggleFavorite = {
                                        // USAR EL NUEVO TOGGLE SIMPLIFICADO
                                        favoritesViewModel.toggleFavorite(product)
                                    },
                                    isFavorite = true // Siempre true porque estamos en favoritos
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}