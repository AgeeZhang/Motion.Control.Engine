package tech.runchen.mce.slamtec.retrofit.model.entity.cloud

data class ResultEntity<T>(
    val code: Int,
    var data: T,
    var msg: String
)
