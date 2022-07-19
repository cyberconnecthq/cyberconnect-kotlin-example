package cyberconnect_sample.cyberconnect
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.security.PublicKey

class NetworkRequestManager {
    fun getIdentity(address: String, updateResults: (result: String) -> Int) {
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

    fun registerKey(address: String, publicKeyString: String, signature: String, network: NetworkType, updateResults: (result: String) -> Int) {
        val client = OkHttpClient()
        //val publicKey = Utils().getPublicKeyString(address)
        val message = "I authorize CyberConnect from this device using signing key:\n${publicKeyString}"

        val variable = Variables(address = address, signature = signature, network = network, message = message)

        val input = Input(variable)
        val operationInputData = OperationInputData("registerKey", "mutation registerKey(\$input: RegisterKeyInput!) {\n      registerKey(input: \$input) {\n        result\n      }\n    }", input)

        val gson = Gson()
        val operationDataJsonString: String = gson.toJson(operationInputData)

        val gsonPrettyPrinter = GsonBuilder().setPrettyPrinting().create()
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
                Log.d("success:", "response: result")
                if (result != null) {
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