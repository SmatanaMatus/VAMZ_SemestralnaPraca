package com.example.foodsync

import com.example.foodsync.ui.prihlasovanie.LoginScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.foodsync.ui.obrazovky.FavoritesScreen
import com.example.foodsync.ui.obrazovky.FridgeScreen
import com.example.foodsync.ui.obrazovky.HomeScreen
import com.example.foodsync.ui.obrazovky.MyRecipesScreen
import com.example.foodsync.ui.prihlasovanie.RegisterScreen
import com.example.foodsync.ui.obrazovky.RecipeScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("registration") {
            RegisterScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("favorites") {
            FavoritesScreen(navController = navController)
        }
        composable("fridge") {
            FridgeScreen(navController = navController)
        }
        composable("recipes") {
            MyRecipesScreen(navController = navController)
        }
        composable(
            route = "recipe/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            RecipeScreen(recipeId = recipeId, navController = navController)
        }
    }
}