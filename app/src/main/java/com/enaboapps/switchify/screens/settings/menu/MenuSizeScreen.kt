package com.enaboapps.switchify.screens.settings.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.enaboapps.switchify.service.menu.MenuSizeManager
import com.enaboapps.switchify.widgets.NavBar

@Composable
fun MenuSizeScreen(navController: NavController) {
    val context = LocalContext.current
    val menuSizeManager = MenuSizeManager(context)
    val currentMenuSize = MutableLiveData<String>()
    currentMenuSize.value = menuSizeManager.getMenuSize().name
    val currentMenuSizeState = currentMenuSize.observeAsState()

    Scaffold(
        topBar = {
            NavBar("Menu Size", navController)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 16.dp)
        ) {
            MenuSizeManager.menuSizes.forEach { menuSize ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentMenuSizeState.value == menuSize.name,
                        onClick = {
                            currentMenuSize.value = menuSize.name
                            menuSizeManager.setMenuSize(menuSize)
                        }
                    )
                    Text(
                        text = menuSize.name
                    )
                }
            }
        }
    }
}