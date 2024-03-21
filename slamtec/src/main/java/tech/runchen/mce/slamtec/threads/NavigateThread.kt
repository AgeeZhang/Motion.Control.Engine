package tech.runchen.mce.slamtec.threads

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tech.runchen.mce.slamtec.client.mqtt.MqttClient
import tech.runchen.mce.slamtec.client.mqtt.model.MqttData
import tech.runchen.mce.slamtec.client.mqtt.model.MqttMessage
import tech.runchen.mce.slamtec.client.mqtt.model.NavigateRequest
import tech.runchen.mce.slamtec.client.robot.RobotClient

class NavigateThread(
    private var robotClient: RobotClient,
    private var mqttClient: MqttClient,
    private var request: NavigateRequest
) : Thread(NavigateThread::class.java.simpleName) {

    companion object {
        private val TAG: String = NavigateThread::class.java.simpleName
        private const val DEFAULT: String = "default"
        private const val MOVE_TO_ACTION: String = "MoveToAction"
        private const val ROTATE_TO_ACTION: String = "RotateToAction"
        private const val GO_HOME_ACTION: String = "GoHomeAction"
        private var actionId: Int = -1
        private var stage: String = DEFAULT
        private lateinit var navigateStatusJob: Job
    }

    override fun run() {
        Log.i(TAG, "run ==> 线程运行")
        navigateStatusJob = CoroutineScope(Dispatchers.IO).launch(start = CoroutineStart.LAZY) {
            while (!isInterrupted && this.isActive) {
                try {
                    if (robotClient.isConnected()) {
                        navigateStatus();
                    }
                    delay(1000)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            var result = robotClient.navigateRequest(request);
            if (result != null) {
                actionId = result.action_id
                stage = MOVE_TO_ACTION
                navigateStatusJob.start()
            }
        }
    }

    private suspend fun navigateStatus() {
        val actionStatus = robotClient.getActionsStatus(actionId)
        val params = hashMapOf(
            "stage" to actionStatus?.stage,
            "status" to actionStatus?.state?.status,
            "result" to actionStatus?.state?.result
        )
        if (actionStatus?.state?.result == -1 || actionStatus?.state?.result == -2) {
            stopThread()
        }
        if (stage == MOVE_TO_ACTION && actionStatus?.state?.status == 4) {
            params["status"] = 3
            var result = robotClient.rotateToAction(request.position.yaw);
            if (result != null) {
                actionId = result.action_id
                stage = ROTATE_TO_ACTION
            }
        }
        val mqttMessage = MqttMessage(
            "DeliveryRobot", MqttData(robotClient.getDeviceId(), "NavigateStatus", params)
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