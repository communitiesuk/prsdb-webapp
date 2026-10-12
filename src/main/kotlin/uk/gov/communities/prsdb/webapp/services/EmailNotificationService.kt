package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.exceptions.NoLandlordEmailRecipientsException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel

interface EmailNotificationService<in EmailModel : EmailTemplateModel> {
    fun sendEmail(
        recipientAddress: String,
        email: EmailModel,
    )

    fun sendEmailToLandlord(
        landlord: Landlord,
        email: EmailModel,
    ) {
        val recipientAddresses =
            when (landlord) {
                is IndividualLandlord -> listOf(landlord.email)
                is OrganisationalLandlord -> landlord.adminEmailAddresses
                else -> throw IllegalArgumentException("Unsupported landlord type: ${landlord::class.simpleName}")
            }

        if (recipientAddresses.isEmpty()) {
            throw NoLandlordEmailRecipientsException(landlord.id)
        }

        recipientAddresses.forEach { recipientAddress ->
            sendEmail(recipientAddress, email)
        }
    }
}
