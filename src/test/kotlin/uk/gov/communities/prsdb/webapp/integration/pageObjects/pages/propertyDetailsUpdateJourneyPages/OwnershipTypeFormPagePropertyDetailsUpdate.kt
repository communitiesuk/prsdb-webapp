package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OwnershipTypeFormPage

class OwnershipTypeFormPagePropertyDetailsUpdate(
    page: Page,
) : OwnershipTypeFormPage(
        page,
        "${PropertyDetailsController.getUpdatePropertyDetailsPath(1)}/${RegisterPropertyStepId.OwnershipType.urlPathSegment}",
    )
