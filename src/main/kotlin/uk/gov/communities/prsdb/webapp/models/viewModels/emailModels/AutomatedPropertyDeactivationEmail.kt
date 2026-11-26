package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.constants.enums.ReactivationTaskType

data class AutomatedPropertyDeactivationEmail(
    val landlordName: String,
    val multiLineAddress: String,
    val prn: String,
    val reactivationUrl: String,
    val reasons: Collection<ReactivationTaskType>,
    val landlordDeactivated: Boolean,
) : EmailTemplateModel {
    override val template = EmailTemplate.AUTOMATED_PROPERTY_DEACTIVATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            "landlord name" to landlordName,
            "property address" to multiLineAddress,
            "prn number" to prn,
            "reactivation url" to reactivationUrl,
            "missing details" to formatAsBulletList(reasons.map(::message)),
            "landlord deactivated" to landlordDeactivated.toNotifyYesNo(),
        )

    private fun message(reason: ReactivationTaskType): String =
        when (reason) {
            ReactivationTaskType.LICENSING -> "licensing details"
            ReactivationTaskType.GAS_SAFETY -> "gas safety certificate"
            ReactivationTaskType.ELECTRICAL_SAFETY -> "electrical safety certificate"
            ReactivationTaskType.EPC -> "energy performance certificate (EPC)"
            ReactivationTaskType.TENANCY_DETAILS -> "tenancy details"
        }
}
