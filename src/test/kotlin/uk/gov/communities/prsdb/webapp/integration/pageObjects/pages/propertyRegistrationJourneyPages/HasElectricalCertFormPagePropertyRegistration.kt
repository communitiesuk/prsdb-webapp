package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.HasElectricalCertFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep

class HasElectricalCertFormPagePropertyRegistration(
    page: Page,
) : HasElectricalCertFormBasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${HasElectricalCertStep.ROUTE_SEGMENT}")
