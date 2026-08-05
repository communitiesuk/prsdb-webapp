package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages.CheckInvitationsPageInviteJointLandlord
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages.CheckJointLandlordsFormPageInviteJointLandlord
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages.HasJointLandlordsFormPageInviteJointLandlord
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages.InviteJointLandlordConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages.InviteJointLandlordFormPageInviteJointLandlord

@WithOrgLandlordProfile
class InviteJointLandlordOrgLandlordJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val jointPropertyOwnershipId = 48L
    private val solePropertyOwnershipId = 47L
    private val jointUrlArguments = mapOf("propertyOwnershipId" to jointPropertyOwnershipId.toString())
    private val soleUrlArguments = mapOf("propertyOwnershipId" to solePropertyOwnershipId.toString())

    @Test
    fun `An org landlord can complete the standalone invite joint landlord journey`(page: Page) {
        val inviteJointLandlordPage = navigator.goToInviteJointLandlordPage(jointPropertyOwnershipId)
        assertThat(inviteJointLandlordPage.heading).containsText("Invite a joint landlord to this property")
        inviteJointLandlordPage.submitEmail("new.joint.landlord@example.com")

        val checkJointLandlordsPage =
            assertPageIs(page, CheckJointLandlordsFormPageInviteJointLandlord::class, jointUrlArguments)
        assertThat(checkJointLandlordsPage.summaryList.firstRow.value).containsText("new.joint.landlord@example.com")
        checkJointLandlordsPage.form.submit()

        val checkInvitationsPage =
            assertPageIs(page, CheckInvitationsPageInviteJointLandlord::class, jointUrlArguments)
        assertThat(checkInvitationsPage.summaryList.invitationsRow.value).containsText("new.joint.landlord@example.com")
        checkInvitationsPage.confirm()

        val confirmationPage = assertPageIs(page, InviteJointLandlordConfirmationPage::class, jointUrlArguments)
        assertThat(confirmationPage.confirmationBanner.title).containsText("Joint landlord invitations sent")
    }

    @Test
    fun `An org landlord can invite a joint landlord to a property they solely own`(page: Page) {
        val detailsPage = navigator.goToPropertyDetailsLandlordView(solePropertyOwnershipId)
        detailsPage.tabs.goToLandlordDetails()
        detailsPage.inviteJointLandlordLink.clickAndWait()

        val hasJointLandlordsPage =
            assertPageIs(page, HasJointLandlordsFormPageInviteJointLandlord::class, soleUrlArguments)
        hasJointLandlordsPage.submitHasJointLandlords()

        val inviteJointLandlordPage =
            assertPageIs(page, InviteJointLandlordFormPageInviteJointLandlord::class, soleUrlArguments)
        inviteJointLandlordPage.submitEmail("new.joint.landlord@example.com")

        val checkJointLandlordsPage =
            assertPageIs(page, CheckJointLandlordsFormPageInviteJointLandlord::class, soleUrlArguments)
        assertThat(checkJointLandlordsPage.summaryList.firstRow.value).containsText("new.joint.landlord@example.com")
        checkJointLandlordsPage.form.submit()

        val checkInvitationsPage = assertPageIs(page, CheckInvitationsPageInviteJointLandlord::class, soleUrlArguments)
        assertThat(checkInvitationsPage.summaryList.invitationsRow.value).containsText("new.joint.landlord@example.com")
        checkInvitationsPage.confirm()

        val confirmationPage = assertPageIs(page, InviteJointLandlordConfirmationPage::class, soleUrlArguments)
        assertThat(confirmationPage.confirmationBanner.title).containsText("Joint landlord invitations sent")
    }
}
