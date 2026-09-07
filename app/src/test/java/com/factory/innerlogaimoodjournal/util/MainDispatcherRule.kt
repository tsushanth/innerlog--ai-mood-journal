package com.factory.innerlogaimoodjournal.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Swaps [kotlinx.coroutines.Dispatchers.Main] for a [TestDispatcher] so ViewModels and
 * managers that launch on `Dispatchers.Main` (directly or via `viewModelScope`) run
 * synchronously in JVM unit tests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        setMain(dispatcher)
    }

    override fun finished(description: Description) {
        resetMain()
    }
}
