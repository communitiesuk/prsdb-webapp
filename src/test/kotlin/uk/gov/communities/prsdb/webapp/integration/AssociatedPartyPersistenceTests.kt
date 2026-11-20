package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.database.entity.PropertyManager
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord
import uk.gov.communities.prsdb.webapp.database.repository.FreeholderRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyManagerRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.SuperiorLandlordRepository
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

class AssociatedPartyPersistenceTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Autowired
    private lateinit var freeholderRepository: FreeholderRepository

    @Autowired
    private lateinit var superiorLandlordRepository: SuperiorLandlordRepository

    @Autowired
    private lateinit var propertyManagerRepository: PropertyManagerRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    private fun countRowsIn(table: String) = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM $table", Int::class.java)!!

    private fun propertyManagerNameFor(propertyOwnershipId: Long) =
        jdbcTemplate.queryForObject(
            "SELECT pm.name FROM property_manager pm JOIN property_ownership po ON po.property_manager_id = pm.id WHERE po.id = ?",
            String::class.java,
            propertyOwnershipId,
        )

    private fun countRowsFor(
        table: String,
        propertyOwnershipId: Long,
    ) = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM $table WHERE property_ownership_id = ?",
        Int::class.java,
        propertyOwnershipId,
    )!!

    private fun anyPropertyOwnershipId() = propertyOwnershipRepository.findAll().minOf { it.id }

    private fun anyActivePropertyOwnershipId() =
        jdbcTemplate.queryForObject("SELECT MIN(id) FROM property_ownership WHERE is_active = true", Long::class.java)!!

    private fun setOwnershipType(
        propertyOwnershipId: Long,
        ownershipType: OwnershipType,
    ) = jdbcTemplate.update(
        "UPDATE property_ownership SET ownership_type = ? WHERE id = ?",
        ownershipType.ordinal,
        propertyOwnershipId,
    )

    private fun updateOwnershipType(
        propertyOwnershipId: Long,
        ownershipType: OwnershipType,
    ) {
        val lastModifiedDate =
            inTransactionReturning { propertyOwnershipRepository.findById(propertyOwnershipId).get().getMostRecentlyUpdated() }

        propertyOwnershipService.updateOwnershipType(propertyOwnershipId, ownershipType, lastModifiedDate)
    }

    private fun inTransaction(block: () -> Unit) = TransactionTemplate(transactionManager).executeWithoutResult { block() }

    private fun <T : Any> inTransactionReturning(block: () -> T): T = TransactionTemplate(transactionManager).execute { block() }!!

    @Test
    fun `deleting a property ownership deletes its associated parties`() {
        // Arrange
        val propertyOwnershipId = anyPropertyOwnershipId()
        val seededPropertyManagerCount = countRowsIn("property_manager")
        inTransaction {
            val propertyOwnership = propertyOwnershipRepository.findById(propertyOwnershipId).get()
            propertyOwnership.freeholders.add(freeholderRepository.save(Freeholder(propertyOwnership, name = "A Freeholder")))
            // A superior landlord with no name or email: "no details known"
            propertyOwnership.superiorLandlords.add(superiorLandlordRepository.save(SuperiorLandlord(propertyOwnership)))
            propertyOwnership.propertyManager = propertyManagerRepository.save(PropertyManager("A Manager", "manager@example.com"))
            propertyOwnershipRepository.save(propertyOwnership)
        }
        assertEquals(seededPropertyManagerCount + 1, countRowsIn("property_manager"))
        val superiorLandlord =
            jdbcTemplate.queryForMap(
                "SELECT name, email FROM superior_landlord WHERE property_ownership_id = ?",
                propertyOwnershipId,
            )
        assertNull(superiorLandlord["name"])
        assertNull(superiorLandlord["email"])

        // Act
        inTransaction {
            propertyOwnershipRepository.delete(propertyOwnershipRepository.findById(propertyOwnershipId).get())
        }

        // Assert
        assertEquals(0, countRowsFor("freeholder", propertyOwnershipId))
        assertEquals(0, countRowsFor("superior_landlord", propertyOwnershipId))
        assertEquals(seededPropertyManagerCount, countRowsIn("property_manager"))
    }

    @Test
    fun `every seeded property whose ownership type requires freeholder records has at least one`() {
        // Types where the landlord is not the sole freeholder
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
    fun `updating to an ownership type that requires freeholder records creates a placeholder freeholder`() {
        // Arrange
        val propertyOwnershipId = anyActivePropertyOwnershipId()
        setOwnershipType(propertyOwnershipId, OwnershipType.SOLE_FREEHOLD)
        jdbcTemplate.update("DELETE FROM freeholder WHERE property_ownership_id = ?", propertyOwnershipId)

        // Act
        updateOwnershipType(propertyOwnershipId, OwnershipType.LEASEHOLD_ONLY)

        // Assert
        assertEquals(1, countRowsFor("freeholder", propertyOwnershipId))
        val freeholder =
            jdbcTemplate.queryForMap(
                "SELECT name, email FROM freeholder WHERE property_ownership_id = ?",
                propertyOwnershipId,
            )
        assertNull(freeholder["name"])
        assertNull(freeholder["email"])
    }

    @Test
    fun `updating to an ownership type that requires no freeholder records deletes the existing ones`() {
        // Arrange
        val propertyOwnershipId = anyActivePropertyOwnershipId()
        setOwnershipType(propertyOwnershipId, OwnershipType.LEASEHOLD_ONLY)
        inTransaction {
            val propertyOwnership = propertyOwnershipRepository.findById(propertyOwnershipId).get()
            propertyOwnership.freeholders.add(freeholderRepository.save(Freeholder(propertyOwnership)))
            propertyOwnershipRepository.save(propertyOwnership)
        }

        // Act
        updateOwnershipType(propertyOwnershipId, OwnershipType.SOLE_FREEHOLD)

        // Assert
        assertEquals(0, countRowsFor("freeholder", propertyOwnershipId))
    }

    @Test
    fun `replacing a property manager does not leave the previous one orphaned`() {
        // Arrange
        val propertyOwnershipId = anyPropertyOwnershipId()
        val seededPropertyManagerCount = countRowsIn("property_manager")
        inTransaction {
            val propertyOwnership = propertyOwnershipRepository.findById(propertyOwnershipId).get()
            propertyOwnership.propertyManager = propertyManagerRepository.save(PropertyManager("First Manager", "first@example.com"))
            propertyOwnershipRepository.save(propertyOwnership)
        }
        assertEquals(seededPropertyManagerCount + 1, countRowsIn("property_manager"))

        // Act
        inTransaction {
            val propertyOwnership = propertyOwnershipRepository.findById(propertyOwnershipId).get()
            propertyOwnership.propertyManager = propertyManagerRepository.save(PropertyManager("Second Manager", "second@example.com"))
            propertyOwnershipRepository.save(propertyOwnership)
        }

        // Assert
        assertEquals(seededPropertyManagerCount + 1, countRowsIn("property_manager"))
        assertEquals("Second Manager", propertyManagerNameFor(propertyOwnershipId))
    }

    @Test
    fun `removing a property manager from a property ownership deletes it from the database`() {
        // Arrange
        val propertyOwnershipId = anyPropertyOwnershipId()
        val seededPropertyManagerCount = countRowsIn("property_manager")
        inTransaction {
            val propertyOwnership = propertyOwnershipRepository.findById(propertyOwnershipId).get()
            propertyOwnership.propertyManager = propertyManagerRepository.save(PropertyManager("A Manager", "manager@example.com"))
            propertyOwnershipRepository.save(propertyOwnership)
        }
        assertEquals(seededPropertyManagerCount + 1, countRowsIn("property_manager"))

        // Act
        inTransaction {
            propertyOwnershipService.removePropertyManager(
                propertyOwnershipId,
                propertyOwnershipService.getLastModifiedDateIncludingPropertyManager(propertyOwnershipId),
            )
        }

        // Assert
        assertEquals(seededPropertyManagerCount, countRowsIn("property_manager"))
    }
}
