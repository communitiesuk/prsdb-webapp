package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordDeactivationReason
import uk.gov.communities.prsdb.webapp.database.repository.LandlordDeactivationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordDeactivatedEmail
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class LandlordDeactivationServiceTests {
    @Mock
    private lateinit var landlordRepository: LandlordRepository

    @Mock
    private lateinit var landlordDeactivationRepository: LandlordDeactivationRepository

    @Mock
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var landlordDeactivatedEmailSender: EmailNotificationService<LandlordDeactivatedEmail>

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @InjectMocks
    private lateinit var landlordDeactivationService: LandlordDeactivationService

    @Test
    fun `getIdsOfLandlordsWhoNeverRegisteredAProperty uses the start of the UK day 3 months ago as the cutoff`() {
        val expectedCutoff =
            LocalDate
                .now(DateTimeHelper.UK_ZONE)
                .minusMonths(3)
                .atStartOfDay(DateTimeHelper.UK_ZONE)
                .toInstant()
        whenever(landlordDeactivationRepository.findIdsOfActiveLandlordsWithNoRegisteredPropertyCreatedBefore(expectedCutoff))
            .thenReturn(listOf(1L, 2L))

        val ids = landlordDeactivationService.getIdsOfLandlordsWhoNeverRegisteredAProperty()

        assertEquals(listOf(1L, 2L), ids)
    }

    @Test
    fun `deactivateLandlordIfNoActiveProperties deactivates an active landlord with no active properties`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(landlordRepository.findById(landlord.id)).thenReturn(Optional.of(landlord))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)).thenReturn(false)

        val wasDeactivated = landlordDeactivationService.deactivateLandlordIfNoActiveProperties(landlord.id)

        assertTrue(wasDeactivated)
        assertFalse(landlord.isActive)
        assertEquals(LocalDate.now(DateTimeHelper.UK_ZONE), landlord.lastDeactivatedDate)
        verify(landlordRepository).save(landlord)
    }

    @Test
    fun `deactivateLandlordIfNoActiveProperties emails the landlord when they are deactivated`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(landlordRepository.findById(landlord.id)).thenReturn(Optional.of(landlord))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)).thenReturn(false)
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://localhost/landlord"))

        landlordDeactivationService.deactivateLandlordIfNoActiveProperties(landlord.id)

        verify(landlordDeactivatedEmailSender).sendEmailToLandlord(
            landlord,
            LandlordDeactivatedEmail(
                recipientName = landlord.name,
                registrationNumber = RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString(),
                reason = LandlordDeactivationReason.NO_PROPERTIES_REGISTERED,
                dashboardUrl = "http://localhost/landlord",
            ),
        )
    }

    @Test
    fun `deactivateLandlordIfNoActiveProperties still deactivates the landlord when the email fails to send`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(landlordRepository.findById(landlord.id)).thenReturn(Optional.of(landlord))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)).thenReturn(false)
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://localhost/landlord"))
        whenever(landlordDeactivatedEmailSender.sendEmailToLandlord(any(), any())).doThrow(RuntimeException("Notify down"))

        val wasDeactivated = landlordDeactivationService.deactivateLandlordIfNoActiveProperties(landlord.id)

        assertTrue(wasDeactivated)
        assertFalse(landlord.isActive)
    }

    @Test
    fun `deactivateLandlordIfNoActiveProperties makes no change when the landlord has an active property`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        whenever(landlordRepository.findById(landlord.id)).thenReturn(Optional.of(landlord))
        whenever(propertyOwnershipRepository.existsByOwnershipLinks_Landlord_IdAndIsActiveTrue(landlord.id)).thenReturn(true)

        val wasDeactivated = landlordDeactivationService.deactivateLandlordIfNoActiveProperties(landlord.id)

        assertFalse(wasDeactivated)
        assertTrue(landlord.isActive)
        assertNull(landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
        verify(landlordDeactivatedEmailSender, never()).sendEmailToLandlord(any(), any())
    }

    @Test
    fun `deactivateLandlordIfNoActiveProperties makes no change for an already inactive landlord`() {
        val landlord = MockLandlordData.createIndividualLandlord()
        landlord.deactivate(ORIGINAL_DATE)
        whenever(landlordRepository.findById(landlord.id)).thenReturn(Optional.of(landlord))

        val wasDeactivated = landlordDeactivationService.deactivateLandlordIfNoActiveProperties(landlord.id)

        assertFalse(wasDeactivated)
        assertEquals(ORIGINAL_DATE, landlord.lastDeactivatedDate)
        verify(landlordRepository, never()).save(any())
        verify(landlordDeactivatedEmailSender, never()).sendEmailToLandlord(any(), any())
    }

    companion object {
        private val ORIGINAL_DATE = LocalDate.of(2025, 1, 1)
    }
}
