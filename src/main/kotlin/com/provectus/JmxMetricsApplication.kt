package com.provectus

import com.provectus.service.SchedulerService
import com.typesafe.config.Config
import org.koin.core.KoinComponent
import org.koin.core.inject

class JmxMetricsApplication : KoinComponent {
    val config by inject<Config>()
    val schedulerService by inject<SchedulerService>()


}