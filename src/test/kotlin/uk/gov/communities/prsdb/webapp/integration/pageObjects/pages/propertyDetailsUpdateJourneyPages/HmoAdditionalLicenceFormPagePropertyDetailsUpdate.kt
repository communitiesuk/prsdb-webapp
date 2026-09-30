package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateLicensingController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LicenceNumberFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep

class HmoAdditionalLicenceFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : LicenceNumberFormPage(
        page,
        LandlordUpdateLicensingController.getUpdateLicensingBaseRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${HmoAdditionalLicenceStep.ROUTE_SEGMENT}",
    )
