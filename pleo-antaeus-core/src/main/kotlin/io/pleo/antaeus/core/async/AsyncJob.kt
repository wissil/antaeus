package io.pleo.antaeus.core.async

import org.quartz.Job
import org.quartz.JobDetail

interface AsyncJob: Job {

    fun getJobDetail(): JobDetail
}