package site.lxwis.codelab

/** Pure tutorial checkpoint logic: live navigation must never silently finish onboarding. */
internal object TutorialProgress {
    fun restore(savedIndex: Int, topicCount: Int): Int =
        savedIndex.coerceIn(0, (topicCount - 1).coerceAtLeast(0))

    fun afterTry(currentIndex: Int, topicCount: Int): Int =
        restore(currentIndex + 1, topicCount)

    fun showExtendedTopics(savedIndex: Int): Boolean = savedIndex >= 4
}
