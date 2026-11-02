package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.database.entity.PropertyManager
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord
import uk.gov.communities.prsdb.webapp.database.repository.PropertyManagerRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository

class AssociatedPartyPersistenceTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    private lateinit var propertyManagerRepository: PropertyManagerRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    private fun countRowsIn(table: String) = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM $table", Int::class.java)!!

    private fun countRowsFor(
        table: String,
        propertyOwnershipId: Long,
    ) = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM $table WHERE property_ownership_id = ?",
        Int::class.java,
        propertyOwnershipId,
    )!!

    private fun anyPropertyOwnershipId() = propertyOwnershipRepository.findAll().minOf { it.id }

    private fun inTransaction(block: () -> Unit) = TransactionTemplate(transactionManager).executeWithoutResult { block() }

    @Test
    fun `deleting a property ownership deletes its freeholders, superior landlords and property manager`() {
        // Arrange
        val propertyOwnershipId = anyPropertyOwnershipId()
        inTransaction {
            val propertyOwnership = propertyOwnershipRepository.findById(propertyOwnershipId).get()
            propertyOwnership.freeholders.add(Freeholder(propertyOwnership, name = "A Freeholder"))
            // No name, email or address: a landlord may tell us they cannot provide the party's details
            propertyOwnership.superiorLandlords.add(SuperiorLandlord(propertyOwnership))
            // Saved separately because the association has no cascade, matching how License is handled
            propertyOwnership.propertyManager = propertyManagerRepository.save(PropertyManager("A Manager", "manager@example.com"))
            propertyOwnershipRepository.save(propertyOwnership)
        }
        assertEquals(1, countRowsIn("property_manager"))
        // The seed data already gives this property a "no details known" placeholder freeholder
        val freeholderNames =
            jdbcTemplate.queryForList(
                "SELECT name FROM freeholder WHERE property_ownership_id = ?",
                String::class.java,
                propertyOwnershipId,
            )
        assertTrue(freeholderNames.contains("A Freeholder"))
        val superiorLandlord =
            jdbcTemplate.queryForMap(
                "SELECT name, email, address_id FROM superior_landlord WHERE property_ownership_id = ?",
                propertyOwnershipId,
            )
        assertNull(superiorLandlord["name"])
        assertNull(superiorLandlord["email"])
        assertNull(superiorLandlord["address_id"])

        // Act
        inTransaction {
            propertyOwnershipRepository.delete(propertyOwnershipRepository.findById(propertyOwnershipId).get())
        }

        // Assert
        assertEquals(0, countRowsFor("freeholder", propertyOwnershipId))
        assertEquals(0, countRowsFor("superior_landlord", propertyOwnershipId))
        assertEquals(0, countRowsIn("property_manager"))
    }

    @Test
    fun `every seeded property whose ownership type implies another freeholder has at least one`() {
        // Ownership types whose holder is not the sole freeholder: NONE, LEASEHOLD_ONLY,
        // JOINT_TENANTS, TENANTS_IN_COMMON, SHARE_OF_JOINT_TENANTS, SHARE_OF_TENANTS_IN_COMMON.
        val propertiesWithoutAFreeholder =
            jdbcTemplate.queryForList(
                "SELECT po.id FROM property_ownership po " +
                    "WHERE po.ownership_type IN (0, 1, 3, 4, 7, 8) " +
                    "AND NOT EXISTS (SELECT 1 FROM freeholder f WHERE f.property_ownership_id = po.id)",
                Long::class.java,
            )

        assertEquals(emptyList<Long>(), propertiesWithoutAFreeholder)
    }

    @Test
    fun `replacing a property manager does not leave the previous one orphaned`() {
        // Arrange
        val propertyOwnershipId = anyPropertyOwnershipId()
        inTransaction {
            val propertyOwnership = propertyOwnershipRepository.findById(propertyOwnershipId).get()
            propertyOwnership.propertyManager = propertyManagerRepository.save(PropertyManager("First Manager", "first@example.com"))
            propertyOwnershipRepository.save(propertyOwnership)
        }
        assertEquals(1, countRowsIn("property_manager"))

        // Act
        inTransaction {
            val propertyOwnership = propertyOwnershipRepository.findById(propertyOwnershipId).get()
            propertyOwnership.propertyManager = propertyManagerRepository.save(PropertyManager("Second Manager", "second@example.com"))
            propertyOwnershipRepository.save(propertyOwnership)
        }

        // Assert
        assertEquals(1, countRowsIn("property_manager"))
        assertEquals("Second Manager", jdbcTemplate.queryForObject("SELECT name FROM property_manager", String::class.java))
    }
}
