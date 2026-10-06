package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.EmailAddressPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.FullNamePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptInvitationJourneyPages.JoinOrganisationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.TokenValidity
import kotlin.test.assertEquals

class AcceptOrganisationInvitationSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun enableFeatureFlag() {
        featureFlagManager.enable(MULTI_USER_ORGANISATIONS)
    }

    @Nested
    inner class AcceptOrganisationInvitationStepFullName {
        @Test
        fun `full name page shows the heading and button`() {
            // Arrange and Act
            val fullNamePage = navigator.goToAcceptOrganisationalLandlordInvitationFullNamePage()

            // Assert
            assertEquals("What is your full name?", fullNamePage.heading.getText().trim())
            BaseComponent.assertThat(fullNamePage.form.submitButton).isVisible()
        }

        @Test
        fun `submitting an empty full name returns an error`(page: Page) {
            // Arrange
            val fullNamePage = navigator.goToAcceptOrganisationalLandlordInvitationFullNamePage()

            // Act
            fullNamePage.form.submit()

            // Assert
            assertThat(fullNamePage.form.getErrorMessage()).containsText("You must enter your full name")
            assertPageIs(page, FullNamePage::class)
        }

        @ParameterizedTest
        @ValueSource(strings = ["", "   ", "\t"])
        fun `submitting null or whitespace full name returns an error`(
            blankFullName: String,
            page: Page,
        ) {
            // Arrange
            val fullNamePage = navigator.goToAcceptOrganisationalLandlordInvitationFullNamePage()

            // Act
            fullNamePage.submitName(blankFullName)

            // Assert
            assertThat(fullNamePage.form.getErrorMessage()).containsText("You must enter your full name")
            assertPageIs(page, FullNamePage::class)
        }

        @Test
        fun `full name page back link returns to join organisation page`(page: Page) {
            // Arrange
            val validateTokenPage =
                navigator.goToAcceptOrganisationalLandlordInvitationJourney("1234abcd-5678-abcd-1234-567abcd2222a")
            validateTokenPage.form.radios.selectValue(TokenValidity.VALID)
            validateTokenPage.form.submit()
            val joinOrganisationPage = assertPageIs(page, JoinOrganisationPage::class)
            joinOrganisationPage.form.submit()
            val fullNamePage = assertPageIs(page, FullNamePage::class)

            // Act
            fullNamePage.backLink.clickAndWait()

            // Assert
            assertPageIs(page, JoinOrganisationPage::class)
        }
    }

    @Nested
    inner class AcceptOrganisationInvitationStepEmailAddress {
        @Test
        fun `email address page shows the heading and button`() {
            // Arrange and Act
            val emailAddressPage = navigator.goToAcceptOrganisationalLandlordInvitationEmailAddressPage()

            // Assert
            assertEquals("What is your email address?", emailAddressPage.heading.getText().trim())
            BaseComponent.assertThat(emailAddressPage.form.submitButton).hasText("Save and continue")
        }

        @Test
        fun `submitting an empty email address returns an error`(page: Page) {
            // Arrange
            val emailAddressPage = navigator.goToAcceptOrganisationalLandlordInvitationEmailAddressPage()

            // Act
            emailAddressPage.submitEmail("")

            // Assert
            val emailPage = assertPageIs(page, EmailAddressPage::class)
            assertThat(emailPage.form.getErrorMessage())
                .containsText("Enter your email address")
        }

        @ParameterizedTest
        @ValueSource(strings = ["", "   ", "\t"])
        fun `submitting empty or whitespace email address returns an error`(
            blankEmail: String,
            page: Page,
        ) {
            // Arrange
            val emailAddressPage = navigator.goToAcceptOrganisationalLandlordInvitationEmailAddressPage()

            // Act
            emailAddressPage.submitEmail(blankEmail)

            // Assert
            assertThat(emailAddressPage.form.getErrorMessage()).containsText("Enter your email address")
            assertPageIs(page, EmailAddressPage::class)
        }

        @Test
        fun `submitting an invalid email address returns an error`(page: Page) {
            // Arrange
            val emailAddressPage = navigator.goToAcceptOrganisationalLandlordInvitationEmailAddressPage()

            // Act
            emailAddressPage.submitEmail("notAnEmail")

            // Assert
            val emailPage = assertPageIs(page, EmailAddressPage::class)
            assertThat(emailPage.form.getErrorMessage())
                .containsText("Enter an email address in the correct format, like name@example.com")
        }

        @Test
        fun `email address page back link returns to full name page`(page: Page) {
            // Arrange
            val emailAddressPage = navigator.goToAcceptOrganisationalLandlordInvitationEmailAddressPage()

            // Act
            emailAddressPage.backLink.clickAndWait()

            // Assert
            assertPageIs(page, FullNamePage::class)
        }
    }
}
