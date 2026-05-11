package com.finance.batch.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.support.JdbcTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration(proxyBeanMethods = false)
class DataSourceConfig {

    @Primary
    @Bean(name = ["dataSource", "metaDataSource"])
    @ConfigurationProperties(prefix = "spring.datasource.meta")
    fun metaDataSource(): DataSource =
        DataSourceBuilder.create().type(HikariDataSource::class.java).build()

    @Bean("financeDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.finance")
    fun financeDataSource(): DataSource =
        DataSourceBuilder.create().type(HikariDataSource::class.java).build()

    @Primary
    @Bean(name = ["transactionManager", "metaTransactionManager"])
    fun metaTransactionManager(
        @Qualifier("metaDataSource") datasource: DataSource,
    ) : PlatformTransactionManager = JdbcTransactionManager(datasource)

    @Bean("financeTransactionManager")
    fun financeTransactionManager(
        @Qualifier("financeDataSource") dataSource: DataSource,
    ) : PlatformTransactionManager = JdbcTransactionManager(dataSource)
}