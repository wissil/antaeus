package io.pleo.antaeus.core.services.billing

import io.pleo.antaeus.core.async.AsyncService
import io.pleo.antaeus.core.external.PaymentProvider
import mu.KotlinLogging
import org.quartz.*

private val logger = KotlinLogging.logger {}

class BillingService(
    private val paymentProvider: PaymentProvider
): AsyncService(
        // set job
        JobBuilder.newJob(BillingJob::class.java).build(),

        // set trigger
        TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(
                        CronScheduleBuilder
                                .monthlyOnDayAndHourAndMinute(1, 0, 0))
                .build()) {

    companion object {
        const val SERVICE_NAME = "BILLING_SERVICE"
    }


    fun execute() {
        logger.info("Billing execution started ...")

        logger.info("Billing successfully executed!")
    }

    override fun getServiceName(): String {
        return SERVICE_NAME
    }
}