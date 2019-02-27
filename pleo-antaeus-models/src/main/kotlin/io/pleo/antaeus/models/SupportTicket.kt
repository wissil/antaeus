package io.pleo.antaeus.models

data class SupportTicket(
        val description: String,
        val data: Map<String, Any>? = null
)