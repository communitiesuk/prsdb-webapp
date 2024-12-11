package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

@ExtendWith(MockitoExtension::class)
class PropertyRegistrationServiceTests {
    @Mock
    private lateinit var propertyRepository: PropertyRepository

    @Mock
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @InjectMocks
    private lateinit var propertyRegistrationService: PropertyRegistrationService

    @Test
    fun `getAddressIsRegistered returns false if no matching property is found`() {
        val uprn = 123456.toLong()
        whenever(propertyRepository.findByAddress_Uprn(uprn)).thenReturn(null)

        assertFalse(propertyRegistrationService.getIsAddressRegistered(uprn))
    }

    @Test
    fun `getAddressIsRegistered returns false if the property is inactive`() {
        val uprn = 123456.toLong()
        val address =
            Address(
                AddressDataModel(
                    singleLineAddress = "1 Street Name, City, AB1 2CD",
                    uprn = uprn,
                ),
            )
        val propertyId = 789.toLong()

        val property = Property(id = propertyId, address = address, isActive = false)
        whenever(propertyRepository.findByAddress_Uprn(uprn)).thenReturn(property)

        assertFalse(propertyRegistrationService.getIsAddressRegistered(uprn))
    }

    @Test
    fun `getAddressIsRegistered returns false if the active property has no ownerships`() {
        val uprn = 123456.toLong()
        val address =
            Address(
                AddressDataModel(
                    singleLineAddress = "1 Street Name, City, AB1 2CD",
                    uprn = uprn,
                ),
            )
        val propertyId = 789.toLong()

        val property = Property(id = propertyId, address = address, isActive = true)
        whenever(propertyRepository.findByAddress_Uprn(uprn)).thenReturn(property)

        whenever(propertyOwnershipRepository.findByProperty_Id(propertyId)).thenReturn(listOf())

        assertFalse(propertyRegistrationService.getIsAddressRegistered(uprn))
    }

    @Test
    fun `getAddressIsRegistered returns false if the active property has no active ownerships`() {
        val uprn = 123456.toLong()
        val address =
            Address(
                AddressDataModel(
                    singleLineAddress = "1 Street Name, City, AB1 2CD",
                    uprn = uprn,
                ),
            )
        val propertyId = 789.toLong()

        val property = Property(id = propertyId, address = address, isActive = true)
        whenever(propertyRepository.findByAddress_Uprn(uprn)).thenReturn(property)

        val inactiveOwnership = PropertyOwnership(id = 123.toLong(), isActive = false)
        whenever(propertyOwnershipRepository.findByProperty_Id(propertyId)).thenReturn(listOf(inactiveOwnership))

        assertFalse(propertyRegistrationService.getIsAddressRegistered(uprn))
    }

    @Test
    fun `getAddressIsRegistered returns true if the active property has one active ownership`() {
        val uprn = 123456.toLong()
        val address =
            Address(
                AddressDataModel(
                    singleLineAddress = "1 Street Name, City, AB1 2CD",
                    uprn = uprn,
                ),
            )
        val propertyId = 789.toLong()

        val property = Property(id = propertyId, address = address, isActive = true)
        whenever(propertyRepository.findByAddress_Uprn(uprn)).thenReturn(property)

        val activeOwnership = PropertyOwnership(id = 456.toLong(), isActive = true)
        whenever(propertyOwnershipRepository.findByProperty_Id(propertyId)).thenReturn(listOf(activeOwnership))

        assertTrue(propertyRegistrationService.getIsAddressRegistered(uprn))
    }

    @Test
    fun `getAddressIsRegistered returns false if the active property has multiple active ownerships`() {
        val uprn = 123456.toLong()
        val address =
            Address(
                AddressDataModel(
                    singleLineAddress = "1 Street Name, City, AB1 2CD",
                    uprn = uprn,
                ),
            )
        val propertyId = 789.toLong()

        val property = Property(id = propertyId, address = address, isActive = true)
        whenever(propertyRepository.findByAddress_Uprn(uprn)).thenReturn(property)

        val inactiveOwnership = PropertyOwnership(id = 123.toLong(), isActive = false)
        val activeOwnership1 = PropertyOwnership(id = 456.toLong(), isActive = true)
        val activeOwnership2 = PropertyOwnership(id = 789.toLong(), isActive = true)
        whenever(propertyOwnershipRepository.findByProperty_Id(propertyId)).thenReturn(
            listOf(inactiveOwnership, activeOwnership1, activeOwnership2),
        )

        assertTrue(propertyRegistrationService.getIsAddressRegistered(uprn))
    }
}
