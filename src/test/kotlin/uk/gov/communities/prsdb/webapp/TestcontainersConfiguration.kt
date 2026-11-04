package uk.gov.communities.prsdb.webapp

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistrar

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    fun postgresContainer(): SharedPostgresContainer = SharedContainers.postgres

    @Bean
    @ServiceConnection(name = "redis")
    fun redisContainer(): SharedRedisContainer = SharedContainers.redis

    @Bean
    fun elasticMqContainer(): SharedElasticMqContainer = SharedContainers.elasticMq

    @Bean
    fun elasticMqProperties(elasticMqContainer: SharedElasticMqContainer) =
        DynamicPropertyRegistrar { registry ->
            registry.add("local.sqs.endpoint") {
                "http://${elasticMqContainer.host}:${elasticMqContainer.getMappedPort(SharedContainers.ELASTICMQ_PORT)}"
            }
        }
}
