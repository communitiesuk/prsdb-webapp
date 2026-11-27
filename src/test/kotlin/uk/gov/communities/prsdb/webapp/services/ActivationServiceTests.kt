package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.exceptions.PropertyAlreadyReactivatedException
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.LocalDate
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

        // As claimPropertyForReactivation leaves it: active in the database, but still carrying its deactivation date
        private fun claimedProperty(landlords: MutableSet<Landlord> = mutableSetOf(MockLandlordData.createIndividualLandlord())) =
            MockLandlordData.createPropertyOwnership(id = PROPERTY_ID, renewalDate = ORIGINAL_DATE, landlords = landlords).apply {
                ReflectionTestUtils.setField(this, "lastDeactivatedDate", ORIGINAL_DATE)
            }
    }
}
