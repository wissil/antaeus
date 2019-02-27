package io.pleo.antaeus.core.network

import java.lang.Thread.sleep


class RetryService {

    /*
        Retries to invoke a given function for `nRetries` times.
        Waits for `waitForRetry` seconds between the tries.
     */
    fun retry(nRetries: Int = 5, waitForRetry: Long = 1000, retryFunction: () -> Boolean): Boolean {
        var success = false

        var attempt = 0
        while (!success and (attempt++ < nRetries)) {
            sleep(waitForRetry)
            success = retryFunction.invoke()
        }

        return success
    }
}