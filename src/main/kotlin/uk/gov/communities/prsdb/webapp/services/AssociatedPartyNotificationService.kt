package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.AssociatedPartyNotificationOutcome
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord
import uk.gov.communities.prsdb.webapp.models.dataModels.LetterAddressDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplateModel

@PrsdbWebService
class AssociatedPartyNotificationService(
    private val emailNotificationService: EmailNotificationService<EmailTemplateModel>,
    private val letterNotificationService: LetterNotificationService<LetterTemplateModel>,
) {
    // TODO PDJB-1987, PDJB-1988: implement once the freeholder creation email and letter templates are available
    fun sendCreationNotificationForFreeholder(freeholder: Freeholder): AssociatedPartyNotificationOutcome =
        TODO("Implement freeholder creation notification when its email and letter templates are available")

    // TODO PDJB-1989, PDJB-1990: implement once the superior landlord creation email and letter templates are available
    fun sendCreationNotificationForSuperiorLandlord(superiorLandlord: SuperiorLandlord): AssociatedPartyNotificationOutcome =
        TODO("Implement superior landlord creation notification when its email and letter templates are available")

    internal fun sendNotificationToFreeholder(
        freeholder: Freeholder,
        email: EmailTemplateModel,
        letter: LetterTemplateModel,
    ): AssociatedPartyNotificationOutcome = sendNotification(freeholder.name, freeholder.email, freeholder.address, email, letter)

    internal fun sendNotificationToSuperiorLandlord(
        superiorLandlord: SuperiorLandlord,
        email: EmailTemplateModel,
        letter: LetterTemplateModel,
    ): AssociatedPartyNotificationOutcome =
        sendNotification(superiorLandlord.name, superiorLandlord.email, superiorLandlord.address, email, letter)

    private fun sendNotification(
        name: String?,
        emailAddress: String?,
        postalAddress: Address?,
        email: EmailTemplateModel,
        letter: LetterTemplateModel,
    ): AssociatedPartyNotificationOutcome {
        if (emailAddress.isNullOrBlank() && postalAddress == null) {
            return AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED
        }

        if (!emailAddress.isNullOrBlank()) {
            emailNotificationService.sendEmail(emailAddress, email)
            return AssociatedPartyNotificationOutcome.EMAIL_SENT
        }

        val recipientName =
            checkNotNull(name?.takeIf { it.isNotBlank() }) {
                "An associated party must have a name to receive a letter"
            }
        val recipientAddress =
            checkNotNull(postalAddress) { "An associated party that is not fully redacted and has no email must have an address" }
        letterNotificationService.sendLetter(letterAddress(recipientName, recipientAddress), letter)
        return AssociatedPartyNotificationOutcome.LETTER_SENT
    }

    private fun letterAddress(
        name: String?,
        address: Address,
    ): LetterAddressDataModel {
        val addressLines = listOfNotNull(name) + address.toMultiLineAddress().lines()

        // Notify accepts at most seven lines; condense the middle while keeping the final postcode line separate.
        if (addressLines.size <= LetterAddressDataModel.MAX_ADDRESS_LINES) {
            return LetterAddressDataModel(addressLines)
        }

        val linesToCondense = addressLines.drop(LetterAddressDataModel.MAX_ADDRESS_LINES - 2)
        val condensedAddressLines =
            addressLines.take(LetterAddressDataModel.MAX_ADDRESS_LINES - 2) +
                linesToCondense.dropLast(1).joinToString(", ") +
                linesToCondense.last()
        return LetterAddressDataModel(condensedAddressLines)
    }
}
