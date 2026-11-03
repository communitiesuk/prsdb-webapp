package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable

// TODO PDJB-1969: populate from the freeholder details collected by the real freeholder pages
@Serializable
data class FreeholderDataModel(
    val detailsKnown: Boolean,
    val name: String? = null,
    val email: String? = null,
    val address: AddressDataModel? = null,
)
