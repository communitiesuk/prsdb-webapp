package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordOtherLandlordLeftNotification
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI
import kotlin.test.assertEquals

class JointLandlordOtherLandlordLeftEmailServiceTests {
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider
    private lateinit var mockNotificationEmailService: EmailNotificationService<JointLandlordOtherLandlordLeftNotification>
    private lateinit var emailService: JointLandlordOtherLandlordLeftEmailServiceImplFlagOn

    @BeforeEach
    fun setup() {
        mockAbsoluteUrlProvider = mock()
        mockNotificationEmailService = mock()
        emailService =
            JointLandlordOtherLandlordLeftEmailServiceImplFlagOn(
                mockAbsoluteUrlProvider,
                mockNotificationEmailService,
            )
    }

    @Test
    fun `sendNotificationToRemainingLandlords emails each remaining landlord with the correct details`() {
        val address = MockLandlordData.createAddress(singleLineAddress = "10 High Street, London, SW1A 1AA")
        val previousLandlord = MockLandlordData.createLandlord(name = "Alice", email = "alice@example.com")
        val remainingLandlordOne = MockLandlordData.createLandlord(name = "Bob", email = "bob@example.com")
        val remainingLandlordTwo = MockLandlordData.createLandlord(name = "Carol", email = "carol@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = 7,
                landlords = mutableSetOf(remainingLandlordOne, remainingLandlordTwo),
                address = address,
            )
        val propertyRecordUri = URI("https://example.com/landlord/property/7")
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(7)).thenReturn(propertyRecordUri)

        emailService.sendNotificationToRemainingLandlords(propertyOwnership, previousLandlord)

        val emailCaptor = argumentCaptor<JointLandlordOtherLandlordLeftNotification>()
        verify(mockNotificationEmailService).sendEmail(eq("bob@example.com"), emailCaptor.capture())
        verify(mockNotificationEmailService).sendEmail(eq("carol@example.com"), emailCaptor.capture())

        val bobEmail = emailCaptor.allValues.single { it.notifiedLandlord == "Bob" }
        assertEquals("Alice", bobEmail.leavingLandlord)
        assertEquals(address.toMultiLineAddress(), bobEmail.address)
        assertEquals(propertyRecordUri.toString(), bobEmail.propertyRecordUrl)

        val carolEmail = emailCaptor.allValues.single { it.notifiedLandlord == "Carol" }
        assertEquals("Alice", carolEmail.leavingLandlord)
        assertEquals(address.toMultiLineAddress(), carolEmail.address)
        assertEquals(propertyRecordUri.toString(), carolEmail.propertyRecordUrl)
    }

    @Test
    fun `sendNotificationToRemainingLandlords sends no emails when there are no remaining landlords`() {
        val previousLandlord = MockLandlordData.createLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(),
            )

        emailService.sendNotificationToRemainingLandlords(propertyOwnership, previousLandlord)

        verify(mockNotificationEmailService, never()).sendEmail(any(), any())
    }

    @Test
    fun `flag-off implementation does nothing`() {
        val flagOff = JointLandlordOtherLandlordLeftEmailServiceImplFlagOff()
        val previousLandlord = MockLandlordData.createLandlord(name = "Alice")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(MockLandlordData.createLandlord(name = "Bob", email = "bob@example.com")),
            )

        flagOff.sendNotificationToRemainingLandlords(propertyOwnership, previousLandlord)

        verify(mockNotificationEmailService, never()).sendEmail(any(), any())
    }
}
