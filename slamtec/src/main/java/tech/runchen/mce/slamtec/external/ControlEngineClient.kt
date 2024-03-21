package tech.runchen.mce.slamtec.external

import android.content.Context
import tech.runchen.mce.slamtec.client.http.CloudClient
import tech.runchen.mce.slamtec.client.mqtt.MqttClient
import tech.runchen.mce.slamtec.client.robot.RobotClient
import tech.runchen.mce.slamtec.threads.MessageDownLinkThread
import tech.runchen.mce.slamtec.threads.RobotStatusThread

class ControlEngineClient {

    private lateinit var options: ControlEngineOptions
    private lateinit var robotClient: RobotClient
    private lateinit var mqttClient: MqttClient
    private lateinit var cloudClient: CloudClient

    companion object {
        private lateinit var messageDownLinkThread: MessageDownLinkThread
        private lateinit var robotStatusThread: RobotStatusThread
    }

    fun init(context: Context, options: ControlEngineOptions) {
        this.options = options
        cloudClient = CloudClient(options.cloudServiceUrl)
        robotClient = RobotClient(options.robotOptions, options.chassisServiceUrl)
        robotClient.connect()
        if (robotClient.isConnected()) {
            this.mqttClient = MqttClient(options.mqttOptions, context, robotClient.getDeviceId())
            this.mqttClient.connect()
        }
    }

    fun start() {
        messageDownLinkThread = MessageDownLinkThread(this.robotClient, this.mqttClient)
        messageDownLinkThread.start()
        robotStatusThread = RobotStatusThread(this.robotClient, this.mqttClient)
        robotStatusThread.start()
    }

    fun getRobotClient(): RobotClient {
        return robotClient
    }

    fun getCloudClient(): CloudClient {
        return cloudClient
    }

    fun close() {
        messageDownLinkThread.stopThread()
        robotStatusThread.stopThread()
    }
}