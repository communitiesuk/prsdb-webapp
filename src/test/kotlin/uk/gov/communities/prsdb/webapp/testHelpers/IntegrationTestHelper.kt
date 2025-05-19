package uk.gov.communities.prsdb.webapp.testHelpers

import org.flywaydb.core.Flyway
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.util.ResourceUtils

class IntegrationTestHelper {
    companion object {
        fun resetDatabase(flyway: Flyway) {
            flyway.clean()
            flyway.migrate()
        }

        fun seedDatabase(
            scripts: List<String>,
            jdbcTemplate: JdbcTemplate,
        ) {
            scripts.forEach { script ->
                val scriptContents = ResourceUtils.getFile("classpath:$script").readText()
                jdbcTemplate.execute(scriptContents)
            }
        }
    }
}
