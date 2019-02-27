package io.pleo.antaeus.core.services

import io.mockk.every
import org.junit.jupiter.api.Test
import io.mockk.mockk
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.billing.BillingJob
import io.pleo.antaeus.core.services.billing.BillingService
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.data.CustomerTable
import io.pleo.antaeus.data.InvoiceTable
import io.pleo.antaeus.models.InvoiceStatus
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import insertInitialData
import io.pleo.antaeus.core.network.RetryService
import io.pleo.antaeus.core.services.billing.InvoicePaymentExecutor
import org.junit.jupiter.api.Assertions.assertEquals
import java.sql.Connection


class BillingServiceTest {

    /*
        Mock instance of a payment provider. Returns true for every charge attempt.
     */
    private val paymentProvider = mockk<PaymentProvider> {
        every { charge(any()) } returns true
    }

    private val retryService = mockk<RetryService> {}

    private val supportService = mockk<SupportService> {}

    companion object {
        // The tables to create in the database.
        private val tables = arrayOf(InvoiceTable, CustomerTable)

        // Connect to the database and create the needed tables. Drop any existing data.
        private val db = Database
                .connect("jdbc:sqlite:/tmp/data.db", "org.sqlite.JDBC")
                .also {
                    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
                    transaction(it) {
                        addLogger(StdOutSqlLogger)
                        // Drop all existing tables to ensure a clean slate on each run
                        SchemaUtils.drop(*tables)
                        // Create all tables
                        SchemaUtils.create(*tables)
                    }
                }

        private val antaeusDal = AntaeusDal(db)

        @JvmStatic
        @BeforeAll
        internal fun beforeAll() {
            // Insert example data in the database before test execution
            insertInitialData(antaeusDal)
        }

        @JvmStatic
        @AfterAll
        fun dropTables() {
            // Drop example data after all test execute
            transaction(db) {
                SchemaUtils.drop(*tables)
            }
        }
    }

    private val invoiceService = InvoiceService(dal = antaeusDal)
    private val invoicePaymentExecutor =
            InvoicePaymentExecutor(paymentProvider = paymentProvider, retryService = retryService, supportService = supportService)

    private val billingService = BillingService(BillingJob(), createTrigger(), invoiceService, invoicePaymentExecutor)

    @Test
    fun `should update all pending invoices to paid`() {
        // assert there are some pending invoices
        assert(!invoiceService.fetchAllWithStatus(InvoiceStatus.PENDING).isEmpty())

        billingService.execute()

        // assert every invoice is paid
        invoiceService.fetchAll().forEach { assertEquals(InvoiceStatus.PAID, it.status) }
    }

    private fun createTrigger(): Trigger {
        val intervalInSeconds = 2

        return TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(
                        SimpleScheduleBuilder
                                .simpleSchedule()
                                .withIntervalInSeconds(intervalInSeconds)
                                .repeatForever())
                .build()
    }
}