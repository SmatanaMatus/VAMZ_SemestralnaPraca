package com.example.foodsync

import com.example.foodsync.ui.prihlasovanie.LoginScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
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

    val loginScreen = stringResource(id = R.string.screen_login)
    val registrationScreen = stringResource(id = R.string.screen_registration)
    val homeScreen = stringResource(id = R.string.screen_home)
    val favoritesScreen = stringResource(id = R.string.screen_favorites)
    val fridgeScreen = stringResource(id = R.string.screen_fridge)
    val recipesScreen = stringResource(id = R.string.screen_recipes)
    val recipeScreen = stringResource(id = R.string.screen_recipe)

    NavHost(navController = navController, startDestination = loginScreen) {
        composable(loginScreen) {
            LoginScreen(navController = navController)
        }
        composable(registrationScreen) {
            RegisterScreen(navController = navController)
        }
        composable(homeScreen) {
            HomeScreen(navController = navController)
        }
        composable(favoritesScreen) {
            FavoritesScreen(navController = navController)
        }
        composable(fridgeScreen) {
            FridgeScreen(navController = navController)
        }
        composable(recipesScreen) {
            MyRecipesScreen(navController = navController)
        }
        composable(
            route = recipeScreen,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            RecipeScreen(recipeId = recipeId, navController = navController)
        }
    }
}