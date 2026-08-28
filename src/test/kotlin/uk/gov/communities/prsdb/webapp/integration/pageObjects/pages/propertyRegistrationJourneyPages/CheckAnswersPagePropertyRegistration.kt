package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Paragraph
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Warning
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

    val rentedOutHeading =
        Heading(page.locator("h2.govuk-heading-m", Page.LocatorOptions().setHasText("How your property’s rented out")))

    val lettingAgentDelegationSubheading =
        Heading(
            page.locator(
                "h3.govuk-heading-s",
                Page.LocatorOptions().setHasText("Who will provide these details"),
            ),
        )

    val lettingAgentDelegationBodyText =
        Paragraph.byText(
            page,
            "After you’ve paid, we’ll ask your letting agent or property manager to provide the remaining details:",
        )

    val warning = Warning.default(page)

    val submitButton = Button.default(page)

    val complianceSummaryList = ComplianceSummaryList(page)

    val tenancyHeading =
        Heading(page.locator("h2.govuk-heading-m", Page.LocatorOptions().setHasText("Tenancy and rental information")))

    val restructuredTenancyHeading =
        Heading(page.locator("h3.govuk-heading-s", Page.LocatorOptions().setHasText("Tenancy details")))
    val restructuredTenancyUnoccupiedBodyText =
        Paragraph.byText(
            page,
            "We’ll ask for tenancy details when your property becomes occupied.",
        )
    private val restructuredTenancyRowKeys =
        page
            .locator("h3.govuk-heading-s", Page.LocatorOptions().setHasText("Tenancy details"))
            .locator("xpath=following-sibling::dl[1]//dt[contains(@class,'govuk-summary-list__key')]")

    fun restructuredTenancyRowHeadings(): List<String> {
        val rowCount = restructuredTenancyRowKeys.count()
        return (0 until rowCount).map { index -> restructuredTenancyRowKeys.nth(index).innerText().trim() }
    }

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
        // Heading text differs between the legacy CYA ("Ownership type") and the restructured CYA
        // ("How do you own this property?"), so match either depending on which template is rendered.
        val ownershipRow = getRow(Pattern.compile("^(Ownership type|How do you own this property\\?)$"))
        val licensingRow = getRow("Licensing type")
        val licensingNumberRow = getRow("Licensing number")
        val occupancyQuestionRow = getRow("Is this property occupied by tenants?")
        val whoProvidesRentalDetailsRow = getRow("Who will provide this property’s rental details?")
        val lettingAgentEmailRow = getRow("Letting agent or property manager’s email address")
        val occupiedByTenantsRow = getRow(Pattern.compile("^Occupied by tenants$"))
        val tenancyDetailsRow = getRow("Tenancy details")
        val numberOfHouseholdsRow = getRow("Number of households")
        val numberOfTenantsRow = getRow("Number of tenants")
        val numberOfBedroomsRow = getRow("Number of bedrooms")
        val rentAmountRow = getRow("Rent amount")

        // Heading text differs between the legacy CYA ("Invitations") and the restructured CYA
        // ("Joint landlord invitations"), so match either depending on which template is rendered.
        val jointLandlordsInvitationsRow = getRow(Pattern.compile("^(Invitations|Joint landlord invitations)$"))
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
