package com.breitjoda.mineram64.model

data class PoolMessage(
    val id: Int? = null,
    val method: String? = null,
    val params: List<Any>? = null,
    val result: Any? = null,
    val error: String? = null
)

data class StratumJob(
    val jobId: String,
    val prevhash: String,
    val coinbase1: String,
    val coinbase2: String,
    val merkle: List<String>,
    val version: String,
    val nbits: String,
    val ntime: String,
    val cleanJobs: Boolean
)
