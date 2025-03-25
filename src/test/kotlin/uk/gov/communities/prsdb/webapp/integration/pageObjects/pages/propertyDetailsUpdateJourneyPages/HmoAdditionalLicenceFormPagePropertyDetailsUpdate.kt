package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LicenceNumberFormPage

class HmoAdditionalLicenceFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : LicenceNumberFormPage(
        page,
        PropertyDetailsController.getUpdatePropertyDetailsPath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UpdatePropertyDetailsStepId.UpdateHmoAdditionalLicence.urlPathSegment}",
    )
