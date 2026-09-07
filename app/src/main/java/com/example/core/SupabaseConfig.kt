package com.example.core

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Supabase configuration and runtime endpoints.
 * Supports credentials from BuildConfig (injected via .env) as well as dynamic runtime updates.
 */
object SupabaseConfig {
    private const val PREFS_NAME = "pulse_supabase_config"
    private const val KEY_URL = "supabase_url"
    private const val KEY_KEY = "supabase_anon_key"

    // Default fallback demo endpoints (or configured via environment)
    private var customUrl: String? = null
    private var customKey: String? = null

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        customUrl = prefs.getString(KEY_URL, null)
        customKey = prefs.getString(KEY_KEY, null)
    }

    fun getUrl(): String {
        return customUrl?.takeIf { it.isNotBlank() }
            ?: com.example.BuildConfig.SUPABASE_URL.takeIf { it.isNotBlank() }
            ?: "https://p5cxels6qrwyqs2tfwblun.supabase.co"
    }

    fun getAnonKey(): String {
        return customKey?.takeIf { it.isNotBlank() }
            ?: com.example.BuildConfig.SUPABASE_ANON_KEY.takeIf { it.isNotBlank() }
            ?: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy_anon_key_for_pulse_platform"
    }

    fun isConfigured(): Boolean {
        return !getUrl().contains("dummy") && !getAnonKey().contains("dummy")
    }

    fun saveConfig(context: Context, url: String, key: String) {
        customUrl = url.trim()
        customKey = key.trim()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_URL, customUrl)
            .putString(KEY_KEY, customKey)
            .apply()
    }

    fun getRestUrl(table: String): String = "${getUrl()}/rest/v1/$table"
    fun getAuthUrl(endpoint: String): String = "${getUrl()}/auth/v1/$endpoint"
    fun getStorageUrl(bucket: String, path: String): String = "${getUrl()}/storage/v1/object/public/$bucket/$path"
}

enum class AdminRole(val value: String, val displayName: String, val level: Int) {
    SUPER_ADMIN("super_admin", "Super Admin", 4),
    ADMIN("admin", "Administrator", 3),
    MODERATOR("moderator", "Moderator", 2),
    SUPPORT("support", "Support Specialist", 1),
    NONE("none", "Standard User", 0);

    companion object {
        fun fromString(roleStr: String?): AdminRole {
            return when (roleStr?.lowercase()?.trim()) {
                "super_admin" -> SUPER_ADMIN
                "admin" -> ADMIN
                "moderator" -> MODERATOR
                "support" -> SUPPORT
                else -> NONE
            }
        }
    }
}

/**
 * Manages active user authentication session, token, role, and UI mode.
 */
object SessionManager {
    private const val PREFS_SESSION = "pulse_user_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_EMAIL = "email"
    private const val KEY_USERNAME = "username"
    private const val KEY_TOKEN = "access_token"
    private const val KEY_ADMIN_ROLE = "admin_role"

    private lateinit var prefs: SharedPreferences

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _currentUsername = MutableStateFlow<String>("pulse_creator")
    val currentUsername: StateFlow<String> = _currentUsername.asStateFlow()

    private val _adminRole = MutableStateFlow(AdminRole.NONE)
    val adminRole: StateFlow<AdminRole> = _adminRole.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE)
        val savedId = prefs.getString(KEY_USER_ID, "u_pulse_creator_01")
        val savedEmail = prefs.getString(KEY_EMAIL, "creator@pulse.social")
        val savedUsername = prefs.getString(KEY_USERNAME, "alex_pulse")
        val savedRole = prefs.getString(KEY_ADMIN_ROLE, AdminRole.NONE.value)

        _currentUserId.value = savedId
        _currentUserEmail.value = savedEmail
        _currentUsername.value = savedUsername ?: "alex_pulse"
        _adminRole.value = AdminRole.fromString(savedRole)
    }

    fun setSession(userId: String, email: String, username: String, token: String? = null, role: AdminRole = AdminRole.NONE) {
        _currentUserId.value = userId
        _currentUserEmail.value = email
        _currentUsername.value = username
        _adminRole.value = role

        if (::prefs.isInitialized) {
            prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_EMAIL, email)
                .putString(KEY_USERNAME, username)
                .putString(KEY_TOKEN, token)
                .putString(KEY_ADMIN_ROLE, role.value)
                .apply()
        }
    }

    fun setAdminRole(role: AdminRole) {
        _adminRole.value = role
        if (::prefs.isInitialized) {
            prefs.edit().putString(KEY_ADMIN_ROLE, role.value).apply()
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun logout() {
        _currentUserId.value = null
        _currentUserEmail.value = null
        _adminRole.value = AdminRole.NONE
        if (::prefs.isInitialized) {
            prefs.edit().clear().apply()
        }
    }

    fun isLoggedIn(): Boolean = _currentUserId.value != null
    fun isAdmin(): Boolean = _adminRole.value != AdminRole.NONE
    fun isSuperAdmin(): Boolean = _adminRole.value == AdminRole.SUPER_ADMIN
}
