package site.lxwis.codelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    private lateinit var model: WorkspaceModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = ViewModelProvider(this)[WorkspaceModel::class.java]
        // Transparent system bars; content still accounts for safeDrawing and IME insets.
        enableEdgeToEdge()
        setContent {
            val motion = rememberCodeLabMotion(model.motionPreference, model.motionIntensity, model.motionSpeed, model.springIntensity, model.transitionEffect)
            val entrance = remember { Animatable(1f) }
            LaunchedEffect(Unit) {
                if (motion.millis(240) > 0) { entrance.snapTo(.82f); entrance.animateTo(1f, tween(motion.millis(240))) }
            }
            CodeLabTheme(
                themePreset = model.themePreset,
                visualStyle = model.visualStyle,
                appearance = model.appearance,
                accentPreset = model.accent,
                highContrast = model.highContrast,
                largeInterfaceText = model.largeInterfaceText,
                studio = model.studioAppearance
            ) { dark ->
                val lightBars = MaterialTheme.colorScheme.background.luminance() > .179f
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = lightBars
                        isAppearanceLightNavigationBars = lightBars
                    }
                }
                CodeLabBackdrop(model.visualStyle, dark, motion.millis(300)) {
                    CodeLabForegroundSurface(Modifier.fillMaxSize().graphicsLayer { alpha = entrance.value }) {
                        Studio(model, Modifier.windowInsetsPadding(WindowInsets.safeDrawing).imePadding())
                    }
                }
            }
        }
    }
    override fun onPause() {
        model.flush()
        super.onPause()
    }

    override fun onStop() {
        if (model.dirty) model.flush()
        super.onStop()
    }
}
