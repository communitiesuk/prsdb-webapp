package uk.gov.communities.prsdb.webapp.integration

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.communities.prsdb.webapp.testHelpers.IntegrationTestHelper

abstract class SinglePageTestWithSeedData(
    private val scripts: List<String>,
) : IntegrationTest() {
    constructor(script: String) : this(listOf(script))

    @BeforeAll
    fun setUpBeforeAll(
        @Autowired flyway: Flyway,
        @Autowired jdbcTemplate: JdbcTemplate,
    ) {
        IntegrationTestHelper.resetDatabase(flyway)
        IntegrationTestHelper.seedDatabase(scripts, jdbcTemplate)
    }

    abstract class NestedSinglePageTestWithSeedData(
        private val scripts: List<String>,
    ) : NestedIntegrationTestWithSeedData() {
        constructor(script: String) : this(listOf(script))

        @BeforeAll
        fun setUpBeforeAll(
            @Autowired flyway: Flyway,
            @Autowired jdbcTemplate: JdbcTemplate,
        ) {
            IntegrationTestHelper.resetDatabase(flyway)
            IntegrationTestHelper.seedDatabase(scripts, jdbcTemplate)
        }
    }
}
