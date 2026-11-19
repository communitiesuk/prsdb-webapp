package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class LandlordRegistrationConfirmationEmail(
    val lrn: String,
    val prsdURL: String,
    // TODO PDJB-2149: Make non-nullable when the ACTIVE_INACTIVE flag is removed
    val scheduledDeactivationDate: LocalDate? = null,
) : EmailTemplateModel {
    // TODO PDJB-2149: Always use LANDLORD_REGISTRATION_CONFIRMATION_EMAIL when the ACTIVE_INACTIVE flag is removed
    override val template =
        if (scheduledDeactivationDate != null) {
            EmailTemplate.LANDLORD_REGISTRATION_CONFIRMATION_EMAIL
        } else {
            EmailTemplate.LANDLORD_REGISTRATION_CONFIRMATION_EMAIL_BEFORE_844
        }

    override fun toHashMap(): HashMap<String, String> {
        val hashMap =
            hashMapOf(
                "PRSD URL" to prsdURL,
                "LRN" to lrn,
            )

        // TODO PDJB-2149: Always add the scheduled deactivation date when the ACTIVE_INACTIVE flag is removed
        scheduledDeactivationDate?.let { hashMap["deactivation date"] = it.format(DATE_FORMATTER) }

        return hashMap
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
    }
}
