package uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

class PropertySearchResultViewModelTests {
    @Test
    fun `fromPropertyOwnership returns a corresponding PropertySearchResultViewModel`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership()

        val expectedPropertySearchResultViewModel =
            PropertySearchResultViewModel(
                id = propertyOwnership.id,
                address = propertyOwnership.property.address.singleLineAddress,
                registrationNumber =
                    RegistrationNumberDataModel
                        .fromRegistrationNumber(propertyOwnership.registrationNumber)
                        .toString(),
                localAuthority =
                    propertyOwnership.property.address.localAuthority
                        ?.name,
                landlord =
                    PropertySearchResultLandlordViewModel(
                        id = propertyOwnership.primaryLandlord.id,
                        name = propertyOwnership.primaryLandlord.name,
                        recordLink = LandlordDetailsController.getLandlordDetailsForLaUserPath(propertyOwnership.primaryLandlord.id),
                    ),
                recordLink = PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLaView = true),
            )

        val propertySearchResultViewModel = PropertySearchResultViewModel.fromPropertyOwnership(propertyOwnership)

        assertEquals(expectedPropertySearchResultViewModel, propertySearchResultViewModel)
    }
}
