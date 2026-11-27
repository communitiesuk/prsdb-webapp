package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateSystemOperatorPropertyNotesController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SystemOperatorNotesFormBasePage

class SystemOperatorPropertyNotesFormPage(
    page: Page,
    urlArguments: Map<String, String>,
) : SystemOperatorNotesFormBasePage(
        page,
        UpdateSystemOperatorPropertyNotesController.getUpdateNotesStepPath(urlArguments["propertyOwnershipId"]!!.toLong()),
    )
