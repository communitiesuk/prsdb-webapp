package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

// TODO PDJB-117: Implement RemoveJointLandlord page object
class RemoveJointLandlordFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.RemoveJointLandlord.urlPathSegment}",
    ) {
    val form = RemoveJointLandlordForm(page)

    class RemoveJointLandlordForm(
        page: Page,
    ) : FormWithSectionHeader(page)
}
