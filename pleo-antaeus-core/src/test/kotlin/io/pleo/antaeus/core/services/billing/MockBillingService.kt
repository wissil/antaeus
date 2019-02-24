package io.pleo.antaeus.core.services.billing

import io.pleo.antaeus.core.async.AsyncService
import org.quartz.JobDetail
import org.quartz.Trigger

class MockBillingService(
        job: JobDetail,
        trigger: Trigger
): AsyncService(job, trigger) {

    companion object {
        const val SERVICE_NAME = "MOCK_BILLING_SERVICE"
    }

    fun execute(): Boolean {
        return true
    }

    override fun getServiceName(): String {
        return SERVICE_NAME
    }

}