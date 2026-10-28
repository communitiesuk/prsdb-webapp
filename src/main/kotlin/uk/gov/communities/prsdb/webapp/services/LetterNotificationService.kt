package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.exceptions.PersistentLetterSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientLetterSendException
import uk.gov.communities.prsdb.webapp.models.dataModels.LetterAddressDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplateModel

interface LetterNotificationService<in LetterModel : LetterTemplateModel> {
    /**
     * Submits a letter; successful return does not confirm postal delivery.
     *
     * @throws TransientLetterSendException if the letter was not sent and retrying later may succeed.
     * @throws PersistentLetterSendException if retrying will not help, or it is unknown whether the letter was sent.
     */
    fun sendLetter(
        recipientAddress: LetterAddressDataModel,
        letter: LetterModel,
    )
}
