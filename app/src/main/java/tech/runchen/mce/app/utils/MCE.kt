package tech.runchen.mce.app.utils

import android.content.Context
import tech.runchen.mce.slamtec.external.ControlEngineClient
import tech.runchen.mce.slamtec.external.ControlEngineOptions

class MCE {

    companion object {
        private val TAG: String = MCE::class.java.simpleName
        private lateinit var options: ControlEngineOptions
        private val engineClient: ControlEngineClient = ControlEngineClient();
        fun getInstance() = InstanceHelper.mce
    }

    object InstanceHelper {
        val mce = MCE()
    }

    fun initMCE(context: Context, _options: ControlEngineOptions) {
        options = _options
        engineClient.init(context, options)
    }

    fun start() {
        engineClient.start()
    }

    suspend fun addPoint(name: String) {
        var position = engineClient.getRobotClient().getPose()
        var deviceId = engineClient.getRobotClient().getDeviceId()
        engineClient.getCloudClient().addPoint(deviceId, name, position)
    }

    suspend fun autoRecoverPose() {
        engineClient.getRobotClient().autoRecoverPose()
    }
}