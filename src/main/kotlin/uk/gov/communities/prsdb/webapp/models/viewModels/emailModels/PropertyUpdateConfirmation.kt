package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class PropertyUpdateConfirmation(
    val singleLineAddress: String,
    val registrationNumber: String,
    val dashboardUrl: URI,
    val updatedBullets: EmailBulletPointList,
) : EmailTemplateModel {
    private val singleLineAddressKey = "single line address"
    private val registrationNumberKey = "registration number"
    private val dashboardUrlKey = "dashboard url"
    private val updatedBulletsKey = "updated bullets"

    override val templateId = EmailTemplateId.PROPERTY_UPDATE_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            singleLineAddressKey to singleLineAddress,
            registrationNumberKey to registrationNumber,
            dashboardUrlKey to dashboardUrl.toASCIIString(),
            updatedBulletsKey to updatedBullets.toString(),
        )
}
