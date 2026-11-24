package site.lxwis.codelab

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeCommand
import org.eclipse.jgit.dircache.DirCacheEditor
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

/**
 * Local version control is offline. GitHub remote operations are invoked ONLY
 * by explicit UI actions and only over a canonical GitHub HTTPS origin.
 * No credentials or source files are uploaded by inspection or project open.
 */
internal class GitWorkspace(private val folder: File) {
    data class Change(val path: String, val kind: String)
    data class Revision(val shortId: String, val message: String, val author: String)
    data class Snapshot(val initialized: Boolean, val branch: String,
                        val branches: List<String>, val changes: List<Change>,
                        val commits: List<Revision>, val staged: List<String> = emptyList(),
                        val remoteUrl: String? = null, val unsafeRemote: Boolean = false)

    private val gitFolder get() = File(folder, ".git")

    fun init() {
        if (gitFolder.isDirectory) return
        Git.init().setDirectory(folder).call().close()
    }

    private fun <T> open(action: (Git) -> T): T {
        if (!gitFolder.isDirectory) throw IllegalStateException("Initialize Git first.")
        Git.open(folder).use { return action(it) }
    }

    fun inspect(): Snapshot {
        if (!gitFolder.isDirectory) return Snapshot(false, "", emptyList(), emptyList(), emptyList())
        return open { git ->
            val status = git.status().call()
            val changes = buildList {
                status.added.sorted().forEach { add(Change(it, "Staged · added")) }
                status.changed.sorted().forEach { add(Change(it, "Staged · changed")) }
                status.removed.sorted().forEach { add(Change(it, "Staged · removed")) }
                status.modified.sorted().forEach { add(Change(it, "Modified")) }
                status.missing.sorted().forEach { add(Change(it, "Deleted")) }
                status.untracked.sorted().forEach { add(Change(it, "Untracked")) }
                status.conflicting.sorted().forEach { add(Change(it, "Conflict")) }
            }.take(200)
            val commits = try {
                git.log().setMaxCount(12).call().map { commit ->
                    Revision(commit.name.take(9),
                        commit.shortMessage.take(130),
                        commit.authorIdent?.name.orEmpty())
                }.toList()
            } catch (_: org.eclipse.jgit.api.errors.NoHeadException) {
                emptyList()
            }
            val names = git.branchList().call().map { it.name.removePrefix("refs/heads/") }.sorted()
            val staged = (status.added + status.changed + status.removed).sorted()
            val configUrl = git.repository.config.getString("remote", "origin", "url")
            val remoteUrl = configUrl?.takeIf {
                runCatching { GitHubRemoteAddress.requireConfigured(it) }.isSuccess
            }
            Snapshot(true, git.repository.branch, names, changes, commits, staged,
                remoteUrl, configUrl != null && remoteUrl == null)
        }
    }

    /**
     * Stage ONLY explicitly selected paths, never the whole workspace.
     * The paths must appear in JGit's current unstaged/untracked status.
     */
    fun stage(paths: List<String>) {
        require(paths.isNotEmpty() && paths.size <= 200) { "Select up to 200 changed files." }
        open { git ->
            val status = git.status().call()
            val allowed = status.untracked + status.modified + status.missing
            val selected = paths.distinct()
            require(selected.all { it in allowed && it !in status.conflicting }) {
                "Selected Git files changed. Refresh and select them again."
            }
            selected.forEach { path ->
                val command = git.add().addFilepattern(path)
                if (path in status.missing) command.setUpdate(true)
                command.call()
            }
        }
    }

    /** Remove only selected staged paths from the index; never reset working files. */
    fun unstage(paths: List<String>) {
        require(paths.isNotEmpty() && paths.size <= 200) { "Select up to 200 staged files." }
        open { git ->
            val status = git.status().call()
            val staged = status.added + status.changed + status.removed
            val selected = paths.distinct()
            require(selected.all { it in staged }) {
                "Staged Git files changed. Refresh and select them again."
            }
            if (git.repository.resolve("HEAD") == null) {
                // An unborn branch has no HEAD tree for a standard path reset.
                val index = git.repository.lockDirCache()
                try {
                    val editor = index.editor()
                    selected.forEach { editor.add(DirCacheEditor.DeletePath(it)) }
                    editor.finish()
                    check(index.commit()) { "Git index changed; refresh and retry." }
                } finally {
                    index.unlock()
                }
            } else {
                selected.forEach { git.reset().addPath(it).call() }
            }
        }
    }

    fun commit(message: String, name: String, email: String): String {
        require(message.isNotBlank() && message.length <= 500) { "Enter a commit message (up to 500 characters)." }
        require(name.isNotBlank() && name.length <= 100) { "Enter an author name." }
        require(email.contains('@') && email.length <= 200) { "Enter an author email address." }
        return open { git ->
            val status = git.status().call()
            if (status.added.isEmpty() && status.changed.isEmpty() && status.removed.isEmpty()) {
                throw IllegalStateException("Stage files explicitly before committing.")
            }
            val result = git.commit().setMessage(message.trim()).setAuthor(name.trim(), email.trim())
                .setCommitter(name.trim(), email.trim()).call()
            result.name.take(10)
        }
    }

    /** Updates .git/config only. Merely configuring a remote never connects. */
    fun configureGitHubRemote(input: String) {
        val canonical = GitHubRemoteAddress.canonical(input)
        open { git ->
            val config = git.repository.config
            config.setString("remote", "origin", "url", canonical)
            config.setString("remote", "origin", "fetch",
                "+refs/heads/*:refs/remotes/origin/*")
            config.save()
        }
    }

    private fun checkedRemote(git: Git): String =
        GitHubRemoteAddress.requireConfigured(
            git.repository.config.getString("remote", "origin", "url"))

    private fun credentials(token: String) =
        UsernamePasswordCredentialsProvider("x-access-token", token)

    /** Fetch moves only refs/remotes/origin; it never changes working files. */
    fun fetchGitHub(token: String): String = open { git ->
        checkedRemote(git)
        val command = git.fetch().setRemote("origin").setTimeout(30)
        if (token.isNotBlank()) command.setCredentialsProvider(credentials(token))
        command.call()
        "Fetched remote branches. Your files are unchanged."
    }

    /**
     * Pull may update source files; only performed after explicit confirmation.
     * Never perform a merge commit, rebase or overwrite dirty local work.
     */
    fun pullFastForwardGitHub(token: String): String = open { git ->
        checkedRemote(git)
        require(git.status().call().isClean) {
            "Commit or remove local modifications before pulling."
        }
        val branch = git.repository.branch
        require(git.repository.fullBranch == "refs/heads/$branch") {
            "Switch to a local branch before pulling."
        }
        val command = git.pull()
            .setRemote("origin")
            .setRemoteBranchName(branch)
            .setFastForward(MergeCommand.FastForwardMode.FF_ONLY)
            .setTimeout(30)
        if (token.isNotBlank()) command.setCredentialsProvider(credentials(token))
        val result = command.call()
        check(result.isSuccessful) { "Pull was not fast-forward. Your local commits were preserved." }
        "Fast-forward pull finished on $branch."
    }

    /** An explicit, single-branch, non-force push; server rejection is an error. */
    fun pushGitHub(token: String): String = open { git ->
        checkedRemote(git)
        require(token.isNotBlank()) { "Save a GitHub access token before pushing." }
        require(git.status().call().isClean) {
            "Commit or remove local changes before pushing."
        }
        val branch = git.repository.branch
        require(git.repository.fullBranch == "refs/heads/$branch") {
            "Switch to a local branch before pushing."
        }
        val updates = git.push()
            .setRemote("origin")
            .setRefSpecs(RefSpec("refs/heads/$branch:refs/heads/$branch"))
            .setCredentialsProvider(credentials(token))
            .setTimeout(30)
            .setForce(false)
            .call()
            .flatMap { it.remoteUpdates }
        check(updates.isNotEmpty()) { "No remote branch was updated." }
        check(updates.all {
            it.status == RemoteRefUpdate.Status.OK ||
                it.status == RemoteRefUpdate.Status.UP_TO_DATE
        }) { "GitHub rejected the push (possibly a non-fast-forward update)." }
        "Pushed branch $branch without force."
    }

    fun createBranch(name: String) {
        require(name.matches(Regex("[A-Za-z0-9][A-Za-z0-9._/-]{0,79}")) &&
            !name.contains("..") && !name.endsWith(".lock") &&
            !name.endsWith("/") && !name.contains("//")) { "Invalid branch name." }
        open { git ->
            if (!git.status().call().isClean) throw IllegalStateException("Commit changes before switching branches.")
            git.branchCreate().setName(name).call()
            git.checkout().setName(name).call()
        }
    }

    fun checkout(name: String) {
        require(name.matches(Regex("[A-Za-z0-9][A-Za-z0-9._/-]{0,79}"))) { "Invalid branch name." }
        open { git ->
            if (!git.status().call().isClean) throw IllegalStateException("Commit changes before switching branches.")
            if (git.repository.branch != name) git.checkout().setName(name).call()
        }
    }
}
