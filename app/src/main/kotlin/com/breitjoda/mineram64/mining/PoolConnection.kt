package com.breitjoda.mineram64.mining

import com.breitjoda.mineram64.model.CoinType
import com.breitjoda.mineram64.model.MiningJob
import com.breitjoda.mineram64.model.PoolMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlin.random.Random

class PoolConnection(
    private val poolUrl: String,
    private val walletAddress: String,
    private val coinType: CoinType,
    private val onJobReceived: (MiningJob) -> Unit,
    private val onConnected: () -> Unit,
    private val onDisconnected: () -> Unit
) {
    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isConnected = false
    private var requestId = 0

    fun connect() {
        scope.launch {
            try {
                val (host, port) = parsePoolUrl(poolUrl)
                socket = Socket(host, port)
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                writer = PrintWriter(socket!!.getOutputStream(), true)

                isConnected = true
                onConnected()

                subscribe()
                listen()
            } catch (e: Exception) {
                e.printStackTrace()
                isConnected = false
                onDisconnected()
            }
        }
    }

    fun disconnect() {
        isConnected = false
        try {
            socket?.close()
            reader?.close()
            writer?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun submitShare(jobId: String, nonce: String) {
        scope.launch {
            try {
                val extraNonce2 = Random.nextBytes(4).joinToString("") { "%02x".format(it) }
                val params = listOf(
                    "worker",
                    jobId,
                    extraNonce2,
                    System.currentTimeMillis().toString(),
                    nonce
                )
                sendRequest("mining.submit", params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun subscribe() {
        sendRequest("mining.subscribe", listOf("SoloMiner/1.0.0"))
        sendRequest("mining.authorize", listOf(walletAddress, ""))
    }

    private fun sendRequest(method: String, params: List<String>) {
        try {
            val request = PoolMessage(
                id = ++requestId,
                method = method,
                params = params
            )
            val json = gson.toJson(request) + "\n"
            writer?.print(json)
            writer?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun listen() {
        while (isConnected) {
            try {
                val line = reader?.readLine() ?: break
                if (line.isEmpty()) continue

                val message = gson.fromJson(line, PoolMessage::class.java)

                when (message.method) {
                    "mining.notify" -> {
                        val job = MiningJob(
                            jobId = "job_${System.currentTimeMillis()}",
                            jobData = "",
                            target = "0000ffff",
                            extraNonce1 = "00000000",
                            extraNonce2Size = 4
                        )
                        onJobReceived(job)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                break
            }
        }
        isConnected = false
        onDisconnected()
    }

    private fun parsePoolUrl(url: String): Pair<String, Int> {
        val parts = url.split(":")
        val host = parts.getOrNull(0) ?: "localhost"
        val port = parts.getOrNull(1)?.toIntOrNull() ?: 3333
        return Pair(host, port)
    }
}
