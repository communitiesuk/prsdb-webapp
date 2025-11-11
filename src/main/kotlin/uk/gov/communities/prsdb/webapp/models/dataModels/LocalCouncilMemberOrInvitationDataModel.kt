package uk.gov.communities.prsdb.webapp.models.dataModels

interface LocalCouncilMemberOrInvitationDataModel {
    val id: Long
    val userNameOrEmail: String
    val localAuthorityName: String
    val isPending: Boolean
}
