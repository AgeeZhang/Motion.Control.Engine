package tech.runchen.mce.vm

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tech.runchen.mce.app.utils.MCE

class MainViewModel : ViewModel() {

    var viewStates by mutableStateOf(MainViewState())
        private set

    data class MainViewState(
        val deviceId: String = "",
    )

    fun dispatch(action: MainViewAction) {
        when (action) {
            is MainViewAction.AddPoint -> addPoint(action.name)
            is MainViewAction.AutoRecoverPose -> autoRecoverPose()
        }
    }

    private fun addPoint(name: String) {
        viewModelScope.launch {
            MCE.getInstance().addPoint(name);
        }
    }

    private fun autoRecoverPose() {
        viewModelScope.launch {
            MCE.getInstance().autoRecoverPose()
        }
    }

    sealed class MainViewAction {
        class AddPoint(val name: String) : MainViewAction()
        object AutoRecoverPose : MainViewAction()
    }

}