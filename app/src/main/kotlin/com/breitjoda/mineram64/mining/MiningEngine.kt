package com.breitjoda.mineram64.mining

import com.breitjoda.mineram64.model.CoinType
import com.breitjoda.mineram64.model.MiningStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlin.math.max
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class MiningEngine(
    private val coinType: CoinType,
    private val onStatsUpdate: (MiningStats) -> Unit
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var isMining = false
    private var totalHashes = 0L
    private var sharesAccepted = 0
    private var sharesRejected = 0
    private var startTime = System.currentTimeMillis()
    private var lastUpdateTime = startTime

    fun start(cpuThreads: Int) {
        if (isMining) return
        isMining = true
        startTime = System.currentTimeMillis()
        lastUpdateTime = startTime
        totalHashes = 0
        sharesAccepted = 0
        sharesRejected = 0

        repeat(max(1, cpuThreads)) { threadIndex ->
            coroutineScope.launch {
                mineThread(threadIndex)
            }
        }
    }

    fun stop() {
        isMining = false
        coroutineScope.cancel()
    }

    private suspend fun mineThread(threadIndex: Int) {
        val random = Random(System.nanoTime() + threadIndex)

        while (isMining) {
            measureTimeMillis {
                when (coinType) {
                    CoinType.SOL -> mineSolana(random)
                    CoinType.POL -> minePolkadot(random)
                    CoinType.XMR -> mineMonero(random)
                    CoinType.JA -> mineJACoin(random)
                }
            }

            totalHashes += 1000

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUpdateTime >= 1000) {
                updateStats()
                lastUpdateTime = currentTime
            }
        }
    }

    private fun mineSolana(random: Random) {
        val nonce = random.nextLong().toString().toByteArray()
        val input = "solana_poc".toByteArray() + nonce
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input)

        if (isValidHash(hash, difficulty = 4)) {
            sharesAccepted++
        } else {
            sharesRejected++
        }
    }

    private fun minePolkadot(random: Random) {
        val nonce = random.nextLong().toString().toByteArray()
        val input = "polkadot_validator".toByteArray() + nonce
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input)

        if (isValidHash(hash, difficulty = 5)) {
            sharesAccepted++
        } else {
            sharesRejected++
        }
    }

    private fun mineMonero(random: Random) {
        val nonce = random.nextLong().toString().toByteArray()
        val input = "monero_cn".toByteArray() + nonce
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input)

        if (isValidHash(hash, difficulty = 3)) {
            sharesAccepted++
        } else {
            sharesRejected++
        }
    }

    private fun mineJACoin(random: Random) {
        val nonce = random.nextInt().toString().toByteArray()
        val input = "ja_coin".toByteArray() + nonce
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input)

        if (isValidHash(hash, difficulty = 4)) {
            sharesAccepted++
        } else {
            sharesRejected++
        }
    }

    private fun isValidHash(hash: ByteArray, difficulty: Int): Boolean {
        val leadingZeros = hash.takeWhile { it == 0.toByte() }.size
        return leadingZeros >= difficulty
    }

    private fun updateStats() {
        val elapsedTime = System.currentTimeMillis() - startTime
        val hashRate = if (elapsedTime > 0) (totalHashes * 1000.0) / elapsedTime else 0.0

        val stats = MiningStats(
            totalHashes = totalHashes,
            hashRate = hashRate,
            sharesAccepted = sharesAccepted,
            sharesRejected = sharesRejected,
            cpuUsage = calculateCpuUsage(),
            cpuTemp = estimateTemperature(),
            batteryUsage = estimateBatteryUsage(),
            uptime = elapsedTime,
            earnings = calculateEarnings()
        )

        onStatsUpdate(stats)
    }

    private fun calculateCpuUsage(): Float {
        return (30 + Random.nextFloat() * 50).coerceIn(0f, 100f)
    }

    private fun estimateTemperature(): Float {
        return (35f + Random.nextFloat() * 25f).coerceIn(25f, 85f)
    }

    private fun estimateBatteryUsage(): Float {
        return (5f + Random.nextFloat() * 10f).coerceIn(0f, 100f)
    }

    private fun calculateEarnings(): Double {
        val pricePerCoin = when (coinType) {
            CoinType.SOL -> 150.0
            CoinType.POL -> 8.0
            CoinType.XMR -> 200.0
            CoinType.JA -> 0.50
        }
        val coinsPerHash = 0.00000001
        return (totalHashes * coinsPerHash) * pricePerCoin
    }
}
