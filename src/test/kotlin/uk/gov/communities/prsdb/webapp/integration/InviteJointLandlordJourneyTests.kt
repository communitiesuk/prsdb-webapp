package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.controllers.InviteJointLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages.CheckInvitationsPageInviteJointLandlord
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages.CheckJointLandlordsFormPageInviteJointLandlord
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages.InviteAnotherJointLandlordFormPageInviteJointLandlord
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages.InviteJointLandlordConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages.InviteJointLandlordFormPageInviteJointLandlord
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordStep
import java.net.URI

class InviteJointLandlordJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @BeforeEach
    fun setUp() {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://localhost:$port/landlord"))
        whenever(absoluteUrlProvider.buildJointLandlordInvitationUri(any()))
            .thenReturn(URI("http://localhost:$port/invite/test-token"))
        whenever(absoluteUrlProvider.buildPropertyDetailsUri(any()))
            .thenReturn(URI("http://localhost:$port/landlord/property-details/$propertyOwnershipId"))
    }

    @Test
    fun `Landlord can complete the standalone invite joint landlord journey`(page: Page) {
        val firstStepUrl =
            "http://localhost:$port${InviteJointLandlordController.getInviteJointLandlordRoute(propertyOwnershipId)}/" +
                InviteJointLandlordStep.INVITE_FIRST_ROUTE_SEGMENT
        page.navigate(firstStepUrl)

        val inviteJointLandlordPage = assertPageIs(page, InviteJointLandlordFormPageInviteJointLandlord::class, urlArguments)
        assertThat(inviteJointLandlordPage.heading).containsText("Invite a joint landlord to this property")
        inviteJointLandlordPage.submitEmail("first@example.com")

        var checkJointLandlordsPage = assertPageIs(page, CheckJointLandlordsFormPageInviteJointLandlord::class, urlArguments)
        assertThat(checkJointLandlordsPage.summaryList.firstRow.value).containsText("first@example.com")
        checkJointLandlordsPage.form.addAnotherButton.clickAndWait()

        val inviteAnotherJointLandlordPage =
            assertPageIs(page, InviteAnotherJointLandlordFormPageInviteJointLandlord::class, urlArguments)
        inviteAnotherJointLandlordPage.submitEmail("second@example.com")

        checkJointLandlordsPage = assertPageIs(page, CheckJointLandlordsFormPageInviteJointLandlord::class, urlArguments)
        assertThat(checkJointLandlordsPage.summaryList.firstRow.value).containsText("first@example.com")
        assertThat(checkJointLandlordsPage.summaryList.getRowByIndex(1).value).containsText("second@example.com")
        checkJointLandlordsPage.form.submit()

        val checkInvitationsPage = assertPageIs(page, CheckInvitationsPageInviteJointLandlord::class, urlArguments)
        assertThat(checkInvitationsPage.summaryName).containsText("Invited email addresses")
        assertThat(checkInvitationsPage.summaryList.invitationsRow.value).containsText("first@example.com")
        assertThat(checkInvitationsPage.summaryList.invitationsRow.value).containsText("second@example.com")
        checkInvitationsPage.confirm()

        val confirmationPage = assertPageIs(page, InviteJointLandlordConfirmationPage::class, urlArguments)
        assertThat(confirmationPage.confirmationBanner.title).containsText("Joint landlord invitations sent")
        assertThat(confirmationPage.goBackToPropertyRecordLink).containsText("Go back to the property record")
        assertThat(page.locator("main")).containsText("The joint landlords you’ve invited have 28 days to join the property")
    }

    @Test
    fun `Submitting an email of an existing landlord on the property shows an error`(page: Page) {
        val firstStepUrl =
            "http://localhost:$port${InviteJointLandlordController.getInviteJointLandlordRoute(propertyOwnershipId)}/" +
                InviteJointLandlordStep.INVITE_FIRST_ROUTE_SEGMENT
        page.navigate(firstStepUrl)

        val inviteJointLandlordPage = assertPageIs(page, InviteJointLandlordFormPageInviteJointLandlord::class, urlArguments)
        inviteJointLandlordPage.submitEmail("alex.surname@example.com")

        assertThat(inviteJointLandlordPage.form.getErrorMessage())
            .containsText("This email address is already being used by another joint landlord")
    }
}
