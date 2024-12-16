package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class AddressRegisteredDataModel(
    val uprn: Long? = null,
    val alreadyRegistered: Boolean? = null,
)
