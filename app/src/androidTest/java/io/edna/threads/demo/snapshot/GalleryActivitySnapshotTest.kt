package io.edna.threads.demo.snapshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.testify.ScreenshotRule
import dev.testify.annotation.ScreenshotInstrumentation
import im.threads.ui.activities.GalleryActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GalleryActivitySnapshotTest {
    @get:Rule
    val rule = ScreenshotRule(GalleryActivity::class.java).apply {
        addIntentExtras {
            it.putInt(PHOTOS_REQUEST_CODE_TAG, 2345)
        }
    }

    @ScreenshotInstrumentation
    @Test
    fun testGalleryActivity() {
        rule.assertSame()
    }
}

private const val PHOTOS_REQUEST_CODE_TAG = "PHOTOS_REQUEST_CODE_TAG"
