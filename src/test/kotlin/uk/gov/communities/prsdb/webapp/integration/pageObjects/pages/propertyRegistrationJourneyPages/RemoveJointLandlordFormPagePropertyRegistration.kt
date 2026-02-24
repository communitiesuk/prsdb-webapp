package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveJointLandlordStep

// TODO PDJB-117: Implement RemoveJointLandlord page object
class RemoveJointLandlordFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RemoveJointLandlordStep.ROUTE_SEGMENT}",
    ) {
    val form = RemoveJointLandlordForm(page)

    class RemoveJointLandlordForm(
        page: Page,
    ) : FormWithSectionHeader(page)
}
