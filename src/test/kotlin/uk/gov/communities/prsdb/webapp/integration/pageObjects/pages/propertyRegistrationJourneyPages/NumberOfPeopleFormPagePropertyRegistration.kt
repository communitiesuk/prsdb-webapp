package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NumberOfPeopleFormPage

class NumberOfPeopleFormPagePropertyRegistration(
    page: Page,
) : NumberOfPeopleFormPage(
        page,
        "/$REGISTER_PROPERTY_JOURNEY_URL/${RegisterPropertyStepId.NumberOfPeople.urlPathSegment}",
    )
