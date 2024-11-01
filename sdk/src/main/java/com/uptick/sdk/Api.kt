package com.uptick.sdk

import com.uptick.sdk.model.UptickResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface Api {

    @GET("v1/places/{integration_id}/flows/new")
    suspend fun newFlow(
        @Path("integration_id") id: String,
        @Query("placement") placement: String,
        @QueryMap options: Map<String, String>
    ): Response<UptickResponse>

   /* @GET("v1/places/{integration_id}/flows/{flow_id}/offers/new")
    suspend fun nextOffer(
        @Path("integration_id") id: String,
        @Path("flow_id") flowId: String,
        @Query("placement") placement: String,
        @Query("ev") event: String = "offer_viewed",
        @QueryMap options: Map<String, String>
    ): Response<UptickResponse>*/


    @GET
    suspend fun nextOffer(
        @Url url: String,
        @Query("placement") placement: String,
        @Query("ev") event: String = "offer_viewed",
        @QueryMap options: Map<String, String>
    ): Response<UptickResponse>
}