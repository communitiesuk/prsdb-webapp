package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.database.entity.PropertyManager
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord
import uk.gov.communities.prsdb.webapp.database.repository.AddressRepository
import uk.gov.communities.prsdb.webapp.database.repository.FreeholderRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyManagerRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.SuperiorLandlordRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
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
    private lateinit var addressRepository: AddressRepository

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

    private fun countAddressesWithId(addressId: Long) =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM address WHERE id = ?", Int::class.java, addressId)!!

    private fun inTransaction(block: () -> Unit) = TransactionTemplate(transactionManager).executeWithoutResult { block() }

    private fun <T : Any> inTransactionReturning(block: () -> T): T = TransactionTemplate(transactionManager).execute { block() }!!

    @Test
    fun `deleting a property ownership deletes its associated parties but not their addresses`() {
        // Arrange
        val propertyOwnershipId = anyPropertyOwnershipId()
        val freeholderId =
            inTransactionReturning {
                val propertyOwnership = propertyOwnershipRepository.findById(propertyOwnershipId).get()
                // Referenced only by the freeholder, so the delete orphans it
                val freeholderAddress =
                    addressRepository.save(
                        Address(AddressDataModel(singleLineAddress = "1 Freeholder Way, EG1 2AB", postcode = "EG1 2AB")),
                    )
                val freeholder =
                    freeholderRepository.save(Freeholder(propertyOwnership, name = "A Freeholder", address = freeholderAddress))
                propertyOwnership.freeholders.add(freeholder)
                // A superior landlord with no name, email or address: "no details known"
                propertyOwnership.superiorLandlords.add(superiorLandlordRepository.save(SuperiorLandlord(propertyOwnership)))
                propertyOwnership.propertyManager = propertyManagerRepository.save(PropertyManager("A Manager", "manager@example.com"))
                propertyOwnershipRepository.save(propertyOwnership)
                freeholder.id
            }
        assertEquals(1, countRowsIn("property_manager"))
        val freeholderAddressId =
            jdbcTemplate.queryForObject("SELECT address_id FROM freeholder WHERE id = ?", Long::class.java, freeholderId)!!
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
        // Addresses are never deleted, even once nothing references them
        assertEquals(1, countAddressesWithId(freeholderAddressId))
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
                "SELECT name, email, address_id FROM freeholder WHERE property_ownership_id = ?",
                propertyOwnershipId,
            )
        assertNull(freeholder["name"])
        assertNull(freeholder["email"])
        assertNull(freeholder["address_id"])
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

    @Test
    fun `removing a property manager from a property ownership deletes it from the database`() {
        // Arrange
        val propertyOwnershipId = anyPropertyOwnershipId()
        inTransaction {
            val propertyOwnership = propertyOwnershipRepository.findById(propertyOwnershipId).get()
            propertyOwnership.propertyManager = propertyManagerRepository.save(PropertyManager("A Manager", "manager@example.com"))
            propertyOwnershipRepository.save(propertyOwnership)
        }
        assertEquals(1, countRowsIn("property_manager"))

        // Act
        inTransaction {
            propertyOwnershipService.removePropertyManager(propertyOwnershipRepository.findById(propertyOwnershipId).get())
        }

        // Assert
        assertEquals(0, countRowsIn("property_manager"))
    }
}
