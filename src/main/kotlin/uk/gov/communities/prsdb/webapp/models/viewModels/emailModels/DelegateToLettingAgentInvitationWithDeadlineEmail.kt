package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class DelegateToLettingAgentInvitationWithDeadlineEmail(
    val landlordName: String,
    val propertyAddress: String,
    val invitationLink: String,
    val deadlineDate: String,
    val singleLineAddress: String,
) : EmailTemplateModel {
    private val landlordNameKey = "landlord name"
    private val propertyAddressKey = "property address"
    private val invitationLinkKey = "invitation link"
    private val deadlineDateKey = "deadline date"
    private val singleLineAddressKey = "single line address"

    override val template = EmailTemplate.DELEGATE_TO_LETTING_AGENT_INVITATION_WITH_DEADLINE_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            landlordNameKey to landlordName,
            propertyAddressKey to propertyAddress,
            invitationLinkKey to invitationLink,
            deadlineDateKey to deadlineDate,
            singleLineAddressKey to singleLineAddress,
        )
}
