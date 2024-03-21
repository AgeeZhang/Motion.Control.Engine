package tech.runchen.mce.slamtec.client.robot.model

data class Metadata(
    var dimension: Size = Size(),
    var origin: LocationF = LocationF(),
    var resolution: PointF = PointF()
)

data class Size(
    var width: Int = 0, var height: Int = 0,
)

data class LocationF(
    var x: Float = 0f, var y: Float = 0f, var z: Float = 0f,
)

data class PointF(
    var x: Float = 0f, var y: Float = 0f
)