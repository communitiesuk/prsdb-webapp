package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs

abstract class PropertyDetailsBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val tabs = PropertyDetailsTabs(page)
    val propertyDetailsSummaryList = PropertyDetailsPropertyInformationSummaryList(page)
    val propertyComplianceSummaryList = PropertyComplianceSummaryList(page)
    val gasSafetyCard = SummaryCard(page, "Gas safety certificate")
    val landlordSummaryList = LandlordSummaryList(page)

    val backLink = BackLink.default(page)

    class PropertyDetailsTabs(
        page: Page,
    ) : Tabs(page) {
        fun goToLandlordDetails() {
            goToTab("Landlords")
        }

        fun goToPropertyDetails() {
            goToTab("Property details")
        }

        fun goToComplianceInformation() {
            goToTab("Compliance information")
        }
    }

    class PropertyDetailsPropertyInformationSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val propertyTypeRow = getRow("Property type")
        val ownershipTypeRow = getRow("Ownership type")
        val occupancyRow = getRow("Occupied by tenants")
        val numberOfHouseholdsRow = getRow("Number of households")
        val numberOfPeopleRow = getRow("Number of tenants")
        val numberOfBedroomsRow = getRow("Number of bedrooms")
        val rentIncludesBillsRow = getRow("Rent includes bills")
        val billsIncludedRow = getRow("Which bills are included")
        val furnishedStatusRow = getRow("Furniture provided")
        val rentFrequencyRow = getRow("When rent is paid")
        val rentAmountRow = getRow("Rent amount")
        val licensingTypeRow = getRow("Licensing type")
        val licensingNumberRow = getRow("Licensing number")
    }

    class PropertyComplianceSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val gasSafetyRow = getRow("Gas safety certificate")
        val eicrRow = getRow("Electrical Installation Condition Report (EICR)")
        val epcRow = getRow("Energy Performance Certificate (EPC)")
        val meesExemptionRow = getRow("MEES exemption")
        val fireSafetyRow = getRow("Fire safety responsibilities")
        val propertySafetyRow = getRow("Health and safety in rental properties")
        val responsibilityToTenantsRow = getRow("Your responsibilities to your tenants")
    }

    class LandlordSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val nameRow = getRow("Name")
        val dateOfBirthRow = getRow("Date of Birth")
        val verifiedByOneLoginRow = getRow("Verified by GOV.UK One Login")
        val emailRow = getRow("Email address")
        val contactNumberRow = getRow("Contact number")
        val contactAddressRow = getRow("Contact address")
    }
}
