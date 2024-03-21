package tech.runchen.mce.irobint.retrofit.model.entity.cloud

data class ResultEntity<T>(
    val code: Int,
    var data: T,
    var msg: String
)
