package io.pleo.antaeus.core.services.billing

import io.pleo.antaeus.core.async.AsyncJob
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.JobExecutionContext

/*
    This is a billing job. It references the billing service to execute the billing.
 */
class BillingJob : AsyncJob {

    override fun execute(context: JobExecutionContext?) {
        val schedulerContext = context?.scheduler?.context
        val billingService = schedulerContext?.get(BillingService.SERVICE_NAME) as BillingService
        billingService.execute()
    }

    override fun getJobDetail(): JobDetail {
        return JobBuilder.newJob(this::class.java).build()
    }

}