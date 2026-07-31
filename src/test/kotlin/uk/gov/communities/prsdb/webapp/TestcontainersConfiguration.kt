package uk.gov.communities.prsdb.webapp

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    companion object {
        // One Postgres and one Redis per JVM, shared by every Spring context. Without this, each of the
        // ~16 distinct integration contexts starts its own pair and a serial run ends with 32 containers
        // alive, none of which are ever released.
        //
        // stop() is deliberately a no-op. Spring Boot closes container beans when their context is
        // destroyed, and the test context cache is LRU with a default maxSize of 32 against more contexts
        // than that in a full run. An eviction would otherwise stop these containers out from under the
        // contexts still using them. Testcontainers' Ryuk sidecar removes them when the JVM exits.
        private val postgres: PostgreSQLContainer<*> =
            object : PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:latest")) {
                override fun stop() = Unit
            }.apply {
                // One container now serves every Spring context, and each context keeps its own Hikari
                // pool, so Postgres' default limit of 100 connections is exhausted once ~10 contexts exist.
                setCommand("postgres", "-c", "max_connections=400")
                start()
            }

        private val redis: GenericContainer<*> =
            object : GenericContainer<Nothing>(DockerImageName.parse("redis:latest")) {
                override fun stop() = Unit
            }.apply {
                addExposedPort(6379)
                start()
            }
    }

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> = postgres

    @Bean
    @ServiceConnection(name = "redis")
    fun redisContainer(): GenericContainer<*> = redis
}
