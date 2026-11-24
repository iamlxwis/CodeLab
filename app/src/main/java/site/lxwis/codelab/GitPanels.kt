package site.lxwis.codelab

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.spring
import android.provider.Settings
import android.view.View
import android.view.WindowInsets as PlatformWindowInsets
import android.os.Build
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.ViewModelProvider

@Composable
internal fun GitDialog(m: WorkspaceModel, dismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("git-identity", 0) }
    var message by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf(prefs.getString("author", "") ?: "") }
    var email by rememberSaveable { mutableStateOf(prefs.getString("email", "") ?: "") }
    var branch by rememberSaveable { mutableStateOf("") }
    var remoteExpanded by rememberSaveable { mutableStateOf(false) }
    var remoteInput by rememberSaveable(m.project) { mutableStateOf("") }
    var credentialInput by remember { mutableStateOf("") }
    var confirmRemoteAction by remember { mutableStateOf<String?>(null) }
    val selectedForStage = remember(m.project) { mutableStateListOf<String>() }
    val state = m.gitState
    LaunchedEffect(state?.remoteUrl) {
        if (state?.remoteUrl != null && remoteInput.isBlank()) {
            remoteInput = state.remoteUrl.removePrefix("https://github.com/").removeSuffix(".git")
        }
    }
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("Git · local history") },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (m.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (state == null) Text("Loading repository status…")
                else if (!state.initialized) {
                    Text("No repository in this project.")
                    Button(enabled = true, onClick = m::gitInit) { Text("Initialize Git") }
                } else {
                    Text("Branch · " + state.branch, fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.titleSmall)
                    state.trackingBranch?.let { upstream ->
                        Text("Tracking · $upstream",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(enabled = true, onClick = m::gitRefresh) { Text("Refresh") }
                        state.branches.forEach { name ->
                            FilterChip(selected = name == state.branch, enabled = true,
                                onClick = { m.gitCheckout(name) }, label = { Text(name) })
                        }
                    }
                    OutlinedTextField(branch, { branch = it }, modifier = Modifier.fillMaxWidth(),
                        label = { Text("New branch") }, singleLine = true)
                    TextButton(enabled = branch.isNotBlank(), onClick = {
                        m.gitBranch(branch)
                        branch = ""
                    }) { Text("Create and switch branch") }
                    HorizontalDivider()
                    Text("Working tree · " + state.changes.size + " entries",
                        style = MaterialTheme.typography.titleSmall)
                    Text("Staged · " + state.staged.size + " file(s)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                    state.changes.take(20).forEach { change ->
                        val selectable = change.kind in setOf("Modified", "Deleted", "Untracked")
                        val staged = change.kind.startsWith("Staged")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectable) Checkbox(
                                checked = change.path in selectedForStage,
                                onCheckedChange = { checked ->
                                    if (checked && change.path !in selectedForStage)
                                        selectedForStage.add(change.path)
                                    else if (!checked) selectedForStage.remove(change.path)
                                })
                            Text(change.kind + " · " + change.path,
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2, modifier = Modifier.weight(1f))
                            if (staged) IconButton(
                                enabled = !m.busy,
                                onClick = { m.gitUnstage(listOf(change.path)) }
                            ) {
                                Icon(Icons.Default.Undo, "Unstage ${change.path}")
                            }
                        }
                    }
                    if (state.changes.size > 20)
                        Text("Additional entries not shown.", style = MaterialTheme.typography.labelSmall)
                    Button(
                        enabled = !m.busy && selectedForStage.isNotEmpty(),
                        onClick = {
                            m.gitStage(selectedForStage.toList())
                            selectedForStage.clear()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Stage selected files") }
                    HorizontalDivider()
                    Text("Commit staged changes", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(message, { message = it.take(500) },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Commit message") },
                        maxLines = 3)
                    OutlinedTextField(author, { author = it.take(100) },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Author name") },
                        singleLine = true)
                    OutlinedTextField(email, { email = it.take(200) },
                        modifier = Modifier.fillMaxWidth(), label = { Text("Author email") },
                        singleLine = true)
                    Button(enabled = message.isNotBlank()
                        && author.isNotBlank() && email.contains('@') && state.staged.isNotEmpty() && !m.busy,
                        onClick = {
                            prefs.edit().putString("author", author).putString("email", email).apply()
                            m.gitCommit(message, author, email)
                            message = ""
                        }, modifier = Modifier.fillMaxWidth()) { Text("Commit staged files") }
                    HorizontalDivider()
                    Text("Recent commits", style = MaterialTheme.typography.titleSmall)
                    if (state.commits.isEmpty()) Text("No commits yet.")
                    state.commits.forEach { commit ->
                        Text(commit.shortId + " · " + commit.message + " — " + commit.author,
                            style = MaterialTheme.typography.bodySmall, maxLines = 2)
                    }
                    HorizontalDivider()
                    TextButton(onClick = { remoteExpanded = !remoteExpanded }) {
                        Icon(Icons.Default.CloudSync, null, Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (remoteExpanded) "Hide GitHub sync" else "GitHub sync · optional")
                    }
                    if (remoteExpanded) {
                        Text("Remote operations are optional. Only GitHub HTTPS URLs are accepted.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (state.unsafeRemote) Text("This repository has an unsupported origin URL. Configure a trusted GitHub remote.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(remoteInput, { remoteInput = it.take(180) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("GitHub owner/repository") }, singleLine = true,
                            placeholder = { Text("owner/repository") })
                        OutlinedButton(
                            enabled = !m.busy && remoteInput.isNotBlank(),
                            onClick = { m.gitConfigureRemote(remoteInput) }
                        ) { Text("Set GitHub remote") }
                        state.remoteUrl?.let { url ->
                            Text("Origin · " + url,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        OutlinedTextField(credentialInput, { credentialInput = it.take(512) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            label = { Text("GitHub token (optional for public fetch)") },
                            singleLine = true)
                        Text(if (m.gitTokenConfigured) "Encrypted token stored on this device"
                            else "No GitHub token stored",
                            style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                enabled = !m.busy && credentialInput.isNotBlank(),
                                onClick = {
                                    m.gitSaveToken(credentialInput)
                                    credentialInput = ""
                                }
                            ) { Text("Save token") }
                            TextButton(
                                enabled = !m.busy && m.gitTokenConfigured,
                                onClick = { m.gitSaveToken("") }
                            ) { Text("Remove token") }
                        }
                        OutlinedButton(
                            enabled = !m.busy && m.isOnline && state.remoteUrl != null,
                            onClick = m::gitFetch
                        ) { Text("Fetch · no file changes") }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                enabled = !m.busy && m.isOnline && state.remoteUrl != null,
                                onClick = { confirmRemoteAction = "pull" }
                            ) { Text("Pull (FF only)") }
                            Button(
                                enabled = !m.busy && m.isOnline && state.remoteUrl != null &&
                                    m.gitTokenConfigured && state.changes.isEmpty(),
                                onClick = { confirmRemoteAction = "push" }
                            ) { Text("Push branch") }
                        }
                        Text("Pull refuses divergence and local changes. Push never forces and may be rejected by GitHub.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    m.gitFeedback?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = dismiss) { Text("Done") } })

    if (confirmRemoteAction != null) {
        val pushing = confirmRemoteAction == "push"
        AlertDialog(
            onDismissRequest = { confirmRemoteAction = null },
            title = { Text(if (pushing) "Push branch to GitHub?" else "Pull updates from GitHub?") },
            text = { Text(if (pushing)
                "Upload local committed changes from the current branch to the configured GitHub remote. This is non-force and cannot overwrite a newer remote history."
                else "Download and apply only fast-forward changes to the current branch. The pull is refused when the working tree is dirty or histories diverge.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmRemoteAction = null
                    if (pushing) m.gitPush() else m.gitPullFastForward()
                }) { Text(if (pushing) "Push branch" else "Pull fast-forward") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemoteAction = null }) { Text("Cancel") }
            }
        )
    }
}


@Composable
internal fun GitHubImportDialog(m: WorkspaceModel, dismiss: () -> Unit) {
    var slug by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = dismiss,
        title = { Text("Import public GitHub project") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(slug, { slug = it.take(190) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("owner/repository or GitHub URL") },
                    placeholder = { Text("owner/example") }, singleLine = true)
                OutlinedTextField(name, { name = it.take(100) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Local project name") }, singleLine = true)
                Text("Requires internet. Imports a source snapshot from a public GitHub repository, not commit history. " +
                    "Project size and file safety limits apply. No token or account is needed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(enabled = slug.isNotBlank() && name.isNotBlank(),
                onClick = {
                    m.importPublicGitHub(slug, name)
                    dismiss()
                }) { Text("Import") }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}
