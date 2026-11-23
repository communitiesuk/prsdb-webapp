package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordOtherLandlordLeftNotification(
    val leavingLandlord: String,
    val notifiedLandlord: String,
    val address: String,
    val propertyRecordUrl: String,
    // TODO PDJB-2149: Remove this when the ACTIVE_INACTIVE flag is removed
    val useBeforePdjb844Template: Boolean = false,
) : EmailTemplateModel {
    private val leavingLandlordKey = "leaving landlord"
    private val notifiedLandlordKey = "notified landlord"
    private val addressKey = "property address"
    private val propertyRecordUrlKey = "property record url"

    // TODO PDJB-2149: Always use JOINT_LANDLORD_OTHER_LANDLORD_LEFT_NOTIFICATION when the ACTIVE_INACTIVE flag is removed
    override val template =
        if (useBeforePdjb844Template) {
            EmailTemplate.JOINT_LANDLORD_OTHER_LANDLORD_LEFT_NOTIFICATION_BEFORE_PDJB_844
        } else {
            EmailTemplate.JOINT_LANDLORD_OTHER_LANDLORD_LEFT_NOTIFICATION
        }

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            leavingLandlordKey to leavingLandlord,
            notifiedLandlordKey to notifiedLandlord,
            addressKey to address,
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
