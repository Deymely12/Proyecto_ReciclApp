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
    object Idle : AuthState() // Estado inicial: sin acción
    object Loading : AuthState() // Estado cuando se está procesando algo (login, registro)
    data class Success(val user: User?) : AuthState() // Estado de éxito, guarda los datos del usuario
    data class Error(val message: String) : AuthState() // Estado de error con un mensaje
}

// ViewModel que maneja la lógica de autenticación y conexión con Firebase
class AuthViewModel : ViewModel() {

    // ----------------------
    // Instancias de Firebase
    // ----------------------
    private val auth = FirebaseAuth.getInstance() // Servicio de autenticación
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Flujo de estado de autenticación (StateFlow)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // ----------------------
    // Google Sign-In
    // ----------------------
    private var _googleSignInClient: GoogleSignInClient? = null // Cliente de inicio de sesión de Google
    var isRegisterFlow: Boolean = false // Bandera que indica si el flujo es de registro
        private set

    // Inicializamos la configuración de Google Sign-In
    fun initGoogleSignIn(activity: Activity, isRegister: Boolean) {
        this.isRegisterFlow = isRegister // Guardamos si el usuario está registrándose o iniciando sesión

        // Configuración de opciones para el login con Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))// Token necesario para Firebase
            .requestEmail() // Solicitamos el email del usuario
            .build()

        _googleSignInClient = GoogleSignIn.getClient(activity, gso)
        _googleSignInClient?.signOut() // fuerza la elección de cuenta
    }

    fun getGoogleSignInIntent(): Intent? = _googleSignInClient?.signInIntent

    // Maneja el resultado de Google Sign-In
    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>, isRegister: Boolean = isRegisterFlow) {
        try {
            // Obtenemos la cuenta seleccionada
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            // Si se obtiene correctamente el ID token, autenticamos con Firebase
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
        _authState.value = AuthState.Loading // Cambiamos estado a "cargando"
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        // Iniciamos sesión con Firebase usando las credenciales de Google
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user!! // Usuario autenticado
                val uid = user.uid
                val userDoc = firestore.collection("users").document(uid)

                // Verificamos si el usuario ya existe en Firestore
                userDoc.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // Si existe y el flujo era de registro → error
                        if (isRegister) {
                            _authState.value = AuthState.Error("Ya existe una cuenta con este correo")
                        } else {
                            // Si era login, cargamos su perfil
                            loadUserProfile()
                        }
                    } else {
                        // Si no existe y el flujo era de registro → creamos nuevo usuario
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
        // Llamamos a Firebase Authentication para iniciar sesión con email y contraseña
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { loadUserProfile() }  // Si inicia sesión, cargamos su perfil
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Error al iniciar sesión")
            }
    }

    fun register(firstname: String, lastname: String, email: String, password: String) {
        _authState.value = AuthState.Loading

        // Creamos una nueva cuenta en Firebase Authentication con el email y la contraseña proporcionados
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                // Si el registro fue exitoso, obtenemos el ID único del usuario (UID)
                val uid = result.user!!.uid
                // Creamos un objeto con la información básica del usuario
                val user = User(firstname, lastname, email, photoUrl = null)
                // Guardamos los datos del usuario en la base de datos Firestore
                firestore.collection("users").document(uid).set(user)
                    // Si se guarda correctamente, cambiamos el estado a "Success"
                    .addOnSuccessListener { _authState.value = AuthState.Success(user) }
                    // Si falla al guardar, notificamos el error
                    .addOnFailureListener {
                        _authState.value = AuthState.Error(it.message ?: "Error guardando usuario")
                    }
            }
            // Si hubo algún error durante la creación de cuenta (por ejemplo, email ya en uso)
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Error al registrar")
            }
    }

    // ----------------------
    // Cargar perfil
    // ----------------------
    private fun loadUserProfile() {
        // Obtenemos el ID (UID) del usuario actual logueado
        val uid = auth.currentUser?.uid ?: return

        // Consultamos en Firestore el documento del usuario
        firestore.collection("users").document(uid).get()

            // Si encontramos el documento, convertimos sus datos a un objeto "User"
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java)

                // Actualizamos el estado con los datos del usuario
                _authState.value = AuthState.Success(user)
            }
            // Si ocurre algún error al leer el documento, mostramos el mensaje de error
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Error cargando perfil")
            }
    }

    // ----------------------
    // Logout
    // ----------------------
    fun logout() {
        // Cierra la sesión actual en Firebase Authentication
        auth.signOut()
        // Cambia el estado de autenticación a "Idle" (sin usuario logueado)
        _authState.value = AuthState.Idle
    }

    // ----------------------
    // Editar perfil
    // ----------------------
    fun updateUserProfile(firstname: String, lastname: String, photoUri: Uri?) {
        // Obtenemos el ID del usuario actual
        val uid = auth.currentUser?.uid ?: return

        // Cambiamos estado a "cargando" mientras actualizamos
        _authState.value = AuthState.Loading

        // Si el usuario seleccionó una nueva foto de perfil
        if (photoUri != null) {
            // Creamos una referencia en Firebase Storage para subir la imagen
            val ref = storage.reference.child("profile_photos/$uid.jpg")
            // Subimos la imagen
            ref.putFile(photoUri)

                // Si se sube correctamente, obtenemos la URL de descarga
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        // Llamamos a la función que guarda los nuevos datos (nombre + URL foto)
                        saveUserData(uid, firstname, lastname, url.toString())
                    }
                }
                // Si falla la subida, actualizamos el estado a Error
                .addOnFailureListener {
                    _authState.value = AuthState.Error(it.message ?: "Error subiendo foto")
                }
        } else {
            // Si no hay foto nueva, solo actualizamos los nombres
            saveUserData(uid, firstname, lastname, null)
        }
    }
    // Función auxiliar para guardar los datos actualizados en Firestore
    private fun saveUserData(uid: String, firstname: String, lastname: String, photoUrl: String?) {
        // Creamos un mapa con los campos que queremos actualizar
        val data = mutableMapOf<String, Any>(
            "firstname" to firstname,
            "lastname" to lastname
        )
        // Si hay una nueva URL de foto, la agregamos
        photoUrl?.let { data["photoUrl"] = it }

        // Actualizamos el documento del usuario en Firestore
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
        // Obtenemos el usuario actualmente logueado
        val user = auth.currentUser ?: return

        // Creamos una credencial para reautenticar al usuario
        // (Firebase pide confirmar la contraseña actual antes de cambiarla)
        val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)

        // Reautenticamos al usuario con su contraseña actual
        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Si la reautenticación fue correcta, actualizamos la contraseña
                user.updatePassword(newPassword)
                    // Si se cambió exitosamente, marcamos éxito
                    .addOnSuccessListener { _authState.value = AuthState.Success(null) }
                    // Si falla, mostramos el error
                    .addOnFailureListener {
                        _authState.value = AuthState.Error(it.message ?: "Error cambiando contraseña")
                    }
            }
            // Si la reautenticación falla (por ejemplo, contraseña incorrecta)
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Error reautenticando")
            }
    }
    fun obtenerUsuarioActual(): FirebaseUser? {
        // Devuelve el usuario que actualmente está logueado en Firebase Authentication.
        // Si no hay nadie logueado, devuelve null.
        val usuarioActual: FirebaseUser? = auth.currentUser
        return usuarioActual
    }

}
