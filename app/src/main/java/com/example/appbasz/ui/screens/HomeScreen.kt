package com.example.appbasz.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appbasz.ViewModel.FavoritesViewModel
import com.example.appbasz.ViewModel.FavoritesViewModelFactory
import com.example.appbasz.ViewModel.SearchViewModel
import com.example.appbasz.ViewModel.SearchViewModelFactory
import com.example.appbasz.data.local.PreferencesManager
import com.example.appbasz.ui.components.BottomNavigationBar
import com.example.appbasz.ui.components.ProductGrid
import com.example.appbasz.ui.components.SearchAppBar
import com.example.appbasz.ui.components.SearchSuggestions
import com.example.appbasz.viewmodel.AuthViewModel
import com.example.appbasz.viewmodel.CartViewModel
import com.example.appbasz.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (Int) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val viewModel: ProductViewModel = viewModel()
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    val cartViewModel: CartViewModel = viewModel()

    // OBTENER EL CONTEXTO PARA PREFERENCES MANAGER
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }

    // VIEWMODEL DE FAVORITOS
    val favoritesViewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModelFactory(preferencesManager)
    )

    // NUEVO: VIEWMODEL DE BÚSQUEDA
    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(preferencesManager)
    )

    // Estados de búsqueda
    val searchQuery by searchViewModel.searchQuery.collectAsState()
    val searchHistory by searchViewModel.searchHistory.collectAsState()
    val isSearchActive by searchViewModel.isSearchActive.collectAsState()

    // Estado para los IDs de favoritos
    val favoriteIds by favoritesViewModel.favoriteIds.collectAsState()

    // Filtrar productos basado en la búsqueda
    val filteredProducts = searchViewModel.filterProducts(products, searchQuery)

    // Función para verificar si un producto es favorito
    val isProductFavorite: (Int) -> Boolean = { productId ->
        favoriteIds.contains(productId.toString())
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                SearchAppBar(
                    query = searchQuery,
                    onQueryChange = { newQuery ->
                        searchViewModel.setSearchQuery(newQuery)
                    },
                    onSearch = { query ->
                        if (query.isNotBlank()) {
                            searchViewModel.addToSearchHistory(query)
                        }
                    },
                    onBackClick = {
                        searchViewModel.setSearchActive(false)
                    }
                )
            } else {
                androidx.compose.material3.TopAppBar(
                    title = { Text("Ruñau´s Store") },
                    actions = {
                        androidx.compose.material3.IconButton(
                            onClick = { searchViewModel.setSearchActive(true) }
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Search,
                                contentDescription = "Buscar"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen = "home",
                isUserLoggedIn = isAuthenticated,
                onHomeClick = { },
                onCartClick = onNavigateToCart,
                onProfileClick = onNavigateToProfile,
                onLoginClick = onNavigateToLogin,
                onSettingsClick = onNavigateToSettings
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (currentUser != null && !isSearchActive) {
                Text(
                    text = "Bienvenido, ${currentUser?.displayName ?: currentUser?.email}",
                    modifier = Modifier.padding(16.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }

            if (isSearchActive) {
                // Mostrar sugerencias de búsqueda cuando está activa
                if (searchQuery.isEmpty()) {
                    SearchSuggestions(
                        searchHistory = searchHistory,
                        onSearch = { query ->
                            searchViewModel.setSearchQuery(query)
                            searchViewModel.addToSearchHistory(query)
                        },
                        onClearHistory = {
                            searchViewModel.clearSearchHistory()
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Mostrar productos filtrados cuando hay query
                    ProductGrid(
                        products = filteredProducts,
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
                        onToggleFavorite = { product ->
                            favoritesViewModel.toggleFavorite(product)
                        },
                        isProductFavorite = isProductFavorite,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // Mostrar todos los productos cuando no hay búsqueda activa
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
                    onToggleFavorite = { product ->
                        favoritesViewModel.toggleFavorite(product)
                    },
                    isProductFavorite = isProductFavorite,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}