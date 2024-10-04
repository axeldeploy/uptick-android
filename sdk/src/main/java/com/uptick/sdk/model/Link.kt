package com.uptick.sdk.model

import com.google.gson.annotations.SerializedName

data class Link(
    val self: String,
    @SerializedName("next_offer") val nextOffer: String?,
    @SerializedName("offer_event") val offerEvent:String?
)
