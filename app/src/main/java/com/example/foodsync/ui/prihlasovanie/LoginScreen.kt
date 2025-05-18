package com.example.foodsync.ui.prihlasovanie

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.stringResource
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

    Surface(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            Image(
                painter = backgroundImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize()
            )
            Box(
                modifier = modifier
                    .align(Alignment.TopCenter)
                    .offset(0.dp, 80.dp)
            ) {
                Image(
                    painter = logoImage,
                    contentDescription = null,
                    modifier = modifier.size(225.dp)
                )
            }
            Box(
                modifier = modifier
                    .offset(0.dp, 50.dp)
                    .align(Alignment.Center)
                    .height(300.dp)
                    .width(375.dp)
                    .alpha(0.8f)
                    .background(
                        color = Color.White,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                    .border(
                        2.dp,
                        color = Color.Black,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
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
                        text = stringResource(id = R.string.label_email),
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
                            .border(
                                1.dp,
                                color = Color.Black,
                                shape = RectangleShape
                            )
                    )
                }
                Spacer(modifier = modifier.height(30.dp))
                Row {
                    Text(
                        text = stringResource(id = R.string.label_password),
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
                            keyboardType = KeyboardType.Password
                        ),
                        modifier = modifier
                            .alpha(0.7f)
                            .width(250.dp)
                            .border(
                                1.dp,
                                color = Color.Black,
                                shape = RectangleShape
                            )
                    )
                }
                Row(modifier = modifier.offset(y = 20.dp)) {
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
                        Text(
                            text = stringResource(id = R.string.registration_button),
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    val dialogEmptyFieldsMessage = stringResource(id = R.string.dialog_empty_fields)
                    val dialogSuccessMessage = stringResource(id = R.string.dialog_login_success)
                    val dialogFailedMessage = stringResource(id = R.string.dialog_login_failed)

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                dialogMessage = dialogEmptyFieldsMessage
                                showDialog = true
                            } else {
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            dialogMessage = dialogSuccessMessage
                                            showDialog = true
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            dialogMessage = dialogFailedMessage.format(
                                                task.exception?.message ?: ""
                                            )
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
                        Text(
                            text = stringResource(id = R.string.login_button),
                            fontSize = 20.sp
                        )
                    }
                }
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(stringResource(id = R.string.alert_title)) },
                    text = { Text(dialogMessage) },
                    confirmButton = {
                        Button(onClick = { showDialog = false }) {
                            Text(stringResource(id = R.string.ok_button))
                        }
                    }
                )
            }
        }
    }
}


