package com.breitjoda.mineram64.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.breitjoda.mineram64.model.CoinType
import com.breitjoda.mineram64.model.MiningStats
import com.breitjoda.mineram64.service.MiningService

@Composable
fun MainScreen() {
    var selectedCoin by remember { mutableStateOf(CoinType.SOL) }
    var walletAddress by remember { mutableStateOf("") }
    var isMining by remember { mutableStateOf(false) }
    var cpuThreads by remember { mutableStateOf(4f) }
    var miningStats by remember { mutableStateOf(MiningStats()) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solo Miner ARM64/CPU", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Coin Selection
            Text(
                "Select Coin",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CoinType.values().forEach { coin ->
                    FilterChip(
                        selected = selectedCoin == coin,
                        onClick = { selectedCoin = coin },
                        label = {
                            Text(
                                coin.displayName,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Divider()

            // Wallet Address Input
            Text(
                "Wallet Address",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = walletAddress,
                onValueChange = { walletAddress = it },
                label = { Text("Enter wallet address") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                singleLine = false
            )

            Divider()

            // CPU Threads Configuration
            Text(
                "CPU Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CPU Threads:", fontWeight = FontWeight.Medium)
                        Text(
                            cpuThreads.toInt().toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = cpuThreads,
                        onValueChange = { cpuThreads = it },
                        valueRange = 1f..8f,
                        steps = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Divider()

            // Mining Stats Display
            if (isMining) {
                Text(
                    "Mining Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatsRow("Hash Rate", "%.2f H/s".format(miningStats.hashRate))
                        StatsRow("Total Hashes", formatNumber(miningStats.totalHashes))
                        StatsRow("Shares Accepted", miningStats.sharesAccepted.toString())
                        StatsRow("Shares Rejected", miningStats.sharesRejected.toString())
                        Divider()
                        StatsRow("CPU Usage", "%.1f%%".format(miningStats.cpuUsage))
                        StatsRow("CPU Temp", "%.0f°C".format(miningStats.cpuTemp))
                        StatsRow("Battery Usage", "%.1f%%".format(miningStats.batteryUsage))
                        Divider()
                        StatsRow("Uptime", formatUptime(miningStats.uptime))
                        StatsRow("Earnings", "\$%.6f".format(miningStats.earnings))
                    }
                }
            }

            // Control Buttons
            Button(
                onClick = {
                    if (!isMining && walletAddress.isEmpty()) {
                        return@Button
                    }
                    isMining = !isMining
                    if (isMining) {
                        val intent = Intent(context, MiningService::class.java).apply {
                            putExtra("coin_type", selectedCoin)
                            putExtra("wallet_address", walletAddress)
                            putExtra("cpu_threads", cpuThreads.toInt())
                        }
                        context.startService(intent)
                    } else {
                        context.stopService(Intent(context, MiningService::class.java))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMining) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    if (isMining) "Stop Mining" else "Start Mining",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

fun formatUptime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = millis / (1000 * 60 * 60)
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

fun formatNumber(number: Long): String {
    return when {
        number >= 1_000_000_000 -> "%.2f B".format(number / 1_000_000_000.0)
        number >= 1_000_000 -> "%.2f M".format(number / 1_000_000.0)
        number >= 1_000 -> "%.2f K".format(number / 1_000.0)
        else -> number.toString()
    }
}
