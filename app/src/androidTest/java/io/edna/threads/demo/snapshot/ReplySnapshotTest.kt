package io.edna.threads.demo.snapshot

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.testify.annotation.ScreenshotInstrumentation
import io.edna.threads.demo.R
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReplySnapshotTest : SnapshotBaseTest(R.raw.snapshot_test_history_text_response_1) {
    @ScreenshotInstrumentation
    @Test
    override fun testChat() {
        rule.setEspressoActions {
            Espresso.onView(ViewMatchers.withTagValue(Matchers.`is`("bubble")))
                .perform(ViewActions.longClick())
            Espresso.onView(ViewMatchers.withTagValue(Matchers.`is`("replyIcon")))
                .perform(ViewActions.click())
        }.assertSame()
    }
}
