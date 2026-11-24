package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.AssociatedPartyNotificationOutcome
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LetterAddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyManagerDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.FreeholderAddedToPropertyEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyManagerAddedToPropertyEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.FreeholderAddedToPropertyLetter
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.SuperiorLandlordAddedToPropertyLetter
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import java.net.URI

@ExtendWith(MockitoExtension::class)
class AssociatedPartyNotificationServiceTests {
    private class TestEmail : EmailTemplateModel {
        override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_ACCEPTED_EMAIL

        override fun toHashMap(): HashMap<String, String> = hashMapOf()
    }

    private class TestLetter : LetterTemplateModel {
        override val template = LetterTemplate.FREEHOLDER_ADDED_TO_PROPERTY_LETTER

        override fun toHashMap(): HashMap<String, String> = hashMapOf()
    }

    @Mock
    private lateinit var emailNotificationService: EmailNotificationService<EmailTemplateModel>

    @Mock
    private lateinit var letterNotificationService: LetterNotificationService<LetterTemplateModel>

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    private lateinit var associatedPartyNotificationService: AssociatedPartyNotificationService

    @BeforeEach
    fun setUp() {
        associatedPartyNotificationService =
            AssociatedPartyNotificationService(
                emailNotificationService,
                letterNotificationService,
                absoluteUrlProvider,
                GDS_START_PAGE_URL,
            )
    }

    private val email = TestEmail()
    private val letter = TestLetter()
    private val expectedFreeholderAddedToPropertyLetter =
        FreeholderAddedToPropertyLetter(
            freeholderName = "Sarah Thompson",
            propertyAddress = "12 Real Street\nBristol\nBS8 1AA\nEngland",
            landlords = listOf("Jamie Jameson (jamie@example.com)"),
            freeholderAddress = "Flat 2\n18 Market Street\nBristol\nBS1 4DJ\nEngland",
            privacyNoticeUrl = "example.test/landlord/privacy-notice",
        )
    private val expectedSuperiorLandlordAddedToPropertyLetter =
        SuperiorLandlordAddedToPropertyLetter(
            superiorLandlordName = "Daniel Roberts",
            propertyAddress = "12 Real Street\nBristol\nBS8 1AA\nEngland",
            landlords = listOf("Jamie Jameson (jamie@example.com)"),
            superiorLandlordAddress = "Flat 2\n18 Market Street\nBristol\nBS1 4DJ\nEngland",
            privacyNoticeUrl = "example.test/landlord/privacy-notice",
        )
    private val postalAddress = AddressDataModel(singleLineAddress = "Flat 2, 18 Market Street, Bristol, BS1 4DJ")
    private val propertyManager = PropertyManagerDataModel(name = "James Smith", email = "james.smith@example.com")
    private val expectedPropertyManagerAddedToPropertyEmail =
        PropertyManagerAddedToPropertyEmail(
            propertyManagerName = "James Smith",
            propertyManagerEmail = "james.smith@example.com",
            propertyAddress = "12 Real Street\nBristol\nBS8 1AA",
            landlords = listOf("Jamie Jameson (jamie@example.com)"),
            gdsStartPageUrl = GDS_START_PAGE_URL,
            privacyNoticeUrl = "https://example.test/landlord/privacy-notice",
        )

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
    fun `buildFreeholderAddedToPropertyLetter includes property and freeholder details with landlords sorted by name`() {
        // Arrange
        val propertyOwnership =
            createPropertyOwnership(
                address = createAddress(singleLineAddress = "12 Real Street, Bristol, BS8 1AA"),
                landlords =
                    mutableSetOf(
                        createOrgLandlord(name = "Zebra Homes", email = "contact@zebra.example", registrantEmail = "admin@zebra.example"),
                        createIndividualLandlord(name = "Jamie Jameson", email = "jamie@example.com"),
                    ),
            )
        val freeholder = createFreeholderModel(name = "Sarah Thompson", address = postalAddress)
        whenever(absoluteUrlProvider.buildLandlordPrivacyNoticeLetterDisplayUrl())
            .thenReturn("example.test/landlord/privacy-notice")

        // Act
        val result = associatedPartyNotificationService.buildFreeholderAddedToPropertyLetter(freeholder, propertyOwnership)

        // Assert
        assertEquals(
            expectedFreeholderAddedToPropertyLetter.copy(
                landlords = listOf("Jamie Jameson (jamie@example.com)", "Zebra Homes (contact@zebra.example)"),
            ),
            result,
        )
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `freeholder added to property notification is sent as a letter built from journey details when the address is known`() {
        // Arrange
        val freeholder = createFreeholderModel(name = "Sarah Thompson", address = postalAddress)
        val propertyOwnership =
            createPropertyOwnership(
                address = createAddress(singleLineAddress = "12 Real Street, Bristol, BS8 1AA"),
                landlords = mutableSetOf(createIndividualLandlord(name = "Jamie Jameson", email = "jamie@example.com")),
            )
        val expectedLetter =
            expectedFreeholderAddedToPropertyLetter.copy(
                landlords = listOf("Jamie Jameson (jamie@example.com)"),
            )
        whenever(absoluteUrlProvider.buildLandlordPrivacyNoticeLetterDisplayUrl())
            .thenReturn("example.test/landlord/privacy-notice")

        // Act
        val outcome =
            associatedPartyNotificationService.sendNotificationToFreeholderAddedToProperty(
                freeholder,
                propertyOwnership,
            )

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.LETTER_SENT, outcome)
        verify(letterNotificationService).sendLetter(
            LetterAddressDataModel(listOf("Sarah Thompson", "Flat 2", "18 Market Street", "Bristol", "BS1 4DJ", "England")),
            expectedLetter,
        )
        verifyNoInteractions(emailNotificationService)
    }

    @Test
    fun `freeholder with no name does not receive a notification`() {
        // Arrange
        val freeholder = createFreeholderModel(address = postalAddress)

        // Act
        val outcome =
            associatedPartyNotificationService.sendNotificationToFreeholderAddedToProperty(
                freeholder,
                createPropertyOwnership(),
            )

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `freeholder with an email receives an added-to-property email with a full privacy notice link`() {
        // Arrange
        val freeholder = createFreeholderModel(name = "Sarah Thompson", email = "sarah@example.com")
        val propertyOwnership =
            createPropertyOwnership(
                address = createAddress(singleLineAddress = "12 Real Street, Bristol, BS8 1AA"),
                landlords = mutableSetOf(createIndividualLandlord(name = "Jamie Jameson", email = "jamie@example.com")),
            )
        val expectedEmail =
            FreeholderAddedToPropertyEmail(
                freeholderName = "Sarah Thompson",
                freeholderEmail = "sarah@example.com",
                propertyAddress = "12 Real Street\nBristol\nBS8 1AA",
                landlords = listOf("Jamie Jameson (jamie@example.com)"),
                gdsStartPageUrl = GDS_START_PAGE_URL,
                privacyNoticeUrl = "https://www.example.test/landlord/privacy-notice",
            )
        whenever(absoluteUrlProvider.buildLandlordPrivacyNoticeUri())
            .thenReturn(URI("https://www.example.test/landlord/privacy-notice"))

        // Act
        val outcome =
            associatedPartyNotificationService.sendNotificationToFreeholderAddedToProperty(
                freeholder,
                propertyOwnership,
            )

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.EMAIL_SENT, outcome)
        verify(emailNotificationService).sendEmail("sarah@example.com", expectedEmail)
        verifyNoInteractions(letterNotificationService)
    }

    @Test
    fun `freeholder with both contact methods throws without sending a notification`() {
        // Arrange
        val freeholder =
            createFreeholderModel(
                name = "Sarah Thompson",
                email = "sarah@example.com",
                address = postalAddress,
            )
        // Act & Assert
        assertThrows(PrsdbWebException::class.java) {
            associatedPartyNotificationService.sendNotificationToFreeholderAddedToProperty(
                freeholder,
                createPropertyOwnership(),
            )
        }
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `freeholder added to property notification throws when a named freeholder has no contact method`() {
        // Arrange
        val freeholder = createFreeholderModel(name = "Sarah Thompson")

        // Act & Assert
        assertThrows(PrsdbWebException::class.java) {
            associatedPartyNotificationService.sendNotificationToFreeholderAddedToProperty(
                freeholder,
                createPropertyOwnership(),
            )
        }
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `buildSuperiorLandlordAddedToPropertyLetter includes property and superior landlord details with landlords sorted by name`() {
        // Arrange
        val propertyOwnership =
            createPropertyOwnership(
                address = createAddress(singleLineAddress = "12 Real Street, Bristol, BS8 1AA"),
                landlords =
                    mutableSetOf(
                        createOrgLandlord(name = "Zebra Homes", email = "contact@zebra.example", registrantEmail = "admin@zebra.example"),
                        createIndividualLandlord(name = "Jamie Jameson", email = "jamie@example.com"),
                    ),
            )
        val superiorLandlord = createSuperiorLandlordModel(name = "Daniel Roberts", address = postalAddress)
        whenever(absoluteUrlProvider.buildLandlordPrivacyNoticeLetterDisplayUrl())
            .thenReturn("example.test/landlord/privacy-notice")

        // Act
        val result = associatedPartyNotificationService.buildSuperiorLandlordAddedToPropertyLetter(superiorLandlord, propertyOwnership)

        // Assert
        assertEquals(
            expectedSuperiorLandlordAddedToPropertyLetter.copy(
                landlords = listOf("Jamie Jameson (jamie@example.com)", "Zebra Homes (contact@zebra.example)"),
            ),
            result,
        )
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `superior landlord added to property notification is sent as a letter built from journey details when the address is known`() {
        // Arrange
        val superiorLandlord = createSuperiorLandlordModel(name = "Daniel Roberts", address = postalAddress)
        val propertyOwnership =
            createPropertyOwnership(
                address = createAddress(singleLineAddress = "12 Real Street, Bristol, BS8 1AA"),
                landlords = mutableSetOf(createIndividualLandlord(name = "Jamie Jameson", email = "jamie@example.com")),
            )
        whenever(absoluteUrlProvider.buildLandlordPrivacyNoticeLetterDisplayUrl())
            .thenReturn("example.test/landlord/privacy-notice")

        // Act
        val outcome =
            associatedPartyNotificationService.sendNotificationToSuperiorLandlordAddedToProperty(
                superiorLandlord,
                propertyOwnership,
            )

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.LETTER_SENT, outcome)
        verify(letterNotificationService).sendLetter(
            LetterAddressDataModel(listOf("Daniel Roberts", "Flat 2", "18 Market Street", "Bristol", "BS1 4DJ", "England")),
            expectedSuperiorLandlordAddedToPropertyLetter,
        )
        verifyNoInteractions(emailNotificationService)
    }

    @Test
    fun `superior landlord with no name does not receive a notification`() {
        // Arrange
        val superiorLandlord = createSuperiorLandlordModel(address = postalAddress)

        // Act
        val outcome =
            associatedPartyNotificationService.sendNotificationToSuperiorLandlordAddedToProperty(
                superiorLandlord,
                createPropertyOwnership(),
            )

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    // TODO PDJB-1989: Update this test to expect an email once superior-landlord email support is implemented.
    @Test
    fun `superior landlord added to property notification sends nothing yet to a superior landlord with only an email address`() {
        // Arrange
        val superiorLandlord = createSuperiorLandlordModel(name = "Daniel Roberts", email = "daniel.roberts@example.com")

        // Act
        val outcome =
            associatedPartyNotificationService.sendNotificationToSuperiorLandlordAddedToProperty(
                superiorLandlord,
                createPropertyOwnership(),
            )

        // Assert
        assertNull(outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `superior landlord with both contact methods throws without sending a notification`() {
        // Arrange
        val superiorLandlord =
            createSuperiorLandlordModel(
                name = "Daniel Roberts",
                email = "daniel.roberts@example.com",
                address = postalAddress,
            )

        // Act & Assert
        assertThrows(PrsdbWebException::class.java) {
            associatedPartyNotificationService.sendNotificationToSuperiorLandlordAddedToProperty(
                superiorLandlord,
                createPropertyOwnership(),
            )
        }
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `superior landlord added to property notification throws when a named superior landlord has no contact method`() {
        // Arrange
        val superiorLandlord = createSuperiorLandlordModel(name = "Daniel Roberts")

        // Act & Assert
        assertThrows(PrsdbWebException::class.java) {
            associatedPartyNotificationService.sendNotificationToSuperiorLandlordAddedToProperty(
                superiorLandlord,
                createPropertyOwnership(),
            )
        }
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `buildPropertyManagerAddedToPropertyEmail includes property and property manager details with landlords sorted by name`() {
        // Arrange
        val propertyOwnership =
            createPropertyOwnership(
                address = createAddress(singleLineAddress = "12 Real Street, Bristol, BS8 1AA"),
                landlords =
                    mutableSetOf(
                        createOrgLandlord(name = "Zebra Homes", email = "contact@zebra.example", registrantEmail = "admin@zebra.example"),
                        createIndividualLandlord(name = "Jamie Jameson", email = "jamie@example.com"),
                    ),
            )
        whenever(absoluteUrlProvider.buildLandlordPrivacyNoticeUri()).thenReturn(URI("https://example.test/landlord/privacy-notice"))

        // Act
        val result = associatedPartyNotificationService.buildPropertyManagerAddedToPropertyEmail(propertyManager, propertyOwnership)

        // Assert
        assertEquals(
            expectedPropertyManagerAddedToPropertyEmail.copy(
                landlords = listOf("Jamie Jameson (jamie@example.com)", "Zebra Homes (contact@zebra.example)"),
            ),
            result,
        )
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `property manager added to property notification is emailed to the property manager`() {
        // Arrange
        val propertyOwnership =
            createPropertyOwnership(
                address = createAddress(singleLineAddress = "12 Real Street, Bristol, BS8 1AA"),
                landlords = mutableSetOf(createIndividualLandlord(name = "Jamie Jameson", email = "jamie@example.com")),
            )
        whenever(absoluteUrlProvider.buildLandlordPrivacyNoticeUri()).thenReturn(URI("https://example.test/landlord/privacy-notice"))

        // Act
        associatedPartyNotificationService.sendNotificationToPropertyManagerAddedToProperty(propertyManager, propertyOwnership)

        // Assert
        verify(emailNotificationService).sendEmail("james.smith@example.com", expectedPropertyManagerAddedToPropertyEmail)
        verifyNoInteractions(letterNotificationService)
    }

    private fun sendNotificationToFreeholder(freeholder: FreeholderDataModel): AssociatedPartyNotificationOutcome =
        checkNotNull(
            associatedPartyNotificationService.sendNotificationToAssociatedParty(
                name = freeholder.name,
                emailAddress = freeholder.email,
                postalAddress = freeholder.address,
                buildEmail = { email },
                buildLetter = { letter },
            ),
        )

    private fun sendNotificationToSuperiorLandlord(superiorLandlord: SuperiorLandlordDataModel): AssociatedPartyNotificationOutcome =
        checkNotNull(
            associatedPartyNotificationService.sendNotificationToAssociatedParty(
                name = superiorLandlord.name,
                emailAddress = superiorLandlord.email,
                postalAddress = superiorLandlord.address,
                buildEmail = { email },
                buildLetter = { letter },
            ),
        )

    @Test
    fun `sendNotification sends nothing to a fully redacted associated party`() {
        // Arrange
        val freeholder = createFreeholderModel()

        // Act
        val outcome = sendNotificationToFreeholder(freeholder)

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotificationToSuperiorLandlord sends nothing when contact details are redacted`() {
        // Arrange
        val superiorLandlord = createSuperiorLandlordModel()

        // Act
        val outcome = sendNotificationToSuperiorLandlord(superiorLandlord)

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotification emails a non-redacted associated party when their email address is known`() {
        // Arrange
        val freeholder = createFreeholderModel(name = "Sarah Thompson", email = "sarah.thompson@example.com")

        // Act
        val outcome = sendNotificationToFreeholder(freeholder)

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.EMAIL_SENT, outcome)
        verify(emailNotificationService).sendEmail("sarah.thompson@example.com", email)
        verifyNoInteractions(letterNotificationService)
    }

    @Test
    fun `sendNotification does not email a freeholder with an email address but no name`() {
        // Arrange
        val freeholder = createFreeholderModel(email = "sarah.thompson@example.com")

        // Act
        val outcome = sendNotificationToFreeholder(freeholder)

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotification does not email a superior landlord with an email address but a blank name`() {
        // Arrange
        val superiorLandlord = createSuperiorLandlordModel(name = " ", email = "daniel.roberts@example.com")

        // Act
        val outcome = sendNotificationToSuperiorLandlord(superiorLandlord)

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotificationToSuperiorLandlord throws when both contact methods are available`() {
        // Arrange
        val superiorLandlord =
            createSuperiorLandlordModel(
                name = "Daniel Roberts",
                email = "daniel.roberts@example.com",
                address = postalAddress,
            )

        // Act & Assert
        assertThrows(PrsdbWebException::class.java) { sendNotificationToSuperiorLandlord(superiorLandlord) }
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotification posts a letter addressed by name to an associated party without an email address`() {
        // Arrange
        val superiorLandlord = createSuperiorLandlordModel(name = "Daniel Roberts", address = postalAddress)

        // Act
        val outcome = sendNotificationToSuperiorLandlord(superiorLandlord)

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.LETTER_SENT, outcome)
        verify(letterNotificationService).sendLetter(
            LetterAddressDataModel(listOf("Daniel Roberts", "Flat 2", "18 Market Street", "Bristol", "BS1 4DJ", "England")),
            letter,
        )
        verifyNoInteractions(emailNotificationService)
    }

    @Test
    fun `sendNotification posts a letter to an associated party with a blank email address`() {
        // Arrange
        val freeholder = createFreeholderModel(name = "Sarah Thompson", email = " ", address = postalAddress)

        // Act
        val outcome = sendNotificationToFreeholder(freeholder)

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.LETTER_SENT, outcome)
        verify(letterNotificationService).sendLetter(
            LetterAddressDataModel(listOf("Sarah Thompson", "Flat 2", "18 Market Street", "Bristol", "BS1 4DJ", "England")),
            letter,
        )
        verifyNoInteractions(emailNotificationService)
    }

    @Test
    fun `sendNotification does not try to notify by post when the associated party's name is missing`() {
        // Arrange
        val freeholder = createFreeholderModel(address = postalAddress)

        // Act
        val outcome = sendNotificationToFreeholder(freeholder)

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotification does not try to notify by post when the associated party's name is blank`() {
        // Arrange
        val freeholder = createFreeholderModel(name = " ", address = postalAddress)

        // Act
        val outcome = sendNotificationToFreeholder(freeholder)

        // Assert
        assertEquals(AssociatedPartyNotificationOutcome.NOT_SENT_FULLY_REDACTED, outcome)
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotification throws when a named associated party has no contact method`() {
        // Arrange
        val freeholder = createFreeholderModel(name = "Sarah Thompson")

        // Act & Assert
        assertThrows(PrsdbWebException::class.java) { sendNotificationToFreeholder(freeholder) }
        verifyNoInteractions(emailNotificationService, letterNotificationService)
    }

    @Test
    fun `sendNotification condenses a long address while keeping the last line separate`() {
        // Arrange
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

        // Act
        sendNotificationToFreeholder(freeholder)

        // Assert
        verify(letterNotificationService).sendLetter(
            LetterAddressDataModel(
                listOf(
                    "Daniel Roberts",
                    "Example Organisation",
                    "Flat 2",
                    "12",
                    "Rose Court",
                    "High Street, Example Locality, London, SW1A 1AA",
                    "England",
                ),
            ),
            letter,
        )
    }

    @Test
    fun `sendNotification condenses a long manually entered address while keeping the last line separate`() {
        // Arrange
        val manualAddress =
            AddressDataModel(
                singleLineAddress =
                    "Example Organisation, Flat 2, 12, Rose Court, High Street, Example Locality, London, SW1A 1AA",
            )
        val freeholder = createFreeholderModel(name = "Daniel Roberts", address = manualAddress)

        // Act
        sendNotificationToFreeholder(freeholder)

        // Assert
        verify(letterNotificationService).sendLetter(
            LetterAddressDataModel(
                listOf(
                    "Daniel Roberts",
                    "Example Organisation",
                    "Flat 2",
                    "12",
                    "Rose Court",
                    "High Street, Example Locality, London, SW1A 1AA",
                    "England",
                ),
            ),
            letter,
        )
    }

    companion object {
        private const val GDS_START_PAGE_URL = "https://example.test/gds-start-page"
    }
}
