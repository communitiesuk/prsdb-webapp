package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.switchToIndividualJourneyPages.CheckInvitationsPageSwitchToIndividual
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.switchToIndividualJourneyPages.ConfirmPageSwitchToIndividual
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.switchToIndividualJourneyPages.ConfirmationPageSwitchToIndividual
import uk.gov.communities.prsdb.webapp.testHelpers.FeatureFlagConfigUpdater

class SwitchToIndividualJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    companion object {
        const val PROPERTY_OWNERSHIP_ID_WITH_PENDING_INVITATIONS_AND_NO_JOINT_LANDLORDS = 13L
    }

    @BeforeEach
    fun enableJointLandlords() {
        FeatureFlagConfigUpdater(featureFlagManager).enableUnreleasedFeature(JOINT_LANDLORDS)
    }

    @Test
    fun `user can complete the switch to individual journey with pending invitations`(page: Page) {
        val propertyOwnershipId = PROPERTY_OWNERSHIP_ID_WITH_PENDING_INVITATIONS_AND_NO_JOINT_LANDLORDS
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToLandlordDetails()
        assertThat(propertyDetailsPage.markAsSingleLandlordInsetText).isVisible()
        propertyDetailsPage.switchToIndividualLink.clickAndWait()

        val checkInvitationsPage =
            assertPageIs(
                page,
                CheckInvitationsPageSwitchToIndividual::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        checkInvitationsPage.submitContinue()

        val confirmPage =
            assertPageIs(
                page,
                ConfirmPageSwitchToIndividual::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        confirmPage.submitConfirm()

        val confirmationPage =
            assertPageIs(
                page,
                ConfirmationPageSwitchToIndividual::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        BaseComponent.assertThat(confirmationPage.confirmationBanner).containsText("Registered as the only landlord")

        confirmationPage.viewPropertyRecordLink.clickAndWait()
        val returnedPropertyDetailsPage =
            assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        returnedPropertyDetailsPage.tabs.goToLandlordDetails()
        assertThat(returnedPropertyDetailsPage.inviteJointLandlordIndividualText).isVisible()
        assertThat(propertyDetailsPage.markAsSingleLandlordInsetText).isHidden()
    }

    @Test
    fun `cancelling from check invitations page returns to property details with mark as single landlord inset still shown`(page: Page) {
        val propertyOwnershipId = PROPERTY_OWNERSHIP_ID_WITH_PENDING_INVITATIONS_AND_NO_JOINT_LANDLORDS
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToLandlordDetails()
        propertyDetailsPage.switchToIndividualLink.clickAndWait()

        val checkInvitationsPage =
            assertPageIs(
                page,
                CheckInvitationsPageSwitchToIndividual::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        checkInvitationsPage.cancelLink.clickAndWait()

        val returnedPropertyDetailsPage =
            assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        returnedPropertyDetailsPage.tabs.goToLandlordDetails()
        assertThat(returnedPropertyDetailsPage.markAsSingleLandlordInsetText).isVisible()
    }

    @Test
    fun `cancelling from confirm page returns to property details with mark as single landlord inset still shown`(page: Page) {
        val propertyOwnershipId = PROPERTY_OWNERSHIP_ID_WITH_PENDING_INVITATIONS_AND_NO_JOINT_LANDLORDS
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToLandlordDetails()
        propertyDetailsPage.switchToIndividualLink.clickAndWait()

        val checkInvitationsPage =
            assertPageIs(
                page,
                CheckInvitationsPageSwitchToIndividual::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        checkInvitationsPage.submitContinue()

        val confirmPage =
            assertPageIs(
                page,
                ConfirmPageSwitchToIndividual::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        confirmPage.cancelLink.clickAndWait()

        val returnedPropertyDetailsPage =
            assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        returnedPropertyDetailsPage.tabs.goToLandlordDetails()
        assertThat(returnedPropertyDetailsPage.markAsSingleLandlordInsetText).isVisible()
    }
}
