package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LicensingTypeFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep

class LicensingTypeFormPagePropertyRegistration(
    page: Page,
) : LicensingTypeFormPage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${LicensingTypeStep.ROUTE_SEGMENT}",
    )
