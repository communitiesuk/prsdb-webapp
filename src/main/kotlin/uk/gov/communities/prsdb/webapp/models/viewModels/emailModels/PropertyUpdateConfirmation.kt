package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class PropertyUpdateConfirmation(
    val singleLineAddress: String,
    val registrationNumber: String,
    val updatedBullets: List<String>,
    val dashboardUrl: URI,
) : EmailTemplateModel {
    private val singleLineAddressKey = "single line address"
    private val registrationNumberKey = "registration number"
    private val updatedBulletsKey = "updated bullets"
    private val dashboardUrlKey = "dashboard url"

    override val template = EmailTemplate.PROPERTY_UPDATE_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            singleLineAddressKey to singleLineAddress,
            registrationNumberKey to registrationNumber,
            updatedBulletsKey to updatedBullets.joinToString("\n") { "* $it" },
            dashboardUrlKey to dashboardUrl.toASCIIString(),
        )
}
