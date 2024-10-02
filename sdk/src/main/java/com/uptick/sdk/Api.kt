package com.uptick.sdk

import com.uptick.sdk.model.UptickResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {

    @GET("v1/places/{integration_id}/flows/new")
    suspend fun newFlow(
        @Path("integration_id") id: String,
        @Query("placement") placement: String = "order_confirmation"
    ): Response<UptickResponse>

    @GET("v1/places/{integration_id}/flows/{flow_id}/offers/new")
    suspend fun nextOffer(
        @Path("integration_id") id: String,
        @Path("flow_id") flowId: String,
        @Query("placement") placement: String = "order_confirmation"
    ): Response<UptickResponse>
}