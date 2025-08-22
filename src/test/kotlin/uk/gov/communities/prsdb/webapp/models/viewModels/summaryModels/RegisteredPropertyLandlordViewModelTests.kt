package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership

class RegisteredPropertyLandlordViewModelTests {
    @Test
    fun `Returns a corresponding RegisteredPropertyLandlordViewModel from a PropertyOwnership`() {
        val propertyOwnership = createPropertyOwnership()

        val expectedRegisteredPropertyLandlordViewModel =
            RegisteredPropertyLandlordViewModel(
                address = propertyOwnership.property.address.singleLineAddress,
                registrationNumber = RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
                recordLink = PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id),
            )

        val result = RegisteredPropertyLandlordViewModel.fromPropertyOwnership(propertyOwnership)

        assertEquals(expectedRegisteredPropertyLandlordViewModel, result)
    }
}
