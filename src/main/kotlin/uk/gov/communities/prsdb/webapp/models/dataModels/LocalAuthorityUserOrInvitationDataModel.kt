package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class LocalAuthorityUserOrInvitationDataModel(
    val id: Long,
    val userNameOrEmail: String,
    val localAuthorityName: String,
    val isManager: Boolean,
    val isPending: Boolean = false,
)
