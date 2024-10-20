package com.enaboapps.switchify.screens.settings.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enaboapps.switchify.nav.NavigationRoute
import com.enaboapps.switchify.service.menu.store.MenuItemJson
import com.enaboapps.switchify.service.menu.store.MenuItemJsonStore
import com.enaboapps.switchify.widgets.NavBar
import com.enaboapps.switchify.widgets.NavRouteLink
import com.enaboapps.switchify.widgets.Section

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyActionsScreen(navController: NavController) {
    val context = LocalContext.current
    val menuItemJsonStore = MenuItemJsonStore(context)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val menuItems = remember { mutableStateListOf<MenuItemJson>() }
    menuItems.addAll(menuItemJsonStore.getMenuItems())

    Scaffold(
        topBar = {
            NavBar(title = "My Actions", navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(NavigationRoute.AddMyActionsMenuItem.name)
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Section(title = "Actions") {
                menuItems.forEach { menuItem ->
                    ActionItem(
                        id = menuItem.id,
                        action = menuItem.text,
                        navController = navController,
                        onDelete = {
                            menuItemJsonStore.removeMenuItem(menuItem.id)
                            menuItems.remove(menuItem)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionItem(
    id: String,
    action: String,
    navController: NavController,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            NavRouteLink(
                title = action,
                summary = "Edit this action",
                navController = navController,
                route = "${NavigationRoute.EditMyActionsMenuItem.name}/${id}"
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Action",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}