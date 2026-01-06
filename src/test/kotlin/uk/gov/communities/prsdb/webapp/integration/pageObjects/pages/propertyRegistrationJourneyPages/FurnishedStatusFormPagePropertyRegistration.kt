package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.controllers.NewRegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class FurnishedStatusFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${NewRegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.FurnishedStatus.urlPathSegment}",
    ) {
    val form = FurnishedForm(page)

    fun submitFurnishedStatus(furnishedStatus: FurnishedStatus) {
        form.furnishedRadios.selectValue(furnishedStatus)
        form.submit()
    }

    class FurnishedForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val furnishedRadios = Radios(page)
    }
}
