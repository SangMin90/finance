package com.finance.batch.global.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.batch.BatchDataSource
import org.springframework.boot.autoconfigure.batch.BatchTransactionManager
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.support.JdbcTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration(proxyBeanMethods = false)
class DataSourceConfig {

    @Bean("metaDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.meta")
    fun metaDataSourceProperties(): DataSourceProperties = DataSourceProperties()

    @Bean
    @BatchDataSource
    fun metaDataSource(
        @Qualifier("metaDataSourceProperties") properties: DataSourceProperties
    ): DataSource = properties.initializeDataSourceBuilder()
        .type(HikariDataSource::class.java).build()

    @Bean
    @BatchTransactionManager
    fun metaTransactionManager(
        @BatchDataSource datasource: DataSource,
    ) : PlatformTransactionManager = JdbcTransactionManager(datasource)

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.finance")
    fun financeDataSourceProperties(): DataSourceProperties = DataSourceProperties()

    @Bean(name = ["dataSource", "financeDataSource"])
    @Primary
    fun financeDataSource(properties: DataSourceProperties): DataSource =
        properties.initializeDataSourceBuilder().type(HikariDataSource::class.java).build()

    @Primary
    @Bean(name = ["transactionManager", "financeTransactionManager"])
    fun financeTransactionManager(
        @Qualifier("financeDataSource") datasource: DataSource,
    ) : PlatformTransactionManager = JdbcTransactionManager(datasource)
}