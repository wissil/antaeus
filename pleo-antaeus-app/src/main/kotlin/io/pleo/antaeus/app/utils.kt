
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.network.RetryService
import io.pleo.antaeus.core.services.SupportService
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.*
import org.quartz.*
import java.math.BigDecimal
import kotlin.random.Random

// This will create all schemas and setup initial data
internal fun setupInitialData(dal: AntaeusDal) {
    val customers = (1..100).mapNotNull {
        dal.createCustomer(
            currency = Currency.values()[Random.nextInt(0, Currency.values().size)]
        )
    }

    customers.forEach { customer ->
        (1..10).forEach {
            dal.createInvoice(
                amount = Money(
                    value = BigDecimal(Random.nextDouble(10.0, 500.0)),
                    currency = customer.currency
                ),
                customer = customer,
                status = if (it == 1) InvoiceStatus.PENDING else InvoiceStatus.PAID
            )
        }
    }
}

// This is a trigger that runs on every 1st of the month at midnight
internal fun getTrigger(): Trigger {
    return TriggerBuilder
            .newTrigger()
            .startNow()
            .withSchedule(CronScheduleBuilder
                    .monthlyOnDayAndHourAndMinute(1, 0, 0))
            .build()
}

// This is a trigger that runs every 15 seconds; used only for debugging purposes on the localhost
internal fun getDebugTrigger(): Trigger {
    return TriggerBuilder
            .newTrigger()
            .startNow()
            .withSchedule(SimpleScheduleBuilder
                    .simpleSchedule()
                    .withIntervalInSeconds(15)
                    .repeatForever())
            .build()
}

// This is the mocked instance of the payment provider
internal fun getPaymentProvider(): PaymentProvider {
    return object : PaymentProvider {
        override fun charge(invoice: Invoice): Boolean {
                return Random.nextBoolean()
        }
    }
}

// This is the mocked instance of the support service
internal fun getSupportService(): SupportService {
    return object : SupportService {
        override fun raiseTicket(supportTicket: SupportTicket) {
            // do nothing
        }

    }
}