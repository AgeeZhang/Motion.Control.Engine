package tech.runchen.mce.slamtec.retrofit.model.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import tech.runchen.mce.slamtec.retrofit.model.entity.cloud.ResultEntity
import tech.runchen.mce.slamtec.retrofit.model.entity.cloud.TargetEntity

interface CloudApi {

    /**
     * 保存目标点
     */
    @POST("/app/target/save")
    suspend fun saveTarget(
        @Header("machineNo") machineNo: String,
        @Body body: TargetEntity
    ): Response<ResultEntity<Unit>>

    /**
     * 删除目标点
     */
    @DELETE("/app/target/{targetId}")
    suspend fun deleteTarget(
        @Header("machineNo") machineNo: String,
        @Path("targetId") targetId: Int
    ): Response<ResultEntity<Unit>>

    /**
     * 获取目标点列表
     */
    @GET("/app/target/list")
    suspend fun queryTargetList(@Header("machineNo") machineNo: String): Response<ResultEntity<List<TargetEntity>>>

}