package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.FormBasePage

class PropertyTypeFormPagePropertyRegistration(
    page: Page,
) : FormBasePage(
        page,
        "/$REGISTER_PROPERTY_JOURNEY_URL/${RegisterPropertyStepId.PropertyType.urlPathSegment}",
    ) {
    val customPropertyTypeInput = form.getTextInput("customPropertyType")

    fun setRadioValue(propertyType: PropertyType) {
        val radio = form.getRadios().getRadio(propertyType.name)
        radio.check()
    }
}
