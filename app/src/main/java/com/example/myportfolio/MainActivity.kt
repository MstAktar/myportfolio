@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myportfolio

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myportfolio.model.User
import com.example.myportfolio.model.UserData
import com.example.myportfolio.ui.theme.MyportfolioTheme
import com.example.myportfolio.viewmodel.AuthViewModel
import com.example.myportfolio.viewmodel.FirestoreViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyportfolioApp()
        }
    }
}

@Composable
fun MyportfolioApp() {
    MyportfolioTheme {

        val navController = rememberNavController()
        val authViewModel: AuthViewModel = viewModel()
        val firestoreViewModel: FirestoreViewModel = viewModel()

        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Navigation(navController, authViewModel, firestoreViewModel)
        }
    }
}

@Composable
fun Navigation(navController: NavHostController, authViewModel: AuthViewModel, firestoreViewModel: FirestoreViewModel) {
    NavHost(navController, startDestination = "login") {
        composable("home") {
            HomeScreen(navController, authViewModel, firestoreViewModel)
        }
        composable("signup") {
            SignUpScreen(navController, authViewModel)
        }
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController, authViewModel: AuthViewModel, firestoreViewModel: FirestoreViewModel) {
    val userEmail = authViewModel.getCurrentUserEmail()
    val userData by firestoreViewModel.getUserData(userEmail ?: "").collectAsState(initial = null)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (userData != null) {
                UserDataSection(userData!!)
            } else {
                Text(
                    text = "Loading user data...",
                    style = TextStyle(color = Color.Gray, fontSize = 16.sp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    authViewModel.signOut()
                    navController.navigate("login")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sign Out",
                    style = TextStyle(color = Color.White, fontSize = 16.sp)
                )
            }
        }
    }
}

@Composable
fun UserDataSection(userData: UserData) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StyledText("Name: ${userData.name}")
        StyledText("Email: ${userData.email}")
        StyledText("Address: ${userData.address}")
        StyledText("Bio: ${userData.bio}")
    }
}

@Composable
fun StyledText(text: String) {
    Text(
        text = text,
        style = TextStyle(color = Color.Black, fontSize = 16.sp)
    )
}



@Composable
fun SignUpScreen(navController: NavHostController, viewModel: AuthViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Text fields
            SignUpTextField("Name", name) { name = it }
            Spacer(modifier = Modifier.height(8.dp))
            SignUpTextField("Email", email) { email = it }
            Spacer(modifier = Modifier.height(8.dp))
            SignUpTextField("Address", address) { address = it }
            Spacer(modifier = Modifier.height(8.dp))
            SignUpTextField("Bio", bio) { bio = it }
            Spacer(modifier = Modifier.height(8.dp))
            SignUpTextField("Password", password, passwordField = true) { password = it }
            Spacer(modifier = Modifier.height(8.dp))
            SignUpTextField("Confirm Password", confirmPassword, passwordField = true) {
                confirmPassword = it
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (password == confirmPassword) {
                    viewModel.registerUser(
                        email = email,
                        password = password,
                        name = name,
                        address = address,
                        bio = bio,
                        onSuccess = {
                            navController.navigate("home")
                        },
                        onFailure = { errorMessage = it }
                    )
                } else {
                    errorMessage = "Passwords do not match"
                }
            }) {
                Text(text = "Sign Up")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colors.error
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text("Aldready has an account?")
            Spacer(modifier = Modifier.height(5.dp))
            Button(onClick = { navController.navigate("login") }) {
                Text(text = "Login")
            }
        }
    }
}

@Composable
fun SignUpTextField(label: String, value: String, passwordField: Boolean = false, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (passwordField) PasswordVisualTransformation() else VisualTransformation.None
    )
}

@Composable
fun LoginScreen(navController: NavHostController, viewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email
                ),
                isError = errorMessage != null
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = errorMessage != null
            )
            Spacer(modifier = Modifier.height(8.dp))
            errorMessage?.let { message ->
                Text(
                    text = message,
//                    color = MaterialTheme.colors.error,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                errorMessage = null
                viewModel.loginUser(
                    email = email,
                    password = password,
                    onSuccess = {
                        navController.navigate("home")
                    },
                    onFailure = {
                        errorMessage = it
                    }
                )
            }) {
                Text(text = "Login")
            }

            Spacer(modifier = Modifier.height(5.dp))
            Text("Doesn't have any account?")
            Spacer(modifier = Modifier.height(5.dp))
            Button(onClick = { navController.navigate("signup") }) {
                Text(text = "Sign Up")
            }
        }
    }
}

