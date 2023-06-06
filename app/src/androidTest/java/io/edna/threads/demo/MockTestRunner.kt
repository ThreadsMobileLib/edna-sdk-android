package io.edna.threads.demo

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import io.edna.threads.demo.integrationCode.EdnaThreadsApplication

class MockTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, EdnaThreadsApplication::class.java.name, context)
    }
}
