package com.example.util

import com.example.data.model.CourierType
import com.example.data.model.PlatformType

data class ClassificationResult(
    val platform: PlatformType,
    val courier: CourierType,
    val confidence: String
)

data class ValidationResult(
    val isValid: Boolean,
    val message: String
)

object ClassificationHelper {

    fun classifyBarcode(code: String): ClassificationResult {
        val clean = code.trim().uppercase()

        if (clean.isBlank()) {
            return ClassificationResult(PlatformType.SHOPEE, CourierType.JT_EXPRESS, "Awaiting barcode input")
        }

        return when {
            // Shopee & SPX
            clean.startsWith("SPX") || clean.startsWith("PH") && clean.endsWith("SPX") -> {
                ClassificationResult(PlatformType.SHOPEE, CourierType.SPX, "Auto-detected: Shopee Xpress (SPX)")
            }
            // J&T Express
            clean.startsWith("JZ") || clean.startsWith("JNT") || (clean.length == 12 && clean.startsWith("78")) -> {
                val platform = if (clean.contains("SHP") || clean.startsWith("JZ")) PlatformType.SHOPEE else PlatformType.TIKTOK
                ClassificationResult(platform, CourierType.JT_EXPRESS, "Auto-detected: J&T Express AWB")
            }
            // Lazada & LEX / Ninja Van
            clean.startsWith("LZD") || clean.startsWith("LEX") || clean.startsWith("NLPH") || clean.startsWith("MP") -> {
                ClassificationResult(PlatformType.LAZADA, CourierType.NINJA_VAN, "Auto-detected: Lazada (LEX / Ninja Van)")
            }
            // TikTok Shop
            clean.startsWith("TT") || clean.startsWith("TTS") || clean.startsWith("990") -> {
                ClassificationResult(PlatformType.TIKTOK, CourierType.JT_EXPRESS, "Auto-detected: TikTok Shop")
            }
            // LBC Express
            clean.startsWith("LBC") || (clean.length in 12..14 && clean.startsWith("17")) -> {
                ClassificationResult(PlatformType.DIRECT_ORDER, CourierType.LBC, "Auto-detected: LBC Express")
            }
            // Flash Express
            clean.startsWith("FLASH") || clean.startsWith("FL") || clean.startsWith("FPE") -> {
                ClassificationResult(PlatformType.FB_MARKETPLACE, CourierType.FLASH, "Auto-detected: Flash Express")
            }
            // Standard 14-digit numeric e-commerce barcode
            clean.length in 12..16 && clean.all { it.isDigit() } -> {
                ClassificationResult(PlatformType.SHOPEE, CourierType.JT_EXPRESS, "Auto-detected: Standard 12-16 Digit Waybill")
            }
            // Fallback for custom or direct orders
            clean.startsWith("DIR") || clean.startsWith("IG") -> {
                ClassificationResult(PlatformType.DIRECT_ORDER, CourierType.LBC, "Auto-detected: Direct Order")
            }
            else -> {
                ClassificationResult(PlatformType.SHOPEE, CourierType.JT_EXPRESS, "Pattern unrecognized: Defaulted to Shopee (Manual correction available)")
            }
        }
    }

    fun validateTrackingFormat(code: String): ValidationResult {
        val clean = code.trim().uppercase()
        if (clean.isBlank()) {
            return ValidationResult(false, "Tracking number cannot be empty")
        }
        if (clean.length < 5) {
            return ValidationResult(false, "Code too short (minimum 5 characters required)")
        }
        if (!clean.all { it.isLetterOrDigit() || it == '-' || it == '_' }) {
            return ValidationResult(false, "Code contains invalid special characters")
        }
        return ValidationResult(true, "Valid barcode / QR tracking format")
    }

    fun isValidTrackingCode(code: String): Boolean {
        return validateTrackingFormat(code).isValid
    }
}
