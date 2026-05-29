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
                address = propertyOwnership.propertyDetails.address.singleLineAddress,
                registrationNumber =
                    RegistrationNumberDataModel
                        .fromRegistrationNumber(propertyOwnership.landlordship.registrationNumber)
                        .toString(),
                localCouncil =
                    propertyOwnership.propertyDetails.address.localCouncil?.name,
                landlord =
                    PropertySearchResultLandlordViewModel(
                        id = propertyOwnership.landlordship.primaryLandlord.id,
                        name = propertyOwnership.landlordship.primaryLandlord.name,
                        recordLink =
                            LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(
                                propertyOwnership.landlordship.primaryLandlord.id,
                            ),
                    ),
                recordLink = PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = true),
            )

        val propertySearchResultViewModel = PropertySearchResultViewModel.fromPropertyOwnership(propertyOwnership)

        assertEquals(expectedPropertySearchResultViewModel, propertySearchResultViewModel)
    }
}
