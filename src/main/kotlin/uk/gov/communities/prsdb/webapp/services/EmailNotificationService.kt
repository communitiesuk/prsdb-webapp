package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.exceptions.NoLandlordEmailRecipientsException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordEmailTemplateModel

interface EmailNotificationService<in EmailModel : EmailTemplateModel> {
    fun sendEmail(
        recipientAddress: String,
        email: EmailModel,
    )
}

fun <T : LandlordEmailTemplateModel> EmailNotificationService<T>.sendEmailToLandlord(
    landlord: Landlord,
    email: T,
) {
    val recipientAddresses =
        when (landlord) {
            is IndividualLandlord -> listOf(landlord.email)
            is OrganisationalLandlord -> resolveOrgRecipientAddresses(landlord, email)
            else -> throw IllegalArgumentException("Unsupported landlord type: ${landlord::class.simpleName}")
        }

    if (recipientAddresses.isEmpty()) {
        throw NoLandlordEmailRecipientsException(landlord.id, email.orgRolesToSendTo)
    }

    recipientAddresses.forEach { recipientAddress ->
        sendEmail(recipientAddress, email)
    }
}

private fun <T : LandlordEmailTemplateModel> resolveOrgRecipientAddresses(
    landlord: OrganisationalLandlord,
    email: T,
): List<String> =
    landlord.organisationalLandlordUsers
        .filter { user -> user.role in email.orgRolesToSendTo }
        .map { it.email }
