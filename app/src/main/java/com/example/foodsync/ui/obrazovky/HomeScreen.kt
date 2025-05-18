package com.example.foodsync.ui.obrazovky

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodsync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class Recipe(
    val id: String,
    val name: String,
    val rating: String,
    val ingredients: List<String> = emptyList()
)


enum class Filters {
    HODNOTENIE_OD_HORA,
    HODNOTENIE_OD_DOLA,
    NAZOV_OD_A,
    NAZOV_OD_Z,
    PODLA_CHLADNICKY
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavController) {
    var filtre by remember { mutableStateOf(Filters.HODNOTENIE_OD_HORA) }
    var filtering by remember { mutableStateOf("") }
    var expandedFilterMenu by remember { mutableStateOf(false) }

    filtering = when (filtre) {
        Filters.HODNOTENIE_OD_HORA -> "Hodnotenie zostupne(↓)"
        Filters.HODNOTENIE_OD_DOLA -> "Hodnotenie vzostupne(↑)"
        Filters.NAZOV_OD_A -> "Názov (A→Z)"
        Filters.NAZOV_OD_Z -> "Názov (Z→A)"
        Filters.PODLA_CHLADNICKY -> "Podľa chladníčky"
    }

    val oblubene = painterResource(R.drawable.oblubene)
    val domov = painterResource(R.drawable.domov)
    val chladnicka = painterResource(R.drawable.chladnicka)
    val recepty = painterResource(R.drawable.recepty)
    val filtrovanie = painterResource(R.drawable.filtrovanie)

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    val recipes = remember { mutableStateListOf<Recipe>() }
    val myRecipes = remember { mutableStateListOf<MyRecipe>() }

    val favorites = remember { mutableStateListOf<String>() }

    val fridgeIngredients = remember { mutableStateListOf<String>() }

    val combinedRecipes by derivedStateOf {
        recipes + myRecipes.map { Recipe(it.id, it.name, it.rating, it.ingredients) }
    }

    fun filterRecipesByFridge(allRecipes: List<Recipe>, fridge: List<String>): List<Recipe> {
        val normalizedFridge = fridge.map { it.trim().lowercase() }
        return allRecipes.filter { recipe ->
            recipe.ingredients.all { ingredient ->
                normalizedFridge.contains(ingredient.trim().lowercase())
            }
        }
    }

    val sortedRecipes by derivedStateOf {
        when (filtre) {
            Filters.HODNOTENIE_OD_HORA -> combinedRecipes.sortedByDescending { it.rating.length }
            Filters.HODNOTENIE_OD_DOLA -> combinedRecipes.sortedBy { it.rating.length }
            Filters.NAZOV_OD_A -> combinedRecipes.sortedBy { it.name }
            Filters.NAZOV_OD_Z -> combinedRecipes.sortedByDescending { it.name }
            Filters.PODLA_CHLADNICKY -> filterRecipesByFridge(combinedRecipes, fridgeIngredients)
        }
    }

    LaunchedEffect(Unit) {
        firestore.collection("recipes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeScreen", "Chyba načítania globálnych receptov", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    recipes.clear()
                    for (document in snapshot.documents) {
                        val id = document.id
                        val name = document.getString("name") ?: "Neznámy recept"
                        val rating = document.getString("rating") ?: "*****"
                        val ingredients = document.get("ingredients") as? List<String> ?: emptyList()
                        recipes.add(Recipe(id, name, rating, ingredients))
                    }
                }
            }
    }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("HomeScreen", "Chyba načítania používateľských receptov", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val recipesData = snapshot.get("recipes") as? List<Map<String, Any>> ?: emptyList()
                        val recipesList = recipesData.mapNotNull { data ->
                            val id = data["id"] as? String ?: return@mapNotNull null
                            val name = data["name"] as? String ?: ""
                            val ingredients = data["ingredients"] as? List<String> ?: emptyList()
                            val instructions = data["instructions"] as? String ?: ""
                            val rating = data["rating"] as? String ?: ""
                            MyRecipe(id, name, ingredients, instructions, rating)
                        }
                        myRecipes.clear()
                        myRecipes.addAll(recipesList)
                    } else {
                        firestore.collection("users").document(userId)
                            .set(mapOf("recipes" to emptyList<Map<String, Any>>()), SetOptions.merge())
                    }
                }
        }
    }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("HomeScreen", "Chyba načítania ingrediencií z chladničky", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val fridgeList = snapshot.get("fridge") as? List<String> ?: emptyList()
                        fridgeIngredients.clear()
                        fridgeIngredients.addAll(fridgeList)
                    }
                }
        }
    }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("HomeScreen", "Chyba načítania favorites", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val favList = snapshot.get("favorites") as? List<String> ?: emptyList()
                        favorites.clear()
                        favorites.addAll(favList)
                    } else {
                        firestore.collection("users").document(userId)
                            .set(mapOf("favorites" to emptyList<String>()), SetOptions.merge())
                    }
                }
        }
    }

    Surface(
        modifier = modifier
            .systemBarsPadding()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(color = Color.DarkGray)) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(color = Color.White)
            ) {
                Text(
                    text = filtering,
                    textAlign = TextAlign.Center,
                    fontSize = 25.sp,
                    modifier = modifier
                        .fillMaxWidth()
                        .offset(x = (-10).dp, y = 3.dp)
                )
            }
            LazyColumn(
                modifier = Modifier
                    .padding(
                        top = 50.dp,
                        start = 40.dp,
                        end = 16.dp)
                    .height(720.dp)
            ) {
                items(sortedRecipes) { recipe ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(
                                width = 1.dp,
                                color = Color.White)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { navController.navigate("recipe/${recipe.id}") }
                        ) {
                            Text(
                                text = recipe.name,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Hodnotenie: ${recipe.rating}",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                        Button(
                            onClick = {
                                if (recipe.id in favorites) {
                                    firestore.collection("users").document(userId)
                                        .update("favorites", FieldValue.arrayRemove(recipe.id))
                                } else {
                                    firestore.collection("users").document(userId)
                                        .update("favorites", FieldValue.arrayUnion(recipe.id))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text(
                                text = if (recipe.id in favorites) "♥" else "○",
                                fontSize = 24.sp,
                                color = if (recipe.id in favorites) Color.Red else Color.Gray
                            )
                        }
                    }
                }
            }
            Box(
                modifier = modifier
                    .align(Alignment.TopStart)
                    .width(20.dp)
                    .fillMaxHeight()
                    .background(color = Color.White)
            )
            Box(
                modifier = modifier
                    .background(color = Color.White)
                    .align(Alignment.TopEnd)
                    .height(100.dp)
                    .width(70.dp)
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    onClick = { expandedFilterMenu = true },
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Black
                    )
                ) {
                    Image(
                        painter = filtrovanie,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                DropdownMenu(
                    expanded = expandedFilterMenu,
                    onDismissRequest = { expandedFilterMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("Hodnotenie zostupne(↓)",
                            fontSize = 14.sp) },
                        onClick = {
                            filtre = Filters.HODNOTENIE_OD_HORA
                            expandedFilterMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("Hodnotenie vzostupne(↑)",
                            fontSize = 14.sp) },
                        onClick = {
                            filtre = Filters.HODNOTENIE_OD_DOLA
                            expandedFilterMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("Názov (A→Z)",
                            fontSize = 14.sp) },
                        onClick = {
                            filtre = Filters.NAZOV_OD_A
                            expandedFilterMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("Názov (Z→A)",
                            fontSize = 14.sp) },
                        onClick = {
                            filtre = Filters.NAZOV_OD_Z
                            expandedFilterMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("Podľa chladníčky",
                            fontSize = 14.sp) },
                        onClick = {
                            filtre = Filters.PODLA_CHLADNICKY
                            expandedFilterMenu = false
                        }
                    )
                }
            }
            Box(
                modifier = modifier
                    .fillMaxSize()) {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .background(color = Color.White)
                        .align(Alignment.BottomCenter)
                        .height(100.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)) {
                        Button(
                            modifier = Modifier
                                .size(70.dp)
                                .border(
                                    width = 2.dp,
                                    color = Color.Black),
                            onClick = { navController.navigate("favorites") },
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Black
                            )
                        ) {
                            Image(
                                painter = oblubene,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .width(20.dp))
                        Button(
                            modifier = Modifier
                                .size(70.dp)
                                .blur(35.dp)
                                .border(
                                    width = 2.dp,
                                    color = Color.Black),
                            onClick = { navController.navigate("home") },
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray,
                                contentColor = Color.Black
                            )
                        ) {
                            Image(
                                painter = domov,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .width(20.dp))
                        Button(
                            modifier = Modifier
                                .size(70.dp)
                                .border(
                                    width = 2.dp,
                                    color = Color.Black),
                            onClick = { navController.navigate("fridge") },
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Black
                            )
                        ) {
                            Image(
                                painter = chladnicka,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .width(20.dp))
                        Button(
                            modifier = Modifier
                                .size(70.dp)
                                .border(
                                    width = 2.dp,
                                    color = Color.Black),
                            onClick = { navController.navigate("recipes") },
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Black
                            )
                        ) {
                            Image(
                                painter = recepty,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}