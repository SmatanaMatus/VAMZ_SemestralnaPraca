package com.example.foodsync.ui.obrazovky

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import java.util.UUID

data class MyRecipe(
    val id: String,
    val name: String,
    val ingredients: List<String>,
    val instructions: String,
    val rating: String
)

@Composable
fun MyRecipesScreen(modifier: Modifier = Modifier, navController: NavController) {

    val oblubene = painterResource(R.drawable.oblubene)
    val domov = painterResource(R.drawable.domov)
    val chladnicka = painterResource(R.drawable.chladnicka)
    val recepty = painterResource(R.drawable.recepty)

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    val myRecipes = remember { mutableStateListOf<MyRecipe>() }
    var showDialog by remember { mutableStateOf(false) }
    var recipeName by remember { mutableStateOf("") }
    var recipeIngredients by remember { mutableStateOf("") }
    var recipeInstructions by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            firestore.collection("users").document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MyRecipesScreen", "Error loading recipes", error)
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

    Surface(
        modifier = modifier
            .systemBarsPadding()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color.White)
            ) {
                Text(
                    text = "Moje recepty",
                    textAlign = TextAlign.Center,
                    fontSize = 25.sp,
                    modifier = Modifier
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
                items(myRecipes) { recipe ->
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
                        Text(
                            text = recipe.name.ifEmpty { "Nepomenovaný recept" },
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { navController.navigate("recipe/${recipe.id}") }
                        )
                        Button(
                            onClick = {
                                val recipeMap = mapOf(
                                    "id" to recipe.id,
                                    "name" to recipe.name,
                                    "ingredients" to recipe.ingredients,
                                    "instructions" to recipe.instructions,
                                    "rating" to recipe.rating
                                )
                                firestore.collection("users").document(userId)
                                    .update("recipes", FieldValue.arrayRemove(recipeMap))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White)
                        ) {
                            Text(
                                text = "Odstrániť",
                                fontSize = 14.sp)
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(20.dp)
                    .fillMaxHeight()
                    .background(Color.White)
            )
            Box(
                modifier = modifier
                    .fillMaxSize()) {
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .background(color = Color.White)
                        .align(Alignment.BottomCenter)
                        .height(100.dp)) {
                    Row(modifier = Modifier
                        .align(Alignment.Center)) {
                        Button(
                            modifier = Modifier
                                .size(70.dp)
                                .border(width = 2.dp, color = Color.Black),
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
                                .blur(35.dp)
                                .border(
                                    width = 2.dp,
                                    color = Color.Black),
                            onClick = { navController.navigate("recipes") },
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray,
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
            Button(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-10).dp, y = (-115).dp)
                    .border(
                        width = 2.dp,
                        color = Color.Black),
                onClick = { showDialog = true },
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "+",
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center)
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(text = "Pridať nový recept") },
                    text = {
                        Column {
                            TextField(
                                value = recipeName,
                                onValueChange = { recipeName = it },
                                label = { Text("Názov receptu") },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            Spacer(modifier = Modifier
                                .height(8.dp))
                            TextField(
                                value = recipeIngredients,
                                onValueChange = { recipeIngredients = it },
                                label = { Text("Ingrediencie (oddelené čiarkou)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            Spacer(modifier = Modifier
                                .height(8.dp))
                            TextField(
                                value = recipeInstructions,
                                onValueChange = { recipeInstructions = it },
                                label = { Text("Postup") },
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val ingredientsList = recipeIngredients.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                val newId = UUID.randomUUID().toString()
                                val newRecipe = mapOf(
                                    "id" to newId,
                                    "name" to recipeName,
                                    "ingredients" to ingredientsList,
                                    "instructions" to recipeInstructions,
                                    "rating" to ""
                                )
                                firestore.collection("users").document(userId)
                                    .update("recipes", FieldValue.arrayUnion(newRecipe))
                                recipeName = ""
                                recipeIngredients = ""
                                recipeInstructions = ""
                                showDialog = false
                            }
                        ) {
                            Text("Pridať")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("Zrušiť")
                        }
                    }
                )
            }
        }
    }
}