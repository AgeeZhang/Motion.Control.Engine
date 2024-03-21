package tech.runchen.mce.slamtec.client.mqtt.model

import tech.runchen.mce.slamtec.retrofit.model.entity.chassis.Pose2D

data class NavigateRequest(
    val name: String, val floor: String, val position: Pose2D
)
