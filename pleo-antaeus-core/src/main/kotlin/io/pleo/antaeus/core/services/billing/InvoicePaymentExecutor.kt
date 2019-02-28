package io.pleo.antaeus.core.services.billing

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.network.RetryService
import io.pleo.antaeus.core.services.SupportService
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.SupportTicket
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/*
    This is the Invoice Payment Executor.
    This class is responsible for executing the payment of invoices.

    It also acts as a wrapper around the `PaymentProvider` taking care of the fail scenarios that the
    `PaymentProvider` might cause.
 */
class InvoicePaymentExecutor(
        private val paymentProvider: PaymentProvider,
        private val supportService: SupportService,
        private val retryService: RetryService
) {

    /*
        Executes payment of a single `invoice`.

        Returns:
          `True` when the customer account was successfully charged the given amount.
          `False` when the customer account could not be charged.
     */
    fun executePayment(invoice: Invoice): Boolean {
        try {
            return chargeInvoice(invoice)
        } catch (e: CustomerNotFoundException) {
            logger.warn("Customer with ID=${invoice.customerId} not found.", e)
            supportService.raiseTicket(SupportTicket("Customer with ID=${invoice.customerId} not found."))
        } catch (e: CurrencyMismatchException) {
            logger.warn("Currency of invoice with ID=${invoice.id} doesn't match currency of customer with ID=${invoice.customerId}", e)
            supportService.raiseTicket(SupportTicket("Currency of invoice with ID=${invoice.id} doesn't match currency of customer with ID=${invoice.customerId}"))
        } catch (e: NetworkException) {
            logger.warn("Network error has occurred while attempting to charge invoice with ID=${invoice.id}", e)
            if (!retryService.retry { paymentProvider.charge(invoice) }) {
                supportService.raiseTicket(SupportTicket("Network error has occurred while attempting to charge invoice with ID=${invoice.id}"))
            } else return true
        }

        return false
    }


    private fun chargeInvoice(invoice: Invoice): Boolean {
        return if (paymentProvider.charge(invoice)) {
            true
        } else {
            logger.warn("Invoice with ID=${invoice.id} couldn't be charged. Customer balance not sufficient.")
            supportService.raiseTicket(SupportTicket("Invoice with ID=${invoice.id} couldn't be charged. Customer balance not sufficient."))
            false
        }
    }
}