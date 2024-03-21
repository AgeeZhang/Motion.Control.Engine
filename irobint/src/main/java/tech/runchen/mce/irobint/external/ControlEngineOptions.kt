package tech.runchen.mce.irobint.external

import tech.runchen.mce.slamtec.client.mqtt.MqttOptions
import tech.runchen.mce.slamtec.client.robot.RobotOptions

data class ControlEngineOptions(
    var debug: Boolean = false,
    var chassisServiceUrl: String = "http://127.0.0.1:1448/",
    var cloudServiceUrl: String = "http://127.0.0.1:8080/",
    var robotOptions: RobotOptions,
    var mqttOptions: MqttOptions,
)
