package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController.Companion.UPDATE_ORG_TYPE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EmailFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeEmailStep

class LeadTrusteeEmailFormPageUpdateOrganisationType(
    page: Page,
) : EmailFormPage(page, "$UPDATE_ORG_TYPE_ROUTE/${LeadTrusteeEmailStep.ROUTE_SEGMENT}")
