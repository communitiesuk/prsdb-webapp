package uk.gov.communities.prsdb.webapp

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

// The containers are shared by every Spring test context in the JVM, so they must outlive any one context.
// Spring closes container beans when a context is destroyed, which would take the containers away from every
// other cached context, so closing is suppressed here. Testcontainers' own reaper stops them when the JVM exits.
class SharedPostgresContainer(
    image: DockerImageName,
) : PostgreSQLContainer<SharedPostgresContainer>(image) {
    override fun stop() = Unit

    override fun close() = Unit
}

class SharedRedisContainer(
    image: DockerImageName,
) : GenericContainer<SharedRedisContainer>(image) {
    override fun stop() = Unit

    override fun close() = Unit
}

object SharedContainers {
    val postgres: SharedPostgresContainer =
        SharedPostgresContainer(DockerImageName.parse("postgres:latest")).apply {
            // One Postgres now serves every context, so it needs headroom for all their connection pools.
            setCommand("postgres", "-c", "max_connections=400")
            start()
        }

    val redis: SharedRedisContainer =
        SharedRedisContainer(DockerImageName.parse("redis:latest")).apply {
            addExposedPort(6379)
            start()
        }
}
