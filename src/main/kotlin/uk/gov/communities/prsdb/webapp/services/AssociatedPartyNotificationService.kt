package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.AssociatedPartyNotificationOutcome
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.helpers.extensions.LandlordExtensions.Companion.getContactEmail
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LetterAddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyManagerDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.FreeholderAddedToPropertyEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyManagerAddedToPropertyEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.FreeholderAddedToPropertyLetter
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.SuperiorLandlordAddedToPropertyLetter

@PrsdbWebService
class AssociatedPartyNotificationService(
    private val emailNotificationService: EmailNotificationService<EmailTemplateModel>,
    private val letterNotificationService: LetterNotificationService<LetterTemplateModel>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    @Value("\${gds.start-page-url}") private val gdsStartPageUrl: String,
) {
    fun sendNotificationToFreeholderAddedToProperty(
        freeholder: FreeholderDataModel,
        propertyOwnership: PropertyOwnership,
    ): AssociatedPartyNotificationOutcome =
        // TODO PDJB-1989: Remove this check once the superior landlord email is implemented
        // and the shared helper returns a non-nullable outcome.
        checkNotNull(
            sendNotificationToAssociatedParty(
                name = freeholder.name,
                emailAddress = freeholder.email,
                postalAddress = freeholder.address,
                buildEmail = { buildFreeholderAddedToPropertyEmail(freeholder, propertyOwnership) },
                buildLetter = { buildFreeholderAddedToPropertyLetter(freeholder, propertyOwnership) },
            ),
        )

    internal fun buildFreeholderAddedToPropertyLetter(
        freeholder: FreeholderDataModel,
        propertyOwnership: PropertyOwnership,
    ): FreeholderAddedToPropertyLetter =
        FreeholderAddedToPropertyLetter(
            freeholderName = freeholder.name.orEmpty(),
            propertyAddress = propertyOwnership.address.toMultiLineAddress(),
            landlords = landlordsRentingOutProperty(propertyOwnership),
            freeholderAddress = freeholder.address?.toMultiLineAddress().orEmpty(),
            privacyNoticeUrl = absoluteUrlProvider.buildLandlordPrivacyNoticeLetterDisplayUrl(),
        )

    private fun buildFreeholderAddedToPropertyEmail(
        freeholder: FreeholderDataModel,
        propertyOwnership: PropertyOwnership,
    ): FreeholderAddedToPropertyEmail =
        FreeholderAddedToPropertyEmail(
            freeholderName = freeholder.name.orEmpty(),
            freeholderEmail = freeholder.email.orEmpty(),
            propertyAddress = propertyOwnership.address.toMultiLineAddress(),
            landlords = landlordsRentingOutProperty(propertyOwnership),
            gdsStartPageUrl = gdsStartPageUrl,
            privacyNoticeUrl = absoluteUrlProvider.buildLandlordPrivacyNoticeUri().toString(),
        )

    // TODO PDJB-1989: make the email template and return type non-nullable once the superior landlord added email is sent.
    fun sendNotificationToSuperiorLandlordAddedToProperty(
        superiorLandlord: SuperiorLandlordDataModel,
        propertyOwnership: PropertyOwnership,
    ): AssociatedPartyNotificationOutcome? =
        sendNotificationToAssociatedParty(
            name = superiorLandlord.name,
            emailAddress = superiorLandlord.email,
            postalAddress = superiorLandlord.address,
            buildEmail = { null },
            buildLetter = { buildSuperiorLandlordAddedToPropertyLetter(superiorLandlord, propertyOwnership) },
        )

    internal fun buildSuperiorLandlordAddedToPropertyLetter(
        superiorLandlord: SuperiorLandlordDataModel,
        propertyOwnership: PropertyOwnership,
    ): SuperiorLandlordAddedToPropertyLetter =
        SuperiorLandlordAddedToPropertyLetter(
            superiorLandlordName = superiorLandlord.name.orEmpty(),
            propertyAddress = propertyOwnership.address.toMultiLineAddress(),
            landlords = landlordsRentingOutProperty(propertyOwnership),
            superiorLandlordAddress = superiorLandlord.address?.toMultiLineAddress().orEmpty(),
            privacyNoticeUrl = absoluteUrlProvider.buildLandlordPrivacyNoticeLetterDisplayUrl(),
        )

    private fun landlordsRentingOutProperty(propertyOwnership: PropertyOwnership): List<String> =
        propertyOwnership.landlords.sortedBy { it.name }.map { landlord ->
            "${landlord.name} (${landlord.getContactEmail()})"
        }

    fun sendNotificationToPropertyManagerAddedToProperty(
        propertyManager: PropertyManagerDataModel,
        propertyOwnership: PropertyOwnership,
    ) {
        emailNotificationService.sendEmail(
            propertyManager.email,
            buildPropertyManagerAddedToPropertyEmail(propertyManager, propertyOwnership),
        )
    }

    internal fun buildPropertyManagerAddedToPropertyEmail(
        propertyManager: PropertyManagerDataModel,
        propertyOwnership: PropertyOwnership,
    ): PropertyManagerAddedToPropertyEmail =
        PropertyManagerAddedToPropertyEmail(
            propertyManagerName = propertyManager.name,
            propertyManagerEmail = propertyManager.email,
            propertyAddress = propertyOwnership.address.toMultiLineAddress(),
            landlords = landlordsRentingOutProperty(propertyOwnership),
            gdsStartPageUrl = gdsStartPageUrl,
            privacyNoticeUrl = absoluteUrlProvider.buildLandlordPrivacyNoticeUri().toString(),
        )

    internal fun sendNotificationToAssociatedParty(
        name: String?,
        emailAddress: String?,
        postalAddress: AddressDataModel?,
        buildEmail: () -> EmailTemplateModel?,
        buildLetter: () -> LetterTemplateModel,
    ): AssociatedPartyNotificationOutcome? {
        if (name.isNullOrBlank()) {
            return AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED
        }

        if (!emailAddress.isNullOrBlank() && postalAddress != null) {
            throw PrsdbWebException("Associated party has both an email address and a postal address")
        }

        if (!emailAddress.isNullOrBlank()) {
            val emailTemplate = buildEmail()
            if (emailTemplate == null) {
                // TODO PDJB-1989: build and send the superior landlord added email.
                return null
            }
            emailNotificationService.sendEmail(emailAddress, emailTemplate)
            return AssociatedPartyNotificationOutcome.EMAIL_SENT
        }

        if (postalAddress != null) {
            letterNotificationService.sendLetter(letterAddress(name, postalAddress), buildLetter())
            return AssociatedPartyNotificationOutcome.LETTER_SENT
        }

        throw PrsdbWebException("Associated party has a name but no email address or postal address")
    }

    private fun letterAddress(
        name: String,
        address: AddressDataModel,
    ): LetterAddressDataModel {
        val formattedAddressLines = address.toMultiLineAddress().lines()
        val addressLines =
            listOf(name) +
                formattedAddressLines +
                address.country
                    .takeIf { address.isInternational && it.isNotBlank() && formattedAddressLines.lastOrNull() != it }
                    ?.let(::listOf)
                    .orEmpty()

        // Notify accepts at most seven lines; keep the postcode and international country on separate lines.
        if (addressLines.size <= LetterAddressDataModel.MAX_ADDRESS_LINES) {
            return LetterAddressDataModel(addressLines)
        }

        val trailingLinesToPreserve = if (address.isInternational) 2 else 1
        val leadingLinesToPreserve = LetterAddressDataModel.MAX_ADDRESS_LINES - trailingLinesToPreserve - 1
        val linesToCondense =
            addressLines
                .drop(leadingLinesToPreserve)
                .dropLast(trailingLinesToPreserve)
        val condensedAddressLines =
            addressLines.take(leadingLinesToPreserve) +
                linesToCondense.joinToString(", ") +
                addressLines.takeLast(trailingLinesToPreserve)
        return LetterAddressDataModel(condensedAddressLines)
    }
}
