package cyberconnect_sample.cyberconnect

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.annotation.NonNull
import cyberconnect_sample.utils.toHexString
import java.security.*

public final class Utils {
    fun getAuthorizeString(@NonNull localPublicKeyPem: String): String {
        return "I authorize CyberConnect from this device using signing key:\n${localPublicKeyPem}"
    }

    fun getKey(address: String): String {
        return "CyberConnectKey_${address}"
    }

    fun retrieveCyberConnectSignKey(address: String): KeyPair {
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
        val data = message.toByteArray()
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val entry: KeyStore.Entry = ks.getEntry(keyString, null)
        if (entry !is KeyStore.PrivateKeyEntry) {
            Log.w("TAG", "Not an instance of a PrivateKeyEntry")
            return null
        }
        //3742840875962924207
        //3742840875962924207
        //115792089210356248762697446949407573529996955224135760342422259061068512044369
        val signature: ByteArray = Signature.getInstance("SHA256withECDSA").run {
            initSign(entry.privateKey)
            update(data)
            sign()
        }

        return signature.toHexString()
    }

    fun getPublicKeyString(address: String): String? {
        val keyString = getKey(address)
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val entry: KeyStore.Entry = ks.getEntry(keyString, null)
        if (entry !is KeyStore.PrivateKeyEntry) {
            Log.w("TAG", "Not an instance of a PrivateKeyEntry")
            return null
        }

        val certificate = ks.getCertificate(keyString)
        val publicKey = certificate.publicKey
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }





}