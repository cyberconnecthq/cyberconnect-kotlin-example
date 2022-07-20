package cyberconnect_sample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.TimeUtils
import cyberconnect_sample.cyberconnect.*
import cyberconnect_sample.utils.*
import io.iotex.walletconnect_sample.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.web3j.crypto.StructuredDataEncoder
import org.web3j.utils.Numeric
import java.math.BigInteger

class MainActivity : AppCompatActivity() {

    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        exception.message?.e()
        exception.message?.toast()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WalletConnector.init(::onConnected, ::onDisconnected)
        mBtnConnect.setOnClickListener {
            WalletConnector.connect()
        }
    }

    private var cyberconnectInstance: CyberConnect? = null

    private fun onConnected(address: String, chainId: Long) {
        lifecycleScope.launch(Dispatchers.Main) {
            "address : $address -- chainId : $chainId".i()
            mLlConnectedUI.visibility = View.VISIBLE
            mBtnConnect.visibility = View.GONE

            mTvAddress.text = "Address:$address"
            mTvChainId.text = "ChainID:$chainId"
            cyberconnectInstance = CyberConnect(address)
            mBtnDisconnect.setOnClickListener {
                WalletConnector.disconnect()
            }

            mBtnGetWalletAuthority.setOnClickListener {
                testPersonalSignMessage(address)
            }

            mBtnGetIdentity.setOnClickListener {
                cyberconnectInstance?.getIdentity { result ->
                    Log.d("getIdentity:", result)
                }
            }

            mBtnFollow.setOnClickListener {
                cyberconnectInstance?.connect("0xf6b6f07862a02c85628b3a9688beae07fea9c863","", NetworkType.ETH, ConnectionType.follow) { result ->
                    Log.d("connect:", result)
                }
            }

            mBtnSetAlia.setOnClickListener {
                NetworkRequestManager().setAlias(address, "0xf6b6f07862a02c85628b3a9688beae07fea9c863","Hello", NetworkType.ETH){ result ->
                    Log.d("setAlias:", result)
                }
            }

            mBtnUnFollow.setOnClickListener {
                cyberconnectInstance?.disconnect("0xf6b6f07862a02c85628b3a9688beae07fea9c863","", NetworkType.ETH, ConnectionType.follow){ result ->
                    Log.d("disconnect:", result)
                }
            }
        }
    }

    private fun onDisconnected() {
        lifecycleScope.launch(Dispatchers.Main) {
            mLlConnectedUI.visibility = View.GONE
            mBtnConnect.visibility = View.VISIBLE
        }
    }

    private fun testSendTransaction(address: String, chainId: Long) {
        lifecycleScope.launch(errorHandler) {
            val from = address
            val to = address

//            val rawNonce = HttpManager.getAccountNonce(address, chainId) ?: return@launch
//            val nonce =  Numeric.prependHexPrefix(BigInteger(rawNonce.toString()).toString(16))
//
//            val rawGasPrice = HttpManager.getGasPrices()?.slow?.price ?: return@launch
//            val gasPrice = Numeric.prependHexPrefix(BigInteger(rawGasPrice.toString()).times(BigInteger.TEN.pow(9)).toString())
////
//            val gasLimit = Numeric.prependHexPrefix(BigInteger("21000").toString(16))

            val value = "0.1"

            val data = "0x"

            val response = WalletConnector.sendTransaction(from, value, data)
            "response : $response".i()
        }
    }

    private fun testSignTransaction(address: String, chainId: Long) {
        lifecycleScope.launch(errorHandler) {
            val from = address
            val to = address

            val gasLimit = Numeric.prependHexPrefix(BigInteger("21000").toString(16))

            val value = "0"

            val data = "0x"

            val params = mutableMapOf<String, String>().apply {
                this["from"] = from
                this["to"] = to
                this["data"] = data
                this["value"] = value
                this["gas"] = gasLimit
            }
            val response = WalletConnector.signTransaction(listOf(params))

            "response : ${response.result}".i()
        }
    }

    private fun testSignTypedData(address: String) {
        lifecycleScope.launch {
            val json = ResourceUtils.readAssets2String("eip712.json")
            val params = listOf(address, json)
            val dialog = ValidateDialog(this@MainActivity).show()
            val response = WalletConnector.signTypeData(params)
            if (response.result != null) {
                val data = StructuredDataEncoder(json).hashStructuredData().toHexString()
                val valid = EncryptUtil.validateSignature(response.result.toString(), data, address, false)
                dialog.setMethod("eth_signTypedData")
                    .setAddress(address)
                    .setValid(valid.toString())
                    .setResult(response.result.toString())
                    .renderConnect()
            }
        }
    }

    private fun showDialog(address: String, message: String) {
        val dialog = ValidateDialog(this@MainActivity).show()
        dialog.setMethod("response")
            .setAddress(address)
            .setValid("")
            .setResult(message)
            .renderConnect()
    }

    private fun testSignMessage(address: String) {
        lifecycleScope.launch(errorHandler) {
            val message = "My email is john@doe.com - ${TimeUtils.getNowString()}"
            val hexMsg = message.toByteArray().toHexString()
            val dialog = ValidateDialog(this@MainActivity).show()
            val response = WalletConnector.signMessage(address, hexMsg)
            if (response.result != null) {
                val valid = EncryptUtil.validateSignature(response.result.toString(), message, address)
                dialog.setMethod("eth_signTypedData")
                    .setAddress(address)
                    .setValid(valid.toString())
                    .setResult(response.result as String)
                    .renderConnect()
            }
        }
    }

    private fun testPersonalSignMessage(address: String) {
        lifecycleScope.launch(errorHandler) {
            val publicKeyString = Utils().getPublicKeyString(address)
            val authorizeString = publicKeyString?.let {
                Utils().getAuthorizeString(it)
            }
            val hexMsg = authorizeString?.toByteArray()?.toHexString()
            val params = listOf(hexMsg, address)
            val dialog = ValidateDialog(this@MainActivity).show()
            val response = WalletConnector.personalSign(params)
            Log.e("address:", address)
            if (authorizeString != null) {
                Log.e("authorizeString:", authorizeString)
            }
            Log.e("response:", response.toString())
            if (response.result != null) {
                val valid = authorizeString?.let {
                    EncryptUtil.validateSignature(response.result.toString(),
                        it, address)
                }
                dialog.setMethod("eth_signTypedData")
                    .setAddress(address)
                    .setValid(valid.toString())
                    .setResult(response.result as String)
                    .renderConnect()

                if (authorizeString != null) {
                    cyberconnectInstance?.registerKey(response.result.toString(), NetworkType.ETH) { result ->
                        Log.d("registerKey:", result)
                    }
                }
            }
        }
    }

}