package uk.gov.communities.prsdb.webapp.models.dataModels

data class LocalCouncilUserOrInvitationDataModel(
    override val id: Long,
    override val userNameOrEmail: String,
    override val localAuthorityName: String,
    val isManager: Boolean,
    override val isPending: Boolean = false,
) : LocalCouncilMemberOrInvitationDataModel
