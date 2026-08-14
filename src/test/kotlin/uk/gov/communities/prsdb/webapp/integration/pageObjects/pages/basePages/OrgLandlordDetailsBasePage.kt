package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList

abstract class OrgLandlordDetailsBasePage(
    page: Page,
    urlSegment: String,
) : LandlordDetailsBasePage(page, urlSegment) {
    override val tabs = OrgLandlordDetailsTabs(page)
    val deleteOrganisationLink = Link.byText(page, "Delete organisation")
    val organisationDetailsPanel: Locator = page.locator("#organisation-details")
    val organisationContactsPanel: Locator = page.locator("#organisation-contacts")
    val mainContent: Locator = page.locator("main")
    val mainContactCard = MainContactSummaryCard(page)
    val leadTrusteeCard = LeadTrusteeSummaryCard(page)
    val registrationContactCard = RegistrationContactSummaryCard(page)
    val governingBodyMembersLink = Link.byText(page, "Add, change or remove members of your governing body")
    val organisationDetailsSummaryList = OrganisationDetailsSummaryList(page)

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

    class OrganisationDetailsSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val registrationDateRow = getRow("Registration date")
        val lrnRow = getRow("Landlord Registration Number")
        val landlordTypeRow = getRow("Landlord type")
        val nameRow = getRow("Organisation name")
        val addressRow = getRow("Organisation address")
        val emailRow = getRow("Organisation email")
        val phoneRow = getRow("Organisation phone")
        val organisationTypeRow = getRow("Organisation type")
        val registeredCharityRow = getRow("Registered charity")
        val charityCommissionRow = getRow("Charity commission")
        val charityNumberRow = getRow("Charity number")
        val registeredWithCompaniesHouseRow = getRow("Registered with Companies House")
        val companyNumberRow = getRow("Companies House number")
    }
}
