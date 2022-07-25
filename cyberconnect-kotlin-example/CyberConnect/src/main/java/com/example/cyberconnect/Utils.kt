package com.example.cyberconnect

import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import com.example.cyberconnect.store.CryptoKeyStore
import org.web3j.utils.Numeric

@RequiresApi(Build.VERSION_CODES.M)
class Utils {
    fun getAuthorizeString(@NonNull localPublicKeyPem: String): String {
        return "I authorize CyberConnect from this device using signing key:\n${localPublicKeyPem}"
    }

    fun getKey(address: String): String {
        return "CyberConnectKey_${address}"
    }

    fun signMessage(address: String, message: String): String? {
        return CryptoKeyStore().signMessage(address, message)
    }

    fun toByte64(enc: ByteArray): ByteArray {
        var rLength = enc[3].toInt()
        var sLength = enc[5 + rLength].toInt()

        val sPos = 6 + rLength
        val res = ByteArray(64)
        if (rLength <= 32) {
            System.arraycopy(enc, 4, res, 32 - rLength, rLength)
            rLength = 32
        } else if (rLength == 33 && enc[4].toInt() == 0) {
            rLength--
            System.arraycopy(enc, 5, res, 0, rLength)
        } else {
            throw Exception("unsupported r-length - r-length:" + rLength.toString() + ",s-length:" + sLength.toString() + ",enc:" + enc.toHexString())
        }
        if (sLength <= 32) {
            System.arraycopy(enc, sPos, res, rLength + 32 - sLength, sLength)
            sLength = 32
        } else if (sLength == 33 && enc[sPos].toInt() == 0) {
            System.arraycopy(enc, sPos + 1, res, rLength, sLength - 1)
        } else {
            throw Exception("unsupported s-length - r-length:" + rLength.toString() + ",s-length:" + sLength.toString() + ",enc:" + enc.toHexString())
        }

        return res
    }

    fun getPublicKeyString(address: String): String? {
        return CryptoKeyStore().getPublicKeyString(address)
    }

    fun generatePublicKeyFor(address: String) {
        CryptoKeyStore().generateKeyPair(address)
    }
}

fun ByteArray.toHexString(): String {
    return Numeric.toHexString(this)
}