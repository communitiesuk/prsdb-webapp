package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.Serializable
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationGoverningBodyMember

@Serializable
data class GoverningBodyMemberDataModel(
    val name: String,
    val type: GoverningBodyMemberType,
    val dateOfBirth: LocalDate,
    val address: AddressDataModel,
    val addressSearchPostcode: String? = null,
    val addressSearchHouseNameOrNumber: String? = null,
    val selectedAddress: String? = null,
    val manualAddressLineOne: String? = null,
    val manualAddressLineTwo: String? = null,
    val manualTownOrCity: String? = null,
    val manualCounty: String? = null,
    val manualPostcode: String? = null,
) {
    companion object {
        fun fromEntity(member: OrganisationGoverningBodyMember) =
            GoverningBodyMemberDataModel(
                name = member.name,
                type = member.type,
                dateOfBirth = member.dateOfBirth.toKotlinLocalDate(),
                address = AddressDataModel.fromAddress(member.address),
            )
    }
}
