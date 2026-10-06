package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithImmutableData.NestedIntegrationTestWithImmutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat

@WithOrgLandlordProfile
class OrgLandlordDetailsPermissionsTests : IntegrationTest() {
    // TODO PDJB-1828: Remove this setup when the MULTI_USER_ORGANISATIONS feature flag is removed
    @BeforeEach
    fun enableMultiUserOrganisations() {
        featureFlagManager.enableFeature(MULTI_USER_ORGANISATIONS)
    }

    @Nested
    inner class OrgAdmin : NestedIntegrationTestWithImmutableData("data-mockuser-org-landlord-trust.sql") {
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
