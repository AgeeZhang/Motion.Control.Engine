package tech.runchen.mce.slamtec.retrofit.model.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.ActionFormEntity
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.ActionResultEntity
import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.FloorEntity

interface ChassisApi {

    /**
     * 获取机器人所在楼层信息
     */
    @GET("/api/multi-floor/map/v1/floors/:current")
    suspend fun getRobotFloor(): Response<FloorEntity>

    /**
     * 创建新的运动行为
     */
    @POST("/api/core/motion/v1/actions")
    suspend fun createActions(@Body body: ActionFormEntity): Response<ActionResultEntity>

    /**
     * 获取Action状态
     */
    @GET("/api/core/motion/v1/actions/{action_id}")
    suspend fun getActionsStatus(@Path("action_id") actionId: Int): Response<ActionResultEntity>

    /**
     * 终止当前行为
     */
    @DELETE("/api/core/motion/v1/actions/:current")
    suspend fun cancelAction(): Response<Unit>

    /**
     * 获取定位质量
     */
    @GET("/api/core/slam/v1/localization/quality")
    suspend fun getLocalizationQuality(): Response<Int>
}