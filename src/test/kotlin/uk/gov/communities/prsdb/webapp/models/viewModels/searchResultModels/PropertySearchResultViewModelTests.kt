package uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
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
                address = propertyOwnership.address.singleLineAddress,
                registrationNumber =
                    RegistrationNumberDataModel
                        .fromRegistrationNumber(propertyOwnership.registrationNumber)
                        .toString(),
                localCouncil =
                    propertyOwnership.address.localCouncil?.name,
                landlords =
                    propertyOwnership.landlords
                        .sortedBy { it.id }
                        .map { landlord ->
                            PropertySearchResultLandlordViewModel(
                                id = landlord.id,
                                name = landlord.name,
                                recordLink =
                                    LandlordDetailsController
                                        .getLandlordDetailsForLocalCouncilUserPath(landlord.id)
                                        .overrideBackLinkForUrl(null),
                            )
                        },
                recordLink = PropertyDetailsController.getPropertyDetailsPath(propertyOwnership.id, isLocalCouncilView = true),
            )

        val propertySearchResultViewModel = PropertySearchResultViewModel.fromPropertyOwnership(propertyOwnership)

        assertEquals(expectedPropertySearchResultViewModel, propertySearchResultViewModel)
    }

    @Test
    fun `fromPropertyOwnership includes every landlord on the property ordered by id`() {
        val landlordA = MockLandlordData.createLandlord(name = "Alice")
        val landlordB = MockLandlordData.createLandlord(name = "Bob")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(primaryLandlord = landlordA, otherLandlords = mutableSetOf(landlordB))

        val result = PropertySearchResultViewModel.fromPropertyOwnership(propertyOwnership)

        assertEquals(
            propertyOwnership.landlords.sortedBy { it.id }.map { it.id },
            result.landlords.map { it.id },
        )
        assertEquals(
            propertyOwnership.landlords.sortedBy { it.id }.map { it.name },
            result.landlords.map { it.name },
        )
    }
}
