package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.InvitationStatus
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.Instant
import java.time.temporal.ChronoUnit

class OrganisationalLandlordInvitationTests {
    @Test
    fun `status returns PENDING when the current day is earlier than the expiry date`() {
        val createdDate =
            Instant.now().minus((ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS - 1).toLong(), ChronoUnit.DAYS)
        val invitation = MockLandlordData.createOrganisationalLandlordInvitation(createdDate = createdDate)

        assertEquals(InvitationStatus.PENDING, invitation.status)
    }

    @Test
    fun `status returns PENDING when the current day equals the expiry date`() {
        val createdDate =
            Instant.now().minus(ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS.toLong(), ChronoUnit.DAYS)
        val invitation = MockLandlordData.createOrganisationalLandlordInvitation(createdDate = createdDate)

        assertEquals(InvitationStatus.PENDING, invitation.status)
    }

    @Test
    fun `status returns EXPIRED when the current day is later than the expiry date`() {
        val createdDate =
            Instant.now().minus((ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(), ChronoUnit.DAYS)
        val invitation = MockLandlordData.createOrganisationalLandlordInvitation(createdDate = createdDate)

        assertEquals(InvitationStatus.EXPIRED, invitation.status)
    }

    @Test
    fun `status returns HIDDEN when the invitation is hidden`() {
        val invitation = MockLandlordData.createOrganisationalLandlordInvitation(isHidden = true)

        assertEquals(InvitationStatus.HIDDEN, invitation.status)
    }

    @Test
    fun `status returns HIDDEN when a hidden invitation has also expired`() {
        val createdDate =
            Instant.now().minus((ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(), ChronoUnit.DAYS)
        val invitation =
            MockLandlordData.createOrganisationalLandlordInvitation(createdDate = createdDate, isHidden = true)

        assertEquals(InvitationStatus.HIDDEN, invitation.status)
    }
}
