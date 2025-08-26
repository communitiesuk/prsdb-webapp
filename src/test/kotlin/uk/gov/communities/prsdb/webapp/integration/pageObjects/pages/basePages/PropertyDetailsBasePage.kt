package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs

abstract class PropertyDetailsBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val tabs = PropertyDetailsTabs(page)
    val propertyDetailsSummaryList = PropertyDetailsSummaryList(page)
    val propertyComplianceSummaryList = PropertyComplianceSummaryList(page)

    fun getLandlordNameLinkFromKeyDetails(landlordName: String) = Link.byText(page, landlordName, 0)

    fun getLandlordLinkFromLandlordDetails(landlordName: String) = Link.byText(page, landlordName, 1)

    val backLink = BackLink.default(page)

    class PropertyDetailsTabs(
        page: Page,
    ) : Tabs(page) {
        fun goToLandlordDetails() {
            goToTab("Landlord details")
        }

        fun goToPropertyDetails() {
            goToTab("Property details")
        }

        fun goToComplianceInformation() {
            goToTab("Compliance information")
        }
    }

    class PropertyDetailsSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val ownershipTypeRow = getRow("Ownership type")
        val occupancyRow = getRow("Occupied by tenants")
        val numberOfHouseholdsRow = getRow("Number of households")
        val numberOfPeopleRow = getRow("Number of people")
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
}
