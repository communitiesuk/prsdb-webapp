package uk.gov.communities.prsdb.webapp.integration

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.communities.prsdb.webapp.testHelpers.IntegrationTestHelper

abstract class JourneyTestWithSeedData(
    private val scripts: List<String>,
) : IntegrationTest() {
    constructor(script: String) : this(listOf(script))

    @BeforeEach
    fun setUpBeforeEach(
        @Autowired flyway: Flyway,
        @Autowired jdbcTemplate: JdbcTemplate,
    ) {
        IntegrationTestHelper.resetAndSeedDatabase(flyway, scripts, jdbcTemplate)
    }

    abstract class NestedJourneyTestWithSeedData(
        private val scripts: List<String>,
    ) : NestedIntegrationTest() {
        constructor(script: String) : this(listOf(script))

        @BeforeEach
        fun setUpBeforeEach(
            @Autowired flyway: Flyway,
            @Autowired jdbcTemplate: JdbcTemplate,
        ) {
            IntegrationTestHelper.resetAndSeedDatabase(flyway, scripts, jdbcTemplate)
        }
    }
}
