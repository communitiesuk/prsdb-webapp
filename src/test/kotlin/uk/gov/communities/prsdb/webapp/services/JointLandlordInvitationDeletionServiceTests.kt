package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_DELETION_GRACE_PERIOD_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockJointLandlordData
import java.time.Instant
import java.time.temporal.ChronoUnit

class JointLandlordInvitationDeletionServiceTests {
    private lateinit var mockInvitationRepository: JointLandlordInvitationRepository
    private lateinit var deletionService: JointLandlordInvitationDeletionServiceImplFlagOn

    private val expiredAndPastGracePeriodCreatedDate: Instant =
        Instant.now().minus(
            (JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + JOINT_LANDLORD_INVITATION_DELETION_GRACE_PERIOD_IN_DAYS + 1).toLong(),
            ChronoUnit.DAYS,
        )

    private val expiredButWithinGracePeriodCreatedDate: Instant =
        Instant.now().minus(
            (JOINT_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(),
            ChronoUnit.DAYS,
        )

    @BeforeEach
    fun setup() {
        mockInvitationRepository = mock()
        deletionService = JointLandlordInvitationDeletionServiceImplFlagOn(mockInvitationRepository)
    }

    @Test
    fun `deleteExpiredInvitations deletes invitations that have been expired for longer than the grace period`() {
        val invitation =
            MockJointLandlordData.createJointLandlordInvitation(
                id = 1,
                createdDate = expiredAndPastGracePeriodCreatedDate,
            )

        whenever(mockInvitationRepository.findAllByCreatedDateBefore(any()))
            .thenReturn(listOf(invitation))

        val deletedIds = deletionService.deleteExpiredInvitations()

        assertEquals(listOf(1L), deletedIds)
        verify(mockInvitationRepository).deleteAll(listOf(invitation))
    }

    @Test
    fun `deleteExpiredInvitations does not delete hidden invitations even if past grace period`() {
        val hiddenInvitation =
            MockJointLandlordData.createJointLandlordInvitation(
                id = 1,
                createdDate = expiredAndPastGracePeriodCreatedDate,
                isHidden = true,
            )

        whenever(mockInvitationRepository.findAllByCreatedDateBefore(any()))
            .thenReturn(listOf(hiddenInvitation))

        val deletedIds = deletionService.deleteExpiredInvitations()

        assertEquals(emptyList<Long>(), deletedIds)
        verify(mockInvitationRepository).deleteAll(emptyList())
    }

    @Test
    fun `deleteExpiredInvitations returns empty list when no invitations are found`() {
        whenever(mockInvitationRepository.findAllByCreatedDateBefore(any()))
            .thenReturn(emptyList())

        val deletedIds = deletionService.deleteExpiredInvitations()

        assertEquals(emptyList<Long>(), deletedIds)
        verify(mockInvitationRepository).deleteAll(emptyList())
    }

    @Test
    fun `deleteExpiredInvitations deletes multiple expired invitations`() {
        val invitations =
            listOf(
                MockJointLandlordData.createJointLandlordInvitation(id = 1, createdDate = expiredAndPastGracePeriodCreatedDate),
                MockJointLandlordData.createJointLandlordInvitation(id = 2, createdDate = expiredAndPastGracePeriodCreatedDate),
                MockJointLandlordData.createJointLandlordInvitation(id = 3, createdDate = expiredAndPastGracePeriodCreatedDate),
            )

        whenever(mockInvitationRepository.findAllByCreatedDateBefore(any()))
            .thenReturn(invitations)

        val deletedIds = deletionService.deleteExpiredInvitations()

        assertEquals(listOf(1L, 2L, 3L), deletedIds)
        verify(mockInvitationRepository).deleteAll(invitations)
    }

    @Test
    fun `flag-off implementation does nothing`() {
        val flagOff = JointLandlordInvitationDeletionServiceImplFlagOff()

        val result = flagOff.deleteExpiredInvitations()

        assertEquals(emptyList<Long>(), result)
        verify(mockInvitationRepository, never()).findAllByCreatedDateBefore(any())
        verify(mockInvitationRepository, never()).deleteAll(any<List<JointLandlordInvitation>>())
    }
}
