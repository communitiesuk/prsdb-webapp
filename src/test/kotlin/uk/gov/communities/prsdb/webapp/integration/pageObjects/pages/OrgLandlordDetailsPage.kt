package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordAddressController.Companion.UPDATE_ORG_ADDRESS_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordNameController.Companion.UPDATE_ORG_NAME_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LandlordDetailsBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep

class OrgLandlordDetailsPage(
    page: Page,
) : LandlordDetailsBasePage(page, LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE) {
    override val tabs = OrgLandlordDetailsTabs(page)
    val deleteOrganisationLink = Link.byText(page, "Delete organisation")
    val organisationDetailsPanel: Locator = page.locator("#organisation-details")
    val organisationContactsPanel: Locator = page.locator("#organisation-contacts")
    val mainContent: Locator = page.locator("main")
    private val organisationNameChangeLink = Link(page.locator("a[href='$UPDATE_ORG_NAME_ROUTE/${OrgNameStep.ROUTE_SEGMENT}']"))
    private val organisationAddressChangeLink =
        Link(page.locator("a[href='$UPDATE_ORG_ADDRESS_ROUTE/${LookupAddressStep.ROUTE_SEGMENT}']"))

    fun clickOrganisationNameChangeLinkAndWait() = organisationNameChangeLink.clickAndWait()

    fun clickOrganisationAddressChangeLinkAndWait() = organisationAddressChangeLink.clickAndWait()
    val mainContactCard = MainContactSummaryCard(page)
    val leadTrusteeCard = LeadTrusteeSummaryCard(page)
    val registrationContactCard = RegistrationContactSummaryCard(page)
    val governingBodyMembersLink = Link.byText(page, "Add, change or remove members of your governing body")

    fun governingBodyMemberCard(title: String) = GoverningBodyMemberSummaryCard(page, title)

    // Governing body member cards are titled "1. Director", "2. Partner", etc. - a 1-based index
    // followed by the member's role - so their titles start with one or more digits and a dot.
    // The other contact cards (Main contact, Lead trustee, Registration contact) do not, so this
    // regex counts only the governing body member cards.
    fun governingBodyMemberCardCount(): Int =
        page
            .locator("#organisation-contacts h2.govuk-summary-card__title")
            .allInnerTexts()
            .count { Regex("^\\d+\\.").containsMatchIn(it.trim()) }

    class MainContactSummaryCard(
        page: Page,
    ) : SummaryCard(page, "Main contact") {
        override val summaryList = MainContactSummaryList(locator)

        class MainContactSummaryList(
            parentLocator: Locator,
        ) : SummaryList(parentLocator) {
            val nameRow = getRow("Name")
            val emailRow = getRow("Email")
            val phoneNumberRow = getRow("Phone number")
        }
    }

    class LeadTrusteeSummaryCard(
        page: Page,
    ) : SummaryCard(page, "Lead trustee") {
        override val summaryList = LeadTrusteeSummaryList(locator)

        class LeadTrusteeSummaryList(
            parentLocator: Locator,
        ) : SummaryList(parentLocator) {
            val nameRow = getRow("Name")
            val dateOfBirthRow = getRow("Date of birth")
            val emailRow = getRow("Email")
            val phoneNumberRow = getRow("Phone number")
            val addressRow = getRow("Address")
        }
    }

    class RegistrationContactSummaryCard(
        page: Page,
    ) : SummaryCard(page, "Registration contact") {
        override val summaryList = RegistrationContactSummaryList(locator)

        class RegistrationContactSummaryList(
            parentLocator: Locator,
        ) : SummaryList(parentLocator) {
            val nameRow = getRow("Name")
            val dateOfBirthRow = getRow("Date of birth")
            val emailRow = getRow("Email")
            val phoneNumberRow = getRow("Phone number")
        }
    }

    class GoverningBodyMemberSummaryCard(
        page: Page,
        title: String,
    ) : SummaryCard(page, title) {
        override val summaryList = GoverningBodyMemberSummaryList(locator)

        class GoverningBodyMemberSummaryList(
            parentLocator: Locator,
        ) : SummaryList(parentLocator) {
            val roleRow = getRow("Role")
            val nameRow = getRow("Name")
            val dateOfBirthRow = getRow("Date of birth")
            val addressRow = getRow("Address")
        }
    }

    class OrgLandlordDetailsTabs(
        page: Page,
    ) : LandlordDetailsTabs(page) {
        fun goToOrganisationDetails() = goToTab("Organisation details")

        fun goToOrganisationContacts() = goToTab("Organisation contacts")
    }
}
