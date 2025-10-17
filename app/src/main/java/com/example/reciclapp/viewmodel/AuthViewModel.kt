package com.example.reciclapp.viewmodel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.reciclapp.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.reciclapp.R

// ----------------------
// Estados de autenticación
// ----------------------
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    // ----------------------
    // Firebase
    // ----------------------
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // ----------------------
    // Google Sign-In
    // ----------------------
    private var _googleSignInClient: GoogleSignInClient? = null
    var isRegisterFlow: Boolean = false
        private set

    fun initGoogleSignIn(activity: Activity, isRegister: Boolean) {
        this.isRegisterFlow = isRegister

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        _googleSignInClient = GoogleSignIn.getClient(activity, gso)
        _googleSignInClient?.signOut() // fuerza la elección de cuenta
    }

    fun getGoogleSignInIntent(): Intent? = _googleSignInClient?.signInIntent

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>, isRegister: Boolean = isRegisterFlow) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                loginWithGoogle(idToken, isRegister)
            } else {
                _authState.value = AuthState.Error("No se recibió ID Token de Google")
            }
        } catch (e: ApiException) {
            _authState.value = AuthState.Error("Error con Google: ${e.statusCode}")
        }
    }

    // ----------------------
    // Login / Registro con Google
    // ----------------------
    private fun loginWithGoogle(idToken: String, isRegister: Boolean) {
        _authState.value = AuthState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user!!
                val uid = user.uid
                val userDoc = firestore.collection("users").document(uid)

                userDoc.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        if (isRegister) {
                            _authState.value = AuthState.Error("Ya existe una cuenta con este correo")
                        } else {
                            loadUserProfile()
                        }
                    } else {
                        if (isRegister) {
                            val newUser = User(
                                firstname = user.displayName?.split(" ")?.firstOrNull() ?: "",
                                lastname = user.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                                email = user.email ?: "",
                                photoUrl = user.photoUrl?.toString()
                            )
                            userDoc.set(newUser)
                                .addOnSuccessListener {
                                    _authState.value = AuthState.Success(newUser)
                                }
                                .addOnFailureListener {
                                    _authState.value = AuthState.Error(it.message ?: "Error guardando usuario")
                                }
                        } else {
                            _authState.value = AuthState.Error("No existe una cuenta con este correo")
                        }
                    }
                }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Error login Google")
            }
    }

    // ----------------------
    // Login / Registro con Email
    // ----------------------
    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { loadUserProfile() }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Error al iniciar sesión")
            }
    }

    fun register(firstname: String, lastname: String, email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid
                val user = User(firstname, lastname, email, photoUrl = null)
                firestore.collection("users").document(uid).set(user)
                    .addOnSuccessListener { _authState.value = AuthState.Success(user) }
                    .addOnFailureListener {
                        _authState.value = AuthState.Error(it.message ?: "Error guardando usuario")
                    }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Error al registrar")
            }
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
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Error cargando perfil")
            }
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
                .addOnFailureListener {
                    _authState.value = AuthState.Error(it.message ?: "Error subiendo foto")
                }
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
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Error actualizando perfil")
            }
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
                    .addOnFailureListener {
                        _authState.value = AuthState.Error(it.message ?: "Error cambiando contraseña")
                    }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Error reautenticando")
            }
    }
    fun obtenerUsuarioActual(): FirebaseUser? {
        val usuarioActual: FirebaseUser? = auth.currentUser
        return usuarioActual
    }

}
