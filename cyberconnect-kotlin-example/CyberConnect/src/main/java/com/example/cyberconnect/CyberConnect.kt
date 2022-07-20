package com.example.cyberconnect

class CyberConnect(private var walletAddress: String) {
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
}