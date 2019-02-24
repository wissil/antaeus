package io.pleo.antaeus.core.services.billing

import org.quartz.Job
import org.quartz.JobExecutionContext

/*
    This is a billing job. It references a billing service to execute the billing.
 */
class BillingJob: Job {

    override fun execute(context: JobExecutionContext?) {
        val schedulerContext = context?.scheduler?.context
        val billingService = schedulerContext?.get(BillingService.SERVICE_NAME) as BillingService
        billingService.execute()
    }

}