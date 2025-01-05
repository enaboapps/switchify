package com.enaboapps.switchify.screens.paywall.model

import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.backend.iap.IAPHandler
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener

class AppPaywallScreenModel : ViewModel(), PaywallListener {
    override fun onRestoreCompleted(customerInfo: CustomerInfo) {
        super.onRestoreCompleted(customerInfo)

        // Handle restore completion
        IAPHandler.refreshPurchaseStatus()
    }

    override fun onPurchaseCompleted(
        customerInfo: CustomerInfo,
        storeTransaction: StoreTransaction
    ) {
        super.onPurchaseCompleted(customerInfo, storeTransaction)

        // Handle purchase completion
        IAPHandler.refreshPurchaseStatus()
    }
}