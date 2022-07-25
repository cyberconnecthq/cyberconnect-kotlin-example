package com.example.cyberconnect
import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.*

class NetworkRequestManager {
    fun getIdentity(address: String, updateResults: (result: String) -> Unit) {
        val client = OkHttpClient()
        val variables = Variables(address = address, first = 100)
        val operationString =
            "query GetIdentity(\$address: String!, \$first: Int, \$after: String) {\n  identity(address: \$address) {\n    address\n    domain\n    twitter {\n      handle\n      verified\n      __typename\n    }\n    avatar\n    followerCount(namespace: \"\")\n    followingCount(namespace: \"\")\n    followings(first: \$first, after: \$after, namespace: \"\") {\n      pageInfo {\n        ...PageInfo\n        __typename\n      }\n      list {\n        ...Connect\n        __typename\n      }\n      __typename\n    }\n    followers(first: \$first, after: \$after, namespace: \"\") {\n      pageInfo {\n        ...PageInfo\n        __typename\n      }\n      list {\n        ...Connect\n        __typename\n      }\n      __typename\n    }\n    friends(first: \$first, after: \$after, namespace: \"\") {\n      pageInfo {\n        ...PageInfo\n        __typename\n      }\n      list {\n        ...Connect\n        __typename\n      }\n      __typename\n    }\n    __typename\n  }\n}\n\nfragment PageInfo on PageInfo {\n  startCursor\n  endCursor\n  hasNextPage\n  hasPreviousPage\n  __typename\n}\n\nfragment Connect on Connect {\n  address\n  domain\n  alias\n  namespace\n  __typename\n}\n"
        val operationData = OperationData("GetIdentity", operationString, variables)
        val gson = Gson()
        val operationDataJsonString: String = gson.toJson(operationData)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = operationDataJsonString.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api.cybertino.io/connect/")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("failure:", e.toString())
                updateResults(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                Log.d("success:", "response: result")
                if (result != null) {
                    updateResults(result)
                } else {
                    updateResults("response null")
                }
            }
        })
    }

    @SuppressLint("LongLogTag")
    @RequiresApi(Build.VERSION_CODES.M)
    fun registerKey(address: String, signature: String, network: NetworkType, updateResults: (result: String) -> Unit) {
        val client = OkHttpClient()
        Utils().generatePublicKeyFor(address)
        val publicKeyString = Utils().getPublicKeyString(address)
        val message = "I authorize CyberConnect from this device using signing key:\n${publicKeyString}"
        val variable = Variables(address = address, signature = signature, network = network, message = message)

        val input = Input(variable)
        val operationInputData = OperationInputData("registerKey", "mutation registerKey(\$input: RegisterKeyInput!) {\n      registerKey(input: \$input) {\n        result\n      }\n    }", input)

        val gson = GsonBuilder().disableHtmlEscaping().create()
        val operationDataJsonString: String = gson.toJson(operationInputData)

        val gsonPrettyPrinter = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
        val operationDataJsonStringPretty = gsonPrettyPrinter.toJson(operationInputData)
        Log.d("operationDataJsonStringPretty",operationDataJsonStringPretty)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = operationDataJsonString.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api.cybertino.io/connect/")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("failure:", e.toString())
                updateResults(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                if (result != null) {
                    Log.d("success:", "response: ${result}")
                    updateResults(result)
                } else {
                    updateResults("response null")
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun connect(fromAddr: String, toAddr: String, alias: String, network: NetworkType, connectType: ConnectionType, updateResults: (result: String) -> Unit) {
        connectOrDisconnect(true, fromAddr, toAddr, alias, network, connectType, updateResults)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun disconnect(fromAddr: String, toAddr: String, alias: String, network: NetworkType, connectType: ConnectionType, updateResults: (result: String) -> Unit) {
        connectOrDisconnect(false, fromAddr, toAddr, alias, network, connectType, updateResults)
    }

    @SuppressLint("LongLogTag")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun connectOrDisconnect(isConnect: Boolean, fromAddr: String, toAddr: String, alias: String, network: NetworkType, connectType: ConnectionType, updateResults: (result: String) -> Unit) {
        val timestamp = Date().time

        var connectKeyWord = "unfollow"
        if (isConnect) {
            connectKeyWord = "follow"
        }

        val operation = Operation(connectKeyWord, fromAddr, toAddr, "CyberConnect", network, alias, timestamp)
        val gson = GsonBuilder().disableHtmlEscaping().create()
        val operationJsonString: String = gson.toJson(operation)

        val signature = Utils().signMessage(fromAddr, operationJsonString)
        val publicKey = Utils().getPublicKeyString(fromAddr)
        assert(publicKey != null)

        if (signature != null) {
            val variables = Variables(
                fromAddr = fromAddr,
                toAddr = toAddr,
                alias = alias,
                namespace = "CyberConnect",
                signature = signature,
                operation = operationJsonString,
                signingKey = publicKey,
                network = network
            )
            val input = Input(variables)
            var queryString = "mutation disconnect(\$input: UpdateConnectionInput!) {disconnect(input: \$input) {result}}"
            var operationName = "disconnect"
            if (isConnect) {
                queryString = "mutation connect(\$input: UpdateConnectionInput!) {connect(input: \$input) {result}}"
                operationName = "connect"
            }

            val operationInputData = OperationInputData(operationName, queryString, input)
            val operationInputDataJsonString: String = gson.toJson(operationInputData)

            val gsonPrettyPrinter = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
            val operationDataJsonStringPretty = gsonPrettyPrinter.toJson(operationInputData)
            Log.d("operationDataJsonStringPretty",operationDataJsonStringPretty)

            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = operationInputDataJsonString.toRequestBody(mediaType)
            val request = Request.Builder()
                .url("https://api.cybertino.io/connect/")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    updateResults(e.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val result = response.body?.string()
                    if (result != null) {
                        Log.d("success:", "response: ${result}")
                        updateResults(result)
                    } else {
                        updateResults("response null")
                    }
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setAlias(fromAddress: String, toAddress: String, alias: String, network: NetworkType, updateResults: (result: String) -> Unit) {
        val timestamp = Date().time
        val operation = Operation("follow", fromAddress, toAddress, "CyberConnect", network, alias, timestamp)

        val gson = GsonBuilder().disableHtmlEscaping().create()
        val operationJsonString: String = gson.toJson(operation)

        val signature = Utils().signMessage(fromAddress, operationJsonString)
        val publicKey = Utils().getPublicKeyString(fromAddress)
        assert(publicKey != null)

        val variables = Variables(
            fromAddr = fromAddress,
            toAddr = toAddress,
            alias = alias,
            namespace = "CyberConnect",
            signature = signature,
            operation = operationJsonString,
            signingKey = publicKey,
            network = network
        )
        val input = Input(variables)
        val query = "mutation alias(\$input: UpdateConnectionInput!) {alias(input: \$input) {result}}"
        val operationInputData = OperationInputData("alias", query, input)
        val operationInputDataJsonString: String = gson.toJson(operationInputData)

        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = operationInputDataJsonString.toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://api.cybertino.io/connect/")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                updateResults(e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                if (result != null) {
                    Log.d("success:", "response: ${result}")
                    updateResults(result)
                } else {
                    updateResults("response null")
                }
            }
        })
    }
}

data class OperationData (
    val operationName: String,
    val query: String,
    val variables: Variables,
)

data class OperationInputData (
    var operationName: String,
    var query: String,
    var variables: Input
)

data class Input (
    val input: Variables
)

data class Operation (
    val name: String,
    val from: String,
    val to: String,
    val namespace: String,
    val network: NetworkType,
    val alias: String,
    val timestamp: Long
)

data class Variables(
    val fromAddr: String? = null,
    val toAddr: String? = null,
    val from: String? = null,
    val to: Array<String>? = null,
    val namespace: String? = null,
    val address: String? = null,
    val first: Int? = null,
    val alias: String? = null,
    val signature: String? = null,
    val operation: String? = null,
    val signingKey: String? = null,
    val network: NetworkType? = null,
    val message: String? = null,
)

enum class NetworkType {
    ETH,
    SOL
}

enum class ConnectionType {
    follow,
    like,
    report,
    watch,
    vote
}