package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages.ReasonPagePropertyDeregistration

class PropertyDeregistrationSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class HasPendingInvitationsStep {
        @Test
        fun `Page displays correct heading with property address`(page: Page) {
            val propertyOwnershipId = 1
            val deregisterPropertyInfoPage = navigator.goToDeregisterPropertyInfoPage(propertyOwnershipId.toLong())
            assertThat(deregisterPropertyInfoPage.heading).containsText("Deregister")
            assertThat(deregisterPropertyInfoPage.heading).containsText("1, Example Road, EG")
        }

        @Test
        fun `Page displays PRN information`(page: Page) {
            val deregisterPropertyInfoPage = navigator.goToDeregisterPropertyInfoPage(1.toLong())
            assertThat(deregisterPropertyInfoPage.prnHeading).containsText("What happens to the Property Registration Number")
        }

        @Test
        fun `User is returned to the property details page if they click the cancel link`(page: Page) {
            val propertyOwnershipId = 1
            val deregisterPropertyInfoPage = navigator.goToDeregisterPropertyInfoPage(propertyOwnershipId.toLong())
            deregisterPropertyInfoPage.cancelLink.clickAndWait()
            BasePage.assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        }

        @Test
        fun `User is returned to the property details page if they click the back link`(page: Page) {
            val propertyOwnershipId = 1
            val deregisterPropertyInfoPage = navigator.goToDeregisterPropertyInfoPage(propertyOwnershipId.toLong())
            deregisterPropertyInfoPage.backLink.clickAndWait()
            BasePage.assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        }
    }

    @Nested
    inner class CheckPendingInvitationsStep {
        @Test
        fun `Page displays pending invitations for the property`(page: Page) {
            val propertyOwnershipId = 38L
            val checkInvitationsPage = navigator.skipToPropertyDeregistrationCheckPendingInvitationsPage(propertyOwnershipId)
            assertThat(checkInvitationsPage.heading).containsText("Check these actions before you deregister")
            assertThat(checkInvitationsPage.invitationsHeading).containsText("Cancel 2 invitations")
            assertThat(checkInvitationsPage.invitationEmails).hasCount(2)
            assertThat(checkInvitationsPage.invitationEmails.first()).containsText("jl.pending.three@example.com")
        }

        @Test
        fun `Page displays sent date for each invitation`(page: Page) {
            val propertyOwnershipId = 38L
            val checkInvitationsPage = navigator.skipToPropertyDeregistrationCheckPendingInvitationsPage(propertyOwnershipId)
            val sentDates = page.locator("main .govuk-hint")
            assertThat(sentDates.first()).containsText("Sent on")
        }
    }

    @Nested
    inner class ConfirmStep {
        @Test
        fun `Page displays heading, address and warning`(page: Page) {
            val confirmPage = navigator.skipToPropertyDeregistrationConfirmPage(1L)
            assertThat(confirmPage.heading).containsText("Confirm property deregistration")
            assertThat(confirmPage.bodyText).containsText("This property will be deregistered")
            assertThat(confirmPage.warningText).containsText("You cannot undo this action")
        }

        @Test
        fun `User is returned to the property details page if they click the cancel link`(page: Page) {
            val propertyOwnershipId = 1
            val confirmPage = navigator.skipToPropertyDeregistrationConfirmPage(propertyOwnershipId.toLong())
            confirmPage.cancelLink.clickAndWait()
            BasePage.assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        }
    }

    @Nested
    inner class CheckCanDeregisterStep {
        @Test
        fun `User with joint landlords is shown the cannot deregister page`(page: Page) {
            val propertyOwnershipId = 8L
            val cannotDeregisterPage = navigator.goToCannotDeregisterPropertyJointLandlordsPage(propertyOwnershipId)
            assertThat(cannotDeregisterPage.heading).containsText("Deregistering a property when it has joint landlords")
            assertThat(cannotDeregisterPage.bodyText.first()).containsText("You cannot deregister")
            assertThat(page.locator("main")).containsText("EG1 2AA")
        }

        @Test
        fun `User is returned to the property details page if they click the back link`(page: Page) {
            val propertyOwnershipId = 8L
            val cannotDeregisterPage = navigator.goToCannotDeregisterPropertyJointLandlordsPage(propertyOwnershipId)
            cannotDeregisterPage.backLink.clickAndWait()
            BasePage.assertPageIs(
                page,
                PropertyDetailsPageLandlordView::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        }
    }

    @Nested
    inner class ReasonStepWhenJointLandlordsFlagIsDisabled {
        @Test
        fun `Submitting a reason longer than 200 characters returns an error`(page: Page) {
            featureFlagManager.disableFeature(JOINT_LANDLORDS)
            val areYouSurePage = navigator.goToDeregisterPropertyAreYouSurePage(1L)
            areYouSurePage.submitWantsToProceed()
            val reasonPage =
                assertPageIs(
                    page,
                    ReasonPagePropertyDeregistration::class,
                    mapOf("propertyOwnershipId" to "1"),
                )
            val longReason = "x".repeat(201)
            reasonPage.submitReason(longReason)
            assertThat(reasonPage.form.getErrorMessage("reason"))
                .containsText("Your reason for deleting this property must be 200 characters or fewer")
        }
    }
}
