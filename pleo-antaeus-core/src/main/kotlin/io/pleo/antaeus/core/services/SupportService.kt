package io.pleo.antaeus.core.services

import io.pleo.antaeus.models.SupportTicket

/*
    This is a Support Service.

    It is responsible to communicate to the support team at pleo.io,
    whenever any administrative error occurs that the service can't handle by itself.
 */
interface SupportService {

    fun raiseTicket(supportTicket: SupportTicket)
}