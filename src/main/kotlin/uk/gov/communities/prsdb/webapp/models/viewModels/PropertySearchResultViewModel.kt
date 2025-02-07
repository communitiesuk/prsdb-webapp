package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

data class PropertySearchResultViewModel(
    val id: Long,
    val address: String,
    val registrationNumber: String,
    val localAuthority: String?,
    val landlord: PropertySearchResultLandlordViewModel,
) {
    companion object {
        fun fromPropertyOwnership(propertyOwnership: PropertyOwnership) =
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
                    ),
            )
    }
}

data class PropertySearchResultLandlordViewModel(
    val id: Long,
    val name: String,
)
