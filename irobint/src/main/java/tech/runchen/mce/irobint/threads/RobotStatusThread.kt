package tech.runchen.mce.irobint.threads

import android.util.Log
import kotlinx.coroutines.Job

class RobotStatusThread() : Thread(RobotStatusThread::class.java.simpleName) {

    companion object {
        private val TAG: String = RobotStatusThread::class.java.simpleName
        private lateinit var robotInfoJob: Job
    }

    override fun run() {
        super.run()
    }

    fun stopThread() {
        interrupt()
    }

    override fun interrupt() {
        Log.i(TAG, "interrupt ==> 线程终止")
        super.interrupt()
    }
}