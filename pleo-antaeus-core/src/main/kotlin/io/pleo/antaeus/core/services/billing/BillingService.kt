package io.pleo.antaeus.core.services.billing

import io.pleo.antaeus.core.async.AsyncService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import org.quartz.*

private val logger = KotlinLogging.logger {}

class BillingService(
    job: BillingJob,
    trigger: Trigger,
    private val invoiceService: InvoiceService,
    private val invoicePaymentExecutor: InvoicePaymentExecutor
): AsyncService<BillingJob>(job = job, trigger = trigger) {

    companion object {
        const val SERVICE_NAME = "BILLING_SERVICE"
    }


    fun execute() {
        logger.info("Billing execution started ...")

        invoiceService.fetchAllWithStatus(InvoiceStatus.PENDING).forEach {
            logger.info("Charging invoice with ID=${it.id} ...")

            if (invoicePaymentExecutor.executePayment(it)) {
                invoiceService.markInvoiceAsPaid(it)
                logger.info("Invoice with ID=${it.id} successfully charged!")
            } else {
                logger.warn("Invoice with ID=${it.id} couldn't be charged.")
            }
        }

        logger.info("Billing successfully executed!")
    }

    override fun getServiceName(): String {
        return SERVICE_NAME
    }
}