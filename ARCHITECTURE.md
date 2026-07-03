# Solo Miner ARM64/CPU - Architecture

## Project Structure

```
solo-miner-amr64-cpu/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/breitjoda/mineram64/
│   │   │   ├── model/                 # Data Models
│   │   │   │   ├── CoinType.kt
│   │   │   │   ├── MiningModels.kt
│   │   │   │   ├── PoolProtocol.kt
│   │   │   │   └── Extensions.kt
│   │   │   ├── mining/                # Mining Logic
│   │   │   │   ├── MiningEngine.kt
│   │   │   │   └── PoolConnection.kt
│   │   │   ├── service/               # Android Services
│   │   │   │   └── MiningService.kt
│   │   │   └── ui/                    # UI Layer
│   │   │       ├── MainActivity.kt
│   │   │       ├── MainScreen.kt
│   │   │       └── theme/
│   │   │           ├── Theme.kt
│   │   │           ├── Color.kt
│   │   │           └── Type.kt
│   │   ├── res/
│   │   │   └── values/
│   │   │       ├── strings.xml
│   │   │       └── themes.xml
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Architecture Overview

### 1. Data Layer (Model)
- **CoinType**: Enum für unterstützte Coins (SOL, POL, XMR, JA)
- **MiningModels**: Datenklassen für Konfiguration, Stats, Jobs
- **PoolProtocol**: Stratum-Protokoll Modelle

### 2. Mining Layer
- **MiningEngine**: Hauptmining-Logic mit Multi-Threading
  - Pro Coin: Spezialisierte Hash-Algorithmen
  - CPU-Thread Verwaltung
  - Statistik-Tracking
  - Real-time Stats-Updates

- **PoolConnection**: Mining-Pool Kommunikation
  - Stratum Protocol Implementation
  - Job-Management
  - Share-Submission

### 3. Service Layer
- **MiningService**: Android Foreground Service
  - Hintergrund-Mining
  - Wake Lock Management
  - Notifications
  - Lifecycle Management

### 4. UI Layer (Compose)
- **MainActivity**: Entry Point
- **MainScreen**: Hauptoberfläche mit:
  - Coin-Selection
  - Wallet-Eingabe
  - CPU-Konfiguration
  - Live Statistics Display
  - Start/Stop Controls

- **Theme**: Material Design 3
  - Dark/Light Mode Support
  - Custom Colors
  - Typography

## Mining Flow

```
MainActivity (UI)
    ↓
start MiningService (Intent)
    ↓
MiningService.onStartCommand()
    ↓
MiningEngine.start(cpuThreads)
    ↓
mineThread() x cpuThreads
    ↓
Coin-specific mining (SOL/POL/XMR/JA)
    ↓
Hash validation & Stats update
    ↓
PoolConnection.submitShare()
```

## Key Features

✅ **Multi-Coin Mining**
- Solana (PoH)
- Polkadot (NPoS)
- Monero (CryptoNight)
- JA-Coin (SHA-256)

✅ **ARM64 Optimized**
- Native coroutine-based threading
- Efficient memory usage
- NEON SIMD ready

✅ **Real-time Monitoring**
- Hash rate tracking
- Share statistics
- CPU/Battery usage
- Earnings estimation

✅ **Robust Architecture**
- Clean separation of concerns
- Coroutine-based async operations
- Lifecycle-aware services
- Graceful error handling

## Dependencies

- **Android Core**: androidx.core, androidx.appcompat
- **Jetpack Compose**: UI Framework
- **Coroutines**: kotlinx-coroutines (async operations)
- **Networking**: OkHttp3, Retrofit2 (Pool communication)
- **Cryptography**: Bouncy Castle (SHA-256)
- **JSON**: Gson (Protocol parsing)

## Build & Run

```bash
# Build
./gradlew build

# Install Debug
./gradlew installDebug

# Install Release
./gradlew assembleRelease
```

## Future Enhancements

- [ ] Native C++ Mining (NDK)
- [ ] GPU Mining Support
- [ ] Advanced Pool Protocols
- [ ] Statistics Database
- [ ] Auto-switching Algorithm
- [ ] Web Dashboard
- [ ] Wallet Integration
