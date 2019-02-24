package io.pleo.antaeus.core.services.billing

class BillingServiceObserver {

    companion object {
        const val OBSERVER_NAME = "MOCK_OBSERVER"
    }

    var success: Boolean? = null

    fun notify(success: Boolean?) {
        this.success = success!!
    }
}