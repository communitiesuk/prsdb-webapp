package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.NewRegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OccupancyFormPage

class OccupancyFormPagePropertyRegistration(
    page: Page,
) : OccupancyFormPage(
        page,
        "${NewRegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.Occupancy.urlPathSegment}",
    )
