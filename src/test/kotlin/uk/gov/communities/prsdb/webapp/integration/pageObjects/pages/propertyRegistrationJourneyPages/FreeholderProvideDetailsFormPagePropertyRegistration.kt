package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonProvideDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

// TODO PDJB-1963: replace this placeholder with the real "can you provide details about the freeholder" page
class FreeholderProvideDetailsFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${PersonProvideDetailsStep.ROUTE_SEGMENT}") {
    val form = FreeholderProvideDetailsForm(page)

    fun submitProvideDetails(provideDetails: YesOrNo) {
        form.radios.selectValue(provideDetails)
        form.submit()
    }

    class FreeholderProvideDetailsForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val radios = Radios(locator)
    }
}
