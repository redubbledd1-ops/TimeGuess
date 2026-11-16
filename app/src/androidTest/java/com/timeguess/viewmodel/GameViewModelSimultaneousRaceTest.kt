package com.timeguess.viewmodel

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class GameViewModelSimultaneousRaceTest {

    @Test
    fun sameMillisecondClicks_recordAllPlayers_withoutLoss() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = GameViewModel(app)
        viewModel.setPlayerCount(4)

        repeat(150) {
            viewModel.startSimultaneousRound()
            val pool = Executors.newFixedThreadPool(4)
            val ready = CountDownLatch(4)
            val start = CountDownLatch(1)
            val done = CountDownLatch(4)

            for (playerId in 0 until 4) {
                pool.execute {
                    ready.countDown()
                    start.await(1, TimeUnit.SECONDS)
                    viewModel.recordSimultaneousStop(playerId)
                    done.countDown()
                }
            }

            ready.await(1, TimeUnit.SECONDS)
            start.countDown()
            done.await(1, TimeUnit.SECONDS)
            pool.shutdownNow()

            assertEquals(
                "All 4 players should be recorded in iteration=$it",
                4,
                viewModel.simultaneousStopTimes.value.size
            )
        }
    }
}
