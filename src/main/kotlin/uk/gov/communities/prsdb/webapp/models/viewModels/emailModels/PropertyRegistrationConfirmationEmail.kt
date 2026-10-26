package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import uk.gov.communities.prsdb.webapp.helpers.PaymentHelper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class PropertyRegistrationConfirmationEmail(
    val prn: String,
    val multiLineAddress: String,
    val prsdUrl: String,
    val isOccupied: Boolean,
    val jointLandlordEmails: List<String>? = null,
    val isDelegatedToLettingAgent: Boolean = false,
    val isPdjb939PhaseTwoEnabled: Boolean = false,
    val licenseProvideLater: Boolean = false,
    val gasSafetyCertProvideLater: Boolean = false,
    val electricalSafetyCertProvideLater: Boolean = false,
    val epcProvideLater: Boolean = false,
    val tenancyProvideLater: Boolean = false,
    // TODO PDJB-1734: Make non-nullable when the PAYMENTS flag is removed
    val paymentDetails: PaymentDetails? = null,
) : EmailTemplateModel {
    data class PaymentDetails(
        val amountInPence: Int,
        val paymentDate: LocalDate,
        val renewalDate: LocalDate,
    )

    private val prnKey = "prn number"
    private val addressKey = "property address"
    private val prsdUrlKey = "prsd url"
    private val occupiedKey = "occupied"
    private val unoccupiedKey = "unoccupied"
    private val landlordInvitesKey = "landlordInvites"
    private val hasJointLandlordsKey = "hasJointLandlords"
    private val hasDelegatedToLettingAgentKey = "hasDelegatedToLettingAgent"
    private val lettingAgentProvideListKey = "lettingAgentProvideList"
    private val hasMissingDetailsKey = "hasMissingDetails"
    private val missingDetailsListKey = "missingDetailsList"
    private val paymentAmountKey = "payment amount"
    private val paymentDateKey = "payment date"
    private val renewalDateKey = "renewal date"

    // TODO PDJB-1734: Always use PROPERTY_REGISTRATION_CONFIRMATION_WITH_PAYMENT when the PAYMENTS flag is removed
    override val template =
        if (paymentDetails != null) {
            EmailTemplate.PROPERTY_REGISTRATION_CONFIRMATION_WITH_PAYMENT
        } else {
            EmailTemplate.PROPERTY_REGISTRATION_CONFIRMATION
        }

    override fun toHashMap(): HashMap<String, String> {
        val baseMap =
            hashMapOf(
                prnKey to prn,
                addressKey to multiLineAddress,
                prsdUrlKey to prsdUrl,
                occupiedKey to if (isOccupied) "yes" else "no",
                unoccupiedKey to if (!isOccupied) "yes" else "no",
            )

        if (!jointLandlordEmails.isNullOrEmpty()) {
            baseMap[landlordInvitesKey] = formatAsBulletList(jointLandlordEmails)
            baseMap[hasJointLandlordsKey] = "yes"
        } else {
            baseMap[landlordInvitesKey] = ""
            baseMap[hasJointLandlordsKey] = "no"
        }

        baseMap[hasDelegatedToLettingAgentKey] = if (isDelegatedToLettingAgent) "yes" else "no"
        baseMap[lettingAgentProvideListKey] = if (isDelegatedToLettingAgent) buildLettingProvideList() else ""

        val missingDetails = buildMissingDetailsList()
        val showMissingDetails = isPdjb939PhaseTwoEnabled && !isDelegatedToLettingAgent && isOccupied && missingDetails.isNotEmpty()
        baseMap[hasMissingDetailsKey] = if (showMissingDetails) "yes" else "no"
        baseMap[missingDetailsListKey] = if (showMissingDetails) formatAsBulletList(missingDetails) else ""

        // TODO PDJB-1734: Always add the payment details when the PAYMENTS flag is removed
        paymentDetails?.let {
            baseMap[paymentAmountKey] = PaymentHelper.formatPence(it.amountInPence)
            baseMap[paymentDateKey] = it.paymentDate.format(DATE_FORMATTER)
            baseMap[renewalDateKey] = it.renewalDate.format(DATE_FORMATTER)
        }

        return baseMap
    }

    private fun buildMissingDetailsList(): List<String> =
        listOfNotNull(
            "licensing details".takeIf { licenseProvideLater },
            "gas safety certificate".takeIf { gasSafetyCertProvideLater },
            "electrical safety certificate".takeIf { electricalSafetyCertProvideLater },
            "energy performance certificate (EPC)".takeIf { epcProvideLater },
            "tenancy details".takeIf { tenancyProvideLater },
        )

    private fun buildLettingProvideList(): String {
        val items =
            listOf(
                "licensing details",
                "gas safety certificate",
                "electrical safety certificate",
                "energy performance certificate (EPC)",
                "tenancy details",
            )
        return formatAsBulletList(items)
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
    }
}
