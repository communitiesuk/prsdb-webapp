package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.CheckInvitationsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.ConfirmPagePropertyDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.ConfirmationPagePropertyDeregistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.ConfirmationPagePropertyDeregistrationOld
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.ReasonPagePropertyDeregistration

class PropertyDeregistrationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @Test
    fun `User can navigate the whole journey if pages are correctly filled in`(page: Page) {
        val propertyOwnershipId = 1
        val deregisterPropertyInfoPage = navigator.goToDeregisterPropertyInfoPage(propertyOwnershipId.toLong())
        assertThat(deregisterPropertyInfoPage.heading).containsText("1, Example Road, EG")
        deregisterPropertyInfoPage.submitContinue()

        val confirmPage =
            assertPageIs(
                page,
                ConfirmPagePropertyDeregistration::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        confirmPage.submitConfirm()

        val confirmationPage =
            assertPageIs(
                page,
                ConfirmationPagePropertyDeregistration::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        BaseComponent.assertThat(confirmationPage.confirmationBanner).containsText("Deregistered 1, Example Road, EG1 1AA")

        confirmationPage.goToDashboardLink.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `User can delete a property record that has compliance information and JL invites`(page: Page) {
        val propertyOwnershipId = 8
        val deregisterPropertyInfoPage = navigator.goToDeregisterPropertyInfoPage(propertyOwnershipId.toLong())
        deregisterPropertyInfoPage.submitContinue()

        val checkInvitationsPage =
            assertPageIs(
                page,
                CheckInvitationsPage::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        checkInvitationsPage.submitContinue()

        val confirmPage =
            assertPageIs(
                page,
                ConfirmPagePropertyDeregistration::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        confirmPage.submitConfirm()

        val confirmationPage =
            assertPageIs(
                page,
                ConfirmationPagePropertyDeregistration::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        BaseComponent.assertThat(confirmationPage.confirmationBanner).containsText("Deregistered 1 PRSDB Square, EG1 2AA")

        confirmationPage.goToDashboardLink.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Nested
    inner class ConfirmStep {
        @Test
        fun `Confirm page deregisters the property and reaches confirmation`(page: Page) {
            val propertyOwnershipId = 1.toLong()
            val confirmPage = navigator.skipToPropertyDeregistrationConfirmPage(propertyOwnershipId)
            confirmPage.submitConfirm()
            assertPageIs(
                page,
                ConfirmationPagePropertyDeregistration::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        }
    }

    @Nested
    inner class WhenJointLandlordsFlagIsDisabled {
        @Test
        fun `User can navigate the whole journey via the are you sure radio page`(page: Page) {
            featureFlagManager.disableFeature(JOINT_LANDLORDS)
            val propertyOwnershipId = 1
            val areYouSurePage = navigator.goToDeregisterPropertyAreYouSurePage(propertyOwnershipId.toLong())
            areYouSurePage.submitWantsToProceed()

            val reasonPage =
                assertPageIs(
                    page,
                    ReasonPagePropertyDeregistration::class,
                    mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
                )
            reasonPage.submitReason("No longer own this property")

            val confirmationPage =
                assertPageIs(
                    page,
                    ConfirmationPagePropertyDeregistrationOld::class,
                    mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
                )
            BaseComponent.assertThat(confirmationPage.confirmationBanner).containsText("You have deleted a property")

            confirmationPage.goToDashboardButton.clickAndWait()
            assertPageIs(page, LandlordDashboardPage::class)
        }

        @Test
        fun `User is returned to property details when they select No on are you sure page`(page: Page) {
            featureFlagManager.disableFeature(JOINT_LANDLORDS)
            val propertyOwnershipId = 1
            val areYouSurePage = navigator.goToDeregisterPropertyAreYouSurePage(propertyOwnershipId.toLong())
            areYouSurePage.submitDoesNotWantToProceed()

            assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        }
    }
}
