package uk.gov.communities.prsdb.webapp.models.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class LocalAuthorityAdminUserOrInvitationDataModel(
    val id: Long,
    val userNameOrEmail: String,
    val localCouncilName: String,
    val isPending: Boolean = false,
)
