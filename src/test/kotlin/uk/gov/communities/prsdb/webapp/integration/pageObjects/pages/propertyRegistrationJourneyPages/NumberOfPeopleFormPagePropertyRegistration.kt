package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NumberOfPeopleFormPage

class NumberOfPeopleFormPagePropertyRegistration(
    page: Page,
) : NumberOfPeopleFormPage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.NumberOfPeople.urlPathSegment}",
    )
