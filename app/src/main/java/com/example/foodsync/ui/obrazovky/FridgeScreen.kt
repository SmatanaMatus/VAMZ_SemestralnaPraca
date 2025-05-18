package com.example.foodsync.ui.obrazovky

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun FridgeScreen(modifier: Modifier = Modifier, navController: NavController) {

    val oblubene = painterResource(id = R.drawable.oblubene)
    val domov = painterResource(id = R.drawable.domov)
    val chladnicka = painterResource(id = R.drawable.chladnicka)
    val recepty = painterResource(id = R.drawable.recepty)

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser
    val userId = currentUser?.uid ?: ""

    var showDialog by remember { mutableStateOf(false) }
    var ingredientName by remember { mutableStateOf("") }

    val ingredients = remember { mutableStateListOf<String>() }

    LaunchedEffect(userId) {
        firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FridgeScreen", "Chyba načítania dokumentu", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val fridge = snapshot.get("fridge") as? List<String> ?: emptyList()
                    ingredients.clear()
                    ingredients.addAll(fridge)
                } else {
                    firestore.collection("users").document(userId)
                        .set(mapOf("fridge" to emptyList<String>()), SetOptions.merge())
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
                    text = "Moja chladníčka",
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
                items(ingredients) { ingredient ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "- $ingredient",
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier
                                .weight(1f)
                        )
                        Button(
                            onClick = {
                                firestore.collection("users").document(userId)
                                    .update("fridge", FieldValue.arrayRemove(ingredient))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text(
                                text = "Odstrániť",
                                fontSize = 14.sp,
                                color = Color.White)
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

            Button(
                modifier = modifier
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
                        modifier = modifier
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
                                .fillMaxSize()
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .width(20.dp))
                    Button(
                        modifier = modifier
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
                                .fillMaxSize()
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .width(20.dp))
                    Button(
                        modifier = modifier
                            .size(70.dp)
                            .blur(35.dp)
                            .border(
                                width = 2.dp,
                                color = Color.Black),
                        onClick = { navController.navigate("fridge") },
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.Black
                        )
                    ) {
                        Image(
                            painter = chladnicka,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                    Spacer(
                        modifier = Modifier
                            .width(20.dp))
                    Button(
                        modifier = modifier
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
                                .fillMaxSize()
                        )
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Pridať surovinu") },
                    text = {
                        Column {
                            Text("Zadajte názov suroviny:")
                            TextField(
                                value = ingredientName,
                                onValueChange = { ingredientName = it.trim() },
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (ingredientName.isNotBlank()) {
                                    firestore.collection("users").document(userId)
                                        .update("fridge", FieldValue.arrayUnion(ingredientName))
                                    showDialog = false
                                    ingredientName = ""
                                }
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