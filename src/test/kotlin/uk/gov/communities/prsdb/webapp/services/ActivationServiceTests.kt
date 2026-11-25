package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.exceptions.PropertyAlreadyReactivatedException
import uk.gov.communities.prsdb.webapp.helpers.RenewalDateHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.LocalDate
import java.time.MonthDay
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ActivationServiceTests {
    @Mock
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var landlordRepository: LandlordRepository

    @Mock
    private lateinit var entityManager: EntityManager

    @InjectMocks
    private lateinit var activationService: ActivationService

    @Test
    fun `reactivateLandlord marks an inactive landlord active, clears the date and saves it`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)

        activationService.reactivateLandlord(landlord)

        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `reactivateLandlord throws and makes no change for an already active landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()

        assertThrows<IllegalStateException> { activationService.reactivateLandlord(landlord) }

        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `reactivateLandlordIfInactive marks an inactive landlord active, clears the date and saves it`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)

        activationService.reactivateLandlordIfInactive(landlord)

        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `reactivateLandlordIfInactive makes no change for an already active landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()

        activationService.reactivateLandlordIfInactive(landlord)

        assertTrue(landlord.isActive)
        verify(landlordRepository, never()).save(any())
    }

    @Test
    fun `reactivateProperty marks an inactive property active, clears the date and saves it`() {
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID)
        property.deactivate(ORIGINAL_DATE)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        activationService.reactivateProperty(PROPERTY_ID, reactivatingLandlord())

        assertTrue(property.isActive)
        assertNull(property.lastDeactivatedDate)
        verify(propertyOwnershipRepository).save(property)
    }

    @Test
    fun `reactivateProperty reactivates an inactive landlord, clears the date and saves it`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, landlords = mutableSetOf(landlord))
        property.deactivate(ORIGINAL_DATE)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        activationService.reactivateProperty(PROPERTY_ID, reactivatingLandlord())

        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `reactivateProperty only reactivates the joint landlords who are inactive`() {
        val activeLandlord = MockLandlordData.createIndividualLandlord(name = "active")
        val inactiveLandlord = MockLandlordData.createIndividualLandlord(name = "inactive")
        inactiveLandlord.deactivate(ORIGINAL_DATE)
        val property =
            MockLandlordData.createPropertyOwnership(
                id = PROPERTY_ID,
                landlords = mutableSetOf(activeLandlord, inactiveLandlord),
            )
        property.deactivate(ORIGINAL_DATE)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        activationService.reactivateProperty(PROPERTY_ID, reactivatingLandlord())

        assertTrue(activeLandlord.isActive)
        assertTrue(inactiveLandlord.isActive)
        verify(landlordRepository).save(inactiveLandlord)
        verify(landlordRepository, never()).save(activeLandlord)
    }

    @Test
    fun `reactivateProperty throws and makes no change for an already active property`() {
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, isActive = true)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        assertThrows<IllegalStateException> { activationService.reactivateProperty(PROPERTY_ID, reactivatingLandlord()) }

        verify(propertyOwnershipRepository, never()).save(any())
    }

    @Test
    fun `reactivateProperty throws NOT_FOUND for an unknown property`() {
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.empty())

        val exception =
            assertThrows<ResponseStatusException> {
                activationService.reactivateProperty(PROPERTY_ID, reactivatingLandlord())
            }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `reactivateProperty moves the renewal date to the next occurrence of the landlord's anniversary when they differ`() {
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, renewalDate = LocalDate.of(2027, 1, 1))
        property.deactivate(ORIGINAL_DATE)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))
        val reactivatingLandlordAnniversary = MonthDay.of(6, 15)

        activationService.reactivateProperty(PROPERTY_ID, reactivatingLandlord(reactivatingLandlordAnniversary))

        assertEquals(RenewalDateHelper.getRenewalDate(reactivatingLandlordAnniversary), property.renewalDate)
        verify(propertyOwnershipRepository).save(property)
    }

    @Test
    fun `reactivateProperty leaves the renewal date unchanged when it is on the landlord's anniversary, even if it has passed`() {
        val pastRenewalDate = LocalDate.of(2020, 6, 15)
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, renewalDate = pastRenewalDate)
        property.deactivate(ORIGINAL_DATE)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        activationService.reactivateProperty(PROPERTY_ID, reactivatingLandlord(MonthDay.of(6, 15)))

        assertEquals(pastRenewalDate, property.renewalDate)
    }

    @Test
    fun `reactivateProperty does not change paidForUntil when it updates the renewal date`() {
        val paidForUntil = LocalDate.of(2027, 1, 1)
        val property =
            MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, renewalDate = paidForUntil, paidForUntil = paidForUntil)
        property.deactivate(ORIGINAL_DATE)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        activationService.reactivateProperty(PROPERTY_ID, reactivatingLandlord(MonthDay.of(6, 15)))

        assertEquals(paidForUntil, property.paidForUntil)
    }

    @Test
    fun `reactivateProperty throws and makes no change when the reactivating landlord has no anniversary`() {
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID)
        property.deactivate(ORIGINAL_DATE)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        assertThrows<IllegalStateException> { activationService.reactivateProperty(PROPERTY_ID, reactivatingLandlord(null)) }

        assertFalse(property.isActive)
        verify(propertyOwnershipRepository, never()).save(any())
    }

    @Test
    fun `claimPropertyForReactivation returns the refreshed property when the claim succeeds`() {
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID)
        whenever(propertyOwnershipRepository.claimForReactivation(PROPERTY_ID)).thenReturn(1)
        whenever(propertyOwnershipRepository.findById(PROPERTY_ID)).thenReturn(Optional.of(property))

        val claimedProperty = activationService.claimPropertyForReactivation(PROPERTY_ID)

        assertSame(property, claimedProperty)
        verify(entityManager).refresh(property)
    }

    @Test
    fun `claimPropertyForReactivation throws when another reactivation has already claimed the property`() {
        whenever(propertyOwnershipRepository.claimForReactivation(PROPERTY_ID)).thenReturn(0)

        assertThrows<PropertyAlreadyReactivatedException> { activationService.claimPropertyForReactivation(PROPERTY_ID) }
        verify(propertyOwnershipRepository, never()).findById(any())
    }

    @Test
    fun `reactivateClaimedProperty clears the deactivation date, sets the renewal date and saves the property`() {
        val property = claimedProperty()

        activationService.reactivateClaimedProperty(property, NEW_RENEWAL_DATE)

        assertTrue(property.isActive)
        assertNull(property.lastDeactivatedDate)
        assertEquals(NEW_RENEWAL_DATE, property.renewalDate)
        verify(propertyOwnershipRepository).save(property)
    }

    @Test
    fun `reactivateClaimedProperty throws without saving a property that hasn't been claimed`() {
        val property = MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, isActive = false)

        assertThrows<IllegalStateException> { activationService.reactivateClaimedProperty(property, NEW_RENEWAL_DATE) }
        verify(propertyOwnershipRepository, never()).save(any())
    }

    @Test
    fun `reactivateClaimedProperty leaves paidForUntil unchanged when no paid period is given`() {
        val property = claimedProperty()

        activationService.reactivateClaimedProperty(property, NEW_RENEWAL_DATE)

        assertEquals(ORIGINAL_DATE, property.paidForUntil)
    }

    @Test
    fun `reactivateClaimedProperty sets paidForUntil when a paid period is given`() {
        val property = claimedProperty()

        activationService.reactivateClaimedProperty(property, NEW_RENEWAL_DATE, paidForUntil = NEW_RENEWAL_DATE)

        assertEquals(NEW_RENEWAL_DATE, property.paidForUntil)
    }

    @Test
    fun `reactivateClaimedProperty only reactivates the joint landlords who are inactive`() {
        val activeLandlord = MockLandlordData.createIndividualLandlord(name = "active")
        val inactiveLandlord = MockLandlordData.createIndividualLandlord(name = "inactive")
        inactiveLandlord.deactivate(ORIGINAL_DATE)
        val property = claimedProperty(landlords = mutableSetOf(activeLandlord, inactiveLandlord))

        activationService.reactivateClaimedProperty(property, NEW_RENEWAL_DATE)

        assertTrue(inactiveLandlord.isActive)
        verify(landlordRepository).save(inactiveLandlord)
        verify(landlordRepository, never()).save(activeLandlord)
    }

    companion object {
        private const val PROPERTY_ID = 1L
        private val ORIGINAL_DATE = LocalDate.of(2026, 1, 1)
        private val NEW_RENEWAL_DATE = LocalDate.of(2027, 6, 15)

        // Matches the default mock renewal date of 1 Jan, so reactivation leaves it unchanged unless a test says otherwise
        private fun reactivatingLandlord(anniversary: MonthDay? = MonthDay.of(1, 1)) =
            MockLandlordData.createIndividualLandlord().apply { anniversary?.let { setAnniversaryIfAbsent(it) } }

        // As claimPropertyForReactivation leaves it: active in the database, but still carrying its deactivation date
        private fun claimedProperty(landlords: MutableSet<Landlord> = mutableSetOf(MockLandlordData.createIndividualLandlord())) =
            MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, renewalDate = ORIGINAL_DATE, landlords = landlords).apply {
                ReflectionTestUtils.setField(this, "lastDeactivatedDate", ORIGINAL_DATE)
            }
    }
}
