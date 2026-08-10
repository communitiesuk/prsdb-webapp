package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController.Companion.UPDATE_ORG_TYPE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.InterruptionPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType.OrgTypeTrustInterruptionStep

class OrgTypeTrustInterruptionPageUpdateOrganisationType(
    page: Page,
) : InterruptionPage(page, "$UPDATE_ORG_TYPE_ROUTE/${OrgTypeTrustInterruptionStep.ROUTE_SEGMENT}")
