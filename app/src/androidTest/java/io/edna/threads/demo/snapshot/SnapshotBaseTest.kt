package io.edna.threads.demo.snapshot

import dev.testify.ScreenshotRule
import dev.testify.annotation.ScreenshotInstrumentation
import io.edna.threads.demo.appCode.test.TestChatActivity
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore("Base class, run it in descendants")
open class SnapshotBaseTest(private val jsonResourceId: Int) {
    @get:Rule
    val rule = ScreenshotRule(TestChatActivity::class.java).apply {
        saveJsonMock(jsonResourceId, this)
    }

    @ScreenshotInstrumentation
    @Test
    open fun testChat() {
        rule.setEspressoActions {
            Thread.sleep(2000)
        }.assertSame()
    }
}
