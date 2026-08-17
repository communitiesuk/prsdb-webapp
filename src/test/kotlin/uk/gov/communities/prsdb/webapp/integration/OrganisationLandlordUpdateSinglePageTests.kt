package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgEmailFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgMainContactFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgNameFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages.OrgPhoneNumberFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages.OrgTypeFormPageUpdateOrganisationType

@WithOrgLandlordProfile
class OrganisationLandlordUpdateSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun setup() {
        featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)
    }

    @Nested
    inner class AddingTrustInterruption {
        @Test
        fun `shows the adding trust page when adding trust to organisation type`(page: Page) {
            val interruptionPage = navigator.skipToUpdateOrgTypeTrustInterruptionPage(listOf(OrgType.TRUST))

            assertThat(interruptionPage.heading).containsText("You must provide trustee details")
        }
    }

    @Nested
    inner class RemovingTrustInterruption : NestedIntegrationTestWithImmutableData("data-org-landlord-trust.sql") {
        @BeforeEach
        fun setup() {
            featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)
        }

        @Test
        fun `shows the removing trust page when removing trust from organisation type`(page: Page) {
            val interruptionPage = navigator.skipToUpdateOrgTypeTrustInterruptionPage(listOf(OrgType.COMPANY))

            assertThat(interruptionPage.heading).containsText("Are you sure you want to change this?")
            assertThat(interruptionPage.body).containsText("from a trust to company")
        }

        @Test
        fun `shows comma-separated org types when multiple are selected`(page: Page) {
            val interruptionPage = navigator.skipToUpdateOrgTypeTrustInterruptionPage(listOf(OrgType.COMPANY, OrgType.CHARITY))

            assertThat(interruptionPage.body).containsText("from a trust to company, charity")
        }
    }

    @Nested
    inner class ValidationErrors {
        @Test
        fun `Submitting an empty organisation name on update shows a validation error`(page: Page) {
            val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.clickOrganisationNameChangeLinkAndWait()

            val updateOrgNamePage = assertPageIs(page, OrgNameFormPageUpdateLandlordDetails::class)
            updateOrgNamePage.submitName("")
            assertThat(updateOrgNamePage.form.getErrorMessage()).containsText("Enter an organisation name")
        }

        @Test
        fun `Submitting an empty organisation email on update shows a validation error`(page: Page) {
            val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.clickOrganisationEmailChangeLinkAndWait()

            val updateOrgEmailPage = assertPageIs(page, OrgEmailFormPageUpdateLandlordDetails::class)
            updateOrgEmailPage.submitEmail("")
            assertThat(updateOrgEmailPage.form.getErrorMessage()).containsText("Enter a valid email address")
        }

        @Test
        fun `Submitting a malformed organisation email on update shows a validation error`(page: Page) {
            val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.clickOrganisationEmailChangeLinkAndWait()

            val updateOrgEmailPage = assertPageIs(page, OrgEmailFormPageUpdateLandlordDetails::class)
            updateOrgEmailPage.submitEmail("not-an-email")
            assertThat(updateOrgEmailPage.form.getErrorMessage()).containsText("Enter an email address in the right format")
        }

        @Test
        fun `Submitting an empty main contact name on update shows a validation error`(page: Page) {
            val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.tabs.goToOrganisationContacts()
            orgLandlordDetailsPage.clickMainContactChangeLinkAndWait()

            val updatePage = assertPageIs(page, OrgMainContactFormPageUpdateLandlordDetails::class)
            updatePage.submit("", "valid@example.com", "07222222222")
            assertThat(updatePage.form.getErrorMessage()).containsText("Enter a full name")
        }

        @Test
        fun `Submitting an empty email on main contact update shows a validation error`(page: Page) {
            val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.tabs.goToOrganisationContacts()
            orgLandlordDetailsPage.clickMainContactChangeLinkAndWait()

            val updatePage = assertPageIs(page, OrgMainContactFormPageUpdateLandlordDetails::class)
            updatePage.submit("Valid Name", "", "07222222222")
            assertThat(updatePage.form.getErrorMessage()).containsText("Enter an email address")
        }

        @Test
        fun `Submitting an invalid email on main contact update shows a validation error`(page: Page) {
            val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.tabs.goToOrganisationContacts()
            orgLandlordDetailsPage.clickMainContactChangeLinkAndWait()

            val updatePage = assertPageIs(page, OrgMainContactFormPageUpdateLandlordDetails::class)
            updatePage.submit("Valid Name", "not-an-email", "07222222222")
            assertThat(updatePage.form.getErrorMessage()).containsText("Enter an email address in the right format")
        }

        @Test
        fun `Submitting an empty phone number on main contact update shows a validation error`(page: Page) {
            val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.tabs.goToOrganisationContacts()
            orgLandlordDetailsPage.clickMainContactChangeLinkAndWait()

            val updatePage = assertPageIs(page, OrgMainContactFormPageUpdateLandlordDetails::class)
            updatePage.submit("Valid Name", "valid@example.com", "")
            assertThat(updatePage.form.getErrorMessage()).containsText("Enter a phone number")
        }

        @Test
        fun `Submitting an invalid phone number on main contact update shows a validation error`(page: Page) {
            val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.tabs.goToOrganisationContacts()
            orgLandlordDetailsPage.clickMainContactChangeLinkAndWait()

            val updatePage = assertPageIs(page, OrgMainContactFormPageUpdateLandlordDetails::class)
            updatePage.submit("Valid Name", "valid@example.com", "not-a-phone")
            assertThat(
                updatePage.form.getErrorMessage(),
            ).containsText("Enter a phone number including the country code for international numbers")
        }

        @Test
        fun `Submitting an empty organisation phone number on update shows a validation error`(page: Page) {
            val orgLandlordDetailsPage = navigator.goToOrgLandlordDetails()
            orgLandlordDetailsPage.clickOrganisationPhoneNumberChangeLinkAndWait()

            val updatePhonePage = assertPageIs(page, OrgPhoneNumberFormPageUpdateLandlordDetails::class)
            updatePhonePage.submitPhoneNumber("")
            assertThat(updatePhonePage.form.getErrorMessage())
                .containsText("Enter a phone number including the country code for international numbers")
        }
    }

    @Nested
    inner class TrustUnchangedCya {
        @Test
        fun `CYA page shows organisation type only when trust status is unchanged`(page: Page) {
            val cyaPage = navigator.skipToUpdateOrgTypeCyaPageTrustUnchanged(listOf(OrgType.CHARITY))

            assertThat(cyaPage.summaryList.organisationTypeRow.value).containsText("Charity")
            assertThat(cyaPage.leadTrusteeCard).isHidden()
        }

        @Test
        fun `Clicking change link on organisation type row navigates to the organisation type page`(page: Page) {
            val cyaPage = navigator.skipToUpdateOrgTypeCyaPageTrustUnchanged(listOf(OrgType.CHARITY))

            cyaPage.summaryList.organisationTypeRow.clickFirstActionLinkAndWait()
            assertPageIs(page, OrgTypeFormPageUpdateOrganisationType::class)
        }
    }

    @Nested
    inner class AddingTrustCya {
        @Test
        fun `CYA page shows organisation type and lead trustee details when adding trust`(page: Page) {
            val cyaPage =
                navigator.skipToUpdateOrgTypeCyaPageAddingTrust(
                    trusteeName = LEAD_TRUSTEE_NAME,
                    trusteeEmail = LEAD_TRUSTEE_EMAIL,
                    trusteePhone = LEAD_TRUSTEE_PHONE,
                )

            assertThat(cyaPage.summaryList.organisationTypeRow.value).containsText("Company")
            assertThat(cyaPage.summaryList.organisationTypeRow.value).containsText("Trust")
            assertThat(cyaPage.leadTrusteeCard.summaryList.nameRow.value).containsText(LEAD_TRUSTEE_NAME)
            assertThat(cyaPage.leadTrusteeCard.summaryList.dateOfBirthRow.value).containsText("15 June 1980")
            assertThat(cyaPage.leadTrusteeCard.summaryList.emailRow.value).containsText(LEAD_TRUSTEE_EMAIL)
            assertThat(cyaPage.leadTrusteeCard.summaryList.phoneRow.value).containsText(LEAD_TRUSTEE_PHONE)
            assertThat(cyaPage.leadTrusteeCard.summaryList.addressRow.value).containsText("1 Example Street")
        }
    }

    companion object {
        private const val LEAD_TRUSTEE_NAME = "Test Lead Trustee Name"
        private const val LEAD_TRUSTEE_EMAIL = "trustee@test.com"
        private const val LEAD_TRUSTEE_PHONE = "07123456789"
    }
}
