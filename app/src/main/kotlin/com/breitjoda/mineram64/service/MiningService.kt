package com.breitjoda.mineram64.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.breitjoda.mineram64.R
import com.breitjoda.mineram64.mining.MiningEngine
import com.breitjoda.mineram64.mining.PoolConnection
import com.breitjoda.mineram64.model.CoinType
import com.breitjoda.mineram64.model.MiningStats
import com.breitjoda.mineram64.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class MiningService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var miningEngine: MiningEngine? = null
    private var poolConnection: PoolConnection? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentStats: MiningStats? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val coinType = try {
            intent?.getSerializableExtra("coin_type") as? CoinType ?: CoinType.SOL
        } catch (e: Exception) {
            CoinType.SOL
        }
        val walletAddress = intent?.getStringExtra("wallet_address") ?: ""
        val cpuThreads = intent?.getIntExtra("cpu_threads", 4) ?: 4
        val poolUrl = coinType.poolUrl

        startMining(coinType, walletAddress, cpuThreads, poolUrl)
        return START_STICKY
    }

    private fun startMining(
        coinType: CoinType,
        walletAddress: String,
        cpuThreads: Int,
        poolUrl: String
    ) {
        miningEngine = MiningEngine(coinType) { stats ->
            currentStats = stats
            updateNotification(stats)
        }

        poolConnection = PoolConnection(
            poolUrl = poolUrl,
            walletAddress = walletAddress,
            coinType = coinType,
            onJobReceived = { job ->
                // Handle new mining job
            },
            onConnected = {
                // Pool connected
            },
            onDisconnected = {
                // Pool disconnected
            }
        )

        miningEngine?.start(cpuThreads)
        poolConnection?.connect()
    }

    private fun updateNotification(stats: MiningStats) {
        val notification = createNotification(
            title = "Mining Active - ${stats.coinType}",
            text = "Rate: %.2f H/s | Shares: %d".format(stats.hashRate, stats.sharesAccepted)
        )
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotification(title: String, text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mining Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SoloMiner::MiningWakeLock"
        ).apply {
            acquire()
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        wakeLock = null
    }

    override fun onDestroy() {
        miningEngine?.stop()
        poolConnection?.disconnect()
        releaseWakeLock()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "mining_service_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
