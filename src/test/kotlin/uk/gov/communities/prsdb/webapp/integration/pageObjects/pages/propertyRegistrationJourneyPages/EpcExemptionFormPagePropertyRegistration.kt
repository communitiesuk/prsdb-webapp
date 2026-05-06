package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcExemptionFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep

class EpcExemptionFormPagePropertyRegistration(
    page: Page,
) : EpcExemptionFormBasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${EpcExemptionStep.ROUTE_SEGMENT}")
