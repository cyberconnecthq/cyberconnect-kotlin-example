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
        //Security.addProvider(BouncyCastleProvider())
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
        return toByte64(signatureBytes).toHexString()
    }

    private fun toByte64(enc: ByteArray): ByteArray {

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