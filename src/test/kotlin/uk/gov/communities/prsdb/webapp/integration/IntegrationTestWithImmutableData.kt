package uk.gov.communities.prsdb.webapp.integration

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.communities.prsdb.webapp.testHelpers.IntegrationTestHelper

abstract class IntegrationTestWithImmutableData(
    private val seedDataScripts: List<String>,
) : IntegrationTest() {
    constructor(script: String) : this(listOf(script))

    @BeforeAll
    fun setUpBeforeAll(
        @Autowired flyway: Flyway,
        @Autowired jdbcTemplate: JdbcTemplate,
    ) {
        IntegrationTestHelper.resetAndSeedDatabase(flyway, seedDataScripts, jdbcTemplate)
    }

    abstract class NestedIntegrationTestWithImmutableData(
        private val seedDataScripts: List<String>,
    ) : NestedIntegrationTest() {
        constructor(script: String) : this(listOf(script))

        @BeforeAll
        fun setUpBeforeAll(
            @Autowired flyway: Flyway,
            @Autowired jdbcTemplate: JdbcTemplate,
        ) {
            IntegrationTestHelper.resetAndSeedDatabase(flyway, seedDataScripts, jdbcTemplate)
        }
    }
}
