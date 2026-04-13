package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class PropertyUpdateConfirmation(
    val name: String,
    val multiLineAddress: String,
    val updatedItems: String,
    val propertyRecordUrl: URI,
) : EmailTemplateModel {
    private val nameKey = "name"
    private val multiLineAddressKey = "multi line address"
    private val updatedItemsKey = "updated items"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.PROPERTY_UPDATE_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            nameKey to name,
            multiLineAddressKey to multiLineAddress,
            updatedItemsKey to updatedItems,
            propertyRecordUrlKey to propertyRecordUrl.toASCIIString(),
        )
}
