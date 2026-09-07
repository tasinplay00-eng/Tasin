package com.example.ui.user.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.SessionManager
import com.example.core.SupabaseConfig
import com.example.ui.components.PulseGradientButton
import com.example.ui.theme.PulsePrimary
import com.example.ui.theme.PulseSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var supabaseUrl by remember { mutableStateOf(SupabaseConfig.getUrl()) }
    var supabaseKey by remember { mutableStateOf(SupabaseConfig.getAnonKey()) }
    var saveStatus by remember { mutableStateOf<String?>(null) }

    var isPrivateAccount by remember { mutableStateOf(false) }
    var allowComments by remember { mutableStateOf(true) }
    var allowDirectMessages by remember { mutableStateOf(true) }

    val isDarkMode by SessionManager.isDarkMode.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Settings & Privacy",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Appearance
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Appearance",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PulsePrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Dark Mode (Video-Optimized)", color = MaterialTheme.colorScheme.onBackground)
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { SessionManager.toggleDarkMode() }
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Privacy Controls
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Privacy Controls",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PulsePrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Private Account", color = MaterialTheme.colorScheme.onBackground)
                Switch(checked = isPrivateAccount, onCheckedChange = { isPrivateAccount = it })
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Allow Comments on Videos", color = MaterialTheme.colorScheme.onBackground)
                Switch(checked = allowComments, onCheckedChange = { allowComments = it })
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Allow Direct Messaging", color = MaterialTheme.colorScheme.onBackground)
                Switch(checked = allowDirectMessages, onCheckedChange = { allowDirectMessages = it })
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Supabase Connection Configuration
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Supabase Backend Connection",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PulseSecondary
            )
            Text(
                text = "Configure your live Supabase URL and Anon Key directly in-app or via .env",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = supabaseUrl,
                onValueChange = { supabaseUrl = it },
                label = { Text("Supabase URL") },
                placeholder = { Text("https://xyz.supabase.co") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = supabaseKey,
                onValueChange = { supabaseKey = it },
                label = { Text("Supabase Anon Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    SupabaseConfig.saveConfig(context, supabaseUrl, supabaseKey)
                    saveStatus = "Supabase Configuration Saved Successfully!"
                },
                colors = ButtonDefaults.buttonColors(containerColor = PulseSecondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Backend Settings", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            if (saveStatus != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = saveStatus!!,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Logout
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedButton(
                onClick = {
                    SessionManager.logout()
                    onLogout()
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PulsePrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("settings_logout_button")
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}
