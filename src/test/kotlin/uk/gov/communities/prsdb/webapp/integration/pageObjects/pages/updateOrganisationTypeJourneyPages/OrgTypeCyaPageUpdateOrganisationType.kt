package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController.Companion.UPDATE_ORG_TYPE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType.OrgTypeCyaStep

class OrgTypeCyaPageUpdateOrganisationType(
    page: Page,
) : BasePage(page, "$UPDATE_ORG_TYPE_ROUTE/${OrgTypeCyaStep.ROUTE_SEGMENT}") {
    val form = Form(page)
    val summaryList = OrgTypeSummaryList(page)
    val leadTrusteeCard = LeadTrusteeSummaryCard(page)

    fun submit() = form.submit()

    class OrgTypeSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val organisationTypeRow = getRow("Organisation type")
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
            val phoneRow = getRow("Phone number")
            val addressRow = getRow("Address")
        }
    }
}
