package com.finance.batch.sample

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class SampleConfig {

    @Bean
    fun sampleJob(
        jobRepository: JobRepository,
        sampleStep1: Step,
        sampleStep2: Step,
    ): Job {
        return JobBuilder("sampleJob", jobRepository)
            .start(sampleStep1)
            .next(sampleStep2)
            .build()
    }

    @Bean
    fun sampleStep1(
        jobRepository: JobRepository,
        @Qualifier("metaTransactionManager") metaTransactionManager: PlatformTransactionManager,
    ): Step {
        return StepBuilder("sampleStep1", jobRepository)
            .tasklet({ _, _ ->
                println("sampleStep1 executing...")
                RepeatStatus.FINISHED
            }, metaTransactionManager)
            .build()
    }

    @Bean
    fun sampleStep2(
        jobRepository: JobRepository,
        @Qualifier("metaTransactionManager") metaTransactionManager: PlatformTransactionManager,
    ): Step {
        return StepBuilder("sampleStep2", jobRepository)
            .tasklet({ _, _ ->
                println("sampleStep2 executing...")
                RepeatStatus.FINISHED
            }, metaTransactionManager)
            .build()
    }

}