package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordOtherLandlordLeftNotification(
    val leavingLandlord: String,
    val notifiedLandlord: String,
    val address: String,
    val propertyRecordUrl: String,
    val removalAction: String,
) : EmailTemplateModel {
    private val leavingLandlordKey = "leaving landlord"
    private val notifiedLandlordKey = "notified landlord"
    private val addressKey = "property address"
    private val propertyRecordUrlKey = "property record url"
    private val removalActionKey = "removal action"

    override val template = EmailTemplate.JOINT_LANDLORD_OTHER_LANDLORD_LEFT_NOTIFICATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            leavingLandlordKey to leavingLandlord,
            notifiedLandlordKey to notifiedLandlord,
            addressKey to address,
            propertyRecordUrlKey to propertyRecordUrl,
            removalActionKey to removalAction,
        )
}
