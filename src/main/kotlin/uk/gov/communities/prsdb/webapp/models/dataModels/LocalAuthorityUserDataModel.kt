package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class LocalAuthorityUserDataModel(
    val id: Long,
    val userName: String,
    val localAuthorityName: String,
    val isManager: Boolean,
    val email: String,
    val isPending: Boolean = false,
)
