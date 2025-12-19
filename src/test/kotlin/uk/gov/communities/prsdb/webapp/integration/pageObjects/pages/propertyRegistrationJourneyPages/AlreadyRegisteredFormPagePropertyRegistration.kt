package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.NewRegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class AlreadyRegisteredFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${NewRegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.AlreadyRegistered.urlPathSegment}",
    )
