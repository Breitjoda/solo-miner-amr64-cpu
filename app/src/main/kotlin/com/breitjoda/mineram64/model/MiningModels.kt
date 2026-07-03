package com.breitjoda.mineram64.model

data class MiningConfig(
    val walletAddress: String = "",
    val coinType: CoinType = CoinType.SOL,
    val poolUrl: String = "",
    val cpuThreads: Int = 4,
    val powerLimit: Int = 80,
    val isActive: Boolean = false
)

data class MiningStats(
    val totalHashes: Long = 0L,
    val hashRate: Double = 0.0,
    val sharesAccepted: Int = 0,
    val sharesRejected: Int = 0,
    val cpuUsage: Float = 0f,
    val cpuTemp: Float = 0f,
    val batteryUsage: Float = 0f,
    val uptime: Long = 0L,
    val earnings: Double = 0.0
)

data class MiningJob(
    val jobId: String,
    val jobData: String,
    val target: String,
    val extraNonce1: String,
    val extraNonce2Size: Int
)

data class ShareSubmission(
    val workerName: String,
    val jobId: String,
    val extranonce2: String,
    val ntime: String,
    val nonce: String
)
