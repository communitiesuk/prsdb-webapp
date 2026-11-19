package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class OrganisationalLandlordRegistrationConfirmationEmail(
    val registrantName: String,
    val organisationName: String,
    val lrn: String,
    val prsdURL: String,
    // TODO: PDJB-1828: Remove when the MULTI_USER_ORGANISATIONS flag is removed
    val isMultiUserOrganisationsEnabled: Boolean = false,
    // TODO PDJB-2149: Make non-nullable when the ACTIVE_INACTIVE flag is removed
    val scheduledDeactivationDate: LocalDate? = null,
) : EmailTemplateModel {
    private val registrantNameKey = "registrant name"
    private val organisationNameKey = "organisation name"
    private val lrnKey = "LRN"
    private val prsdURLKey = "PRSD URL"
    private val isMultiUserOrganisationsEnabledKey = "isMultiUserOrganisationsEnabled"
    private val scheduledDeactivationDateKey = "deactivation date"

    // TODO PDJB-2149: Always use ORGANISATIONAL_LANDLORD_REGISTRATION_CONFIRMATION_EMAIL when the ACTIVE_INACTIVE flag is removed
    override val template =
        if (scheduledDeactivationDate != null) {
            EmailTemplate.ORGANISATIONAL_LANDLORD_REGISTRATION_CONFIRMATION_EMAIL
        } else {
            EmailTemplate.ORGANISATIONAL_LANDLORD_REGISTRATION_CONFIRMATION_EMAIL_BEFORE_844
        }

    override fun toHashMap(): HashMap<String, String> {
        val hashMap =
            hashMapOf(
                registrantNameKey to registrantName,
                organisationNameKey to organisationName,
                lrnKey to lrn,
                prsdURLKey to prsdURL,
                isMultiUserOrganisationsEnabledKey to isMultiUserOrganisationsEnabled.toNotifyYesNo(),
            )

        // TODO PDJB-2149: Always add the scheduled deactivation date when the ACTIVE_INACTIVE flag is removed
        scheduledDeactivationDate?.let { hashMap[scheduledDeactivationDateKey] = it.format(DATE_FORMATTER) }

        return hashMap
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
    }
}
