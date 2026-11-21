package site.lxwis.codelab
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
class MainActivity : ComponentActivity() {
    private val session by lazy { RunSession(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("editor", MODE_PRIVATE)
        setContent {
            var code by remember { mutableStateOf(prefs.getString("main", null) ?: "print('Hello, CodeLab')") }
            MaterialTheme(colorScheme=darkColorScheme()) {
                Surface { Column(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
                    Button(onClick={ if(session.running) session.stop() else session.run("main.js",code) }) { Text(if(session.running) "Stop" else "Run main.js") }
                    CodeEditor("default/main.js","main.js",code,{code=it; prefs.edit().putString("main",it).apply()},Modifier.weight(1f))
                    RunConsole(session,Modifier.weight(0.7f))
                } }
            }
        }
    }
    override fun onDestroy() {session.close(); super.onDestroy()}
}
