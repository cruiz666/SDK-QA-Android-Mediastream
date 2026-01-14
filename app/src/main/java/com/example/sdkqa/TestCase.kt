package com.example.sdkqa

/**
 * Represents a test case item in the SDK QA test suite.
 * Easy to extend - just add new entries to TestCaseType.
 */
data class TestCase(
    val type: TestCaseType,
    val title: String,
    val category: Category
) {
    enum class Category(val displayName: String) {
        AUDIO("Audio"),
        VIDEO("Video")
    }

    enum class TestCaseType {
        AUDIO_AOD_SIMPLE,
        AUDIO_AOD_WITH_SERVICE,
        AUDIO_LIVE,
        AUDIO_LIVE_WITH_SERVICE,
        AUDIO_LIVE_DVR,
        VIDEO_VOD_SIMPLE,
        VIDEO_LIVE,
        VIDEO_LIVE_DVR
    }

    companion object {
        /**
         * Returns all available test cases.
         * Add new test cases here.
         */
        fun getAllTestCases(): List<TestCase> = listOf(
            // Audio Test Cases
            TestCase(TestCaseType.AUDIO_AOD_SIMPLE, "AOD Simple", Category.AUDIO),
            TestCase(TestCaseType.AUDIO_AOD_WITH_SERVICE, "AOD with Service", Category.AUDIO),
            TestCase(TestCaseType.AUDIO_LIVE, "Live Audio", Category.AUDIO),
            TestCase(TestCaseType.AUDIO_LIVE_WITH_SERVICE, "Live Audio with Service", Category.AUDIO),
            TestCase(TestCaseType.AUDIO_LIVE_DVR, "Live Audio DVR", Category.AUDIO),
            // Video Test Cases
            TestCase(TestCaseType.VIDEO_VOD_SIMPLE, "VOD Simple", Category.VIDEO),
            TestCase(TestCaseType.VIDEO_LIVE, "Live Video", Category.VIDEO),
            TestCase(TestCaseType.VIDEO_LIVE_DVR, "Live Video DVR", Category.VIDEO)
        )
    }

    /**
     * Returns the formatted display title with category prefix.
     */
    fun getDisplayTitle(): String = "${category.displayName}: $title"
}
