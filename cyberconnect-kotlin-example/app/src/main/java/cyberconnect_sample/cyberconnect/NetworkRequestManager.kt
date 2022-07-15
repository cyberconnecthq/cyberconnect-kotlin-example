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

data class Variables (
    val fromAddr: String,
    val toAddr: String,
    val from: String,
    val to: Array<String>,
    val namespace: String,
    val address: String,
    val first: UInt,
    val alias: String,
    val signature: String,
    val operation: String,
    val signingKey: String,
    val network: NetworkType,
    val message: String,
)

enum class NetworkType {
    ETH,
    SOL
}