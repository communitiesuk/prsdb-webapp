package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ASYNCHRONOUS_EMAIL_SENDING
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.exceptions.NoLandlordEmailRecipientsException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel

@Service
class EmailNotificationService<in EmailModel : EmailTemplateModel>(
    private val emailDeliveryService: EmailDeliveryService,
    private val emailQueueService: EmailQueueService,
    private val featureFlagManager: FeatureFlagManager,
) {
    fun sendEmail(
        recipientAddress: String,
        email: EmailModel,
    ) {
        if (featureFlagManager.checkFeature(ASYNCHRONOUS_EMAIL_SENDING)) {
            emailQueueService.enqueue(recipientAddress, email)
        } else {
            emailDeliveryService.deliverEmail(recipientAddress, email)
        }
    }

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
