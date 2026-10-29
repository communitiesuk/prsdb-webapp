package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.constants.enums.AdditionalPropertyOwnerType
import uk.gov.communities.prsdb.webapp.database.entity.AdditionalPropertyOwner
import uk.gov.communities.prsdb.webapp.database.entity.PropertyManager
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

    private fun anyPropertyOwnershipId() = propertyOwnershipRepository.findAll().minOf { it.id }

    private fun inTransaction(block: () -> Unit) = TransactionTemplate(transactionManager).executeWithoutResult { block() }

    @Test
    fun `deleting a property ownership deletes its additional property owners and property manager`() {
        // Arrange
        val propertyOwnershipId = anyPropertyOwnershipId()
        inTransaction {
            val propertyOwnership = propertyOwnershipRepository.findById(propertyOwnershipId).get()
            propertyOwnership.additionalPropertyOwners.addAll(
                listOf(
                    AdditionalPropertyOwner(AdditionalPropertyOwnerType.FREEHOLDER, propertyOwnership, name = "A Freeholder"),
                    // No name, email or address: a landlord may tell us they cannot provide the owner's details
                    AdditionalPropertyOwner(AdditionalPropertyOwnerType.SUPERIOR_LANDLORD, propertyOwnership),
                ),
            )
            // Saved separately because the association has no cascade, matching how License is handled
            propertyOwnership.propertyManager = propertyManagerRepository.save(PropertyManager("A Manager", "manager@example.com"))
            propertyOwnershipRepository.save(propertyOwnership)
        }
        assertEquals(1, countRowsIn("property_manager"))
        val ownerTypes =
            jdbcTemplate.queryForList(
                "SELECT owner_type FROM additional_property_owner WHERE property_ownership_id = ? ORDER BY owner_type",
                Int::class.java,
                propertyOwnershipId,
            )
        assertEquals(
            listOf(AdditionalPropertyOwnerType.FREEHOLDER.ordinal, AdditionalPropertyOwnerType.SUPERIOR_LANDLORD.ordinal),
            ownerTypes,
        )
        val superiorLandlord =
            jdbcTemplate.queryForMap(
                "SELECT name, email, address_id FROM additional_property_owner WHERE owner_type = ?",
                AdditionalPropertyOwnerType.SUPERIOR_LANDLORD.ordinal,
            )
        assertNull(superiorLandlord["name"])
        assertNull(superiorLandlord["email"])
        assertNull(superiorLandlord["address_id"])

        // Act
        inTransaction {
            propertyOwnershipRepository.delete(propertyOwnershipRepository.findById(propertyOwnershipId).get())
        }

        // Assert
        assertEquals(0, countRowsIn("additional_property_owner"))
        assertEquals(0, countRowsIn("property_manager"))
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
