package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController.Companion.UPDATE_ORG_TYPE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeDobStep

class LeadTrusteeDobFormPageUpdateOrganisationType(
    page: Page,
) : DateFormPage(page, "$UPDATE_ORG_TYPE_ROUTE/${LeadTrusteeDobStep.ROUTE_SEGMENT}")
