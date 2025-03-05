package uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.database.entity.LandlordWithListedPropertyCount
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlord
import kotlin.test.assertEquals

class LandlordSearchResultsViewModelTests {
    @Test
    fun `fromLandlordWithListedPropertyCount returns a corresponding LandlordSearchResultsViewModel`() {
        val landlord = createLandlord()
        val landlordWithListedPropertyCount = LandlordWithListedPropertyCount(landlord.id, landlord, 3)
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
                listedPropertyCount = 3,
            )

        val landlordSearchResultViewModel =
            LandlordSearchResultViewModel
                .fromLandlordWithListedPropertyCount(landlordWithListedPropertyCount)

        assertEquals(expectedLandlordSearchResultViewModel, landlordSearchResultViewModel)
    }
}
