package site.lxwis.codelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    private lateinit var model: WorkspaceModel
    override fun onCreate(savedInstanceState: Bundle?) {
        val launchStarted = android.os.SystemClock.elapsedRealtimeNanos()
        super.onCreate(savedInstanceState)
        PerformanceRecorder.beginLaunch(this, launchStarted)
        model = ViewModelProvider(this)[WorkspaceModel::class.java]
        // The OS starting splash uses the static default; the activity window
        // follows the saved app palette before Compose renders its first frame.
        val systemDark = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        val launchDark = appearanceIsDark(model.appearance, systemDark)
        val launchScheme = codeLabColorScheme(launchDark, model.highContrast)
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(
            launchScheme.background.toArgb()))
        // Transparent system bars; content still accounts for safeDrawing and IME insets.
        enableEdgeToEdge()
        setContent {
            val motion = rememberCodeLabMotion(model.motionPreference, model.motionIntensity, model.motionSpeed, model.springIntensity, model.transitionEffect)
            val entrance = remember { Animatable(1f) }
            LaunchedEffect(Unit) {
                if (motion.millis(240) > 0) { entrance.snapTo(.82f); entrance.animateTo(1f, tween(motion.millis(240))) }
            }
            CodeLabTheme(
                appearance = model.appearance,
                highContrast = model.highContrast,
                largeInterfaceText = model.largeInterfaceText,
                editorAppearance = model.editorAppearance
            ) { _ ->
                val lightBars = MaterialTheme.colorScheme.background.luminance() > .179f
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = lightBars
                        isAppearanceLightNavigationBars = lightBars
                    }
                }
                CompositionLocalProvider(LocalWorkspaceMotion provides motion,
                    LocalEditorAppearanceUpdater provides { value -> model.updateEditorAppearance(value) },
                    LocalModalStatusVisible provides (model.systemStatusVisible && !(model.focusMode && model.project != null))) {
                    CodeLabForegroundSurface(Modifier.fillMaxSize().graphicsLayer { alpha = entrance.value }) {
                        Studio(model, Modifier.windowInsetsPadding(WindowInsets.safeDrawing).imePadding())
                    }
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        PerformanceRecorder.onStart(this)
    }
    override fun onResume() {
        super.onResume()
        model.developerActivity.setForeground(true)
    }

    override fun onPause() {
        model.developerActivity.setForeground(false)
        model.flush()
        super.onPause()
    }

    override fun onStop() {
        PerformanceRecorder.onStop()
        if (model.dirty) model.flush()
        super.onStop()
    }
}
