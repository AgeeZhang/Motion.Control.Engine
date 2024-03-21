package tech.runchen.mce.slamtec.client.robot

import android.util.Log
import com.google.gson.Gson
import com.slamtec.slamware.AbstractSlamwarePlatform
import com.slamtec.slamware.discovery.DeviceManager
import com.slamtec.slamware.exceptions.ConnectionFailException
import com.slamtec.slamware.robot.GridMap
import com.slamtec.slamware.robot.MapLayer
import com.slamtec.slamware.robot.Pose
import tech.runchen.mce.slamtec.client.mqtt.model.CancelNavigateRequest
import tech.runchen.mce.slamtec.client.mqtt.model.NavigateRequest
import tech.runchen.mce.slamtec.client.robot.model.LocationF
import tech.runchen.mce.slamtec.client.robot.model.PointF
import tech.runchen.mce.slamtec.client.robot.model.Metadata
import tech.runchen.mce.slamtec.client.robot.model.Position
import tech.runchen.mce.slamtec.client.robot.model.Size
import tech.runchen.mce.slamtec.retrofit.ServiceCreator
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.Action
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.ActionFormEntity
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.ActionOptions
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.ActionResultEntity
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.FloorEntity
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.GoHomeOptions
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.Location
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.MoveOptions
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.MultiFloorTarget
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.MultiFloorTargetByPoiName
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.ReLocalizationOptions
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.RecoverLocalizationActionOptions
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.RotateToActionOptions


class RobotClient {

    private var options: RobotOptions
    private var baseUrl: String
    private lateinit var platform: AbstractSlamwarePlatform

    private lateinit var deviceId: String
    private var connected: Boolean = false

    companion object {
        private val TAG: String = RobotClient::class.java.simpleName
    }

    constructor(options: RobotOptions, url: String) {
        this.options = options
        this.baseUrl = url
    }

    fun getPlatform(): AbstractSlamwarePlatform = run {
        platform
    }

    fun isConnected() = run {
        connected
    }

    fun getDeviceId() = run {
        deviceId
    }

    fun connect() {
        try {
            platform = DeviceManager.connect(options.host, options.port)
            connected = true
            deviceId = platform.deviceId
        } catch (e: ConnectionFailException) {
            connected = false
            Log.e(TAG, "connect.failed ==> message: ${e.message}")
        }
    }

    fun disconnect() = run {
        getPlatform().disconnect()
        connected = false
    }

    /**
     * 获取地图元数据
     */
    private fun getGridMap(): GridMap? {
        var gridMap: GridMap? = null
        val compositeMap = getPlatform().compositeMap
        val maps: ArrayList<*> = compositeMap.maps
        val itr: Iterator<*> = maps.iterator()
        while (itr.hasNext()) {
            val mapLayer = itr.next() as MapLayer
            if (mapLayer.usage == "explore") {
                gridMap = mapLayer as GridMap
            }
        }
        return gridMap
    }

    /**
     * 纬度转换
     */
    private fun convertDimension(pose: Pose, metadata: Metadata): Position {
        var x = (pose.x - metadata.origin.x) / metadata.resolution.x
        var y = metadata.dimension.height - 1 - (pose.y - metadata.origin.y) / metadata.resolution.y
        var yaw = 360 - 180 * pose.yaw / Math.PI
        var position = Position()
        position.x = x
        position.y = y
        position.yaw = yaw.toFloat()
        return position
    }

    fun getMetadata(): Metadata {
        var metadata = Metadata()
        var gridMap = getGridMap()
        if (gridMap != null) {
            metadata.dimension = Size(gridMap.dimension.width, gridMap.dimension.height)
            metadata.origin = LocationF(gridMap.origin.x, gridMap.origin.y, gridMap.origin.z)
            metadata.resolution = PointF(gridMap.resolution.x, gridMap.resolution.y)
        }
        return metadata
    }

    fun getPose(): Position {
        var pose = getPlatform().pose
        var metadata = getMetadata()
        return convertDimension(pose, metadata)
    }

    suspend fun getRobotFloor(): FloorEntity? {
        return ServiceCreator.createChassisService(baseUrl).getRobotFloor().body();
    }

    suspend fun autoRecoverPose(): ActionResultEntity? {
        var action = ActionFormEntity(
            Action.RecoverLocalizationAction.value,
            RecoverLocalizationActionOptions(
                relocalization_options = ReLocalizationOptions(
                    max_recover_time = 10000,
                    recover_movement_type = "RotateOnly"
                )
            )
        );
        return ServiceCreator.createChassisService(baseUrl).createActions(action).body()
    }

    suspend fun navigateRequest(params: Any): ActionResultEntity? {
        var param = Gson().fromJson(Gson().toJson(params), NavigateRequest::class.java)
        // actionName判定
        var actionName = if (param.name != null || param.floor != getRobotFloor()!!.floor)
            Action.MultiFloorMoveAction.value
        else
            Action.MoveToAction.value
        // target判定
        var target =
            if (param.name != null) {
                MultiFloorTargetByPoiName(param.name)
            } else if (param.floor != getRobotFloor()!!.floor)
                MultiFloorTarget("", param.floor, param.position)
            else
                Location(param.position.x, param.position.y, 0.0f)
        // moveOptions判定
        var moveOptions =
            if (target is Location)
                MoveOptions(yaw = param.position.yaw)
            else
                MoveOptions()
        // 结构组装
        var action = ActionFormEntity(
            actionName,
            ActionOptions(
                target = target,
                move_options = moveOptions
            )
        )
        return ServiceCreator.createChassisService(baseUrl).createActions(action).body()
    }

    suspend fun cancelNavigateRequest(params: Any) {
        var param = Gson().fromJson(Gson().toJson(params), CancelNavigateRequest::class.java)
        ServiceCreator.createChassisService(baseUrl).cancelAction().body();
        if (param.isCharging) {
            var action = ActionFormEntity(
                Action.GoHomeAction.value,
                ActionOptions(
                    gohome_options = GoHomeOptions()
                )
            );
            ServiceCreator.createChassisService(baseUrl).createActions(action).body()
        }
    }

    suspend fun getActionsStatus(actionId: Int): ActionResultEntity? {
        return ServiceCreator.createChassisService(baseUrl).getActionsStatus(actionId).body()
    }

    suspend fun goHomeAction(): ActionResultEntity? {
        var action = ActionFormEntity(
            Action.GoHomeAction.value,
            ActionOptions(
                gohome_options = GoHomeOptions()
            )
        );
        return ServiceCreator.createChassisService(baseUrl).createActions(action).body()
    }

    suspend fun rotateToAction(yaw: Float): ActionResultEntity? {
        var action = ActionFormEntity(
            Action.RotateToAction.value,
            RotateToActionOptions(
                angle = yaw
            )
        );
        return ServiceCreator.createChassisService(baseUrl).createActions(action).body()
    }
}