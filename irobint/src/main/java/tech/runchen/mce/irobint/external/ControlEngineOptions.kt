package tech.runchen.mce.irobint.external


data class ControlEngineOptions(
    var debug: Boolean = false,
    var chassisServiceUrl: String = "http://127.0.0.1:1448/",
    var cloudServiceUrl: String = "http://127.0.0.1:8080/",
)
