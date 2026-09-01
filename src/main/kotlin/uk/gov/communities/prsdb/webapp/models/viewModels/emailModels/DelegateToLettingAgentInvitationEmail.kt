package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class DelegateToLettingAgentInvitationEmail(
    val landlordName: String,
    val propertyAddress: String,
    val invitationLink: String,
    val singleLineAddress: String,
) : EmailTemplateModel {
    private val landlordNameKey = "landlord name"
    private val propertyAddressKey = "property address"
    private val invitationLinkKey = "invitation link"
    private val singleLineAddressKey = "single line address"

    override val template = EmailTemplate.DELEGATE_TO_LETTING_AGENT_INVITATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            landlordNameKey to landlordName,
            propertyAddressKey to propertyAddress,
            invitationLinkKey to invitationLink,
            singleLineAddressKey to singleLineAddress,
        )
}
