package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.data.CarModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameEngine(
    var car: CarModel,
    var mode: GameMode = GameMode.ENDLESS,
    var weatherTheme: WeatherTheme = WeatherTheme.NEON_NIGHT,
    private val soundManager: SoundManager? = null
) {
    var isGameOver = false
        private set
    var isPaused = false
        private set
    var isStarted = false
        private set

    // Lane definitions: 4 lanes across road normalized from 0.0f to 1.0f
    // Lane 0: 0.125f, Lane 1: 0.375f, Lane 2: 0.625f, Lane 3: 0.875f
    val laneCenters = floatArrayOf(0.125f, 0.375f, 0.625f, 0.875f)

    var playerLane: Int = 1
    var playerX: Float = laneCenters[1]
    var playerTargetX: Float = laneCenters[1]
    val playerY: Float = 0.78f // player fixed vertical position

    var currentSpeedKmh: Float = 110f
    var targetSpeedKmh: Float = 160f
    var maxSpeedReachedKmh: Int = 110

    var nitroGauge: Float = 0.6f // 0f to 1f
    var isNitroActive: Boolean = false
    var isBraking: Boolean = false

    var isShieldActive: Boolean = false
    var magnetTimerSec: Float = 0f

    var lives: Int = 3
    var distanceMeters: Float = 0f
    var score: Float = 0f
    var coinsEarned: Int = 0
    var nearMissesCount: Int = 0
    var combo: Float = 1.0f
    var comboStreakTimer: Float = 0f

    // Time Attack mode timer
    var timeRemainingSec: Float = 45f

    // Road scroll
    var roadScrollOffset: Float = 0f

    // Screen Shake
    var screenShake: Float = 0f

    // Entities
    val trafficVehicles = mutableListOf<TrafficVehicle>()
    val collectibles = mutableListOf<Collectible>()
    val particles = mutableListOf<Particle>()
    val notices = mutableListOf<FloatingNotice>()

    private var spawnTimer: Float = 0f
    private var pickupSpawnTimer: Float = 0f
    private var nextEntityId: Long = 1L

    private val _engineState = MutableStateFlow(0L)
    val engineState = _engineState.asStateFlow()

    fun start() {
        isStarted = true
        isPaused = false
        isGameOver = false
        resetGame()
    }

    fun togglePause() {
        if (!isGameOver && isStarted) {
            isPaused = !isPaused
        }
    }

    fun resetGame() {
        isGameOver = false
        isPaused = false
        playerLane = 1
        playerX = laneCenters[1]
        playerTargetX = laneCenters[1]
        currentSpeedKmh = 120f
        maxSpeedReachedKmh = 120
        nitroGauge = 0.75f
        isNitroActive = false
        isBraking = false
        isShieldActive = false
        magnetTimerSec = 0f
        lives = if (car.isPolice) 4 else 3
        distanceMeters = 0f
        score = 0f
        coinsEarned = 0
        nearMissesCount = 0
        combo = 1.0f
        comboStreakTimer = 0f
        timeRemainingSec = 45f
        roadScrollOffset = 0f
        screenShake = 0f
        trafficVehicles.clear()
        collectibles.clear()
        particles.clear()
        notices.clear()
        spawnTimer = 0f
        pickupSpawnTimer = 0f
    }

    fun steerLeft() {
        if (isGameOver || isPaused) return
        if (playerLane > 0) {
            playerLane--
            playerTargetX = laneCenters[playerLane]
            soundManager?.playClick()
        }
    }

    fun steerRight() {
        if (isGameOver || isPaused) return
        if (playerLane < 3) {
            playerLane++
            playerTargetX = laneCenters[playerLane]
            soundManager?.playClick()
        }
    }

    fun setDirectTargetX(normalizedX: Float) {
        if (isGameOver || isPaused) return
        playerTargetX = normalizedX.coerceIn(0.08f, 0.92f)
        // Find closest lane
        var closest = 0
        var minDiff = Float.MAX_VALUE
        for (i in laneCenters.indices) {
            val diff = abs(laneCenters[i] - playerTargetX)
            if (diff < minDiff) {
                minDiff = diff
                closest = i
            }
        }
        playerLane = closest
    }

    fun setNitro(active: Boolean) {
        if (isGameOver || isPaused) return
        if (active && nitroGauge > 0.08f) {
            if (!isNitroActive) {
                soundManager?.playNitro()
            }
            isNitroActive = true
        } else {
            isNitroActive = false
        }
    }

    fun setBrake(active: Boolean) {
        if (isGameOver || isPaused) return
        isBraking = active
    }

    fun update(dtSec: Float) {
        if (!isStarted || isPaused || isGameOver) return

        val dt = dtSec.coerceIn(0.001f, 0.05f)

        // Update Time Attack
        if (mode == GameMode.TIME_ATTACK) {
            timeRemainingSec -= dt
            if (timeRemainingSec <= 0f) {
                timeRemainingSec = 0f
                triggerGameOver("Süre Bitti!")
                return
            }
        }

        // Screen shake decay
        if (screenShake > 0f) {
            screenShake = max(0f, screenShake - dt * 5f)
        }

        // Magnet timer
        if (magnetTimerSec > 0f) {
            magnetTimerSec = max(0f, magnetTimerSec - dt)
        }

        // Calculate Target Speed
        val baseCarTopSpeed = car.topSpeedKmh.toFloat()
        val topSpeed = if (isNitroActive) baseCarTopSpeed + 65f else baseCarTopSpeed

        if (isBraking) {
            targetSpeedKmh = 75f
            currentSpeedKmh = max(60f, currentSpeedKmh - dt * 160f)
            // Smoke particles behind wheels when braking at high speed
            if (currentSpeedKmh > 100f && Random.nextFloat() < 0.35f) {
                addSmokeParticle(playerX - 0.05f, playerY + 0.08f)
                addSmokeParticle(playerX + 0.05f, playerY + 0.08f)
            }
        } else if (isNitroActive) {
            if (nitroGauge > 0f) {
                val consumption = dt / car.nitroDurationSec
                nitroGauge = max(0f, nitroGauge - consumption)
                currentSpeedKmh = min(topSpeed, currentSpeedKmh + dt * 140f * car.baseAccel)
                screenShake = min(screenShake + dt * 0.5f, 0.3f)

                // Nitro flame particles shooting backward
                addNitroFlame(playerX - 0.035f, playerY + 0.085f)
                addNitroFlame(playerX + 0.035f, playerY + 0.085f)

                if (nitroGauge <= 0f) {
                    isNitroActive = false
                }
            } else {
                isNitroActive = false
            }
        } else {
            // Normal acceleration
            currentSpeedKmh = min(topSpeed, currentSpeedKmh + dt * 35f * car.baseAccel)
            // Passive slow nitro regen
            nitroGauge = min(1.0f, nitroGauge + dt * 0.03f)
        }

        if (currentSpeedKmh.toInt() > maxSpeedReachedKmh) {
            maxSpeedReachedKmh = currentSpeedKmh.toInt()
        }

        // Smooth steering towards target
        val handlingSpeed = 5.0f * car.handlingFactor
        playerX += (playerTargetX - playerX) * min(1f, dt * handlingSpeed)

        // Road scroll distance
        val speedMps = currentSpeedKmh * (1000f / 3600f)
        val distanceThisFrame = speedMps * dt
        distanceMeters += distanceThisFrame
        roadScrollOffset = (roadScrollOffset + (currentSpeedKmh / 60f) * dt) % 1.0f

        // Score update with combo multiplier
        val speedMultiplier = (currentSpeedKmh / 100f).coerceAtLeast(1.0f)
        val nitroBonus = if (isNitroActive) 2.0f else 1.0f
        score += distanceThisFrame * speedMultiplier * combo * nitroBonus

        // Checkpoint in Time Attack every 500m
        if (mode == GameMode.TIME_ATTACK) {
            val lastMeters = distanceMeters - distanceThisFrame
            if ((distanceMeters.toInt() / 500) > (lastMeters.toInt() / 500)) {
                timeRemainingSec += 12f
                addNotice("+12sn KONTROL NOKTASI!", Color(0xFF00F0FF), playerX, playerY - 0.1f)
                soundManager?.playNearMiss()
            }
        }

        // Combo decay or buildup
        if (currentSpeedKmh > 180f) {
            comboStreakTimer += dt
            if (comboStreakTimer > 3.0f) {
                combo = min(4.0f, combo + 0.2f)
                comboStreakTimer = 0f
            }
        } else if (isBraking) {
            combo = max(1.0f, combo - dt * 0.5f)
        }

        // Update Traffic
        updateTraffic(dt)

        // Update Collectibles
        updateCollectibles(dt)

        // Update Particles
        updateParticles(dt)

        // Update Notices
        updateNotices(dt)

        // Spawn new traffic & items
        handleSpawning(dt)

        _engineState.value = System.nanoTime()
    }

    private fun updateTraffic(dt: Float) {
        val playerSpeedNormalized = currentSpeedKmh / 150f
        val carHalfWidth = 0.065f
        val carHalfHeight = 0.055f

        val iterator = trafficVehicles.iterator()
        while (iterator.hasNext()) {
            val traffic = iterator.next()

            // Relative speed downward
            val trafficSpeedNormalized = traffic.speedKmh / 150f
            val relSpeed = playerSpeedNormalized - trafficSpeedNormalized
            // Move traffic
            traffic.y += relSpeed * dt * 0.85f

            // Police lane change intelligence
            if (traffic.type == TrafficType.POLICE && Random.nextFloat() < 0.015f) {
                val targetLane = if (traffic.lane < playerLane) traffic.lane + 1 else if (traffic.lane > playerLane) traffic.lane - 1 else traffic.lane
                traffic.lane = targetLane
                traffic.x += (laneCenters[targetLane] - traffic.x) * 0.1f
            }

            val trafficHalfWidth = 0.065f * traffic.type.widthFactor
            val trafficHalfHeight = 0.055f * traffic.type.lengthFactor

            // Collision Check with Player
            val xOverlap = abs(playerX - traffic.x) < (carHalfWidth + trafficHalfWidth)
            val yOverlap = abs(playerY - traffic.y) < (carHalfHeight + trafficHalfHeight)

            if (xOverlap && yOverlap) {
                // Crash collision!
                handleCollision(traffic)
                iterator.remove()
                continue
            }

            // Near Miss Check (Traffic passed player closely without hitting)
            if (!traffic.hasTriggeredNearMiss && !isGameOver) {
                val nearX = abs(playerX - traffic.x) < (carHalfWidth + trafficHalfWidth + 0.055f)
                val isPassing = abs(playerY - traffic.y) < 0.04f

                if (nearX && isPassing) {
                    traffic.hasTriggeredNearMiss = true
                    nearMissesCount++
                    score += 150f * combo
                    combo = min(4.0f, combo + 0.3f)
                    screenShake = 0.15f
                    soundManager?.playNearMiss()
                    addNotice("YAKIN GEÇİŞ! +150", Color(0xFFFFB703), playerX, playerY - 0.08f)
                }
            }

            // Remove if far off screen
            if (traffic.y > 1.3f || traffic.y < -0.6f) {
                iterator.remove()
            }
        }
    }

    private fun handleCollision(traffic: TrafficVehicle) {
        addExplosion(traffic.x, traffic.y)
        screenShake = 0.8f

        if (isShieldActive) {
            isShieldActive = false
            soundManager?.playShieldBreak()
            addNotice("KALKAN KIRILDI!", Color(0xFF00F0FF), playerX, playerY - 0.1f)
            currentSpeedKmh = max(80f, currentSpeedKmh * 0.65f)
            return
        }

        soundManager?.playCrash()
        lives--
        combo = 1.0f
        currentSpeedKmh = max(60f, currentSpeedKmh * 0.4f)
        addNotice("ÇARPIŞMA! -1 CAN", Color(0xFFFF2A4B), playerX, playerY - 0.1f)

        if (lives <= 0) {
            triggerGameOver("Araç Ağır Hasar Aldı!")
        }
    }

    private fun updateCollectibles(dt: Float) {
        val playerSpeedNormalized = currentSpeedKmh / 150f
        val iterator = collectibles.iterator()

        while (iterator.hasNext()) {
            val item = iterator.next()
            item.y += playerSpeedNormalized * dt * 0.85f

            // Magnet attraction
            if (magnetTimerSec > 0f && item.type == PickupType.COIN) {
                val dx = playerX - item.x
                val dy = playerY - item.y
                item.x += dx * dt * 6.0f
                item.y += dy * dt * 6.0f
            }

            // Pickup Collision
            val xDist = abs(playerX - item.x)
            val yDist = abs(playerY - item.y)

            if (xDist < 0.085f && yDist < 0.075f) {
                when (item.type) {
                    PickupType.COIN -> {
                        coinsEarned += 10
                        score += 80f * combo
                        soundManager?.playCoin()
                        addNotice("+10 COIN", Color(0xFFFFD700), item.x, item.y)
                        addSparkles(item.x, item.y, Color(0xFFFFD700))
                    }
                    PickupType.NITRO -> {
                        nitroGauge = 1.0f
                        soundManager?.playNitro()
                        addNotice("NİTRO DOLDU!", Color(0xFF00F0FF), item.x, item.y)
                        addSparkles(item.x, item.y, Color(0xFF00F0FF))
                    }
                    PickupType.SHIELD -> {
                        isShieldActive = true
                        soundManager?.playNearMiss()
                        addNotice("KALKAN AKTİF!", Color(0xFF38BDF8), item.x, item.y)
                        addSparkles(item.x, item.y, Color(0xFF38BDF8))
                    }
                    PickupType.MAGNET -> {
                        magnetTimerSec = 10f
                        soundManager?.playCoin()
                        addNotice("MIKNATIS AKTİF!", Color(0xFFFF007F), item.x, item.y)
                        addSparkles(item.x, item.y, Color(0xFFFF007F))
                    }
                    PickupType.REPAIR -> {
                        if (lives < 4) lives++
                        soundManager?.playNearMiss()
                        addNotice("TAMİR EDİLDİ! +1 CAN", Color(0xFF00FF66), item.x, item.y)
                        addSparkles(item.x, item.y, Color(0xFF00FF66))
                    }
                }
                iterator.remove()
                continue
            }

            if (item.y > 1.2f) {
                iterator.remove()
            }
        }
    }

    private fun handleSpawning(dt: Float) {
        spawnTimer += dt
        // Traffic spawn interval gets faster as distance increases
        val spawnInterval = max(0.9f, 2.2f - (distanceMeters / 15000f))

        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f
            spawnTrafficVehicle()
        }

        pickupSpawnTimer += dt
        if (pickupSpawnTimer >= 2.4f) {
            pickupSpawnTimer = 0f
            spawnCollectible()
        }
    }

    private fun spawnTrafficVehicle() {
        val lane = Random.nextInt(0, 4)
        // Ensure no overlapping car currently in top spawn area of this lane
        val alreadyOccupied = trafficVehicles.any { it.lane == lane && it.y < 0.1f }
        if (alreadyOccupied) return

        val randType = Random.nextFloat()
        val type = when {
            mode == GameMode.POLICE_CHASE && randType < 0.35f -> TrafficType.POLICE
            randType < 0.20f -> TrafficType.TRUCK
            randType < 0.50f -> TrafficType.SPORTS
            randType < 0.60f && mode != GameMode.POLICE_CHASE -> TrafficType.POLICE
            else -> TrafficType.SEDAN
        }

        val color = when (type) {
            TrafficType.POLICE -> Color(0xFF1E3A8A)
            TrafficType.TRUCK -> listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF64748B)).random()
            TrafficType.SPORTS -> listOf(Color(0xFFFFB703), Color(0xFFFF007F), Color(0xFF00E676), Color(0xFFF97316)).random()
            TrafficType.SEDAN -> listOf(Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFA855F7), Color(0xFF64748B)).random()
        }

        val speed = (type.baseSpeedKmh + Random.nextInt(-15, 20)).toFloat()

        trafficVehicles.add(
            TrafficVehicle(
                id = nextEntityId++,
                lane = lane,
                x = laneCenters[lane],
                y = -0.2f,
                type = type,
                color = color,
                speedKmh = speed
            )
        )
    }

    private fun spawnCollectible() {
        val lane = Random.nextInt(0, 4)
        val occupied = collectibles.any { it.lane == lane && it.y < 0.1f }
        if (occupied) return

        val roll = Random.nextFloat()
        val type = when {
            roll < 0.55f -> PickupType.COIN
            roll < 0.72f -> PickupType.NITRO
            roll < 0.83f -> PickupType.SHIELD
            roll < 0.92f -> PickupType.MAGNET
            else -> PickupType.REPAIR
        }

        collectibles.add(
            Collectible(
                id = nextEntityId++,
                lane = lane,
                x = laneCenters[lane],
                y = -0.15f,
                type = type
            )
        )
    }

    private fun updateParticles(dt: Float) {
        val iter = particles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt
            p.alpha = (p.life / p.lifeMax).coerceIn(0f, 1f)
            if (p.life <= 0f) {
                iter.remove()
            }
        }
    }

    private fun updateNotices(dt: Float) {
        val iter = notices.iterator()
        while (iter.hasNext()) {
            val n = iter.next()
            n.y -= dt * 0.05f
            n.life -= dt
            n.alpha = (n.life / 1.0f).coerceIn(0f, 1f)
            if (n.life <= 0f) {
                iter.remove()
            }
        }
    }

    private fun addNitroFlame(x: Float, y: Float) {
        for (i in 0..1) {
            val vx = (Random.nextFloat() - 0.5f) * 0.04f
            val vy = 0.45f + Random.nextFloat() * 0.2f
            val color = if (Random.nextBoolean()) Color(0xFF00F0FF) else Color(0xFFFFFFFF)
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = color,
                    size = 5f + Random.nextFloat() * 4f,
                    lifeMax = 0.25f,
                    life = 0.25f
                )
            )
        }
    }

    private fun addSmokeParticle(x: Float, y: Float) {
        particles.add(
            Particle(
                x = x,
                y = y,
                vx = (Random.nextFloat() - 0.5f) * 0.05f,
                vy = 0.15f + Random.nextFloat() * 0.1f,
                color = Color(0x88CBD5E1),
                size = 12f + Random.nextFloat() * 8f,
                lifeMax = 0.4f,
                life = 0.4f
            )
        )
    }

    private fun addExplosion(x: Float, y: Float) {
        for (i in 0..18) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 0.15f + Random.nextFloat() * 0.35f
            val vx = kotlin.math.cos(angle) * speed
            val vy = kotlin.math.sin(angle) * speed
            val color = listOf(Color(0xFFFF2A4B), Color(0xFFFFB703), Color(0xFFFFFFFF), Color(0xFFF97316)).random()
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = color,
                    size = 6f + Random.nextFloat() * 7f,
                    lifeMax = 0.55f,
                    life = 0.55f
                )
            )
        }
    }

    private fun addSparkles(x: Float, y: Float, color: Color) {
        for (i in 0..8) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 0.08f + Random.nextFloat() * 0.15f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed,
                    color = color,
                    size = 4f + Random.nextFloat() * 5f,
                    lifeMax = 0.4f,
                    life = 0.4f
                )
            )
        }
    }

    private fun addNotice(text: String, color: Color, x: Float, y: Float) {
        notices.add(
            FloatingNotice(
                text = text,
                color = color,
                x = x.coerceIn(0.15f, 0.85f),
                y = y.coerceIn(0.2f, 0.8f)
            )
        )
    }

    private fun triggerGameOver(reason: String) {
        isGameOver = true
        isNitroActive = false
        isBraking = false
        soundManager?.playCrash()
    }
}
