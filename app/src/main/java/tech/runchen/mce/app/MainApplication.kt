package tech.runchen.mce.app

import android.app.Application
import com.tencent.bugly.crashreport.CrashReport

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
//        CrashReport.initCrashReport(applicationContext, "1924f61976", true);
    }
}