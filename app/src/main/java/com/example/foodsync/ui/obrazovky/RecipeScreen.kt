package com.example.foodsync.ui.obrazovky

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RecipeScreen(recipeId: String, modifier: Modifier = Modifier, navController: NavController) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var recipeName by remember { mutableStateOf("Načítavam...") }
    var rating by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf<List<String>>(emptyList()) }
    var instructions by remember { mutableStateOf("Načítavam pokyny...") }

    LaunchedEffect(recipeId) {
        firestore.collection("recipes").document(recipeId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    recipeName = document.getString("name") ?: "Neznámy recept"
                    rating = document.getString("rating") ?: ""
                    ingredients = document.get("ingredients") as? List<String> ?: emptyList()
                    instructions = document.getString("instructions") ?: "Žiadne pokyny"
                } else {
                    if (userId.isNotEmpty()) {
                        firestore.collection("users").document(userId).get()
                            .addOnSuccessListener { userDoc ->
                                if (userDoc.exists()) {
                                    val recipesData = userDoc.get("recipes") as? List<Map<String, Any>> ?: emptyList()
                                    val recipeData = recipesData.find { it["id"] == recipeId }
                                    if (recipeData != null) {
                                        recipeName = recipeData["name"] as? String ?: "Neznámy recept"
                                        rating = recipeData["rating"] as? String ?: ""
                                        ingredients = recipeData["ingredients"] as? List<String> ?: emptyList()
                                        instructions = recipeData["instructions"] as? String ?: "Žiadne pokyny"
                                    }
                                }
                            }
                    }
                }
            }
    }

    Surface(modifier = modifier
        .systemBarsPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.White)
            ) {
                Text(
                    text = recipeName,
                    textAlign = TextAlign.Center,
                    fontSize = 25.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier
                .height(10.dp))

            Text(text = "Hodnotenie: $rating",
                fontSize = 20.sp,
                color = Color.White)

            Spacer(modifier = Modifier
                .height(16.dp))

            Text(text = "Ingrediencie:",
                fontSize = 20.sp,
                color = Color.White)

            ingredients.forEach { ingredient ->
                Text(
                    text = "- $ingredient",
                    fontSize = 18.sp,
                    color = Color.White)
            }

            Spacer(modifier = Modifier
                .height(16.dp))

            Text(
                text = "Postup:",
                fontSize = 20.sp,
                color = Color.White)

            Text(
                text = instructions,
                fontSize = 18.sp,
                color = Color.White)

            Spacer(modifier = Modifier
                .height(20.dp))

            Button(onClick = { navController.popBackStack() }) {
                Text("Späť")
            }
        }
    }
}
