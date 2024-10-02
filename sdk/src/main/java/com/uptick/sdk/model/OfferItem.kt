package com.uptick.sdk.model

import java.io.Serializable

data class OfferItem(
    val type: String, val text: String, val attributes: AttributeText?,
    val children: List<OfferItem>?
):Serializable