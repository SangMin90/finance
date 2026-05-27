package com.finance.batch.support

import io.mockk.spyk
import org.springframework.batch.core.repository.JobRepository
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestConfiguration {

    @Bean
    @Primary
    fun spyJobRepository(
        realJobRepository: JobRepository
    ): JobRepository = spyk(realJobRepository)
}