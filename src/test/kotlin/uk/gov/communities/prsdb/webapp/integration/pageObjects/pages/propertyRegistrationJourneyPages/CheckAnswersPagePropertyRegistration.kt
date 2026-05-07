package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationCyaStep

class CheckAnswersPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${PropertyRegistrationCyaStep.ROUTE_SEGMENT}") {
    fun confirm() = form.submit()

    val form = PostForm(page)

    val sectionHeader = SectionHeader(page.locator("main"))

    val heading = Heading(page.locator("h1"))

    val summaryList = CheckAnswersPropertyRegistrationSummaryList(page)

    val complianceSummaryList = ComplianceSummaryList(page)

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
    }

    class ComplianceSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val gasSupplyRow = getRow("Does the property have a gas supply or any gas appliances?")
        val electricalCertRow = getRow("Which electrical safety certificate do you have for this property?")
        val hasEpcRow = getRow("Do you have an EPC for this property?")
    }
}
