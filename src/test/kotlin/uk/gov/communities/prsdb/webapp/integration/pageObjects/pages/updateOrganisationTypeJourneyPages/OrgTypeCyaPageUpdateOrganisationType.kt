package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController.Companion.UPDATE_ORG_TYPE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType.OrgTypeCyaStep

class OrgTypeCyaPageUpdateOrganisationType(
    page: Page,
) : BasePage(page, "$UPDATE_ORG_TYPE_ROUTE/${OrgTypeCyaStep.ROUTE_SEGMENT}") {
    val form = PostForm(page)

    fun submit() = form.submit()
}
