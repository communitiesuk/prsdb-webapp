package uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

class LandlordSearchResultsViewModelTests {
    @Test
    fun `fromLandlord returns a corresponding LandlordSearchResultsViewModel`() {
        val landlord = MockLandlordData.createLandlord()
        val expectedLandlordSearchResultViewModel =
            LandlordSearchResultViewModel(
                id = landlord.id,
                name = landlord.name,
                registrationNumber =
                    RegistrationNumberDataModel
                        .fromRegistrationNumber(landlord.registrationNumber)
                        .toString(),
                contactAddress = landlord.address.singleLineAddress,
                email = landlord.email,
                phoneNumber = landlord.phoneNumber,
                propertyCount = 0,
                recordLink = LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(landlord.id),
            )

        val landlordSearchResultViewModel = LandlordSearchResultViewModel.fromLandlord(landlord)

        assertEquals(expectedLandlordSearchResultViewModel, landlordSearchResultViewModel)
    }

    @Test
    fun `fromLandlord only includes active properties in count`() {
        val landlord =
            MockLandlordData.createLandlord(
                propertyOwnerships =
                    setOf(
                        MockLandlordData.createPropertyOwnership(isActive = false),
                        MockLandlordData.createPropertyOwnership(),
                        MockLandlordData.createPropertyOwnership(),
                    ),
            )

        val landlordSearchResultViewModel = LandlordSearchResultViewModel.fromLandlord(landlord)

        assertEquals(2, landlordSearchResultViewModel.propertyCount)
    }
}
