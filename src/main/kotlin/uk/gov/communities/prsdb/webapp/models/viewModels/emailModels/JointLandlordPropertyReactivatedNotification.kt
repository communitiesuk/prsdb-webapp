package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordPropertyReactivatedNotification(
    val recipientName: String,
    val multiLineAddress: String,
    val registrationNumber: String,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val addressKey = "property address"
    private val registrationNumberKey = "prn"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.JOINT_LANDLORD_PROPERTY_REACTIVATED_NOTIFICATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            addressKey to multiLineAddress,
            registrationNumberKey to registrationNumber,
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
