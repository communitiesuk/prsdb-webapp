package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.PersonContactType
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonContactTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsTask

// TODO PDJB-1974: replace this placeholder with the real superior landlord contact type page
class SuperiorLandlordContactTypeFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${SuperiorLandlordsTask.ROUTE_SEGMENT}/" +
            PersonContactTypeStep.ROUTE_SEGMENT,
    ) {
    val form = SuperiorLandlordContactTypeForm(page)

    fun submitContactType(contactType: PersonContactType) {
        form.radios.selectValue(contactType)
        form.submit()
    }

    class SuperiorLandlordContactTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val radios = Radios(locator)
    }
}
