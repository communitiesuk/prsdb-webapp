package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckOccupancyAnswersFormPage

class CheckPeopleAnswersPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckOccupancyAnswersFormPage(
        page,
        PropertyDetailsController.getUpdatePropertyDetailsPath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UpdatePropertyDetailsStepId.CheckYourPeopleAnswers.urlPathSegment}",
    )
