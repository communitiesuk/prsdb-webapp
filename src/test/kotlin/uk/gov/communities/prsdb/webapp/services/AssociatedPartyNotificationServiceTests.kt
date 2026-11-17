package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.constants.enums.AssociatedPartyNotificationOutcome
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LetterAddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.ExampleLetter
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplateModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyManager

class AssociatedPartyNotificationServiceTests {
    private class TestEmail : EmailTemplateModel {
        override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_ACCEPTED_EMAIL

        override fun toHashMap(): HashMap<String, String> = hashMapOf()
    }

    private val emailNotificationService: EmailNotificationService<EmailTemplateModel> = mock()
    private val letterNotificationService: LetterNotificationService<LetterTemplateModel> = mock()
    private val associatedPartyNotificationService =
        AssociatedPartyNotificationService(emailNotificationService, letterNotificationService)

    private val email = TestEmail()
    private val letter = ExampleLetter("Sarah")
    private val postalAddress = AddressDataModel(singleLineAddress = "Flat 2, 18 Market Street, Bristol, BS1 4DJ")

    private fun createFreeholderModel(
        name: String? = null,
        email: String? = null,
        address: AddressDataModel? = null,
    ) = FreeholderDataModel(detailsKnown = name != null, name = name, email = email, address = address)

    private fun createSuperiorLandlordModel(
        name: String? = null,
        email: String? = null,
        address: AddressDataModel? = null,
    ) = SuperiorLandlordDataModel(detailsKnown = name != null, name = name, email = email, address = address)

    @Test
    fun `creation notification for a freeholder fails explicitly until implemented`() {
        val freeholder = createFreeholderModel()

        assertThrows(NotImplementedError::class.java) {
            associatedPartyNotificationService.sendCreationNotificationForFreeholder(freeholder)
        }

        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `creation notification for a superior landlord fails explicitly until implemented`() {
        val superiorLandlord = createSuperiorLandlordModel()

        assertThrows(NotImplementedError::class.java) {
            associatedPartyNotificationService.sendCreationNotificationForSuperiorLandlord(superiorLandlord)
        }

        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `creation notification for a property manager fails explicitly until implemented`() {
        val propertyManager = createPropertyManager()

        assertThrows(NotImplementedError::class.java) {
            associatedPartyNotificationService.sendCreationNotificationForPropertyManager(propertyManager)
        }

        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    private fun sendNotificationToFreeholder(freeholder: FreeholderDataModel): AssociatedPartyNotificationOutcome =
        associatedPartyNotificationService.sendNotificationToFreeholder(freeholder, email, letter)

    private fun sendNotificationToSuperiorLandlord(superiorLandlord: SuperiorLandlordDataModel): AssociatedPartyNotificationOutcome =
        associatedPartyNotificationService.sendNotificationToSuperiorLandlord(superiorLandlord, email, letter)

    @Test
    fun `sendNotification sends nothing to a fully redacted associated party`() {
        val freeholder = createFreeholderModel()

        val outcome = sendNotificationToFreeholder(freeholder)

        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotificationToSuperiorLandlord sends nothing when contact details are redacted`() {
        val superiorLandlord = createSuperiorLandlordModel()

        val outcome = sendNotificationToSuperiorLandlord(superiorLandlord)

        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotification emails a non-redacted associated party when their email address is known`() {
        val freeholder = createFreeholderModel(name = "Sarah Thompson", email = "sarah.thompson@example.com")

        val outcome = sendNotificationToFreeholder(freeholder)

        assertEquals(AssociatedPartyNotificationOutcome.EMAIL_SENT, outcome)
        verify(emailNotificationService).sendEmail("sarah.thompson@example.com", email)
        verifyNoInteractions(letterNotificationService)
    }

    @Test
    fun `sendNotification does not email a freeholder with an email address but no name`() {
        val freeholder = createFreeholderModel(email = "sarah.thompson@example.com")

        val outcome = sendNotificationToFreeholder(freeholder)

        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotification does not email a superior landlord with an email address but a blank name`() {
        val superiorLandlord = createSuperiorLandlordModel(name = " ", email = "daniel.roberts@example.com")

        val outcome = sendNotificationToSuperiorLandlord(superiorLandlord)

        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotificationToSuperiorLandlord prefers email over letter when both contact methods are available`() {
        val superiorLandlord =
            createSuperiorLandlordModel(
                name = "Daniel Roberts",
                email = "daniel.roberts@example.com",
                address = postalAddress,
            )

        val outcome = sendNotificationToSuperiorLandlord(superiorLandlord)

        assertEquals(AssociatedPartyNotificationOutcome.EMAIL_SENT, outcome)
        verify(emailNotificationService).sendEmail("daniel.roberts@example.com", email)
        verifyNoInteractions(letterNotificationService)
    }

    @Test
    fun `sendNotification posts a letter addressed by name to an associated party without an email address`() {
        val superiorLandlord = createSuperiorLandlordModel(name = "Daniel Roberts", address = postalAddress)

        val outcome = sendNotificationToSuperiorLandlord(superiorLandlord)

        assertEquals(AssociatedPartyNotificationOutcome.LETTER_SENT, outcome)
        verify(letterNotificationService).sendLetter(
            LetterAddressDataModel(listOf("Daniel Roberts", "Flat 2", "18 Market Street", "Bristol", "BS1 4DJ")),
            letter,
        )
        verifyNoInteractions(emailNotificationService)
    }

    @Test
    fun `sendNotification posts a letter to an associated party with a blank email address`() {
        val freeholder = createFreeholderModel(name = "Sarah Thompson", email = " ", address = postalAddress)

        val outcome = sendNotificationToFreeholder(freeholder)

        assertEquals(AssociatedPartyNotificationOutcome.LETTER_SENT, outcome)
        verify(letterNotificationService).sendLetter(
            LetterAddressDataModel(listOf("Sarah Thompson", "Flat 2", "18 Market Street", "Bristol", "BS1 4DJ")),
            letter,
        )
        verifyNoInteractions(emailNotificationService)
    }

    @Test
    fun `sendNotification does not try to notify by post when the associated party's name is missing`() {
        val freeholder = createFreeholderModel(address = postalAddress)

        val outcome = sendNotificationToFreeholder(freeholder)

        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotification does not try to notify by post when the associated party's name is blank`() {
        val freeholder = createFreeholderModel(name = " ", address = postalAddress)

        val outcome = sendNotificationToFreeholder(freeholder)

        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotification throws when a named associated party has no contact method`() {
        val freeholder = createFreeholderModel(name = "Sarah Thompson")

        assertThrows(PrsdbWebException::class.java) { sendNotificationToFreeholder(freeholder) }
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotification condenses a long address while keeping the postcode last`() {
        val fullAddress =
            AddressDataModel(
                singleLineAddress =
                    "Example Organisation, Flat 2, 12, Rose Court, High Street, Example Locality, London, SW1A 1AA",
                organisation = "Example Organisation",
                subBuilding = "Flat 2",
                buildingNumber = "12",
                buildingName = "Rose Court",
                streetName = "High Street",
                locality = "Example Locality",
                townName = "London",
                postcode = "SW1A 1AA",
            )
        val freeholder = createFreeholderModel(name = "Daniel Roberts", address = fullAddress)

        sendNotificationToFreeholder(freeholder)

        verify(letterNotificationService).sendLetter(
            LetterAddressDataModel(
                listOf(
                    "Daniel Roberts",
                    "Example Organisation",
                    "Flat 2",
                    "12",
                    "Rose Court",
                    "High Street, Example Locality, London",
                    "SW1A 1AA",
                ),
            ),
            letter,
        )
    }

    @Test
    fun `sendNotification condenses a long manually entered address while keeping the postcode last`() {
        val manualAddress =
            AddressDataModel(
                singleLineAddress =
                    "Example Organisation, Flat 2, 12, Rose Court, High Street, Example Locality, London, SW1A 1AA",
            )
        val freeholder = createFreeholderModel(name = "Daniel Roberts", address = manualAddress)

        sendNotificationToFreeholder(freeholder)

        verify(letterNotificationService).sendLetter(
            LetterAddressDataModel(
                listOf(
                    "Daniel Roberts",
                    "Example Organisation",
                    "Flat 2",
                    "12",
                    "Rose Court",
                    "High Street, Example Locality, London",
                    "SW1A 1AA",
                ),
            ),
            letter,
        )
    }
}
