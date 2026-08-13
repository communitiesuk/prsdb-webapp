package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.UpdateCompaniesHouseController.Companion.UPDATE_COMPANIES_HOUSE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordCharityController.Companion.UPDATE_ORG_CHARITY_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordEmailController.Companion.UPDATE_ORG_EMAIL_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordNameController.Companion.UPDATE_ORG_NAME_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController.Companion.UPDATE_ORG_TYPE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LandlordDetailsBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCharityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep

class OrgLandlordDetailsPage(
    page: Page,
) : LandlordDetailsBasePage(page, LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE) {
    override val tabs = OrgLandlordDetailsTabs(page)
    val deleteOrganisationLink = Link.byText(page, "Delete organisation")
    val organisationDetailsPanel: Locator = page.locator("#organisation-details")
    val organisationContactsPanel: Locator = page.locator("#organisation-contacts")
    val mainContent: Locator = page.locator("main")
    private val organisationNameChangeLink =
        Link(page.locator("a[href='$UPDATE_ORG_NAME_ROUTE/${OrgNameStep.ROUTE_SEGMENT}']"))
    private val companiesHouseChangeLink =
        Link(page.locator("a[href='$UPDATE_COMPANIES_HOUSE_ROUTE/${OrgIsRegisteredCompanyStep.ROUTE_SEGMENT}']"))
    private val organisationTypeChangeLink =
        Link(page.locator("a[href='$UPDATE_ORG_TYPE_ROUTE/${OrgTypeStep.ROUTE_SEGMENT}']"))
    private val organisationEmailChangeLink =
        Link(page.locator("a[href='$UPDATE_ORG_EMAIL_ROUTE/${OrgEmailStep.ROUTE_SEGMENT}']"))
    private val organisationCharityChangeLink =
        Link(page.locator("a[href='$UPDATE_ORG_CHARITY_ROUTE/${OrgIsRegisteredCharityStep.ROUTE_SEGMENT}']"))

    fun clickOrganisationNameChangeLinkAndWait() = organisationNameChangeLink.clickAndWait()

    fun clickCompaniesHouseChangeLinkAndWait() = companiesHouseChangeLink.clickAndWait()

    fun clickOrganisationTypeChangeLinkAndWait() = organisationTypeChangeLink.clickAndWait()

    fun clickOrganisationEmailChangeLinkAndWait() = organisationEmailChangeLink.clickAndWait()

    fun clickMainContactChangeLinkAndWait() = mainContactCard.getAction("Change").link.clickAndWait()

    fun clickOrganisationCharityChangeLinkAndWait() = organisationCharityChangeLink.clickAndWait()

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
