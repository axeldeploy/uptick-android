package com.uptick.sdk.model

data class UptickItem(
    val id: String, val type: String,
    val attributes: OfferData?,
    val personalization:Boolean?
)
