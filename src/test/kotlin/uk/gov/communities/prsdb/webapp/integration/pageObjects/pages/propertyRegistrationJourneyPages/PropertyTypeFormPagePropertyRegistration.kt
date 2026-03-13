package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyTypeStep

class PropertyTypeFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${PropertyTypeStep.ROUTE_SEGMENT}",
    ) {
    val form = PropertyTypeForm(page)

    fun submitPropertyType(propertyType: PropertyType) {
        form.propertyTypeRadios.selectValue(propertyType)
        form.submit()
    }

    fun submitCustomPropertyType(customPropertyType: String) {
        form.propertyTypeRadios.selectValue(PropertyType.OTHER)
        form.customPropertyTypeInput.fill(customPropertyType)
        form.submit()
    }

    class PropertyTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val propertyTypeRadios = Radios(locator)
        val customPropertyTypeInput = TextInput.textByFieldName(locator, "customPropertyType")
    }
}
