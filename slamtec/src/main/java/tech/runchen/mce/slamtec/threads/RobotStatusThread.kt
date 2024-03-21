package tech.runchen.mce.slamtec.threads

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tech.runchen.mce.slamtec.client.mqtt.MqttClient
import tech.runchen.mce.slamtec.client.mqtt.model.MqttData
import tech.runchen.mce.slamtec.client.mqtt.model.MqttMessage
import tech.runchen.mce.slamtec.client.robot.RobotClient
import java.lang.Exception

class RobotStatusThread(private var robotClient: RobotClient, private var mqttClient: MqttClient) :
    Thread(RobotStatusThread::class.java.simpleName) {

    companion object {
        private val TAG: String = RobotStatusThread::class.java.simpleName
        private lateinit var robotInfoJob: Job
    }

    override fun run() {
        Log.i(TAG, "run ==> 线程运行")
        robotInfoJob = CoroutineScope(Dispatchers.Default).launch {
            while (!isInterrupted && this.isActive) {
                try {
                    if (robotClient.isConnected()) {
                        robotInfo();
                    }
                    delay(1000)
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "run: ${e.message}")
                }
            }
        }
    }

    private suspend fun robotInfo() {
        var deviceId = robotClient.getDeviceId()
        var batteryPercentage = robotClient.getPlatform().batteryPercentage
        var batteryIsCharging = robotClient.getPlatform().batteryIsCharging
        var metadata = robotClient.getMetadata()
        var position = robotClient.getPose()
        var floorInfo = robotClient.getRobotFloor();

        val params = hashMapOf(
            "position" to hashMapOf("x" to position.x, "y" to position.y, "yaw" to position.yaw),
            "isCharging" to batteryIsCharging,
            "batteryPercentage" to batteryPercentage,
            "floor" to floorInfo!!.floor,
            "metadata" to metadata
        )
        val mqttMessage = MqttMessage(
            "DeliveryRobot",
            MqttData(deviceId, "RobotStatus", params)
        )
        val json = Gson().toJson(mqttMessage)
        mqttClient.publish("RunChen/Robot/Cloud/reply", json);
    }

    fun stopThread() {
        robotInfoJob?.cancel()
        interrupt()
    }

    override fun interrupt() {
        Log.i(TAG, "interrupt ==> 线程终止")
        super.interrupt()
    }
}