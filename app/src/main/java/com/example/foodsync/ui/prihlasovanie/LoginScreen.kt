package com.example.foodsync.ui.prihlasovanie

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodsync.R
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavController

@Composable
fun LoginScreen(modifier: Modifier = Modifier, navController: NavController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()

    val backgroundImage = painterResource(R.drawable.ovocie)
    val logoImage = painterResource(R.drawable.logo)

    Surface(modifier = modifier
        .fillMaxSize()
        .systemBarsPadding()) {
        Box(modifier = modifier
            .fillMaxSize()) {
            Image(
                painter = backgroundImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .fillMaxSize()
            )
            Box(
                modifier = modifier
                    .align(Alignment.TopCenter)
                    .offset(0.dp, 80.dp)
            ) {
                Image(
                    painter = logoImage,
                    contentDescription = null,
                    modifier = modifier
                        .size(225.dp)
                )
            }
            Box(
                modifier = modifier
                    .offset(0.dp, 50.dp)
                    .align(Alignment.Center)
                    .height(300.dp)
                    .width(375.dp)
                    .alpha(0.8f)
                    .background(color = Color.White,
                        shape = RoundedCornerShape(16.dp))
                    .border(2.dp, color = Color.Black,
                        shape = RoundedCornerShape(16.dp))
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .fillMaxWidth()
                    .offset(y = 55.dp)
                    .align(Alignment.Center)
            ) {
                Row {
                    Text(
                        text = "Email:",
                        fontSize = 30.sp,
                        modifier = modifier
                            .width(100.dp)
                            .offset(y = 15.dp)
                    )
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = modifier
                            .alpha(0.7f)
                            .width(250.dp)
                            .border(1.dp,
                                color = Color.Black,
                                shape = RectangleShape)
                    )
                }
                Spacer(modifier = modifier.height(30.dp))
                Row {
                    Text(
                        text = "Heslo:",
                        fontSize = 30.sp,
                        modifier = modifier
                            .width(100.dp)
                            .offset(y = 15.dp)
                    )
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password),
                        modifier = modifier
                            .alpha(0.7f)
                            .width(250.dp)
                            .border(1.dp,
                                color = Color.Black,
                                shape = RectangleShape)
                    )
                }
                Row(modifier = modifier
                    .offset(y = 20.dp)) {
                    Button(
                        onClick = { navController.navigate("registration") },
                        modifier = modifier
                            .width(150.dp)
                            .height(75.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Registrácia", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier
                        .width(16.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                dialogMessage = "Nevyplnili ste niektoré z polí"
                                showDialog = true
                            } else {
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            dialogMessage = "Úspešne prihlásený!"
                                            showDialog = true
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            dialogMessage = "Prihlásenie zlyhalo: ${task.exception?.message}"
                                            showDialog = true
                                        }
                                    }
                            }
                        },
                        modifier = modifier
                            .width(150.dp)
                            .height(75.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Prihlásiť sa", fontSize = 20.sp)
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Upozornenie") },
                    text = { Text(dialogMessage) },
                    confirmButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}


