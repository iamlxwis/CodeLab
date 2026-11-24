package site.lxwis.codelab

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Only signature/protocol-verified companions count as installed execution modules. */
internal data class InstalledRuntimeState(
    val python: PythonModuleLocator.Status,
    val optional: Map<OptionalRuntimeLocator.Pack, PythonModuleLocator.Status>
) {
    val installedExtensions: Set<String> get() = buildSet {
        if (python.ready) add("py")
        optional.forEach { (pack, state) -> if (state.ready) add(pack.extension) }
    }
}

/** Refresh after Android's installer returns, without package queries on every keystroke. */
@Composable
internal fun rememberInstalledRuntimeState(): InstalledRuntimeState {
    val context = LocalContext.current.applicationContext
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var revision by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) revision++ }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return remember(context, revision) {
        InstalledRuntimeState(PythonModuleLocator.check(context),
            OptionalRuntimeLocator.Pack.entries.associateWith { OptionalRuntimeLocator.check(context, it) })
    }
}
