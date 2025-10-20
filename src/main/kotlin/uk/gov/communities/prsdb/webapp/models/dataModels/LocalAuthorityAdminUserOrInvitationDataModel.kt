package uk.gov.communities.prsdb.webapp.models.dataModels

data class LocalAuthorityAdminUserOrInvitationDataModel(
    override val id: Long,
    override val userNameOrEmail: String,
    override val localAuthorityName: String,
    override val isPending: Boolean = false,
) : LocalAuthorityMemberOrInvitationDataModel
