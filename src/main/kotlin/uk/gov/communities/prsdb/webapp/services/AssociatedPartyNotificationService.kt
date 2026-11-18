package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.AssociatedPartyNotificationOutcome
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LetterAddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyManagerDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplateModel

@PrsdbWebService
class AssociatedPartyNotificationService(
    private val emailNotificationService: EmailNotificationService<EmailTemplateModel>,
    private val letterNotificationService: LetterNotificationService<LetterTemplateModel>,
) {
    // TODO PDJB-1987, PDJB-1988: implement once the freeholder creation email and letter templates are available
    fun sendCreationNotificationForFreeholder(freeholder: FreeholderDataModel): AssociatedPartyNotificationOutcome =
        TODO("Implement freeholder creation notification when its email and letter templates are available")

    // TODO PDJB-1989, PDJB-1990: implement once the superior landlord creation email and letter templates are available
    fun sendCreationNotificationForSuperiorLandlord(superiorLandlord: SuperiorLandlordDataModel): AssociatedPartyNotificationOutcome =
        TODO("Implement superior landlord creation notification when its email and letter templates are available")

    // TODO PDJB-1991: implement once the property manager creation email template is available
    fun sendCreationNotificationForPropertyManager(propertyManager: PropertyManagerDataModel): AssociatedPartyNotificationOutcome =
        TODO("Implement property manager creation notification when its email template is available")

    internal fun sendNotificationToFreeholder(
        freeholder: FreeholderDataModel,
        email: EmailTemplateModel,
        letter: LetterTemplateModel,
    ): AssociatedPartyNotificationOutcome = sendNotification(freeholder.name, freeholder.email, freeholder.address, email, letter)

    internal fun sendNotificationToSuperiorLandlord(
        superiorLandlord: SuperiorLandlordDataModel,
        email: EmailTemplateModel,
        letter: LetterTemplateModel,
    ): AssociatedPartyNotificationOutcome =
        sendNotification(superiorLandlord.name, superiorLandlord.email, superiorLandlord.address, email, letter)

    private fun sendNotification(
        name: String?,
        emailAddress: String?,
        postalAddress: AddressDataModel?,
        email: EmailTemplateModel,
        letter: LetterTemplateModel,
    ): AssociatedPartyNotificationOutcome {
        if (name.isNullOrBlank()) {
            return AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED
        }

        if (!emailAddress.isNullOrBlank()) {
            emailNotificationService.sendEmail(emailAddress, email)
            return AssociatedPartyNotificationOutcome.EMAIL_SENT
        }

        if (postalAddress != null) {
            letterNotificationService.sendLetter(letterAddress(name, postalAddress), letter)
            return AssociatedPartyNotificationOutcome.LETTER_SENT
        }

        throw PrsdbWebException("Associated party has a name but no email address or postal address")
    }

    private fun letterAddress(
        name: String,
        address: AddressDataModel,
    ): LetterAddressDataModel {
        val addressLines = listOf(name) + address.toMultiLineAddress().lines()

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
