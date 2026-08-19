package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ORGANISATION_LANDLORD_REGISTRATION
import uk.gov.communities.prsdb.webapp.constants.PERSONAL_DETAILS_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalCouncilViewLandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLocalCouncilView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RegisterPropertyStartPage
import kotlin.test.assertEquals

class LandlordDetailTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class LandlordDetailsView {
        @Test
        fun `the landlord details page loads with the landlords personal details tab selected by default`(page: Page) {
            val detailsPage = navigator.goToLandlordDetails()

            assertEquals(detailsPage.tabs.activeTabPanelId, PERSONAL_DETAILS_FRAGMENT)
        }

        @Test
        fun `the registered properties tab contains the registered properties table when the landlord has properties`(page: Page) {
            val detailsPage = navigator.goToLandlordDetails()

            detailsPage.tabs.goToRegisteredProperties()

            assertEquals(detailsPage.tabs.activeTabPanelId, REGISTERED_PROPERTIES_FRAGMENT)
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(0)).containsText("Property address")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(1)).containsText("Property Registration Number")
            assertThat(detailsPage.noRegisteredPropertiesMessage).isHidden()
        }

        @Test
        fun `in the registered properties table the property address link goes to the landlord view of the property's details`(page: Page) {
            val propertyOwnershipId = 1
            val detailsPage = navigator.goToLandlordDetails()
            detailsPage.tabs.goToRegisteredProperties()

            detailsPage.getPropertyAddressLink("1, Example Road, EG").clickAndWait()

            val propertyDetailsView =
                assertPageIs(
                    page,
                    PropertyDetailsPageLandlordView::class,
                    mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
                )

            propertyDetailsView.backLink.clickAndWait()
            val detailsPageAfterBack = assertPageIs(page, LandlordDetailsPage::class)
            assertEquals(REGISTERED_PROPERTIES_FRAGMENT, detailsPageAfterBack.tabs.activeTabPanelId)
        }

        @Test
        fun `the personal details tab shows landlord type row when the org landlord flag is enabled`(page: Page) {
            featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

            val detailsPage = navigator.goToLandlordDetails()

            assertThat(detailsPage.personalDetailsSummaryList.landlordTypeRow).isVisible()
            assertThat(detailsPage.personalDetailsSummaryList.landlordTypeRow).containsText("Individual")
        }

        @Test
        fun `the personal details tab does not show landlord type row when the org landlord flag is disabled`(page: Page) {
            featureFlagManager.disable(ORGANISATION_LANDLORD_REGISTRATION)

            val detailsPage = navigator.goToLandlordDetails()

            assertThat(detailsPage.personalDetailsSummaryList.landlordTypeRow).isHidden()
        }

        @Nested
        inner class LandlordWithoutProperties : NestedIntegrationTestWithImmutableData("data-mockuser-landlord-with-no-properties.sql") {
            @Test
            fun `the registered properties table doesn't appear if the landlord has no properties`(page: Page) {
                val detailsPage = navigator.goToLandlordDetails()

                detailsPage.tabs.goToRegisteredProperties()

                assertEquals(detailsPage.tabs.activeTabPanelId, REGISTERED_PROPERTIES_FRAGMENT)
                assertThat(detailsPage.registeredPropertiesTable).isHidden()
                assertThat(detailsPage.noRegisteredPropertiesMessage).containsText("No registered properties.")

                detailsPage.noRegisteredPropertiesLink.clickAndWait()
                assertPageIs(page, RegisterPropertyStartPage::class)
            }

            @Test
            fun `clicking back from register a property link returns to landlord details registered properties tab`(page: Page) {
                val detailsPage = navigator.goToLandlordDetails()
                detailsPage.tabs.goToRegisteredProperties()
                detailsPage.noRegisteredPropertiesLink.clickAndWait()
                val startPage = assertPageIs(page, RegisterPropertyStartPage::class)
                startPage.backLink.clickAndWait()
                val returnedDetailsPage = assertPageIs(page, LandlordDetailsPage::class)
                assertEquals(REGISTERED_PROPERTIES_FRAGMENT, returnedDetailsPage.tabs.activeTabPanelId)
            }
        }
    }

    @Nested
    inner class LandlordDetailsLocalCouncilView {
        @Test
        fun `the landlord details page loads with the landlords personal details tab selected by default`(page: Page) {
            val detailsPage = navigator.goToLandlordDetailsAsALocalCouncilUser(1)

            assertEquals(detailsPage.tabs.activeTabPanelId, PERSONAL_DETAILS_FRAGMENT)
        }

        @Test
        fun `the registered properties tab shows the landlord's registered properties table if they have properties`(page: Page) {
            val detailsPage = navigator.goToLandlordDetailsAsALocalCouncilUser(1)

            detailsPage.tabs.goToRegisteredProperties()

            assertEquals(detailsPage.tabs.activeTabPanelId, REGISTERED_PROPERTIES_FRAGMENT)
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(0)).containsText("Property address")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(1)).containsText("Registration number")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(2)).containsText("Local council")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(3)).containsText("Licensing type")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(4)).containsText("Tenanted")
            assertThat(detailsPage.noRegisteredPropertiesMessage).isHidden()
        }

        @Test
        fun `the registered properties table doesn't appear if the landlord has no properties`(page: Page) {
            val detailsPage = navigator.goToLandlordDetailsAsALocalCouncilUser(3)

            detailsPage.tabs.goToRegisteredProperties()

            assertEquals(detailsPage.tabs.activeTabPanelId, REGISTERED_PROPERTIES_FRAGMENT)
            assertThat(detailsPage.registeredPropertiesTable).isHidden()
            assertThat(detailsPage.noRegisteredPropertiesMessage).containsText("No registered properties.")
            assertThat(detailsPage.noRegisteredPropertiesLink).isHidden()
        }

        @Test
        fun `loading the landlord details page shows the last time the landlords record was updated`(page: Page) {
            val detailsPage = navigator.goToLandlordDetailsAsALocalCouncilUser(1)

            assertThat(detailsPage.insetText).containsText("updated these details on")
        }

        @Test
        fun `in the registered properties table the property address link goes to the LA view of the property's details`(page: Page) {
            val propertyOwnershipId = 1
            val detailsPage = navigator.goToLandlordDetailsAsALocalCouncilUser(propertyOwnershipId.toLong())
            detailsPage.tabs.goToRegisteredProperties()

            detailsPage.getPropertyAddressLink("1, Example Road, EG").clickAndWait()

            val propertyDetailsView =
                assertPageIs(
                    page,
                    PropertyDetailsPageLocalCouncilView::class,
                    mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
                )

            propertyDetailsView.backLink.clickAndWait()
            val detailsPageAfterBack =
                assertPageIs(page, LocalCouncilViewLandlordDetailsPage::class, mapOf("id" to propertyOwnershipId.toString()))
            assertEquals(REGISTERED_PROPERTIES_FRAGMENT, detailsPageAfterBack.tabs.activeTabPanelId)
        }
    }

    @Nested
    inner class OrgLandlordDetailsLocalCouncilView : NestedIntegrationTestWithImmutableData("data-local.sql") {
        private val orgLandlordId = 36L

        @Test
        fun `the org landlord details page loads with the organisation details tab selected and no delete link`(page: Page) {
            featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

            val detailsPage = navigator.goToOrgLandlordDetailsAsALocalCouncilUser(orgLandlordId)

            assertEquals("organisation-details", detailsPage.tabs.activeTabPanelId)
            assertThat(detailsPage.deleteOrganisationLink).isHidden()
        }

        @Test
        fun `the organisation details tab shows the organisation's details with no change links`(page: Page) {
            featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

            val detailsPage = navigator.goToOrgLandlordDetailsAsALocalCouncilUser(orgLandlordId)
            val summaryList = detailsPage.organisationDetailsSummaryList

            assertThat(summaryList.landlordTypeRow).containsText("Organisation")
            assertThat(summaryList.nameRow).containsText("Local Organisation Landlord")
            assertThat(summaryList.emailRow).containsText("local-org-landlord@example.com")
            assertThat(summaryList.phoneRow).containsText("07111111111")
            assertThat(summaryList.companyNumberRow).containsText("12345678")

            assertThat(summaryList.nameRow.actions).isHidden()
            assertThat(summaryList.emailRow.actions).isHidden()
            assertThat(summaryList.phoneRow.actions).isHidden()
            assertThat(summaryList.organisationTypeRow.actions).isHidden()
            assertThat(summaryList.registeredCharityRow.actions).isHidden()
            assertThat(summaryList.registeredWithCompaniesHouseRow.actions).isHidden()
        }

        @Test
        fun `the organisation contacts tab shows the contact cards with no change actions`(page: Page) {
            featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

            val detailsPage = navigator.goToOrgLandlordDetailsAsALocalCouncilUser(orgLandlordId)
            detailsPage.tabs.goToOrganisationContacts()

            assertThat(detailsPage.mainContactCard.summaryList.nameRow).containsText("Local Main Contact")
            assertThat(detailsPage.registrationContactCard.summaryList.nameRow).containsText("Local Registrant")
            assertThat(detailsPage.mainContactCard.getAction("Change")).isHidden()
            assertThat(detailsPage.governingBodyMembersLink).isHidden()
        }

        @Test
        fun `the registered properties tab shows the local council properties table`(page: Page) {
            featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

            val detailsPage = navigator.goToOrgLandlordDetailsAsALocalCouncilUser(orgLandlordId)
            detailsPage.tabs.goToRegisteredProperties()

            assertEquals(REGISTERED_PROPERTIES_FRAGMENT, detailsPage.tabs.activeTabPanelId)
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(0)).containsText("Property address")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(1)).containsText("Registration number")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(2)).containsText("Local council")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(3)).containsText("Licensing type")
            assertThat(detailsPage.registeredPropertiesTable.headerRow.getCell(4)).containsText("Tenanted")
        }

        @Test
        fun `the org landlord details page returns a not found page when the org landlord flag is disabled`(page: Page) {
            featureFlagManager.disable(ORGANISATION_LANDLORD_REGISTRATION)

            navigator.navigate(LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(orgLandlordId))

            val errorPage = createValidPage(page, ErrorPage::class)
            assertThat(errorPage.heading).containsText("Page not found")
        }
    }

    @Nested
    inner class OrgLandlordDetailsLocalCouncilViewForTrust : NestedIntegrationTestWithImmutableData(
        listOf("data-mockuser-org-landlord-trust.sql", "data-mockuser-local-council-user-not-admin.sql"),
    ) {
        private val trustOrgLandlordId = 1L

        @Test
        fun `the organisation contacts tab shows the lead trustee and governing body members with no change links`(page: Page) {
            featureFlagManager.enable(ORGANISATION_LANDLORD_REGISTRATION)

            val detailsPage = navigator.goToOrgLandlordDetailsAsALocalCouncilUser(trustOrgLandlordId)
            detailsPage.tabs.goToOrganisationContacts()

            assertThat(detailsPage.leadTrusteeCard.summaryList.nameRow).containsText("Anita Locke")
            assertEquals(2, detailsPage.governingBodyMemberCardCount())
            assertThat(detailsPage.governingBodyMemberCard("1. Director").summaryList.nameRow).containsText("David Director")

            assertThat(detailsPage.leadTrusteeCard.getAction("Change")).isHidden()
            assertThat(detailsPage.governingBodyMembersLink).isHidden()
        }
    }
}
