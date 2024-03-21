package tech.runchen.mce.irobint.client.mqtt

import org.eclipse.paho.client.mqttv3.MqttMessage

interface SubscribeListener {
    fun onListener(message: MqttMessage)
}