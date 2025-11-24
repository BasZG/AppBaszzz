package com.example.appbasz.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@Composable
fun BottomNavigationBar(
    currentScreen: String,
    isUserLoggedIn: Boolean,
    onHomeClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            icon = {
                Text(
                    "Home",
                    fontWeight = if (currentScreen == "home") FontWeight.Bold else FontWeight.Normal
                )
            },
            selected = currentScreen == "home",
            onClick = onHomeClick
        )
        NavigationBarItem(
            icon = {
                Text(
                    "Carrito",
                    fontWeight = if (currentScreen == "cart") FontWeight.Bold else FontWeight.Normal
                )
            },
            selected = currentScreen == "cart",
            onClick = onCartClick
        )
        if (isUserLoggedIn) {
            NavigationBarItem(
                icon = {
                    Text(
                        "Perfil",
                        fontWeight = if (currentScreen == "profile") FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = currentScreen == "profile",
                onClick = onProfileClick
            )
        } else {
            NavigationBarItem(
                icon = {
                    Text(
                        "Login",
                        fontWeight = if (currentScreen == "login") FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = currentScreen == "login",
                onClick = onLoginClick
            )
        }
    }
}