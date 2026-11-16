package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EicExpiryDateStep

// TODO PDJB-1679: Remove this page object when the EIC_ISSUE_DATE flag is removed
class EicExpiryDateFormPagePropertyRegistration(
    page: Page,
) : DateFormPage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${EicExpiryDateStep.ROUTE_SEGMENT}")
