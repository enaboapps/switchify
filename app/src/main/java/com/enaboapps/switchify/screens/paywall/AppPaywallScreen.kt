package com.enaboapps.switchify.screens.paywall

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.enaboapps.switchify.screens.paywall.model.AppPaywallScreenModel
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@Composable
fun AppPaywallScreen(navController: NavController) {
    val model = AppPaywallScreenModel()
    val options = PaywallDialogOptions.Builder()
        .setListener(model)
        .setDismissRequest { navController.popBackStack() }
        .build()
    PaywallDialog(options)
}