package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep

class CheckEpcAnswersFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${CheckEpcAnswersStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = Form(page)

    val epcCard = SummaryCard(page, "Your EPC")

    val epcExpiredText = page.locator("p.govuk-body", Page.LocatorOptions().setHasText("This EPC has expired."))

    val meetsRequirementsInset =
        page.locator(".govuk-inset-text", Page.LocatorOptions().setHasText("meets the energy efficiency requirements"))

    val lowRatingText = page.locator("p.govuk-body", Page.LocatorOptions().setHasText("below E"))

    val lowRatingOccupiedInset =
        page.locator(".govuk-inset-text", Page.LocatorOptions().setHasText("council can see"))

    val occupiedNoEpcInset =
        page.locator(".govuk-inset-text", Page.LocatorOptions().setHasText("council will see"))

    val rows = EpcCyaRows(page)

    class EpcCyaRows(
        page: Page,
    ) : SummaryList(page) {
        val tenancyCheckRow get() = getRow("Was the EPC still in date when the current tenancy began?")
        val hasMeesExemptionRow get() = getRow("Do you have a registered energy efficiency exemption for this property?")
        val meesExemptionRow get() = getRow("Registered exemption")
        val hasEpcRow get() = getRow("Do you have an EPC for this property?")
        val isEpcRequiredRow get() = getRow("Is an EPC required to let this property?")
        val epcExemptionRow get() = getRow("Why does this property not need an EPC?")
    }
}
