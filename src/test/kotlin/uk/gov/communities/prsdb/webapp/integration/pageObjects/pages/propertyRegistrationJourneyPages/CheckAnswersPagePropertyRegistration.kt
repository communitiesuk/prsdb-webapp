package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationCyaStep
import java.util.regex.Pattern

class CheckAnswersPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${PropertyRegistrationCyaStep.ROUTE_SEGMENT}") {
    fun confirm() = form.submit()

    val form = PostForm(page)

    val sectionHeader = SectionHeader(page.locator("main"))

    val heading = Heading(page.locator("h1"))

    val summaryList = CheckAnswersPropertyRegistrationSummaryList(page)

    val occupancyHeading =
        Heading(page.locator("h3.govuk-heading-s", Page.LocatorOptions().setHasText("Tell us if your property’s occupied")))

    val complianceSummaryList = ComplianceSummaryList(page)

    val tenancyHeading =
        Heading(page.locator("h2.govuk-heading-m", Page.LocatorOptions().setHasText("Tenancy and rental information")))

    val restructuredTenancyHeading =
        Heading(page.locator("h2.govuk-heading-m", Page.LocatorOptions().setHasText("Tenancy details")))
    private val restructuredTenancyRowKeys =
        page
            .locator("h2.govuk-heading-m", Page.LocatorOptions().setHasText("Tenancy details"))
            .locator("xpath=following-sibling::dl[1]//dt[contains(@class,'govuk-summary-list__key')]")

    fun restructuredTenancyRowHeadings(): List<String> {
        val rowCount = restructuredTenancyRowKeys.count()
        return (0 until rowCount).map { index -> restructuredTenancyRowKeys.nth(index).innerText().trim() }
    }

    val jointLandlordsHeading =
        Heading(page.locator("h2.govuk-heading-m", Page.LocatorOptions().setHasText("Invite joint landlords")))

    val complianceCertificatesHeading =
        Heading(page.locator("h2.govuk-heading-m", Page.LocatorOptions().setHasText("Compliance certificates")))

    val gasSafetyHeading =
        Heading(page.locator("h3.govuk-heading-s", Page.LocatorOptions().setHasText("Gas safety certificate")))

    val electricalSafetyHeading =
        Heading(page.locator("h3.govuk-heading-s", Page.LocatorOptions().setHasText("Electrical safety certificate")))

    val epcHeading =
        Heading(page.locator("h3.govuk-heading-s", Page.LocatorOptions().setHasText("Energy performance certificate (EPC)")))

    class CheckAnswersPropertyRegistrationSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val ownershipRow = getRow("Ownership type")
        val licensingRow = getRow("Licensing type")
        val licensingNumberRow = getRow("Licensing number")
        val occupancyQuestionRow = getRow("Is this property occupied by tenants?")
        val occupiedByTenantsRow = getRow(Pattern.compile("^Occupied by tenants$"))
        val tenancyDetailsRow = getRow("Tenancy details")
        val numberOfHouseholdsRow = getRow("Number of households")
        val numberOfTenantsRow = getRow("Number of tenants")
        val numberOfBedroomsRow = getRow("Number of bedrooms")
        val rentAmountRow = getRow("Rent amount")
        val jointLandlordsInvitationsRow = getRow("Invitations")
        val jointLandlordsAreThereRow = getRow("Are there any other landlords for this property?")
    }

    class ComplianceSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val gasSupplyRow = getRow("Does the property have a gas supply or any gas appliances?")
        val electricalCertRow = getRow("Which electrical safety certificate do you have for this property?")
        val hasEpcRow = getRow("Do you have an EPC for this property?")
    }
}
