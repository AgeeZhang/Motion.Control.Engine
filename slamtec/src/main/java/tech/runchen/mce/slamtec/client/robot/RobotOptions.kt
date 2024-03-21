package tech.runchen.mce.slamtec.client.robot

data class RobotOptions(
    var debug: Boolean = false,
    var host: String = "127.0.0.1",
    var port: Int = 1445,
)
