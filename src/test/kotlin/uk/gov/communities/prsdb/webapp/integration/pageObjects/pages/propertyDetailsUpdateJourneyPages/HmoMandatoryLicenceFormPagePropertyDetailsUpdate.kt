package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLicensingController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LicenceNumberFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep

class HmoMandatoryLicenceFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : LicenceNumberFormPage(
        page,
        UpdateLicensingController.getUpdateLicensingBaseRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${HmoMandatoryLicenceStep.ROUTE_SEGMENT}",
    )
