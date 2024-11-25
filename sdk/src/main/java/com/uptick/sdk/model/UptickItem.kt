package com.uptick.sdk.model

import com.google.gson.annotations.SerializedName

data class UptickItem(
    val id: String, val type: String,
    val attributes: OfferData?,
    val personalization:Boolean?,
    @SerializedName("highlight_color") val highlightColor: String?,
    @SerializedName("render_type") val renderType: String?,
    @SerializedName("render_x") val renderX: Boolean?,
)
