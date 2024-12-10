package com.enaboapps.switchify.screens.settings.scanning

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.enaboapps.switchify.components.BaseView
import com.enaboapps.switchify.service.scanning.ScanColorManager

@Composable
fun ScanColorSelectionScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scanColorSets = ScanColorManager.SCAN_COLOR_SETS
    val currentScanColorSet = MutableLiveData<String>()
    currentScanColorSet.value = ScanColorManager.getScanColorSetFromPreferences(context).name
    val currentScanColorSetState = currentScanColorSet.observeAsState()
    val setScanColorSet = { name: String ->
        ScanColorManager.setScanColorSetToPreferences(context, name)
        currentScanColorSet.value = name
    }

    BaseView(
        title = "Scan Colors",
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // radio buttons for each scan color set
            scanColorSets.forEach { scanColorSet ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentScanColorSetState.value == scanColorSet.name,
                        onClick = {
                            setScanColorSet(scanColorSet.name)
                        }
                    )
                    Text(
                        text = scanColorSet.name
                    )
                }
            }
        }
    }
}