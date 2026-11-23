package uk.gov.communities.prsdb.webapp.integration.database

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.sql.DriverManager

class SeedDataSqlTests {
    @Test
    fun `QA registered property backfill only writes changed landlords and is idempotent`() {
        val backfillSql =
            ClassPathResource("data-test.sql")
                .getContentAsString(Charsets.UTF_8)
                .split(";")
                .single { it.contains("SET has_registered_property = true") }

        PostgreSQLContainer(DockerImageName.parse("postgres:latest")).use { postgres ->
            postgres.start()
            DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password).use { connection ->
                connection.createStatement().use { statement ->
                    statement.execute(
                        """
                        CREATE TEMP TABLE landlord (id BIGINT PRIMARY KEY, has_registered_property BOOLEAN);
                        CREATE TEMP TABLE ownership_link (landlord_id BIGINT, landlordship_id BIGINT);
                        INSERT INTO landlord VALUES (1, false), (2, true), (3, false), (4, true), (5, null), (6, null);
                        INSERT INTO ownership_link VALUES (1, 1), (1, 2), (2, 3), (5, 4);
                        """.trimIndent(),
                    )

                    assertEquals(2, statement.executeUpdate(backfillSql), "Only false or null flags with ownership should be written")
                    statement.executeQuery("SELECT has_registered_property FROM landlord ORDER BY id").use { result ->
                        val flags =
                            buildList {
                                while (result.next()) add(result.getObject(1))
                            }
                        assertEquals(listOf(true, true, false, true, true, null), flags)
                    }
                    assertEquals(0, statement.executeUpdate(backfillSql), "Repeating the backfill should not rewrite any landlords")
                }
            }
        }
    }
}
