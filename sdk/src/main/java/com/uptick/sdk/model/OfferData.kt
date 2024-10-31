package com.uptick.sdk.model

import java.io.Serializable

data class OfferData(
    val header: List<OfferItem>?,
    val content: List<OfferItem>?,
    val disclaimer: List<OfferItem>?,
    val actions: List<OfferItem>?,
    val footer: List<OfferItem>?,
    val image: UptickImage?,
    val offers: OfferDigits?,
    val sponsored: List<OfferItem>?,
    val personalization: List<OfferItem>?
) : Serializable
