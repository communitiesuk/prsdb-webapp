package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.communities.prsdb.webapp.constants.enums.AdditionalPropertyOwnerType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipTypeBeforePdjb955
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.AdditionalPropertyOwner
import uk.gov.communities.prsdb.webapp.database.entity.PropertyManager
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyManagerRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.time.MonthDay

class AssociatedPartyPersistenceTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    private lateinit var propertyManagerRepository: PropertyManagerRepository

    @Autowired
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Autowired
    private lateinit var landlordRepository: LandlordRepository

    @Autowired
    private lateinit var addressService: AddressService

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

    @Test
    fun `creating a property ownership with a property manager links the saved property manager`() {
        // Arrange
        var propertyOwnershipId = 0L
        inTransaction {
            val propertyManager = propertyManagerRepository.save(PropertyManager("A Manager", "manager@example.com"))
            val address =
                addressService.findOrCreateAddress(AddressDataModel("1 Property Manager Test Road, EG1 2AB", postcode = "EG1 2AB"))
            val propertyOwnership =
                propertyOwnershipService.createPropertyOwnership(
                    ownershipType = OwnershipTypeBeforePdjb955.FREEHOLD,
                    isOccupied = false,
                    numberOfHouseholds = 0,
                    numberOfPeople = 0,
                    registeringLandlord = landlordRepository.findAll().first(),
                    anniversary = MonthDay.of(1, 1),
                    propertyBuildType = PropertyType.DETACHED_HOUSE,
                    address = address,
                    propertyManager = propertyManager,
                    numBedrooms = 1,
                    billsIncludedList = null,
                    customBillsIncluded = null,
                    furnishedStatus = null,
                    rentFrequency = null,
                    customRentFrequency = null,
                    rentAmount = null,
                    customPropertyType = null,
                    correspondenceEmail = "correspondence@example.com",
                    correspondenceAddressModel = AddressDataModel("2 Correspondence Road, EG1 2AB", postcode = "EG1 2AB"),
                )
            propertyOwnershipId = propertyOwnership.id
        }

        // Act
        val linkedName =
            jdbcTemplate.queryForObject(
                "SELECT pm.name FROM property_ownership po JOIN property_manager pm ON pm.id = po.property_manager_id WHERE po.id = ?",
                String::class.java,
                propertyOwnershipId,
            )

        // Assert
        assertEquals("A Manager", linkedName)
    }
}
