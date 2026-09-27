package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
    assertEquals("GJandAsher ShipTracker", appName)
  }

  @Test
  fun `verify pin hashing and verification`() {
    val pin = "1234"
    val hash = com.example.util.SecurityUtil.hashPin(pin)
    org.junit.Assert.assertTrue(com.example.util.SecurityUtil.verifyPin(pin, hash))
    org.junit.Assert.assertFalse(com.example.util.SecurityUtil.verifyPin("9999", hash))
  }

  @Test
  fun `module 3 and 4 verify barcode classification and format validation`() {
    // Shopee SPX
    val spxResult = com.example.util.ClassificationHelper.classifyBarcode("SPXPH049281729")
    assertEquals(com.example.data.model.PlatformType.SHOPEE, spxResult.platform)
    assertEquals(com.example.data.model.CourierType.SPX, spxResult.courier)

    // J&T Express
    val jtResult = com.example.util.ClassificationHelper.classifyBarcode("JZ99201948210")
    assertEquals(com.example.data.model.CourierType.JT_EXPRESS, jtResult.courier)

    // Lazada LEX / Ninja Van
    val lazResult = com.example.util.ClassificationHelper.classifyBarcode("LZD-88219482")
    assertEquals(com.example.data.model.PlatformType.LAZADA, lazResult.platform)

    // Format validation
    val validFormat = com.example.util.ClassificationHelper.validateTrackingFormat("SPXPH049281729")
    org.junit.Assert.assertTrue(validFormat.isValid)

    val invalidShort = com.example.util.ClassificationHelper.validateTrackingFormat("12")
    org.junit.Assert.assertFalse(invalidShort.isValid)
  }
}
