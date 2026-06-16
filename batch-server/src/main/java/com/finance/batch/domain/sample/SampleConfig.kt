package com.finance.batch.domain.sample

import com.finance.batch.domain.sample.row.Sample
import com.finance.batch.domain.sample.row.SampleSource
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.Order
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class SampleConfig(
    @param:Qualifier("financeDataSource")
    private val dataSource: DataSource,
    private val jobRepository: JobRepository,
    @param:Qualifier("financeTransactionManager")
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    fun sampleJob(
        sampleStep: Step,
    ): Job {
        return JobBuilder("sampleJob", jobRepository)
            .start(sampleStep)
            .build()
    }

    @Bean
    fun sampleStep(
        sampleItemReader: JdbcPagingItemReader<SampleSource>,
        sampleItemProcessor: ItemProcessor<SampleSource, Sample>,
        sampleItemWriter: ItemWriter<Sample?>,
    ): Step {
        return StepBuilder("sampleStep", jobRepository)
            .chunk<SampleSource, Sample>(10, transactionManager)
            .reader(sampleItemReader)
            .processor(sampleItemProcessor)
            .writer(sampleItemWriter)
            .build()
    }

    @Bean
    fun sampleItemReader() : JdbcPagingItemReader<SampleSource> {

        return JdbcPagingItemReaderBuilder<SampleSource>()
            .name("sampleItemReader")
            .dataSource(dataSource)
            .pageSize(10)
            .selectClause("SELECT id, name")
            .fromClause("FROM sample_source")
            .sortKeys(mapOf("id" to Order.ASCENDING))
            .dataRowMapper(SampleSource::class.java)
            .build()
    }

    @Bean
    fun sampleItemWriter() : JdbcBatchItemWriter<Sample?> {

        return JdbcBatchItemWriterBuilder<Sample>()
            .dataSource(dataSource)
            .sql("INSERT INTO sample (name) VALUES (:name)")
            .beanMapped()
            .assertUpdates(true)
            .build()
    }
}