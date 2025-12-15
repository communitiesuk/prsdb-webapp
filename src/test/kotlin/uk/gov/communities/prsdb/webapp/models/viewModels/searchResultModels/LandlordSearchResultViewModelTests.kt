package uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.models.dataModels.LandlordSearchResultDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import kotlin.test.assertEquals

class LandlordSearchResultViewModelTests {
    @Test
    fun `fromDataModel returns a corresponding LandlordSearchResultsViewModel`() {
        val dataModel =
            LandlordSearchResultDataModel(
                id = 1,
                name = "landlord name",
                email = "landlord@test.org",
                phoneNumber = "01234567890",
                registrationNumber = 123456,
                singleLineAddress = "123 Test Street, Test Town, TE1 1ST",
                propertyCount = 5,
            )

        val expectedLandlordSearchResultViewModel =
            LandlordSearchResultViewModel(
                id = dataModel.id,
                name = dataModel.name,
                registrationNumber = RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, dataModel.registrationNumber).toString(),
                contactAddress = dataModel.singleLineAddress,
                email = dataModel.email,
                phoneNumber = dataModel.phoneNumber,
                propertyCount = dataModel.propertyCount.toInt(),
                recordLink = LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(dataModel.id),
            )

        val viewModel = LandlordSearchResultViewModel.fromDataModel(dataModel)

        assertEquals(expectedLandlordSearchResultViewModel, viewModel)
    }
}
