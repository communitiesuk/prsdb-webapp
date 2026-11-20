package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Warning
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckElectricalSafetyAnswersSummaryList
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationCheckAnswersStep

class CheckAnswersPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ReactivationCheckAnswersStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1.govuk-heading-xl"))
    val licensingHeading =
        Heading(page.locator("h2.govuk-heading-m", Page.LocatorOptions().setHasText("Tell us if the property needs a licence")))
    val gasSafetyHeading = Heading(page.locator("h2.govuk-heading-m", Page.LocatorOptions().setHasText("Gas safety certificate")))
    val electricalSafetyHeading =
        Heading(page.locator("h2.govuk-heading-m", Page.LocatorOptions().setHasText("Electrical safety certificate")))
    val epcHeading =
        Heading(page.locator("h2.govuk-heading-m", Page.LocatorOptions().setHasText("Energy performance certificate (EPC)")))
    val tenancyHeading = Heading(page.locator("h2.govuk-heading-m", Page.LocatorOptions().setHasText("Tenancy details")))
    val licensingSummaryList = LicensingSummaryList(page)
    val complianceSummaryList = ComplianceSummaryList(page)
    val tenancySummaryList = TenancySummaryList(page)
    val epcCard = SummaryCard(page, "Your EPC", headingLevel = "h3")
    val warning = Warning.default(page)
    val backLink = BackLink.default(page)
    val form = PostForm(page)

    fun submit() = form.submit()

    class LicensingSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val licensingTypeRow = getRow("Licensing type")
        val licensingNumberRow = getRow("Licensing number")
    }

    class ComplianceSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val gasSupplyRow = getRow("Does the property have a gas supply or any gas appliances?")
        val validGasCertRow = getRow("Do you have a valid gas safety certificate for this property?")
        val gasCertIssueDateRow = getRow("Issue date")
        val gasCertUploadRow = getRow("Your certificate")
        val electricalCertRow = getRow("Which electrical safety certificate do you have for this property?")
        val electricalCertUploadRow = getRow("Your certificate")
        val epcTenancyCheckRow = getRow("Was the EPC still in date when the current tenancy began?")
        val isEpcRequiredRow = getRow("Is an EPC required to let this property?")
        val epcExemptionRow = getRow("Why does this property not need an EPC?")
        val hasMeesExemptionRow = getRow("Do you have a registered energy efficiency exemption for this property?")
        val meesExemptionRow = getRow("Registered exemption")

        // TODO PDJB-1679: Remove this when the EIC_ISSUE_DATE flag is removed
        val eicExpiryDateRow = getRow("Expiry date")

        fun electricalCertDateRow(certificateType: CertificateType) =
            getRow(CheckElectricalSafetyAnswersSummaryList.certificateDateKey(certificateType))
    }

    class TenancySummaryList(
        page: Page,
    ) : SummaryList(page) {
        val numberOfHouseholdsRow = getRow("Number of households")
        val numberOfTenantsRow = getRow("Number of tenants")
        val rentIncludesBillsRow = getRow("Rent includes bills")
        val billsIncludedRow = getRow("Which bills are included")
        val furnishedStatusRow = getRow("Furniture provided")
        val rentFrequencyRow = getRow("When rent is paid")
        val rentAmountRow = getRow("Rent amount")
    }
}
