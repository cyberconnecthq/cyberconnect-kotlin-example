package com.example.cyberconnect

import android.os.Build
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class CyberConnect(@NonNull private var walletAddress: String) {
    private val networkRequestManager = NetworkRequestManager()
    fun registerKey(signature: String, networkType: NetworkType, updateResults: (result: String) -> Unit) {
        networkRequestManager.registerKey(this.walletAddress, signature, networkType, updateResults)
    }

    fun getIdentity(updateResults: (result: String) -> Unit) {
        networkRequestManager.getIdentity(this.walletAddress, updateResults)
    }

    fun connect(toAddr: String, alias: String, network: NetworkType, connectType: ConnectionType, updateResults: (result: String) -> Unit) {
        networkRequestManager.connect(this.walletAddress, toAddr,alias, network, connectType, updateResults)
    }

    fun disconnect(toAddr: String, alias: String, network: NetworkType, connectType: ConnectionType, updateResults: (result: String) -> Unit) {
        networkRequestManager.disconnect(this.walletAddress, toAddr, alias, network, connectType, updateResults)
    }

    fun setAlias(toAddress: String, alias: String, network: NetworkType, updateResults: (result: String) -> Unit) {
        networkRequestManager.setAlias(this.walletAddress, toAddress,alias, network, updateResults)
    }

    fun getPublicKeyString(): String? {
        return Utils().getPublicKeyString(this.walletAddress)
    }

    fun getAuthorizeString(publicKey: String): String {
        return Utils().getAuthorizeString(publicKey)
    }
}