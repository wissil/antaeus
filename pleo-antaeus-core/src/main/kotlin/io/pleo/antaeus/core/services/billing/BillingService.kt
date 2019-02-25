package io.pleo.antaeus.core.services.billing

import io.pleo.antaeus.core.async.AsyncService
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import org.quartz.*

private val logger = KotlinLogging.logger {}

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService
): AsyncService(
        // set job
        JobBuilder.newJob(BillingJob::class.java).build(),

        // set trigger
        TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(
                        CronScheduleBuilder.
                                monthlyOnDayAndHourAndMinute(1, 0, 0))
                .build()) {

    companion object {
        const val SERVICE_NAME = "BILLING_SERVICE"
    }


    fun execute() {
        logger.info("Billing execution started ...")

        invoiceService.fetchAllWithStatus(InvoiceStatus.PENDING).forEach {
            logger.info("Charging invoice with ID=${it.id} ...")
            try {
                if (paymentProvider.charge(it)) {
                    invoiceService.markInvoiceAsPaid(it)
                    logger.info("Invoice with ID= ${it.id} successfully charged!")
                } else {
                    logger.warn("Invoice with ID=${it.id} couldn't be charged. Customer balance not sufficient.")
                }
            } catch (e: CustomerNotFoundException) {

            } catch (e: CurrencyMismatchException) {

            } catch (e: NetworkException) {

            }

        }

        logger.info("Billing successfully executed!")
    }

    override fun getServiceName(): String {
        return SERVICE_NAME
    }
}