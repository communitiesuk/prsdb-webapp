package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NumberOfPeopleFormPage

class NumberOfPeopleFormPagePropertyDetailsUpdate(
    page: Page,
) : NumberOfPeopleFormPage(
        page,
        "${PropertyDetailsController.getUpdatePropertyDetailsPath(1)}/${UpdatePropertyDetailsStepId.UpdateNumberOfPeople.urlPathSegment}",
    )
