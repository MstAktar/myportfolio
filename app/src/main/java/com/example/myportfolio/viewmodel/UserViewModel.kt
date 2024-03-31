package com.example.myportfolio.viewmodel
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myportfolio.model.User
import com.example.myportfolio.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestoreViewModel = FirestoreViewModel()

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun registerUser(
        email: String,
        password: String,
        name: String,
        address: String,
        bio: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    firestoreViewModel.addUserToFirestore(name, email, address, bio)
                    onSuccess.invoke()
                }
                .addOnFailureListener {
                    onFailure.invoke(it.message ?: "Registration failed")
                }
        }
    }

    // Function to login a user
    fun loginUser(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    onSuccess.invoke()
                }
                .addOnFailureListener {
                    onFailure.invoke(it.message ?: "Login failed")
                }
        }
    }

    fun signOut() {
        auth.signOut()
    }
}


class FirestoreViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _isLoading = mutableStateOf(false)
    val isLoading: MutableState<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: MutableState<String?> = _errorMessage

    fun addUserToFirestore(name: String, email: String, address: String, bio: String) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val user = hashMapOf(
                "name" to name,
                "email" to email,
                "address" to address,
                "bio" to bio
            )

            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    println("DocumentSnapshot added with ID: ${documentReference.id}")
                    _isLoading.value = false
                }
                .addOnFailureListener { e ->
                    println("Error adding document: $e")
                    _errorMessage.value = "Failed to add user data: ${e.message}"
                    _isLoading.value = false
                }
        }
    }

    fun getUserData(userEmail: String): Flow<UserData?> = flow {
        try {

            val querySnapshot = db.collection("users").whereEqualTo("email", userEmail).get().await()
            Log.d("RETRIEVE","Retreiving data")
            if (!querySnapshot.isEmpty) {
                val userData = querySnapshot.documents.first().toObject(UserData::class.java)
                emit(userData)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.d("ERROR RETRIEVING", e.message.toString())
            emit(null)
        }
    }
}

