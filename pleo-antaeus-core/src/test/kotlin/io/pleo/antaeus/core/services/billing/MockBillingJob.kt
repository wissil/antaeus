package io.pleo.antaeus.core.services.billing

import org.quartz.Job
import org.quartz.JobExecutionContext

class MockBillingJob: Job {

    override fun execute(context: JobExecutionContext?) {
        val schedulerContext = context?.scheduler?.context
        val billingService = schedulerContext?.get(MockBillingService.SERVICE_NAME) as MockBillingService
        val result = billingService.execute()
        (context.jobDetail.jobDataMap[BillingServiceObserver.OBSERVER_NAME] as BillingServiceObserver).notify(result)
    }
}