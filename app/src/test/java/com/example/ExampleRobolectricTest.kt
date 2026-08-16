package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.DoorDostRepository
import org.junit.Assert.assertEquals
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
        assertEquals("Door Dost", appName)
    }

    @Test
    fun `test transparent pricing calculation`() {
        val breakdown = DoorDostRepository.calculateTransparentPrice(itemCost = 200, distanceKm = 1.2)
        assertEquals(200, breakdown.shopMrpCost)
        assertEquals(20, breakdown.distanceCharge)
        assertEquals(5, breakdown.flatPlatformFee)
        assertEquals(225, breakdown.totalCost)
    }
}

