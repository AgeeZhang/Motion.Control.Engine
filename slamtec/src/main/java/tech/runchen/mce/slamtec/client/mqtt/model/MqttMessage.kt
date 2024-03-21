package tech.runchen.mce.slamtec.client.mqtt.model

data class MqttMessage(
    val type: String, val data: MqttData
)
