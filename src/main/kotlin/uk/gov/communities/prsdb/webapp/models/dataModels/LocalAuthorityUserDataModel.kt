package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class LocalAuthorityUserDataModel(
    val id: Long,
    val userName: String,
    val localAuthorityName: String,
    val isManager: Boolean,
    val isPending: Boolean = false,
    // TODO PRSD-405: capture email during registration
    val email: String = "$userName@$localAuthorityName.gov.uk",
)

@Serializable
data class LocalAuthorityUserAccessLevelDataModel(
    val isManager: Boolean,
)
