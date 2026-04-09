package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.MeesExemptionReasonBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep

class MeesExemptionFormPagePropertyRegistration(
    page: Page,
) : MeesExemptionReasonBasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${MeesExemptionStep.ROUTE_SEGMENT}",
    )
