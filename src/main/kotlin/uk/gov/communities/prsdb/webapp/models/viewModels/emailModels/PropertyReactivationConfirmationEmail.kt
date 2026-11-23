package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.helpers.PaymentHelper
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PropertyReactivationConfirmationEmail(
    val recipientName: String,
    val multiLineAddress: String,
    val registrationNumber: String,
    val dashboardUrl: String,
    val hasJointLandlords: Boolean,
    val landlordReactivated: Boolean,
    val paymentDetails: PropertyRegistrationConfirmationEmail.PaymentDetails? = null,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val addressKey = "property address"
    private val registrationNumberKey = "prn"
    private val dashboardUrlKey = "dashboard url"
    private val hasJointLandlordsKey = "hasJointLandlords"
    private val landlordReactivatedKey = "landlordReactivated"
    private val paymentAmountKey = "payment amount"
    private val paymentDateKey = "payment date"
    private val renewalDateKey = "renewal date"

    override val template =
        if (paymentDetails != null) {
            EmailTemplate.PROPERTY_REACTIVATION_CONFIRMATION_WITH_PAYMENT
        } else {
            EmailTemplate.PROPERTY_REACTIVATION_CONFIRMATION
        }

    override fun toHashMap(): HashMap<String, String> {
        val hashMap =
            hashMapOf(
                recipientNameKey to recipientName,
                addressKey to multiLineAddress,
                registrationNumberKey to registrationNumber,
                dashboardUrlKey to dashboardUrl,
                hasJointLandlordsKey to if (hasJointLandlords) "yes" else "no",
                landlordReactivatedKey to if (landlordReactivated) "yes" else "no",
            )

        paymentDetails?.let {
            hashMap[paymentAmountKey] = PaymentHelper.formatPence(it.amountInPence)
            hashMap[paymentDateKey] = it.paymentDate.format(DATE_FORMATTER)
            hashMap[renewalDateKey] = it.renewalDate.format(DATE_FORMATTER)
        }

        return hashMap
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
    }
}
