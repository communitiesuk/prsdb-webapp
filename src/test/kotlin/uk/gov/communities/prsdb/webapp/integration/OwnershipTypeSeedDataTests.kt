package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

// Verifies that the seed scripts populate ownership_type and ownership_type_before_pdjb955
// consistently. This does NOT exercise the backfill in V1_61_0__add_ownership_type_and_associated_party_tables.sql:
// the seed data is loaded after Flyway has already run, so the migration's UPDATE never sees
// these rows. The backfill itself is not testable post-migration because ownership_type is
// NOT NULL by the time the context is up.
// TODO PDJB-2019: delete this class once the legacy column is dropped.
class OwnershipTypeSeedDataTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    @Test
    fun `every property ownership has a non-null new ownership type`() {
        // Act
        val unmapped =
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM property_ownership WHERE ownership_type IS NULL",
                Int::class.java,
            )!!

        // Assert
        assertEquals(0, unmapped)
    }

    @Test
    fun `every legacy ownership type maps to the ordinal the converter produces`() {
        // Arrange
        val expectedPairs = mapOf(0 to 2, 1 to 1, 2 to 3, 3 to 5)

        // Act
        // Deliberately not DISTINCT and not collapsed into a map: every row is asserted
        // individually so that a single mis-mapped row cannot be masked by a correct one
        // sharing the same legacy value.
        val rows =
            jdbcTemplate.queryForList(
                "SELECT id, ownership_type_before_pdjb955 AS legacy, ownership_type AS current " +
                    "FROM property_ownership WHERE ownership_type_before_pdjb955 IS NOT NULL ORDER BY id",
            )

        // Assert
        assertTrue(rows.isNotEmpty(), "seed data should contain legacy ownership types")
        rows.forEach { row ->
            val legacy = row["legacy"].toString().toInt()
            val current = row["current"].toString().toInt()
            assertEquals(
                expectedPairs[legacy],
                current,
                "property_ownership ${row["id"]}: legacy ownership type $legacy mapped to $current",
            )
        }
    }
}
