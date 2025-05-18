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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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

@SuppressLint("UnrememberedMutableState")
@Composable
fun FavoritesScreen(modifier: Modifier = Modifier, navController: NavController) {

    val oblubene = painterResource(R.drawable.oblubene)
    val domov = painterResource(R.drawable.domov)
    val chladnicka = painterResource(R.drawable.chladnicka)
    val recepty = painterResource(R.drawable.recepty)

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    val favoriteIds = remember { mutableStateListOf<String>() }

    val globalRecipes = remember { mutableStateListOf<Recipe>() }
    val myRecipes = remember { mutableStateListOf<MyRecipe>() }

    val combinedRecipes by derivedStateOf {
        globalRecipes + myRecipes.map { Recipe(it.id, it.name, it.rating, it.ingredients) }
    }

    val favoriteRecipes by derivedStateOf {
        combinedRecipes.filter { it.id in favoriteIds }
    }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FavoritesScreen", "Chyba načítania favorites", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val favList: List<String> = when (val favData = snapshot.get("favorites")) {
                            is List<*> -> favData.filterIsInstance<String>()
                            is String -> listOf(favData)
                            else -> emptyList()
                        }
                        favoriteIds.clear()
                        favoriteIds.addAll(favList)
                    } else {
                        firestore.collection("users").document(userId)
                            .set(mapOf("favorites" to emptyList<String>()))
                    }
                }
        }
    }

    LaunchedEffect(Unit) {
        firestore.collection("recipes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FavoritesScreen", "Chyba načítania globálnych receptov", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    globalRecipes.clear()
                    for (document in snapshot.documents) {
                        val id = document.id
                        val name = document.getString("name") ?: "Neznámy recept"
                        val rating = document.getString("rating") ?: "*****"
                        val ingredients = document.get("ingredients") as? List<String> ?: emptyList()
                        globalRecipes.add(Recipe(id, name, rating, ingredients))
                    }
                }
            }
    }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FavoritesScreen", "Chyba načítania MyRecipes", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val recipesData = snapshot.get("recipes") as? List<Map<String, Any>> ?: emptyList()
                        val recipesList = recipesData.mapNotNull { data ->
                            val id = data["id"] as? String ?: return@mapNotNull null
                            val name = data["name"] as? String ?: "Neznámy recept"
                            val ingredients = data["ingredients"] as? List<String> ?: emptyList()
                            val instructions = data["instructions"] as? String ?: ""
                            val rating = data["rating"] as? String ?: "*****"
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

    Surface(
        modifier = modifier
            .systemBarsPadding()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.DarkGray)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(color = Color.White)) {
                Text(
                    text = "Moje obľúbené",
                    fontSize = 25.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = (-10).dp, y = 3.dp),
                    textAlign = TextAlign.Center)
            }

            LazyColumn(
                modifier = Modifier
                    .padding(
                        top = 50.dp,
                        start = 40.dp,
                        end = 16.dp)
                    .height(720.dp)
            ) {
                items(favoriteRecipes) { recipe ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
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
                                color = Color.White)
                            Text(
                                text = "Hodnotenie: ${recipe.rating}",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                        Button(
                            onClick = {
                                firestore.collection("users").document(userId)
                                    .update("favorites", FieldValue.arrayRemove(recipe.id))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text(
                                text = "♥",
                                fontSize = 24.sp,
                                color = Color.Red)
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(20.dp)
                    .fillMaxHeight()
                    .background(color = Color.White)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .align(Alignment.BottomCenter)
                    .height(100.dp)
            ) {
                Row(modifier = Modifier
                        .align(Alignment.Center)) {
                    Button(
                        modifier = Modifier
                            .size(70.dp)
                            .border(2.dp, Color.Black)
                            .blur(35.dp),
                        onClick = { navController.navigate("favorites") },
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
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
                            .border(
                                width = 2.dp,
                                color = Color.Black),
                        onClick = { navController.navigate("home") },
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
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