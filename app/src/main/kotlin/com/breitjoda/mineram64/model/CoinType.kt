package com.breitjoda.mineram64.model

enum class CoinType(val displayName: String, val symbol: String) {
    SOL("Solana", "SOL"),
    POL("Polkadot", "POL"),
    XMR("Monero", "XMR"),
    JA("JA-Coin", "JA");

    val poolUrl: String
        get() = when (this) {
            SOL -> "stratum.mining.pool:3333"
            POL -> "polkadot.mining.pool:3334"
            XMR -> "monero.mining.pool:3335"
            JA -> "jacoin.mining.pool:3336"
        }

    val algorithm: String
        get() = when (this) {
            SOL -> "PoH"
            POL -> "NPoS"
            XMR -> "CryptoNight"
            JA -> "SHA-256"
        }
}
