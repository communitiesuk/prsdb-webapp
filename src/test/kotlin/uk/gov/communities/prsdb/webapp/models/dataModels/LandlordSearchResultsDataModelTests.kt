package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createLandlord
import kotlin.test.assertEquals

class LandlordSearchResultsDataModelTests {
    @Test
    fun `fromLandlord returns a corresponding LandlordSearchResultsDataModel`() {
        val landlord = createLandlord()
        val expectedLandlordSearchResultDataModel =
            LandlordSearchResultDataModel(
                id = landlord.id,
                name = landlord.name,
                registrationNumber =
                    RegistrationNumberDataModel
                        .fromRegistrationNumber(landlord.registrationNumber)
                        .toString(),
                contactAddress = landlord.address.singleLineAddress,
                email = landlord.email,
                phoneNumber = landlord.phoneNumber,
            )

        val landlordSearchResultDataModel = LandlordSearchResultDataModel.fromLandlord(landlord)

        assertEquals(expectedLandlordSearchResultDataModel, landlordSearchResultDataModel)
    }
}
