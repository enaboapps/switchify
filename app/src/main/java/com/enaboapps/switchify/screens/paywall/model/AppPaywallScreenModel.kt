package com.enaboapps.switchify.screens.paywall.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.enaboapps.switchify.backend.iap.IAPHandler
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener

class AppPaywallScreenModel : ViewModel(), PaywallListener {
    var showingConfirmation = MutableLiveData(false)

    override fun onRestoreCompleted(customerInfo: CustomerInfo) {
        super.onRestoreCompleted(customerInfo)

        // Handle restore completion
        IAPHandler.refreshPurchaseStatus()

        showingConfirmation.value = true
    }

    override fun onPurchaseCompleted(
        customerInfo: CustomerInfo,
        storeTransaction: StoreTransaction
    ) {
        super.onPurchaseCompleted(customerInfo, storeTransaction)

        // Handle purchase completion
        IAPHandler.refreshPurchaseStatus()

        showingConfirmation.value = false
    }
}