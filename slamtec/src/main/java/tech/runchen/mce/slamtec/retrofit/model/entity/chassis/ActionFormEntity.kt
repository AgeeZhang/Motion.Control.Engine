package tech.runchen.mce.slamtec.retrofit.model.entity.chassis


data class ActionFormEntity(val action_name: String, val options: Options)

sealed class Options

data class ActionOptions(
    val target: Target? = null,
    val move_options: MoveOptions? = null,
    val gohome_options: GoHomeOptions? = null
) : Options()

data class RotateToActionOptions(
    val angle: Float = 0.0f,
) : Options()

sealed class Target

data class Location(var x: Float, var y: Float, val z: Float) : Target()

data class MultiFloorTargetByPoiName(var poi_name: String) : Target()

data class MultiFloorTarget(var building: String, var floor: String, var pose: Pose2D) : Target()

data class Pose2D(var x: Float, var y: Float, var yaw: Float)

data class MoveOptions(
    val mode: Int? = 0,
    val flags: List<String>? = listOf(),
    val yaw: Float? = 0.0f,
    val fail_retry_count: Int? = 0,
)

data class GoHomeOptions(
    val flags: String = "dock",
    val back_to_landing: Boolean = true,
    val charging_retry_count: Int = 5
)

data class RecoverLocalizationActionOptions(
    val relocalization_options: ReLocalizationOptions
) : Options()

data class Rectangle(var x: Float, var y: Float, var width: Float, var height: Float)

data class ReLocalizationOptions(var max_recover_time: Int, var recover_movement_type: String)

enum class Action(val value: String) {
    MoveToAction("slamtec.agent.actions.MoveToAction"),
    GoHomeAction("slamtec.agent.actions.GoHomeAction"),
    MultiFloorMoveAction("slamtec.agent.actions.MultiFloorMoveAction"),
    RecoverLocalizationAction("slamtec.agent.actions.RecoverLocalizationAction"),
    RotateToAction("slamtec.agent.actions.RotateToAction"),
}
