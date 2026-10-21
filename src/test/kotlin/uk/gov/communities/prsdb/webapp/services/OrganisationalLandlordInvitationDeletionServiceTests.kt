package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_DELETION_GRACE_PERIOD_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.Instant
import java.time.temporal.ChronoUnit

class OrganisationalLandlordInvitationDeletionServiceTests {
    private lateinit var mockInvitationRepository: OrganisationalLandlordInvitationRepository
    private lateinit var deletionService: OrganisationalLandlordInvitationDeletionService

    @BeforeEach
    fun setup() {
        mockInvitationRepository = mock()
        deletionService = OrganisationalLandlordInvitationDeletionService(mockInvitationRepository)
    }

    @Test
    fun `deleteInvitationsPastRetentionPeriod deletes expired invitations`() {
        val expiredInvitation = createInvitation(id = 1, ageInDays = 36)
        whenever(mockInvitationRepository.findAllByCreatedDateBefore(any()))
            .thenReturn(listOf(expiredInvitation))

        val deletedIds = deletionService.deleteInvitationsPastRetentionPeriod()

        assertEquals(listOf(1L), deletedIds)
        verify(mockInvitationRepository).deleteAll(listOf(expiredInvitation))
    }

    @Test
    fun `deleteInvitationsPastRetentionPeriod deletes hidden invitations`() {
        val hiddenInvitation = createInvitation(id = 1, ageInDays = 36, isHidden = true)
        whenever(mockInvitationRepository.findAllByCreatedDateBefore(any()))
            .thenReturn(listOf(hiddenInvitation))

        val deletedIds = deletionService.deleteInvitationsPastRetentionPeriod()

        assertEquals(listOf(1L), deletedIds)
        verify(mockInvitationRepository).deleteAll(listOf(hiddenInvitation))
    }

    @Test
    fun `deleteInvitationsPastRetentionPeriod does not delete pending invitations`() {
        val pendingInvitation = createInvitation(id = 1, ageInDays = 1)
        whenever(mockInvitationRepository.findAllByCreatedDateBefore(any()))
            .thenReturn(listOf(pendingInvitation))

        val deletedIds = deletionService.deleteInvitationsPastRetentionPeriod()

        assertEquals(emptyList<Long>(), deletedIds)
        verify(mockInvitationRepository).deleteAll(emptyList())
    }

    @Test
    fun `deleteInvitationsPastRetentionPeriod uses invitation lifetime plus grace period as cutoff`() {
        whenever(mockInvitationRepository.findAllByCreatedDateBefore(any()))
            .thenReturn(emptyList())
        val start = Instant.now()

        deletionService.deleteInvitationsPastRetentionPeriod()

        val end = Instant.now()
        val cutoffCaptor = argumentCaptor<Instant>()
        verify(mockInvitationRepository).findAllByCreatedDateBefore(cutoffCaptor.capture())
        val expectedRetentionDays =
            ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS +
                ORGANISATIONAL_LANDLORD_INVITATION_DELETION_GRACE_PERIOD_IN_DAYS
        val earliestExpectedCutoff = start.minus(expectedRetentionDays.toLong(), ChronoUnit.DAYS)
        val latestExpectedCutoff = end.minus(expectedRetentionDays.toLong(), ChronoUnit.DAYS)
        assertFalse(cutoffCaptor.firstValue.isBefore(earliestExpectedCutoff))
        assertFalse(cutoffCaptor.firstValue.isAfter(latestExpectedCutoff))
    }

    private fun createInvitation(
        id: Long,
        ageInDays: Long,
        isHidden: Boolean = false,
    ): OrganisationalLandlordInvitation =
        MockLandlordData
            .createOrganisationalLandlordInvitation(
                createdDate = Instant.now().minus(ageInDays, ChronoUnit.DAYS),
                isHidden = isHidden,
            ).also { org.springframework.test.util.ReflectionTestUtils.setField(it, "id", id) }
}
