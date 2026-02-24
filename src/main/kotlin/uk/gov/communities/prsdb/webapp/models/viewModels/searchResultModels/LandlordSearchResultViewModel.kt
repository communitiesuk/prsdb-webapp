package uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels

import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor.Companion.overrideBackLinkForUrl
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.models.dataModels.LandlordSearchResultDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

data class LandlordSearchResultViewModel(
    val id: Long,
    val name: String,
    val registrationNumber: String,
    val contactAddress: String,
    val email: String,
    val phoneNumber: String,
    val recordLink: String,
    val propertyCount: Int,
) {
    companion object {
        fun fromDataModel(
            dataModel: LandlordSearchResultDataModel,
            currentUrlKey: Int? = null,
        ) = LandlordSearchResultViewModel(
            id = dataModel.id,
            name = dataModel.name,
            registrationNumber = RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, dataModel.registrationNumber).toString(),
            contactAddress = dataModel.singleLineAddress,
            email = dataModel.email,
            phoneNumber = dataModel.phoneNumber,
            propertyCount = dataModel.propertyCount.toInt(),
            recordLink =
                LandlordDetailsController
                    .getLandlordDetailsForLocalCouncilUserPath(dataModel.id)
                    .overrideBackLinkForUrl(currentUrlKey),
        )
    }
}
