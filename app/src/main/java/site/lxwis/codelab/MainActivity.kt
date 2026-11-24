package site.lxwis.codelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.SideEffect
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
            val motion = rememberCodeLabMotion(model.motionPreference, model.motionIntensity, model.motionSpeed, model.springIntensity)
            CodeLabTheme(
                themePreset = model.themePreset,
                visualStyle = model.visualStyle,
                appearance = model.appearance,
                accentPreset = model.accent,
                highContrast = model.highContrast,
                largeInterfaceText = model.largeInterfaceText,
                studio = model.studioAppearance
            ) { dark ->
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !dark
                        isAppearanceLightNavigationBars = !dark
                    }
                }
                CodeLabBackdrop(model.visualStyle, dark, motion.millis(300)) {
                    CodeLabForegroundSurface(Modifier.fillMaxSize()) {
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
