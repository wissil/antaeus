package io.pleo.antaeus.data

import io.pleo.antaeus.models.InvoiceStatus
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import setupInitialData
import java.sql.Connection


class AntaeusDalTest {

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
            // Insert example data in the database.
            setupInitialData(antaeusDal)
        }
    }


    @Test
    fun `will return only pending invoices`() {
        val pendingInvoices = antaeusDal.fetchInvoicesWithStatus(InvoiceStatus.PENDING)

        pendingInvoices.forEach {
            assertEquals(InvoiceStatus.PENDING, it.status)
        }
    }

    @Test
    fun `will return only paid invoices`() {
        val pendingInvoices = antaeusDal.fetchInvoicesWithStatus(InvoiceStatus.PAID)

        pendingInvoices.forEach {
            assertEquals(InvoiceStatus.PAID, it.status)
        }
    }

    @Test
    fun `will update status of the invoice`() {
        val pendingInvoices = antaeusDal.fetchInvoicesWithStatus(InvoiceStatus.PENDING)
        println(pendingInvoices.size)
        val invoice = pendingInvoices[0]

        // should be pending at the start
        assert(invoice.status == InvoiceStatus.PENDING)

        // update
        invoice.status = InvoiceStatus.PAID
        val updated = antaeusDal.updateInvoice(invoice)

        assert(updated.status == InvoiceStatus.PAID)
    }

}