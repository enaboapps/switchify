package com.enaboapps.switchify.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * This is a component used in all screens of the app.
 * It displays a Scaffold with a top bar and manages scrolling behavior.
 *
 * @param title The title of the screen.
 * @param navController The NavController used to navigate between screens.
 * @param navBarActions The actions to display in the top bar.
 * @param floatingActionButton The floating action button to display in the screen.
 * @param enableScroll Whether to enable scrolling for the content. Defaults to true.
 * @param content The content of the screen.
 */
@Composable
fun BaseView(
    title: String,
    navController: androidx.navigation.NavController,
    navBarActions: List<NavBarAction> = emptyList(),
    floatingActionButton: @Composable () -> Unit = {},
    enableScroll: Boolean = true,
    padding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            NavBar(title, navController, navBarActions)
        },
        floatingActionButton = floatingActionButton
    ) { paddingValues ->
        if (enableScroll) {
            ScrollableView(modifier = Modifier.padding(paddingValues)) {
                content()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }
    }
}