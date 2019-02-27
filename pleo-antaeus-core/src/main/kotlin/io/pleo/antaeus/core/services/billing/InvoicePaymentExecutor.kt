package io.pleo.antaeus.core.services.billing

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/*
    This is the Invoice Payment Executor.
    This class is responsible for executing the payment of invoices.
 */
class InvoicePaymentExecutor(
        private val paymentProvider: PaymentProvider
) {

    /*
        Executes payment of a single `invoice`.
     */
    fun executeInvoicePayment(invoice: Invoice): Boolean {
        logger.info("Charging invoice with ID=${invoice.id} ...")
        try {
            return paymentProvider.charge(invoice)
        } catch (e: CustomerNotFoundException) {
            logger.warn("Customer with ID=${invoice.customerId} not found.")

        } catch (e: CurrencyMismatchException) {
            logger.warn("Currency of invoice with ID=${invoice.id} doesn't match currency of customer with ID=${invoice.customerId}")

        } catch (e: NetworkException) {
            logger.warn("Network error has occurred while attempting to charge invoice with ID=${invoice.id}")
        }

        return false
    }
}