package com.finance.batch.domain.sample

import com.finance.batch.domain.finance.sample.row.Sample
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import org.springframework.batch.core.Job
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@ApplyExtension(SpringExtension::class)
@SpringBatchTest
class SampleConfigTest(
    private val jobLauncherTestUtils: JobLauncherTestUtils,
    private val jdbcTemplate: JdbcTemplate,
    private val sampleJob: Job,
) : BehaviorSpec({

    beforeContainer { testCase ->
        if (testCase.name.prefix?.startsWith("Given:") == true) {
            jdbcTemplate.execute("TRUNCATE TABLE sample_source")
            jdbcTemplate.execute("TRUNCATE TABLE sample")
        }
    }

    given("정상적인 샘플 원천 데이터 3건이 있을 때") {
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 1")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 2")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 3")

        `when`("샘플 잡을 실행하면") {
            jobLauncherTestUtils.job = sampleJob
            val jobExecution = jobLauncherTestUtils.launchJob()

            then("성공적으로 완료(COMPLETED)되어야 하며, 3건 적재되어야 한다") {
                jobExecution.exitStatus.exitCode shouldBe "COMPLETED"

                val savedSample = jdbcTemplate.query("SELECT * FROM sample") { rs, _ ->
                    Sample(name = rs.getString("name"))
                }
                savedSample.size shouldBe 3
            }
        }
    }

    given("비정상적인 데이터가 포함된 원천 데이터 10건이 존재할 때") {
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 1")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 2")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 3")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 4")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 5")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 6")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 7")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 8")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 9")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "ERROR")

        `when`("샘플 잡을 실행하면") {
            jobLauncherTestUtils.job = sampleJob
            val jobExecution = jobLauncherTestUtils.launchJob()

            then("실패(FAILED)해야 하고, 롤백되어야 한다") {
                jobExecution.exitStatus.exitCode shouldBe "FAILED"

                val count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sample", Long::class.java)
                count shouldBe 0L
            }
        }
    }

    given("비정상적인 데이터가 포함된 원천 데이터 15건이 존재할 때") {
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 1")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 2")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 3")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 4")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 5")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 6")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 7")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 8")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 9")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 10")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 11")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 12")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 13")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "샘플 데이터 14")
        jdbcTemplate.update("INSERT INTO sample_source (name) VALUES (?)", "ERROR")

        `when`("샘플 잡을 실행하면") {
            jobLauncherTestUtils.job = sampleJob
            val jobExecution = jobLauncherTestUtils.launchJob()

            then("실패(FAILED)해야 하고, 기존에 커밋된 데이터는 그대로 존재해야한다.") {
                jobExecution.exitStatus.exitCode shouldBe "FAILED"

                val count = jdbcTemplate.queryForObject<Long>("SELECT COUNT(*) FROM sample")
                count shouldBe 10L
            }
        }
    }
})