package com.squareup.backfila.client

import com.google.common.util.concurrent.ListenableFuture
import com.squareup.protos.backfila.clientservice.GetNextBatchRangeRequest
import com.squareup.protos.backfila.clientservice.GetNextBatchRangeResponse
import com.squareup.protos.backfila.clientservice.PrepareBackfillRequest
import com.squareup.protos.backfila.clientservice.PrepareBackfillResponse
import com.squareup.protos.backfila.clientservice.RunBatchRequest
import com.squareup.protos.backfila.clientservice.RunBatchResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Methods implemented by the client service library that backfila calls out to.
interface BackfilaClientServiceSquareDcApi {
  @POST("$BASE_PATH/prepare_backfill")
  @Headers(value = [
    "Accept: application/x-protobuf",
    "Content-Type: application/x-protobuf"
  ])
  fun prepareBackfill(
    @Body request: PrepareBackfillRequest
  ): Call<PrepareBackfillResponse>

  @POST("$BASE_PATH/get_next_batch_range")
  @Headers(value = [
    "Accept: application/x-protobuf",
    "Content-Type: application/x-protobuf"
  ])
  fun getNextBatchRange(
    @Body request: GetNextBatchRangeRequest
  ): ListenableFuture<GetNextBatchRangeResponse>

  @POST("$BASE_PATH/run_batch")
  @Headers(value = [
    "Accept: application/x-protobuf",
    "Content-Type: application/x-protobuf"
  ])
  fun runBatch(
    @Body request: RunBatchRequest
  ): ListenableFuture<RunBatchResponse>

  // TODO figure out if we're using service container routing or plain http or grpc
  companion object {
    private const val BASE_PATH = "/services/squareup.backfila.clientservice.BackfilaClientService"
  }
}