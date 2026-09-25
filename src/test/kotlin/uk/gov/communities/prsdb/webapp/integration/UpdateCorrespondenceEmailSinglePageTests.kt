package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.CORRESPONDENCE_ADDRESS
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.enums.CorrespondenceEmailOption
import uk.gov.communities.prsdb.webapp.controllers.UpdateCorrespondenceEmailController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CorrespondenceEmailCyaPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CorrespondenceEmailFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep

class UpdateCorrespondenceEmailSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    private val urlArguments = mapOf("propertyOwnershipId" to "1")

    @BeforeEach
    fun enableFeatureFlags() {
        featureFlagManager.enableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        featureFlagManager.enableFeature(CORRESPONDENCE_ADDRESS)
    }

    @Test
    fun `the update question shows the registration content with a continue button`(page: Page) {
        val emailPage = startJourney(page)

        assertThat(emailPage.form.fieldsetHeading).containsText("Where should the council send emails?")
        assertThat(emailPage.form.sectionHeader).containsText("Who the council should contact")
        assertThat(emailPage.form.whichEmailRadios).containsText("alex.surname@example.com")
        assertThat(emailPage.form.submitButton).hasText("Continue")
        assertThat(emailPage.form.selectedEmailOptions).hasCount(0)
    }

    @Test
    fun `submitting without selecting an email option shows the registration error`(page: Page) {
        val emailPage = startJourney(page)

        emailPage.form.submit()

        assertThat(emailPage.errorSummary).containsText("Select which email address the council should send emails to")
    }

    @Test
    fun `the different email option reveals a required email input`(page: Page) {
        val emailPage = startJourney(page)
        emailPage.form.whichEmailRadios.selectValue(CorrespondenceEmailOption.DIFFERENT_EMAIL)
        assertThat(emailPage.form.differentEmailInput).isVisible()

        emailPage.form.submit()

        assertThat(emailPage.errorSummary).containsText("Enter an email address")
    }

    @Test
    fun `an invalid different email shows the registration format error`(page: Page) {
        val emailPage = startJourney(page)

        emailPage.submitDifferentEmail("not-an-email")

        assertThat(emailPage.errorSummary).containsText("Enter an email address in the correct format")
    }

    @Test
    fun `selecting the account email ignores a previously invalid different email`(page: Page) {
        val emailPage = startJourney(page)
        emailPage.submitDifferentEmail("not-an-email")

        emailPage.submitAccountEmail()

        val cyaPage = assertPageIs(page, CorrespondenceEmailCyaPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(cyaPage.summaryList.emailRow.value).hasText("alex.surname@example.com")
        assertThat(cyaPage.heading).hasText("Check your answers")
        assertThat(cyaPage.summaryHeading).hasText("You updated who the council should contact for this property")
        assertThat(cyaPage.form.submitButton).hasText("Confirm and submit update")
    }

    @Test
    fun `the update route is unavailable when the correspondence flag is disabled`(page: Page) {
        featureFlagManager.disableFeature(CORRESPONDENCE_ADDRESS)
        navigator.navigate(
            UpdateCorrespondenceEmailController.getUpdateCorrespondenceEmailRoute(1) +
                "/${CorrespondenceEmailStep.ROUTE_SEGMENT}",
        )

        val errorPage = assertPageIs(page, ErrorPage::class)
        assertThat(errorPage.heading).containsText("Page not found")
    }

    private fun startJourney(page: Page): CorrespondenceEmailFormPagePropertyDetailsUpdate {
        navigator
            .goToPropertyDetailsLandlordView(1)
            .propertyDetailsSummaryList.contactEmailAddressRow
            .clickFirstActionLinkAndWait()
        return assertPageIs(page, CorrespondenceEmailFormPagePropertyDetailsUpdate::class, urlArguments)
    }
}
