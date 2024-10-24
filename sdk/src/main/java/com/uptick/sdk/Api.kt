package com.uptick.sdk

import com.uptick.sdk.model.UptickResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface Api {

    @GET("v1/places/{integration_id}/flows/new")
    suspend fun newFlow(
        @Path("integration_id") id: String,
        @Query("placement") placement: String,
        @Query("first_name") firstName: String? = null,
        @Query("country_code") countryCode: String? = null,
        @Query("total_price") totalPrice: String? = null,
        @Query("shipping_price") shippingPrice: String? = null
    ): Response<UptickResponse>

    @GET("v1/places/{integration_id}/flows/{flow_id}/offers/new")
    suspend fun nextOffer(
        @Path("integration_id") id: String,
        @Path("flow_id") flowId: String,
        @Query("placement") placement: String,
        @Query("ev") event: String = "offer_viewed"
    ): Response<UptickResponse>

    @GET
    suspend fun offerEvent(
        @Url url: String,
        @Query("ev") event: String = "offer_viewed"
    ): Response<ResponseBody>
}