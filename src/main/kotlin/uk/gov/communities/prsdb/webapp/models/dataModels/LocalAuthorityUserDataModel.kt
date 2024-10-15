package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class LocalAuthorityUserDataModel(
    val userName: String,
    val isManager: Boolean,
)
