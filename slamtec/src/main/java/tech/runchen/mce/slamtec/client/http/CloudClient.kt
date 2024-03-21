package tech.runchen.mce.slamtec.client.http

import tech.runchen.mce.slamtec.client.robot.model.Position
import tech.runchen.mce.slamtec.retrofit.ServiceCreator
import tech.runchen.mce.slamtec.retrofit.model.entity.cloud.ResultEntity
import tech.runchen.mce.slamtec.retrofit.model.entity.cloud.TargetEntity

class CloudClient {

    private var baseUrl: String

    constructor(url: String) {
        this.baseUrl = url
    }

    suspend fun addPoint(deviceId: String, name: String, pose: Position): ResultEntity<Unit>? {
        var target = TargetEntity(name, pose.x, pose.y, pose.yaw)
        return ServiceCreator.createCloudService(baseUrl)
            .saveTarget(deviceId, target).body()
    }

}