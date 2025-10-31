package com.example.uthsmarttasks

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.uthsmarttasks.ui.theme.UTHSmartTasksTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UTHSmartTasksTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GoogleSignInScreen(
                        onSignIn = { startGoogleSignIn() },
                        userEmail = auth.currentUser?.email
                    )
                }
            }
        }
    }

    private fun startGoogleSignIn() {
        lifecycleScope.launch {
            try {
                val credentialManager = CredentialManager.create(this@MainActivity)

                // ✅ Tạo tùy chọn đăng nhập bằng Google
                val googleOption = GetSignInWithGoogleOption.Builder(
                    getString(R.string.default_web_client_id)
                ).build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()

                // ✅ Gọi đúng phiên bản hàm có context
                val result: GetCredentialResponse =
                    credentialManager.getCredential(this@MainActivity, request)

                val credential = result.credential
                val googleIdToken = credential.data.getString("googleIdToken")

                if (googleIdToken != null) {
                    firebaseAuthWithGoogle(googleIdToken)
                } else {
                    Log.w("GoogleSignIn", "Không nhận được ID token")
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Lỗi đăng nhập Google", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("FirebaseAuth", "✅ Đăng nhập thành công: ${user?.email}")
                } else {
                    Log.e("FirebaseAuth", "❌ Đăng nhập thất bại", task.exception)
                }
            }
    }
}

@Composable
fun GoogleSignInScreen(
    onSignIn: () -> Unit,
    userEmail: String?
) {
    val isSignedIn = userEmail != null
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSignedIn) "Xin chào, $userEmail" else "Đăng nhập với Google",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onSignIn) {
            Text(if (isSignedIn) "Đăng nhập lại" else "Đăng nhập Google")
        }
    }
}
