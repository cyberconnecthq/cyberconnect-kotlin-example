package cyberconnect_sample.cyberconnect

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.annotation.NonNull
import cyberconnect_sample.utils.toHexString
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.cert.Certificate

public final class Utils {
    fun getAuthorizeString(@NonNull localPublicKeyPem: String): String {
        return "I authorize CyberConnect from this device using signing key:\n${localPublicKeyPem}"
    }

    private fun getKey(address: String): String {
        return "CyberConnectKey_${address}"
    }

    private fun generateKeyPair(address: String): KeyPair {
        val keyString = getKey(address)
        val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )
        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
            keyString,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).run {
            setDigests(KeyProperties.DIGEST_SHA256)
            build()
        }
        kpg.initialize(parameterSpec)
        return kpg.generateKeyPair()
    }

    fun signMessage(address: String, message: String): String? {
        val keyString = getKey(address)
        val data = message.toByteArray(StandardCharsets.UTF_8)

        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val entry: KeyStore.Entry? = ks.getEntry(keyString, null)
        val privateKey = if (entry == null) {
            generateKeyPair(keyString).private
        } else {
            if (entry !is KeyStore.PrivateKeyEntry) {
                Log.w("TAG", "Not an instance of a PrivateKeyEntry")
                return null
            }
            entry.privateKey
        }

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        signature.update(data)
        val signatureBytes = signature.sign()

        val certificate: Certificate? = ks.getCertificate(keyString)
        if (certificate != null) {
            signature.initVerify(certificate.publicKey)
            signature.update(data)
            val verifyResults = signature.verify(signatureBytes)
            Log.w("verifyResults", verifyResults.toString())
        }

        return signatureBytes.toHexString()
    }

    fun getPublicKeyString(address: String): String? {
        val keyString = getKey(address)
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        val certificate: Certificate? = ks.getCertificate(keyString)
        val publicKey: PublicKey = if (certificate == null) {
            generateKeyPair(address).public
        } else {
            certificate.publicKey
        }
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }
}