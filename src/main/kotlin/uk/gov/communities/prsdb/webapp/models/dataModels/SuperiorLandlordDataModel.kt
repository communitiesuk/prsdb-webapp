package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable

// TODO PDJB-1978: populate from the superior landlord details collected by the real superior landlord pages
@Serializable
data class SuperiorLandlordDataModel(
    val detailsKnown: Boolean,
    val name: String? = null,
    val email: String? = null,
    val address: AddressDataModel? = null,
)
