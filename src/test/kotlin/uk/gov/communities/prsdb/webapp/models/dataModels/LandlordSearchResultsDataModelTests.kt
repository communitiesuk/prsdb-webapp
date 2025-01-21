package uk.gov.communities.prsdb.webapp.models.dataModels

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.database.entity.LandlordWithListedPropertyCount
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

    @Test
    fun `fromLandlordWithListedPropertyCount returns a corresponding LandlordSearchResultsDataModel`() {
        val landlord = createLandlord()
        val landlordWithListedPropertyCount = LandlordWithListedPropertyCount(landlord.id, landlord, 3)
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
                listedPropertyCount = 3,
            )

        val landlordSearchResultDataModel =
            LandlordSearchResultDataModel
                .fromLandlordWithListedPropertyCount(landlordWithListedPropertyCount)

        assertEquals(expectedLandlordSearchResultDataModel, landlordSearchResultDataModel)
    }
}
