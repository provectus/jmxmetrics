package com.provectus.service

import com.provectus.model.Query
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SchedulerService(
    private val queries:List<Query>,
    private val period: Duration,
    private val initialDelay: Duration = Duration.ZERO,
    private val queryService:QueryService,
    queryThreads:Int = 2
) : Closeable {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SchedulerService::class.java)
    }

    private val executor:ExecutorService = Executors.newFixedThreadPool(queryThreads)

    fun run() {
        Thread.sleep(initialDelay.toMillis())
        while (!Thread.interrupted()) {
            for (query in queries) {
                executor.submit {
                    queryService.query(query)
                }
            }
            Thread.sleep(period.toMillis())
        }
    }

    override fun close() {
        LOGGER.info("Closing Scheduler service")
        executor.shutdown()
    }

}