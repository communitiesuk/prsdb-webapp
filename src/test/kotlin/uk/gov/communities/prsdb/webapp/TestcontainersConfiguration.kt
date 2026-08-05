package uk.gov.communities.prsdb.webapp

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    fun postgresContainer(): SharedPostgresContainer = SharedContainers.postgres

    @Bean
    @ServiceConnection(name = "redis")
    fun redisContainer(): SharedRedisContainer = SharedContainers.redis
}
