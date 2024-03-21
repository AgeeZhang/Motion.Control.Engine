package tech.runchen.mce.slamtec.client.mqtt

data class MqttOptions(
    var debug: Boolean = false,
    var serverURI: String = "tcp://127.0.0.1:1883",
    var clientId: String = "RunChen/Client_",
    var userName: String = "admin",
    var password: String = "public"
)
