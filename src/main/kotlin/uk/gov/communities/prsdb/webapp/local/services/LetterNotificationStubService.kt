package uk.gov.communities.prsdb.webapp.local.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.exceptions.PersistentLetterSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientLetterSendException
import uk.gov.communities.prsdb.webapp.models.dataModels.LetterAddressDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplateModel
import uk.gov.communities.prsdb.webapp.services.LetterNotificationService

@Service
// The one-login-simulator profile is only active on NFT during performance runs, so no real letters are sent
@Profile("(local & !use-notify) | one-login-simulator")
@Primary
class LetterNotificationStubService<LetterModel : LetterTemplateModel> : LetterNotificationService<LetterModel> {
    @Value("\${local.letters.transientExceptionPostcode:}")
    var transientExceptionPostcode: String? = null

    @Value("\${local.letters.persistentExceptionPostcode:}")
    var persistentExceptionPostcode: String? = null

    override fun sendLetter(
        recipientAddress: LetterAddressDataModel,
        letter: LetterModel,
    ) {
        val postcode = recipientAddress.lines.last()
        if (postcode == persistentExceptionPostcode) {
            throw PersistentLetterSendException("Letters sent to $persistentExceptionPostcode always persistently fail")
        }
        if (postcode == transientExceptionPostcode) {
            throw TransientLetterSendException("Letters sent to $transientExceptionPostcode always transiently fail")
        }

        val consoleString =
            """
            ****************
            STUB LETTER SENT
            ****************
            
            Letter sent to:  ${recipientAddress.lines.joinToString(", ")}
            Template used:   ${letter.template.name}
            Personalisation: ${letter.toHashMap()}
            """.trimIndent()

        println(consoleString)
    }
}
