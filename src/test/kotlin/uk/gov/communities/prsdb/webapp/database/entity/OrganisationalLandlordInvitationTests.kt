package uk.gov.communities.prsdb.webapp.database.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.testHelpers.DateTimeTestHelper.Companion.getInstantDaysAgoInUK
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class OrganisationalLandlordInvitationTests {
    @Test
    fun `status returns PENDING when the current day is earlier than the expiry date`() {
        val createdDate = getInstantDaysAgoInUK(ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS - 1)
        val invitation = MockLandlordData.createOrganisationalLandlordInvitation(createdDate = createdDate)

        assertEquals(OrganisationalLandlordInvitationStatus.PENDING, invitation.status)
    }

    @Test
    fun `status returns PENDING when the current day equals the expiry date`() {
        val createdDate = getInstantDaysAgoInUK(ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS)
        val invitation = MockLandlordData.createOrganisationalLandlordInvitation(createdDate = createdDate)

        assertEquals(OrganisationalLandlordInvitationStatus.PENDING, invitation.status)
    }

    @Test
    fun `status returns EXPIRED when the current day is later than the expiry date`() {
        val createdDate = getInstantDaysAgoInUK(ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1)
        val invitation = MockLandlordData.createOrganisationalLandlordInvitation(createdDate = createdDate)

        assertEquals(OrganisationalLandlordInvitationStatus.EXPIRED, invitation.status)
    }

    @Test
    fun `status returns HIDDEN when the invitation is hidden`() {
        val invitation = MockLandlordData.createOrganisationalLandlordInvitation(isHidden = true)

        assertEquals(OrganisationalLandlordInvitationStatus.HIDDEN, invitation.status)
    }

    @Test
    fun `status returns HIDDEN when a hidden invitation has also expired`() {
        val createdDate = getInstantDaysAgoInUK(ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1)
        val invitation =
            MockLandlordData.createOrganisationalLandlordInvitation(createdDate = createdDate, isHidden = true)

        assertEquals(OrganisationalLandlordInvitationStatus.HIDDEN, invitation.status)
    }

    @Test
    fun `can be deleted by archive when expired or hidden`() {
        val expiredInvitation =
            MockLandlordData.createOrganisationalLandlordInvitation(
                createdDate = getInstantDaysAgoInUK(ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1),
            )
        val hiddenInvitation = MockLandlordData.createOrganisationalLandlordInvitation(isHidden = true)
        val pendingInvitation = MockLandlordData.createOrganisationalLandlordInvitation()

        assertEquals(true, expiredInvitation.canBeDeletedByArchive())
        assertEquals(true, hiddenInvitation.canBeDeletedByArchive())
        assertEquals(false, pendingInvitation.canBeDeletedByArchive())
    }
}
