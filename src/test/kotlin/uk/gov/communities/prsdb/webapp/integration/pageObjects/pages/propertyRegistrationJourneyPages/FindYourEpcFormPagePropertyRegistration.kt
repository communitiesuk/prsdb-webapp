package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcStep

class FindYourEpcFormPagePropertyRegistration(
    page: Page,
) : EpcLookupBasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${FindYourEpcStep.ROUTE_SEGMENT}")
