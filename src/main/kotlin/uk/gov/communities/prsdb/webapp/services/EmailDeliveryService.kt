package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel

interface EmailDeliveryService {
    fun deliverEmail(
        recipientAddress: String,
        email: EmailTemplateModel,
    )
}
