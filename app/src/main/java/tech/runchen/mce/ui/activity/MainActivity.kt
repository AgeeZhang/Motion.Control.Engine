package tech.runchen.mce.ui.activity

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import tech.runchen.mce.ui.theme.MotionControlEngineTheme
import tech.runchen.mce.vm.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MotionControlEngineTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
            }
        }
        val intent = Intent()
        intent.component = ComponentName(
            "tech.runchen.mce",
            "tech.runchen.mce.app.service.DeviceService"
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(intent)
        } else {
            this.startService(intent)
        }
    }
}

@Composable
fun Greeting(viewModel: MainViewModel = viewModel(), modifier: Modifier = Modifier) {
    Column {
        Button(onClick = {
            viewModel.dispatch(MainViewModel.MainViewAction.AddPoint("name"))
        }) {
            Text(text = "添加目标点")
        }
        Button(onClick = {
            viewModel.dispatch(MainViewModel.MainViewAction.AutoRecoverPose)
        }) {
            Text(text = "自动重定位")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MotionControlEngineTheme {
        Greeting()
    }
}