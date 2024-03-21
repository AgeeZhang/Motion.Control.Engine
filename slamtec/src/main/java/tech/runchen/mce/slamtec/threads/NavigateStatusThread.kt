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

class NavigateStatusThread(
    private var client: RobotClient,
    private var mqttClient: MqttClient,
    private var actionId: Int
) :
    Thread(NavigateStatusThread::class.java.simpleName) {

    companion object {
        private val TAG: String = NavigateStatusThread::class.java.simpleName
        private lateinit var navigateStatusJob: Job
    }

    override fun run() {
        Log.i(TAG, "run ==> 线程运行")
        navigateStatusJob = CoroutineScope(Dispatchers.IO).launch {
            while (!isInterrupted && this.isActive) {
                try {
                    if (client.isConnected()) {
                        navigateStatus();
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

    private suspend fun navigateStatus() {
        val actionStatus = client.getActionsStatus(actionId)
        val params = hashMapOf(
            "stage" to actionStatus?.stage,
            "status" to actionStatus?.state?.status,
            "result" to actionStatus?.state?.result
        )
        if (actionStatus?.state?.status == 4 || actionStatus?.state?.result == -1 || actionStatus?.state?.result == -2) {
            stopThread()
        }
        val mqttMessage = MqttMessage(
            "DeliveryRobot",
            MqttData(client.getDeviceId(), "NavigateStatus", params)
        )
        val json = Gson().toJson(mqttMessage)
        mqttClient.publish("RunChen/Robot/Cloud/reply", json);
    }

    private fun stopThread() {
        navigateStatusJob?.cancel()
        interrupt()
    }

    override fun interrupt() {
        Log.i(TAG, "interrupt ==> 线程终止")
        super.interrupt()
    }
}