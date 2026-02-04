package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class PropertyRegistrationConfirmationEmail(
    val prn: String,
    val singleLineAddress: String,
    val prsdUrl: String,
    val isOccupied: Boolean,
    val jointLandlordEmails: List<String>? = null,
) : EmailTemplateModel {
    private val prnKey = "prn number"
    private val addressKey = "property address"
    private val prsdUrlKey = "prsd url"
    private val occupiedKey = "occupied"
    private val unoccupiedKey = "unoccupied"
    private val landlordInvitesKey = "landlordInvites"

    override val template = EmailTemplate.PROPERTY_REGISTRATION_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> {
        val baseMap =
            hashMapOf(
                prnKey to prn,
                addressKey to singleLineAddress,
                prsdUrlKey to prsdUrl,
                occupiedKey to if (isOccupied) "yes" else "no",
                unoccupiedKey to if (!isOccupied) "yes" else "no",
            )

        if (!jointLandlordEmails.isNullOrEmpty()) {
            baseMap[landlordInvitesKey] = jointLandlordEmails.joinToString("\n") { "* $it" }
        }

        return baseMap
    }
}
