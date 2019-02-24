package io.pleo.antaeus.core.async

import org.quartz.JobDetail
import org.quartz.Trigger
import org.quartz.impl.StdSchedulerFactory

/*
    This is an abstract asynchronous service. It takes in a Job and a Trigger which
    define an asynchronous job behaviour.
 */
abstract class AsyncService(
        private val job: JobDetail,
        private val trigger: Trigger
) {

    /*
        Runs the async service. This function should be called exactly once,
        at the service startup. The service will then continue to work in the background,
        executing this service's job according to this service's trigger.
    */
    fun runAsync() {
        // register this billing service to the scheduler context
        val scheduler = StdSchedulerFactory().scheduler
        scheduler.context.putIfAbsent(getServiceName(), this)

        // schedule the job
        scheduler.scheduleJob(job, trigger)

        // start the scheduled service
        scheduler.start()
    }

    /*
        Gets the name of this service.
     */
    abstract fun getServiceName(): String
}