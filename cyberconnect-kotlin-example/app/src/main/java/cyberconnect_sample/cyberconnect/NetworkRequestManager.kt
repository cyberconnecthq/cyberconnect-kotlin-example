package cyberconnect_sample.cyberconnect

class NetworkRequestManager {

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