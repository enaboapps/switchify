package com.enaboapps.switchify.backend.iap

import android.content.Context
import android.util.Log
import com.enaboapps.switchify.BuildConfig
import com.enaboapps.switchify.backend.preferences.PreferenceManager
import com.revenuecat.purchases.*
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * This object handles in-app purchases using RevenueCat.
 * It provides functionality for purchasing, restoring purchases,
 * and checking subscription status.
 */
object IAPHandler {
    private const val TAG = "IAPHandler"
    const val ENTITLEMENT = "pro"
    private lateinit var preferenceManager: PreferenceManager

    // StateFlow to observe purchase state changes
    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Initial)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState

    /**
     * Represents different states of the purchase process
     */
    sealed class PurchaseState {
        object Initial : PurchaseState()
        object Success : PurchaseState()
        object Error : PurchaseState()
    }

    /**
     * Initializes the IAP handler.
     * This method should be called when the app starts.
     *
     * @param context The application context.
     * @param debugLogsEnabled Enable debug logs for RevenueCat
     */
    fun initialize(context: Context, debugLogsEnabled: Boolean = BuildConfig.DEBUG) {
        val config = PurchasesConfiguration.Builder(context, BuildConfig.REVENUECAT_PUBLIC_KEY)
            .apply {
                if (debugLogsEnabled) {
                    diagnosticsEnabled(true)
                }
            }
            .build()

        Purchases.configure(config)
        preferenceManager = PreferenceManager(context)

        // Fetch initial customer info
        refreshPurchaseStatus()

        Log.d(TAG, "Initialized IAP handler")
    }

    /**
     * Refreshes the current purchase status
     */
    fun refreshPurchaseStatus() {
        Purchases.sharedInstance.getCustomerInfo(
            object : ReceiveCustomerInfoCallback {
                override fun onError(error: PurchasesError) {
                    Log.e(TAG, "Error refreshing status: ${error.message}")
                    _purchaseState.value = PurchaseState.Error
                }

                override fun onReceived(customerInfo: CustomerInfo) {
                    handlePurchaseSuccess(customerInfo)
                }
            }
        )
    }

    /**
     * Handles successful purchase/restore
     *
     * @param customerInfo Customer info from RevenueCat
     */
    private fun handlePurchaseSuccess(customerInfo: CustomerInfo) {
        val hasPro = customerInfo.entitlements[ENTITLEMENT]?.isActive == true
        setProStatus(hasPro)
        _purchaseState.value = if (hasPro) PurchaseState.Success else PurchaseState.Initial
        Log.d(TAG, "Pro status updated: $hasPro")
    }

    /**
     * Checks if the user has purchased the pro version
     *
     * @return True if the user has purchased pro, false otherwise
     */
    fun hasPurchasedPro(): Boolean {
        return preferenceManager.getBooleanValue(PreferenceManager.PREFERENCE_KEY_PRO)
    }

    /**
     * Sets the pro status in local storage
     *
     * @param status The status to set
     */
    private fun setProStatus(status: Boolean) {
        preferenceManager.setBooleanValue(PreferenceManager.PREFERENCE_KEY_PRO, status)
    }
}