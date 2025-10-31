package com.example.uthsmarttasks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    // Replace with your Web client ID (OAuth 2.0 Client ID) from Firebase console
    private val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                LoginScreen(
                    onSignInClick = { startGoogleSignIn() },
                    onSignOutClick = { signOut() },
                    user = auth.currentUser?.displayName
                )
            }
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            // Google Sign-In was successful, authenticate with Firebase
            account?.idToken?.let { idToken ->
                firebaseAuthWithGoogle(idToken)
            } ?: run {
                Log.e("LoginActivity", "No ID token from Google account")
                Toast.makeText(this, "Google sign-in failed (no id token)", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            Log.w("LoginActivity", "Google sign-in failed", e)
            Toast.makeText(this, "Google sign-in failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    private fun startGoogleSignIn() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID) // IMPORTANT: use Web client ID
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent: Intent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser
                    Toast.makeText(this, "Đăng nhập thành công: ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    // TODO: chuyển sang màn hình chính / lưu trạng thái
                } else {
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Xác thực thất bại", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun signOut() {
        auth.signOut()
        // Also sign out from Google client if you want to remove account selection
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
            setContent {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(onSignInClick = { startGoogleSignIn() }, onSignOutClick = {}, user = null)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onSignInClick: () -> Unit, onSignOutClick: () -> Unit, user: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (user == null) {
            Text(text = "Bạn chưa đăng nhập")
            Button(onClick = onSignInClick, modifier = Modifier.padding(top = 16.dp)) {
                Text(text = "Sign in with Google")
            }
        } else {
            Text(text = "Xin chào, $user")
            Button(onClick = onSignOutClick, modifier = Modifier.padding(top = 16.dp)) {
                Text(text = "Sign out")
            }
        }
    }
}
