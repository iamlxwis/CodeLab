package site.lxwis.codelab

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.Repository
import java.io.File

/**
 * Git-backed, offline-only version control. Authentication and remote sync are deliberately
 * excluded until there is an explicit permission and credential workflow.
 */
internal class GitWorkspace(private val folder: File) {
    data class Change(val path: String, val kind: String)
    data class Revision(val shortId: String, val message: String, val author: String)
    data class Snapshot(val initialized: Boolean, val branch: String,
                        val branches: List<String>, val changes: List<Change>,
                        val commits: List<Revision>)

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
            Snapshot(true, git.repository.branch, names, changes, commits)
        }
    }

    fun commit(message: String, name: String, email: String): String {
        require(message.isNotBlank() && message.length <= 500) { "Enter a commit message (up to 500 characters)." }
        require(name.isNotBlank() && name.length <= 100) { "Enter an author name." }
        require(email.contains('@') && email.length <= 200) { "Enter an author email address." }
        return open { git ->
            git.add().addFilepattern(".").call()
            git.add().setUpdate(true).addFilepattern(".").call()
            if (git.status().call().isClean) throw IllegalStateException("Nothing changed since the last commit.")
            val result = git.commit().setMessage(message.trim()).setAuthor(name.trim(), email.trim())
                .setCommitter(name.trim(), email.trim()).call()
            result.name.take(10)
        }
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
