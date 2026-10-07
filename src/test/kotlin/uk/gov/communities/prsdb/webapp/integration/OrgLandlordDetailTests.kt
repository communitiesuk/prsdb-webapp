package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithImmutableData.NestedIntegrationTestWithImmutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import java.util.regex.Pattern
import kotlin.test.assertEquals

@WithOrgLandlordProfile
class OrgLandlordDetailTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `the org landlord details page loads with the organisation name, details tab selected and a delete organisation link`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        assertThat(page.locator("#main-content h1")).containsText("Local Organisation Landlord")
        assertThat(detailsPage.deleteOrganisationLink).isVisible()
        assertEquals("organisation-details", detailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `the organisation contacts tab shows the main contact's details`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        detailsPage.tabs.goToOrganisationContacts()

        assertThat(detailsPage.organisationContactsPanel).containsText("Main contact")
    }

    @Test
    fun `the org landlord details page has organisation details, contacts and registered properties tabs`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        assertEquals(3, detailsPage.tabs.tabsList.count())

        detailsPage.tabs.goToRegisteredProperties()
        assertEquals(REGISTERED_PROPERTIES_FRAGMENT, detailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `clicking each tab activates the corresponding panel`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        assertEquals("organisation-details", detailsPage.tabs.activeTabPanelId)

        detailsPage.tabs.goToOrganisationContacts()
        assertEquals("organisation-contacts", detailsPage.tabs.activeTabPanelId)

        detailsPage.tabs.goToRegisteredProperties()
        assertEquals(REGISTERED_PROPERTIES_FRAGMENT, detailsPage.tabs.activeTabPanelId)

        detailsPage.tabs.goToOrganisationDetails()
        assertEquals("organisation-details", detailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `the registered properties tab shows the landlord properties table`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        detailsPage.tabs.goToRegisteredProperties()

        assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(0)).containsText("Property address")
        assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(1)).containsText("Property Registration Number")
    }

    @Test
    fun `the organisation details tab shows the organisation's registration and organisation type details`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()
        val summaryList = detailsPage.organisationDetailsSummaryList

        assertThat(summaryList.lrnRow.value).containsText("L-")
        assertThat(summaryList.landlordTypeRow.value).containsText("Organisation")
        assertThat(summaryList.nameRow.value).containsText("Local Organisation Landlord")
        assertThat(summaryList.addressRow.value).containsText("FA1 1AE")
        assertThat(summaryList.emailRow.value).containsText("local-org-landlord@example.com")
        assertThat(summaryList.phoneRow.value).containsText("07111111111")
        assertThat(summaryList.organisationTypeRow.value).hasText("Company")
        assertThat(summaryList.registeredCharityRow.value).containsText("No")
        assertThat(summaryList.registeredWithCompaniesHouseRow.value).containsText("Yes")
        assertThat(summaryList.companyNumberRow.value).containsText("12345678")
        // The Companies House rows form a single row section, so the first has no bottom divider
        assertThat(summaryList.registeredWithCompaniesHouseRow)
            .hasClass(Pattern.compile(".*govuk-summary-list__row--no-border.*"))
        // The organisation is not a charity, so its charity section is closed by a divider
        assertThat(summaryList.registeredCharityRow)
            .not()
            .hasClass(Pattern.compile(".*govuk-summary-list__row--no-border.*"))
    }

    @Nested
    inner class OrgAdmin : NestedIntegrationTestWithImmutableData("data-mockuser-org-landlord-trust.sql") {
        // TODO PDJB-1828: Remove this setup when the MULTI_USER_ORGANISATIONS feature flag is removed
        @BeforeEach
        fun enableMultiUserOrganisations() {
            featureFlagManager.enableFeature(MULTI_USER_ORGANISATIONS)
        }

        @Test
        fun `an org admin sees the organisation details change links`() {
            val detailsPage = navigator.goToOrgLandlordDetails()

            assertThat(detailsPage.organisationNameChangeLink).isVisible()
            assertThat(detailsPage.organisationAddressChangeLink).isVisible()
            assertThat(detailsPage.organisationEmailChangeLink).isVisible()
            assertThat(detailsPage.organisationPhoneNumberChangeLink).isVisible()
            assertThat(detailsPage.organisationTypeChangeLink).isVisible()
            assertThat(detailsPage.organisationCharityChangeLink).isVisible()
            assertThat(detailsPage.companiesHouseChangeLink).isVisible()
        }

        @Test
        fun `an org admin sees the contacts change links and the delete organisation link`() {
            val detailsPage = navigator.goToOrgLandlordDetails()
            assertThat(detailsPage.deleteOrganisationLink).isVisible()

            detailsPage.tabs.goToOrganisationContacts()

            assertThat(detailsPage.mainContactCard.getAction("Change")).isVisible()
            assertThat(detailsPage.leadTrusteeCard.getAction("Change")).isVisible()
            assertThat(detailsPage.governingBodyMembersLink).isVisible()
        }
    }

    @Nested
    inner class OrgEditor : NestedIntegrationTestWithImmutableData("data-mockuser-org-landlord-trust-editor.sql") {
        // TODO PDJB-1828: Remove this setup when the MULTI_USER_ORGANISATIONS feature flag is removed
        @BeforeEach
        fun enableMultiUserOrganisations() {
            featureFlagManager.enableFeature(MULTI_USER_ORGANISATIONS)
        }

        @Test
        fun `an org editor does not see the organisation details change links`() {
            val detailsPage = navigator.goToOrgLandlordDetails()

            assertThat(detailsPage.organisationNameChangeLink).isHidden()
            assertThat(detailsPage.organisationAddressChangeLink).isHidden()
            assertThat(detailsPage.organisationEmailChangeLink).isHidden()
            assertThat(detailsPage.organisationPhoneNumberChangeLink).isHidden()
            assertThat(detailsPage.organisationTypeChangeLink).isHidden()
            assertThat(detailsPage.organisationCharityChangeLink).isHidden()
            assertThat(detailsPage.companiesHouseChangeLink).isHidden()
        }

        @Test
        fun `an org editor does not see the contacts change links or the delete organisation link`() {
            val detailsPage = navigator.goToOrgLandlordDetails()
            assertThat(detailsPage.deleteOrganisationLink).isHidden()

            detailsPage.tabs.goToOrganisationContacts()

            assertThat(detailsPage.mainContactCard.getAction("Change")).isHidden()
            assertThat(detailsPage.leadTrusteeCard.getAction("Change")).isHidden()
            assertThat(detailsPage.governingBodyMembersLink).isHidden()
        }

        @Test
        fun `an org editor still sees the organisation details and contact values`() {
            val detailsPage = navigator.goToOrgLandlordDetails()

            assertThat(detailsPage.organisationDetailsSummaryList.nameRow.value).containsText("Keystone Living")
            assertThat(detailsPage.organisationDetailsSummaryList.emailRow.value)
                .containsText("contact@keystoneliving.com")

            detailsPage.tabs.goToOrganisationContacts()

            assertThat(detailsPage.mainContactCard.summaryList.nameRow.value).containsText("Sam Main-Contact")
            assertThat(detailsPage.leadTrusteeCard.summaryList.nameRow.value).containsText("Anita Locke")

            assertEquals(2, detailsPage.governingBodyMemberCardCount())
            assertThat(detailsPage.governingBodyMemberCard("1. Director").summaryList.nameRow)
                .containsText("David Director")
        }

        @Test
        fun `an org editor still sees the registered properties tab and the register a property link`() {
            val detailsPage = navigator.goToOrgLandlordDetails()

            detailsPage.tabs.goToRegisteredProperties()

            assertThat(detailsPage.noRegisteredPropertiesMessage).isVisible()
            assertThat(detailsPage.noRegisteredPropertiesLink).isVisible()
        }
    }

    // TODO PDJB-1828: Remove this nested class when the MULTI_USER_ORGANISATIONS feature flag is removed
    // With the flag off no editors can exist in practice (invitations are flag-gated and the role column
    // defaults to ADMIN), so this only asserts that the flag-off path is a no-op for the view.
    @Nested
    inner class BeforePdjb1210MultiUserOrganisations :
        NestedIntegrationTestWithImmutableData("data-mockuser-org-landlord-trust-editor.sql") {
        @BeforeEach
        fun disableMultiUserOrganisationsForBeforePdjb1210Tests() {
            featureFlagManager.disableFeature(MULTI_USER_ORGANISATIONS)
        }

        @Test
        fun `an org editor sees all actions`() {
            val detailsPage = navigator.goToOrgLandlordDetails()

            assertThat(detailsPage.organisationNameChangeLink).isVisible()
            assertThat(detailsPage.deleteOrganisationLink).isVisible()

            detailsPage.tabs.goToOrganisationContacts()

            assertThat(detailsPage.mainContactCard.getAction("Change")).isVisible()
            assertThat(detailsPage.governingBodyMembersLink).isVisible()
        }
    }
}
