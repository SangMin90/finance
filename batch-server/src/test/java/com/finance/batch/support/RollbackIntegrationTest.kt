package com.finance.batch.support

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

@Import(SpyBatchTestConfiguration::class)
@SpringBootTest
@ActiveProfiles("test")
@ApplyExtension(SpringExtension::class)
@SpringBatchTest
class RollbackIntegrationTest(

    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val jdbcTemplate: JdbcTemplate,
    private val spyJobRepository: JobRepository,
) : BehaviorSpec({

    beforeContainer { testCase ->
        if (testCase.name.prefix?.startsWith("Given:") == true) {
            clearMocks(spyJobRepository)
            jdbcTemplate.execute("TRUNCATE TABLE sample_source")
            jdbcTemplate.execute("TRUNCATE TABLE sample")
        }
    }

    given("sample 데이터 커밋 후 메타 DB에 장애가 발생하여 예외가 발생한 경우") {

        every { spyJobRepository.update(match<JobExecution> { jobExecution ->
            jobExecution.status == BatchStatus.COMPLETED ||
                    (jobExecution.status == BatchStatus.STARTED && jobExecution.endTime != null)
        })} throws DataAccessResourceFailureException("Meta DB 에러!")

        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 1")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 2")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 3")

        `when`("잡을 실행하면") {

            shouldThrow<Exception> {
                jobLauncherTestUtils.launchJob()
            }

            then("메타 DB 장애와 관련 없이, sample 작업은 정상적으로 동작하여 데이터 3건이 존재해야 한다.") {
                val savedCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sample", Long::class.java)
                savedCount shouldBe 3L
            }
        }
    }

    given("배치 도중에 메타 DB에 장애가 발생하여 예외가 발생한 경우") {

        every {
            spyJobRepository.updateExecutionContext(
                match<StepExecution> { stepExecution ->
                    stepExecution.status == BatchStatus.STARTED &&
                    stepExecution.commitCount == 1L &&
                    stepExecution.rollbackCount == 0L
                }
            )
        } throws DataAccessResourceFailureException("Meta DB 에러!")

        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 1")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 2")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 3")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 1")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 2")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 3")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 1")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 2")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 3")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 1")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 2")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 3")

        `when`("잡을 실행하면") {

            val jobExecution = jobLauncherTestUtils.launchJob()

            then("작업은 실패해야 하고, 메타 DB 장애 발생 시점 이전에 커밋된 데이터 10건이 존재해야 한다.") {

                jobExecution.exitStatus.exitCode shouldBe "FAILED"

                val savedCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sample", Long::class.java)
                savedCount shouldBe 10L
            }
        }
    }
})