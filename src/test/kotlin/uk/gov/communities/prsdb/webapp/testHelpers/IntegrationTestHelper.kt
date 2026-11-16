package uk.gov.communities.prsdb.webapp.testHelpers

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.util.ResourceUtils

class IntegrationTestHelper {
    companion object {
        // Tables that must survive a reset. flyway_schema_history is Flyway's own bookkeeping - dropping it
        // would cause every migration to re-run on the next context start. local_council holds reference
        // data inserted by V1_0_0__la_and_address_tables.sql (as local_authority, renamed by V1_6_0) and is
        // never written to by a seed script. These are the only two tables with rows after a bare migrate.
        // Any future migration that inserts reference data must add its table to this set.
        val PRESERVED_TABLES = setOf("flyway_schema_history", "local_council")

        // Scripts appended after whatever seed scripts a test asks for, so that every seed does not have to
        // repeat them. They must be idempotent, since they also run over data a seed script has already set
        // up, and order matters: each runs after the scripts a test named. Anything added here must also be
        // added to spring.sql.init.data-locations in application-local.yml. See docs/IntegrationTestReadMe.md.
        private val ALWAYS_APPENDED_SCRIPTS = listOf("data-placeholder-freeholders.sql")

        fun resetAndSeedDatabase(
            scripts: List<String>,
            jdbcTemplate: JdbcTemplate,
        ) {
            resetDatabase(jdbcTemplate)
            seedDatabase(scripts, jdbcTemplate)
        }

        fun resetDatabase(jdbcTemplate: JdbcTemplate) {
            val tablesToTruncate =
                jdbcTemplate
                    .queryForList("SELECT tablename FROM pg_tables WHERE schemaname = 'public'", String::class.java)
                    .filterNot { it in PRESERVED_TABLES }

            check(tablesToTruncate.isNotEmpty()) {
                "No truncatable tables found in the public schema - have the Flyway migrations run?"
            }

            val quotedTables = tablesToTruncate.joinToString(", ") { "\"$it\"" }
            jdbcTemplate.execute("TRUNCATE TABLE $quotedTables RESTART IDENTITY CASCADE")
        }

        private fun seedDatabase(
            scripts: List<String>,
            jdbcTemplate: JdbcTemplate,
        ) {
            (scripts + ALWAYS_APPENDED_SCRIPTS).forEach { script ->
                val scriptContents = ResourceUtils.getFile("classpath:$script").readText()
                jdbcTemplate.execute(scriptContents)
            }
        }
    }
}
