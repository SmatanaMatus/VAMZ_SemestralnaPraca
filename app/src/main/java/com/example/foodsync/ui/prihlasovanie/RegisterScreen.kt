package com.example.foodsync.ui.prihlasovanie

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodsync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(modifier: Modifier = Modifier, navController: NavController) {

    val backgroundImage = painterResource(R.drawable.ovocie)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var overovacie by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val passwordMismatchMessage = stringResource(id = R.string.error_password_mismatch)
    val emptyFieldsMessage = stringResource(id = R.string.error_empty_fields)
    val successRegistrationMessage = stringResource(id = R.string.success_registration)
    val databaseErrorMessage = stringResource(id = R.string.error_database)
    val registrationFailedMessage = stringResource(id = R.string.error_registration_failed)
    val alertTitle = stringResource(id = R.string.alert_title)
    val okButtonText = stringResource(id = R.string.ok_button)

    val registrationTitle = stringResource(id = R.string.registration_title)
    val emailLabel = stringResource(id = R.string.label_email)
    val passwordLabel = stringResource(id = R.string.label_password)
    val repeatPasswordLabel = stringResource(id = R.string.label_repeat_password)
    val cancelButtonText = stringResource(id = R.string.cancel_button)
    val registerButtonText = stringResource(id = R.string.register_button)

    Surface(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            Image(
                painter = backgroundImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier.matchParentSize()
            )
            Box(
                modifier = modifier
                    .align(Alignment.Center)
                    .width(375.dp)
                    .height(600.dp)
                    .alpha(0.8f)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(
                    modifier = modifier.matchParentSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row {
                        Text(text = registrationTitle, fontSize = 60.sp)
                    }

                    Spacer(modifier = modifier.height(50.dp))

                    Row {
                        Text(text = emailLabel, fontSize = 30.sp, modifier = modifier.width(100.dp).offset(y = 15.dp))
                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = modifier
                                .alpha(0.7f)
                                .width(240.dp)
                                .border(1.dp, color = Color.Black, shape = RectangleShape)
                        )
                    }
                    Spacer(modifier = modifier.height(20.dp))
                    Row {
                        Text(text = passwordLabel, fontSize = 30.sp, modifier = modifier.width(100.dp).offset(y = 15.dp))
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = modifier
                                .alpha(0.7f)
                                .width(240.dp)
                                .border(1.dp, color = Color.Black, shape = RectangleShape)
                        )
                    }
                    Spacer(modifier = modifier.height(20.dp))
                    Row {
                        Text(text = repeatPasswordLabel, fontSize = 30.sp, modifier = modifier.width(120.dp).offset(y = 15.dp))
                        TextField(
                            value = overovacie,
                            onValueChange = { overovacie = it },
                            modifier = modifier
                                .alpha(0.7f)
                                .width(225.dp)
                                .border(1.dp, color = Color.Black, shape = RectangleShape)
                        )
                    }
                    Spacer(modifier = modifier.height(50.dp))
                    Row {
                        Button(
                            onClick = { navController.navigate("login") },
                            modifier = modifier.width(175.dp).height(75.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = cancelButtonText, fontSize = 20.sp)
                        }
                        Button(
                            onClick = {
                                if (password != overovacie) {
                                    dialogMessage = passwordMismatchMessage
                                    showDialog = true
                                } else if (email.isBlank() || password.isBlank() || overovacie.isBlank()) {
                                    dialogMessage = emptyFieldsMessage
                                    showDialog = true
                                } else {
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val userId = auth.currentUser?.uid ?: ""

                                                val user = hashMapOf(
                                                    "email" to email,
                                                    "fridge" to listOf<String>()
                                                )

                                                firestore.collection("users").document(userId).set(user)
                                                    .addOnSuccessListener {
                                                        dialogMessage = successRegistrationMessage
                                                        showDialog = true
                                                        navController.navigate("login") {
                                                            popUpTo("registration") { inclusive = true }
                                                        }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        dialogMessage = databaseErrorMessage.format(e.message ?: "")
                                                        showDialog = true
                                                    }
                                            } else {
                                                dialogMessage = registrationFailedMessage.format(task.exception?.message ?: "")
                                                showDialog = true
                                            }
                                        }
                                }
                            },
                            modifier = modifier.width(175.dp).height(75.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.DarkGray,
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = registerButtonText, fontSize = 20.sp)
                        }
                    }
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text(alertTitle) },
                            text = { Text(dialogMessage) },
                            confirmButton = {
                                Button(onClick = { showDialog = false }) {
                                    Text(okButtonText)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
