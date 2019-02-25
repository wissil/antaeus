package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.billing.*
import io.pleo.antaeus.models.Invoice
import org.junit.jupiter.api.Test
import org.quartz.*
import java.lang.Thread.sleep
import kotlin.random.Random


class BillingServiceTest {


    @Test
    fun `verify that the billing service runs asynchronously`() {
        // register a new context job observer to the job
        // this will help us get the return value from the async job
        val observer = BillingServiceObserver()

        // create the billing service
        val billingService = MockBillingService(createMockBillingJob(observer), createMockTrigger())

        // run the service asynchronously
        billingService.runAsync()

        var billingSuccess = false

        // run the main thread
        while (true) {
            println("...")
            sleep(500)
            if (billingSuccess) break

            billingSuccess = observer.success!!
        }

        // assert that async service returned
        assert(billingSuccess)
    }

    private fun createMockBillingJob(observer: BillingServiceObserver): JobDetail {
        val data = JobDataMap()
        data[BillingServiceObserver.OBSERVER_NAME] = observer
        return JobBuilder.newJob(MockBillingJob::class.java).setJobData(data).build()
    }

    private fun createMockTrigger(): Trigger {
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

    // This is the mocked instance of the payment provider
    internal fun getPaymentProvider(): PaymentProvider {
        return object : PaymentProvider {
            override fun charge(invoice: Invoice): Boolean {
                return Random.nextBoolean()
            }
        }
    }
}