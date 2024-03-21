package tech.runchen.mce.slamtec.retrofit.model.entity.chassis

data class ActionResultEntity(
    val action_id: Int,
    val action_name: String,
    val stage: String,
    val state: ActionState
)

data class ActionState(val status: Int, val result: Int, val reason: String)
