package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordOtherLandlordLeftDeactivateNotification(
    val leavingLandlord: String,
    val notifiedLandlord: String,
    val address: String,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val leavingLandlordKey = "leaving landlord"
    private val notifiedLandlordKey = "notified landlord"
    private val addressKey = "property address"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.JOINT_LANDLORD_OTHER_LANDLORD_LEFT_DEACTIVATE_NOTIFICATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            leavingLandlordKey to leavingLandlord,
            notifiedLandlordKey to notifiedLandlord,
            addressKey to address,
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
