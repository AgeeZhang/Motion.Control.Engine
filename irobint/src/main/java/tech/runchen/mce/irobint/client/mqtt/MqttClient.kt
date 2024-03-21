package tech.runchen.mce.irobint.client.mqtt

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import info.mqtt.android.service.Ack
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

class MqttClient {

    private var options: MqttOptions
    private var context: Context
    private var deviceId: String

    private lateinit var client: MqttAndroidClient
    private lateinit var listener: SubscribeListener

    companion object {
        private val TAG: String = MqttClient::class.java.simpleName
        private const val RECONNECT_INTERVAL_MS = 5000L
        private lateinit var option: MqttConnectOptions
    }

    constructor(options: MqttOptions, context: Context, deviceId: String) {
        this.options = options
        this.context = context
        this.deviceId = deviceId
    }

    fun connect() {
        client = MqttAndroidClient(
            context, options.serverURI, options.clientId + deviceId, Ack.AUTO_ACK
        )
        client.setCallback(object : MqttCallback {

            override fun messageArrived(topic: String, message: MqttMessage) {
                if (options.debug) {
                    Log.i(TAG, "messageArrived ==> topic:$topic,message:$message")
                }
                listener.onListener(message)
            }

            override fun connectionLost(cause: Throwable) {
                if (options.debug) {
                    Log.i(TAG, "connectionLost ==> message:${cause.message}")
                }
                reconnect()
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {}
        })

        // MQTT的连接设置
        option = MqttConnectOptions()
        option.isAutomaticReconnect = true
        option.isCleanSession = true
        option.userName = options.userName
        option.password = options.password.toCharArray()
        client.connect(option, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                if (options.debug) {
                    Log.i(TAG, "connect.onSuccess ==> 连接成功")
                }
                subscribe(options.clientId + deviceId)
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                if (options.debug) {
                    Log.i(TAG, "connect.onFailure ==> 连接失败:${exception.message}")
                }
                reconnect()
            }
        })
    }

    /**
     * 订阅主题
     */
    private fun subscribe(topic: String, qos: Int = 1) {
        try {
            client.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    if (options.debug) {
                        Log.d(TAG, "subscribe.onSuccess：$topic")
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable) {
                    if (options.debug) {
                        Log.d(TAG, "subscribe.onFailure：${exception.message}")
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun reconnect() {
        if (!client.isConnected) {
            Handler(Looper.getMainLooper()).postDelayed({
                client.connect(option)
            }, RECONNECT_INTERVAL_MS)
        }
    }

    fun disconnect() {
        if (client.isConnected) {
            client.disconnect()
        }
    }

    /**
     * 发布消息
     */
    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        if (options.debug) {
            Log.i(
                TAG,
                "publish ==> isConnected:${client.isConnected},topic:$topic,qos:$qos,payload:$msg"
            )
        }
        val message = MqttMessage()
        message.payload = msg.toByteArray()
        message.qos = qos
        message.isRetained = retained
        if (client.isConnected) {
            client.publish(topic, message);
        }
    }

    /**
     * 订阅消息（外部共享）
     */
    fun subscribe(listener: SubscribeListener) {
        this.listener = listener
    }
}