package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateSystemOperatorLandlordNotesController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SystemOperatorNotesFormBasePage

class SystemOperatorLandlordNotesFormPage(
    page: Page,
    urlArguments: Map<String, String>,
) : SystemOperatorNotesFormBasePage(page, UpdateSystemOperatorLandlordNotesController.getUpdateNotesStepPath(urlArguments["id"]!!.toLong()))
