package tech.runchen.mce.slamtec.retrofit.model.entity.chassis

data class PowerStatusEntity(
    val batteryPercentage: Double,
    val dockingStatus: String,
    val isCharging: Boolean,
    val isDCConnected: Boolean,
    val powerStage: String,
    val sleepMode: String
)
