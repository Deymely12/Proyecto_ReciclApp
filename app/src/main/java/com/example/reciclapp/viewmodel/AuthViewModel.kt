package com.example.reciclapp.viewmodel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciclapp.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private lateinit var googleSignInClient: GoogleSignInClient

    // ----------------------
    // Inicializar Google Sign-In
    // ----------------------
    fun initGoogleSignIn(activity: Activity) {
        // Obtenemos automáticamente el Web Client ID del google-services.json
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(com.example.reciclapp.R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun getGoogleSignInIntent(): Intent = googleSignInClient.signInIntent

    fun handleGoogleSignInResult(task: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) loginWithGoogle(idToken)
        } catch (e: ApiException) {
            _authState.value = AuthState.Error("Error con Google: ${e.statusCode}")
        }
    }

    // ----------------------
    // Login y registro con email/password
    // ----------------------
    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { loadUserProfile() }
            .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Error al iniciar sesión") }
    }

    fun register(firstname: String, lastname: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid
                val user = User(firstname = firstname, lastname = lastname, email = email, photoUrl = null)
                firestore.collection("users").document(uid).set(user)
                    .addOnSuccessListener { _authState.value = AuthState.Success(user) }
                    .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Error guardando usuario") }
            }
            .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Error al registrar") }
    }

    // ----------------------
    // Login con Google
    // ----------------------
    private fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user!!
                val uid = user.uid
                val userDoc = firestore.collection("users").document(uid)
                userDoc.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        loadUserProfile()
                    } else {
                        val newUser = User(
                            firstname = user.displayName?.split(" ")?.firstOrNull() ?: "",
                            lastname = user.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                            email = user.email ?: "",
                            photoUrl = user.photoUrl?.toString()
                        )
                        userDoc.set(newUser)
                            .addOnSuccessListener { _authState.value = AuthState.Success(newUser) }
                            .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Error guardando usuario") }
                    }
                }
            }
            .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Error login Google") }
    }

    // ----------------------
    // Cargar perfil
    // ----------------------
    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)
                _authState.value = AuthState.Success(user)
            }
            .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Error cargando perfil") }
    }

    // ----------------------
    // Logout
    // ----------------------
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    // ----------------------
    // Editar perfil
    // ----------------------
    fun updateUserProfile(firstname: String, lastname: String, photoUri: Uri?) {
        val uid = auth.currentUser?.uid ?: return
        _authState.value = AuthState.Loading

        if (photoUri != null) {
            val ref = storage.reference.child("profile_photos/$uid.jpg")
            ref.putFile(photoUri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        saveUserData(uid, firstname, lastname, url.toString())
                    }
                }
                .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Error subiendo foto") }
        } else {
            saveUserData(uid, firstname, lastname, null)
        }
    }

    private fun saveUserData(uid: String, firstname: String, lastname: String, photoUrl: String?) {
        val data = mutableMapOf<String, Any>(
            "firstname" to firstname,
            "lastname" to lastname
        )
        photoUrl?.let { data["photoUrl"] = it }

        firestore.collection("users").document(uid)
            .update(data)
            .addOnSuccessListener { loadUserProfile() }
            .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Error actualizando perfil") }
    }

    // ----------------------
    // Cambiar contraseña
    // ----------------------
    fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser ?: return
        val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener { _authState.value = AuthState.Success(null) }
                    .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Error cambiando contraseña") }
            }
            .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Error reautenticando") }
    }

    fun obtenerUsuarioActual(): FirebaseUser? {
        val usuarioActual: FirebaseUser? = auth.currentUser
        return usuarioActual
    }

}
