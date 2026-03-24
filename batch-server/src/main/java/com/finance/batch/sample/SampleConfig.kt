package com.finance.batch.sample

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class SampleConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {

    @Bean
    fun sampleJob(): Job {
        return JobBuilder("sampleJob", jobRepository)
            .start(sampleStep1())
            .next(sampleStep2())
            .build()
    }

    @Bean
    fun sampleStep1(): Step {
        return StepBuilder("sampleStep1", jobRepository)
            .tasklet({ contribution, chunkContext ->
                println("sampleStep1 executing...")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    @Bean
    fun sampleStep2(): Step {
        return StepBuilder("sampleStep2", jobRepository)
            .tasklet({ contribution, chunkContext ->
                println("sampleStep2 executing...")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

}