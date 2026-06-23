package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordPropertyUpdateNotificationEmail(
    val recipientName: String,
    val propertyAddress: String,
    val updatedBullets: List<String>,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val propertyAddressKey = "property address"
    private val updatedBulletsKey = "updated bullets"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.JOINT_LANDLORD_PROPERTY_UPDATE_NOTIFICATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            propertyAddressKey to propertyAddress,
            updatedBulletsKey to formatAsBulletList(updatedBullets),
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
