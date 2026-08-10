package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateOrganisationTypeJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController.Companion.UPDATE_ORG_TYPE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PhoneNumberFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteePhoneStep

class LeadTrusteePhoneFormPageUpdateOrganisationType(
    page: Page,
) : PhoneNumberFormPage(page, "$UPDATE_ORG_TYPE_ROUTE/${LeadTrusteePhoneStep.ROUTE_SEGMENT}")
