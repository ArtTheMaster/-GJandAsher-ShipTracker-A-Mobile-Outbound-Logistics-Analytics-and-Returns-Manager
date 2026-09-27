package com.example.util

import java.security.MessageDigest

object SecurityUtil {
    private const val SALT = "GJandAsherShipTracker_SecuredSalt_2026"

    fun hashPin(pin: String): String {
        val input = "$SALT:$pin"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(enteredPin: String, storedHash: String, plainPinFallback: String = ""): Boolean {
        if (storedHash.isNotBlank()) {
            val computed = hashPin(enteredPin.trim())
            return computed.equals(storedHash, ignoreCase = true)
        }
        // Fallback for legacy plain PIN verification
        return enteredPin.trim() == plainPinFallback.trim()
    }
}
