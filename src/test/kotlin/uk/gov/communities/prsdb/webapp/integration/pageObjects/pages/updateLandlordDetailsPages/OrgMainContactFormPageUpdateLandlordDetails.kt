package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationMainContactController.Companion.UPDATE_ORG_MAIN_CONTACT_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Warning
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgMainContactFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep

class OrgMainContactFormPageUpdateLandlordDetails(
    page: Page,
) : OrgMainContactFormBasePage(
        page,
        "$UPDATE_ORG_MAIN_CONTACT_ROUTE/${OrgMainContactStep.ROUTE_SEGMENT}",
    ) {
    val warning = Warning.default(page)
}
