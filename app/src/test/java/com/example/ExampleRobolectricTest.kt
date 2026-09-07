package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.AdminRole
import com.example.core.SessionManager
import com.example.data.repository.PulseRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Pulse", appName)
  }

  @Test
  fun `super admin authentication verification`() = runBlocking {
    val repository = PulseRepository()
    val role = repository.authenticateAdmin("yourtasin3@gmail.com", "any_password")
    assertEquals(AdminRole.SUPER_ADMIN, role)
  }

  @Test
  fun `feed videos loaded`() {
    val repository = PulseRepository()
    assertTrue(repository.videos.value.isNotEmpty())
  }
}

