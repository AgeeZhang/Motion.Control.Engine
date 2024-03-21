package tech.runchen.mce.slamtec.threads

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.MqttMessage
import tech.runchen.mce.slamtec.client.mqtt.MqttClient
import tech.runchen.mce.slamtec.client.mqtt.SubscribeListener
import tech.runchen.mce.slamtec.client.mqtt.model.NavigateRequest
import tech.runchen.mce.slamtec.client.robot.RobotClient

class MessageDownLinkThread(
    private var robotClient: RobotClient,
    private var mqttClient: MqttClient
) :
    Thread(MessageDownLinkThread::class.java.simpleName) {

    companion object {
        @JvmField
        val TAG: String = MessageDownLinkThread::class.java.simpleName
        private lateinit var mqttListenerJob: Job
    }

    override fun run() {
        Log.i(TAG, "run ==> 线程运行")
        mqttListenerJob = CoroutineScope(Dispatchers.IO).launch {
            mqttClient.subscribe(object : SubscribeListener {

                override fun onListener(message: MqttMessage) {
                    Log.i(TAG, "onListener: $message.toString()")
                    messageHandle(message.toString());
                }

            });
        }
    }

    private fun messageHandle(payload: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var message = Gson().fromJson(
                payload,
                tech.runchen.mce.slamtec.client.mqtt.model.MqttMessage::class.java
            )
            var data = message.data
            if (data.method == "NavigateRequest") {
                var param = Gson().fromJson(Gson().toJson(data.params), NavigateRequest::class.java)
                NavigateThread(robotClient, mqttClient, param).start()
//                var result = robotClient.navigateRequest(data.params);
//                if (result != null) {
//                    NavigateStatusThread(robotClient, mqttClient, result.action_id).start()
//                }
            } else if ("CancelNavigateRequest" == data.method) {
                robotClient.cancelNavigateRequest(data.params);
            }
        }
    }

    fun stopThread() {
        mqttListenerJob?.cancel()
        interrupt()
    }

    override fun interrupt() {
        Log.i(TAG, "interrupt ==> 线程终止")
        super.interrupt()
    }
}