package uk.gov.communities.prsdb.webapp.models.dataModels

interface LocalAuthorityMemberOrInvitationDataModel {
    val id: Long
    val userNameOrEmail: String
    val localAuthorityName: String
    val isPending: Boolean
}
