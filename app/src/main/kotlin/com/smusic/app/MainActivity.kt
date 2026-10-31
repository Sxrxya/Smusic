package com.smusic.app

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.smusic.app.data.repository.SettingsRepository
import com.smusic.app.player.IntroSoundPlayer
import com.smusic.app.presentation.navigation.AppNavigation
import com.smusic.app.presentation.splash.SplashIntro
import com.smusic.app.presentation.theme.BackgroundPrimary
import com.smusic.app.presentation.theme.SMusicTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.math.sqrt

val LocalMainActivity = staticCompositionLocalOf<MainActivity?> { null }

@AndroidEntryPoint
class MainActivity : ComponentActivity(), SensorEventListener {

    @Inject
    lateinit var introSoundPlayer: IntroSoundPlayer

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var shakeEnabled = false
    private var lastShakeTime: Long = 0
    var onShakeListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Observe shake setting
        settingsRepository.userPreferences
            .onEach { shakeEnabled = it.shakeToSkip }
            .launchIn(lifecycleScope)

        // Read preferences for intro
        val prefs = runBlocking { settingsRepository.userPreferences.first() }

        // BEHAVIOR: Intro plays ONLY on cold start from launcher icon
        val isLauncherIntent = intent?.action == Intent.ACTION_MAIN &&
                              intent?.hasCategory(Intent.CATEGORY_LAUNCHER) == true
        
        val shouldShowIntro = savedInstanceState == null && 
                             !SMusicApp.introShown && 
                             isLauncherIntent && 
                             prefs.startupAnimationEnabled

        if (savedInstanceState == null) {
            if (shouldShowIntro) {
                lifecycleScope.launch {
                    settingsRepository.incrementLaunchCount()
                }
            }
            // Mark as shown immediately so recreation doesn't replay it
            SMusicApp.introShown = true
        }

        val isShortVersion = prefs.launchCount > 3

        setContent {
            var showIntro by rememberSaveable { mutableStateOf(shouldShowIntro) }
            
            SMusicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundPrimary
                ) {
                    CompositionLocalProvider(LocalMainActivity provides this) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val navController = rememberNavController()
                            AppNavigation(navController = navController)
                            
                            if (showIntro) {
                                SplashIntro(
                                    isShortVersion = isShortVersion,
                                    onHit = {
                                        if (prefs.startupSoundEnabled) {
                                            introSoundPlayer.playIntroSound(false)
                                        }
                                    },
                                    onFinished = { showIntro = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        introSoundPlayer.release()
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!shakeEnabled || event == null) return
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
        if (acceleration > 12) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > 1000) {
                lastShakeTime = now
                onShakeListener?.invoke()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
