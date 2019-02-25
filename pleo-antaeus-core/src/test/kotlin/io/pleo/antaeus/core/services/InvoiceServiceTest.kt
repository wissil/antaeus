package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InvoiceServiceTest {

    private val pendingInvoices = mockk<List<Invoice>>() {
        every { isEmpty() } returns false
    }

    private val paidInvoices = mockk<List<Invoice>>() {
        every { isEmpty() } returns false
    }

    private val dal = mockk<AntaeusDal> {
        every { fetchInvoice(404) } returns null
        every { fetchInvoicesWithStatus(InvoiceStatus.PENDING) } returns pendingInvoices
        every { fetchInvoicesWithStatus(InvoiceStatus.PAID) } returns paidInvoices
    }

    private val invoiceService = InvoiceService(dal = dal)

    @Test
    fun `will throw if customer is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun `will return non-empty pending invoices`() {
        assert(invoiceService.fetchAllWithStatus(InvoiceStatus.PENDING).isNotEmpty())
    }

    @Test
    fun `will return non-empty paid invoices`() {
        assert(invoiceService.fetchAllWithStatus(InvoiceStatus.PAID).isNotEmpty())
    }
}