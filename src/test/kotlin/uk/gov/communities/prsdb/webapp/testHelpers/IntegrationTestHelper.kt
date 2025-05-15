package uk.gov.communities.prsdb.webapp.testHelpers

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.TestInfo
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.util.ResourceUtils
import kotlin.reflect.full.findAnnotation

class IntegrationTestHelper {
    companion object {
        fun resetDatabase(flyway: Flyway) {
            flyway.clean()
            flyway.migrate()
        }

        fun seedDatabaseBeforeAll(
            testInfo: TestInfo,
            jdbcTemplate: JdbcTemplate,
        ) {
            val seedDataScripts =
                testInfo.testClass
                    .get()
                    .kotlin
                    .findAnnotation<SqlBeforeAll>()
                    ?.scripts
                    ?: return

            seedDataScripts.forEach { scriptName ->
                val seedDataScript = ResourceUtils.getFile("classpath:.$scriptName").readText()
                jdbcTemplate.execute(seedDataScript)
            }
        }
    }
}
