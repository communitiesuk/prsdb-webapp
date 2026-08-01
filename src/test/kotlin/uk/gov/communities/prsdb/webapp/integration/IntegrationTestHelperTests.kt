package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.communities.prsdb.webapp.testHelpers.IntegrationTestHelper

class IntegrationTestHelperTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    private fun countRowsIn(table: String) = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM $table", Int::class.java)!!

    @Test
    fun `resetDatabase clears seeded data`() {
        assertTrue(countRowsIn("prsdb_user") > 0, "seed data should have been loaded")

        IntegrationTestHelper.resetDatabase(jdbcTemplate)

        assertEquals(0, countRowsIn("prsdb_user"))
        assertEquals(0, countRowsIn("landlord"))
        assertEquals(0, countRowsIn("property_ownership"))
    }

    @Test
    fun `resetDatabase preserves local council reference data`() {
        val localCouncilsBefore = countRowsIn("local_council")
        assertTrue(localCouncilsBefore > 0, "migrations should have inserted local councils")

        IntegrationTestHelper.resetDatabase(jdbcTemplate)

        assertEquals(localCouncilsBefore, countRowsIn("local_council"))
    }

    @Test
    fun `resetDatabase preserves the flyway schema history`() {
        val migrationsBefore = countRowsIn("flyway_schema_history")
        assertTrue(migrationsBefore > 0, "migrations should have been recorded")

        IntegrationTestHelper.resetDatabase(jdbcTemplate)

        assertEquals(migrationsBefore, countRowsIn("flyway_schema_history"))
    }

    @Test
    fun `resetDatabase restarts identity sequences`() {
        IntegrationTestHelper.resetDatabase(jdbcTemplate)

        val id =
            jdbcTemplate.queryForObject(
                "INSERT INTO address (single_line_address, postcode) VALUES ('1 Test Street, EG1 2AB', 'EG1 2AB') RETURNING id",
                Long::class.java,
            )

        assertEquals(1L, id)
    }
}
