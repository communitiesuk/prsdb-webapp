package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController.Companion.UPDATE_ORG_TYPE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.TextFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep

class LeadTrusteeNameFormPageUpdateOrganisationType(
    page: Page,
) : TextFormPage(page, "$UPDATE_ORG_TYPE_ROUTE/${LeadTrusteeNameStep.ROUTE_SEGMENT}")
