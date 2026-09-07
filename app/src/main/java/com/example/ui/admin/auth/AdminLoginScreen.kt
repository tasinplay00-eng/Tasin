package com.example.ui.admin.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.AdminRole
import com.example.core.SessionManager
import com.example.data.repository.PulseRepository
import com.example.ui.components.PulseGradientButton
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary
import kotlinx.coroutines.launch

@Composable
fun AdminLoginScreen(
    repository: PulseRepository,
    onLoginSuccess: (AdminRole) -> Unit,
    onBackToUserApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("yourtasin3@gmail.com") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Back to user app button at top
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            TextButton(onClick = onBackToUserApp) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Back to User App")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Portal Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .background(PulseSecondary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = PulseSecondary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Pulse Admin Portal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Authorized Personnel Only • Role-Based Access Control",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Admin Email") },
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_email_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_password_input")
        )

        // Error Banner
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        PulseGradientButton(
            text = if (isSubmitting) "Verifying Credentials..." else "Sign In to Admin Portal",
            onClick = {
                isSubmitting = true
                coroutineScope.launch {
                    val role = repository.authenticateAdmin(email, password)
                    isSubmitting = false
                    if (role != AdminRole.NONE) {
                        onLoginSuccess(role)
                    } else {
                        errorMessage = "Access Denied: Email is not registered as an authorized administrator. Contact Super Admin (yourtasin3@gmail.com)."
                    }
                }
            },
            enabled = !isSubmitting && email.isNotBlank() && password.isNotBlank(),
            icon = Icons.Default.LockOpen,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_login_submit_button")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Super Admin Demo Preset Chip
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Super Admin: yourtasin3@gmail.com",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
