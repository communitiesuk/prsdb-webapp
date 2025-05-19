package uk.gov.communities.prsdb.webapp.testHelpers

import org.flywaydb.core.Flyway
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.util.ResourceUtils

class IntegrationTestHelper {
    companion object {
        fun resetAndSeedDatabase(
            flyway: Flyway,
            scripts: List<String>,
            jdbcTemplate: JdbcTemplate,
        ) {
            resetDatabase(flyway)
            seedDatabase(scripts, jdbcTemplate)
        }

        private fun resetDatabase(flyway: Flyway) {
            flyway.clean()
            flyway.migrate()
        }

        private fun seedDatabase(
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
