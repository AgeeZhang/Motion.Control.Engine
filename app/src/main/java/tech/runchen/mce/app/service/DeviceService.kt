package tech.runchen.mce.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.runchen.mce.R
import tech.runchen.mce.app.receiver.DeviceBroadcastReceiver
import tech.runchen.mce.app.utils.MCE
import tech.runchen.mce.slamtec.client.mqtt.MqttOptions
import tech.runchen.mce.slamtec.client.robot.RobotOptions
import tech.runchen.mce.slamtec.external.ControlEngineOptions
import tech.runchen.mce.ui.activity.MainActivity

class DeviceService : Service() {

    private lateinit var deviceBroadcastReceiver: DeviceBroadcastReceiver
    private lateinit var mContext: Context

    companion object {
        private val TAG: String = DeviceService::class.java.simpleName

        //标记服务是否启动
        private var serviceIsLive = false

        //唯一前台通知ID
        private const val notificationId = 1000
    }

    override fun onCreate() {
        Log.i(TAG, "onCreate")
        super.onCreate()
        mContext = this
        // 获取服务通知
        val notification: Notification = createForegroundNotification()
        startForeground(notificationId, notification)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null;
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")
        CoroutineScope(Dispatchers.Main).launch {
            receiverRegister()
            MCE.getInstance().initMCE(
                mContext,
                ControlEngineOptions(
                    true,
                    "http://127.0.0.1:1448/",
                    "http://zzj.frp.runchen.tech/",
                    RobotOptions(debug = true, host = "127.0.0.1"),
                    MqttOptions(debug = true, serverURI = "tcp://118.25.11.200:1995")
                )
            )
            MCE.getInstance().start()
            Log.e(TAG, "onStartCommand: " + intent?.getStringExtra("key"))
        }

        // 标记前台服务启动
        serviceIsLive = true
        return super.onStartCommand(intent, flags, startId)
    }

    private fun receiverRegister() {
        deviceBroadcastReceiver = DeviceBroadcastReceiver()
        val filter = IntentFilter()
        filter.addAction("deviceCall")
        registerReceiver(deviceBroadcastReceiver, filter)
    }

    /**
     * 创建前台服务通知
     */
    private fun createForegroundNotification(): Notification {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // 唯一的通知通道的id.
        val notificationChannelId = "notification_channel_id_01"
        // Android8.0以上的系统，新建消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //用户可见的通道名称
            val channelName = "思岚运动控制引擎"
            //通道的重要程度
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel =
                NotificationChannel(notificationChannelId, channelName, importance)
            notificationChannel.description = "运动控制引擎运行中"
            //LED灯
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            //震动
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(this, notificationChannelId)
        //通知小图标
        builder.setSmallIcon(R.mipmap.ic_launcher)
        //通知标题
        builder.setContentTitle("运动控制引擎")
        //通知内容
        builder.setContentText("运动控制引擎正在运行中")
        //设定通知显示的时间
        builder.setWhen(System.currentTimeMillis())
        //设定启动的内容
        val activityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)
        //创建通知并返回
        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 标记服务关闭
        serviceIsLive = false
        // 移除通知
        stopForeground(STOP_FOREGROUND_DETACH)
    }
}