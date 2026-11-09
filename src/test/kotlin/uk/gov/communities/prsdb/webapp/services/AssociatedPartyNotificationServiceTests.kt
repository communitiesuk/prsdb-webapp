package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.constants.enums.AssociatedPartyNotificationOutcome
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord
import uk.gov.communities.prsdb.webapp.models.dataModels.LetterAddressDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.ExampleLetter
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplateModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership

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
    private val postalAddress = createAddress(singleLineAddress = "Flat 2, 18 Market Street, Bristol, BS1 4DJ")

    @Test
    fun `sendNotification sends nothing to a fully redacted associated party`() {
        val freeholder = Freeholder(createPropertyOwnership())

        val outcome = associatedPartyNotificationService.sendNotification(freeholder, email, letter)

        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotification emails an associated party with an email address even if they have a postal address`() {
        val freeholder =
            Freeholder(
                createPropertyOwnership(),
                name = "Sarah Thompson",
                email = "sarah.thompson@example.com",
                address = postalAddress,
            )

        val outcome = associatedPartyNotificationService.sendNotification(freeholder, email, letter)

        assertEquals(AssociatedPartyNotificationOutcome.EMAIL_SENT, outcome)
        verify(emailNotificationService).sendEmail("sarah.thompson@example.com", email)
        verifyNoInteractions(letterNotificationService)
    }

    @Test
    fun `sendNotification posts a letter addressed by name to an associated party without an email address`() {
        val superiorLandlord =
            SuperiorLandlord(createPropertyOwnership(), name = "Daniel Roberts", address = postalAddress)

        val outcome = associatedPartyNotificationService.sendNotification(superiorLandlord, email, letter)

        assertEquals(AssociatedPartyNotificationOutcome.LETTER_SENT, outcome)
        verify(letterNotificationService).sendLetter(
            LetterAddressDataModel(listOf("Daniel Roberts", "Flat 2", "18 Market Street", "Bristol", "BS1 4DJ")),
            letter,
        )
        verifyNoInteractions(emailNotificationService)
    }

    @Test
    fun `sendNotification posts a letter to an associated party with a blank email address`() {
        val freeholder =
            Freeholder(createPropertyOwnership(), name = "Sarah Thompson", email = " ", address = postalAddress)

        val outcome = associatedPartyNotificationService.sendNotification(freeholder, email, letter)

        assertEquals(AssociatedPartyNotificationOutcome.LETTER_SENT, outcome)
        verify(letterNotificationService).sendLetter(
            LetterAddressDataModel(listOf("Sarah Thompson", "Flat 2", "18 Market Street", "Bristol", "BS1 4DJ")),
            letter,
        )
        verifyNoInteractions(emailNotificationService)
    }

    @Test
    fun `sendNotification posts a letter to just the postal address when the associated party's name is unknown`() {
        val freeholder = Freeholder(createPropertyOwnership(), address = postalAddress)

        val outcome = associatedPartyNotificationService.sendNotification(freeholder, email, letter)

        assertEquals(AssociatedPartyNotificationOutcome.LETTER_SENT, outcome)
        verify(letterNotificationService).sendLetter(
            LetterAddressDataModel(listOf("Flat 2", "18 Market Street", "Bristol", "BS1 4DJ")),
            letter,
        )
    }
}
