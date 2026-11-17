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
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordOtherLandlordLeftNotification
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI
import kotlin.test.assertEquals

class JointLandlordOtherLandlordLeftEmailServiceTests {
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider
    private lateinit var mockNotificationEmailService: EmailNotificationService<JointLandlordOtherLandlordLeftNotification>
    private lateinit var mockFeatureFlagManager: FeatureFlagManager
    private lateinit var emailService: JointLandlordOtherLandlordLeftEmailService

    @BeforeEach
    fun setup() {
        mockAbsoluteUrlProvider = mock()
        mockNotificationEmailService = mock()
        mockFeatureFlagManager = mock()
        emailService =
            JointLandlordOtherLandlordLeftEmailService(
                mockAbsoluteUrlProvider,
                mockNotificationEmailService,
                mockFeatureFlagManager,
            )
    }

    // TODO PDJB-2149: Remove this test when the ACTIVE_INACTIVE flag is removed
    @Test
    fun `sendNotificationToRemainingLandlords emails each remaining landlord the pre-PDJB-844 notification when flag disabled`() {
        whenever(mockFeatureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(false)
        val previousLandlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val remainingLandlordOne = MockLandlordData.createIndividualLandlord(name = "Bob", email = "bob@example.com")
        val remainingLandlordTwo =
            MockLandlordData.createIndividualLandlord(name = "Carol", email = "carol@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                id = 7,
                landlords = mutableSetOf(remainingLandlordOne, remainingLandlordTwo),
            )
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(7)).thenReturn(URI("https://example.com/landlord/property/7"))

        emailService.sendNotificationToRemainingLandlords(propertyOwnership, previousLandlord)

        val emailCaptor = argumentCaptor<JointLandlordOtherLandlordLeftNotification>()
        verify(mockNotificationEmailService).sendEmailToLandlord(eq(remainingLandlordOne), emailCaptor.capture())
        verify(mockNotificationEmailService).sendEmailToLandlord(eq(remainingLandlordTwo), emailCaptor.capture())
        emailCaptor.allValues.forEach {
            assertEquals(EmailTemplate.JOINT_LANDLORD_OTHER_LANDLORD_LEFT_NOTIFICATION_BEFORE_PDJB_844, it.template)
        }
    }

    @Test
    fun `sendNotificationToRemainingLandlords emails each remaining landlord with the correct details`() {
        whenever(mockFeatureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
        val address = MockLandlordData.createAddress(singleLineAddress = "10 High Street, London, SW1A 1AA")
        val previousLandlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val remainingLandlordOne = MockLandlordData.createIndividualLandlord(name = "Bob", email = "bob@example.com")
        val remainingLandlordTwo =
            MockLandlordData.createIndividualLandlord(name = "Carol", email = "carol@example.com")
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
        verify(mockNotificationEmailService).sendEmailToLandlord(eq(remainingLandlordOne), emailCaptor.capture())
        verify(mockNotificationEmailService).sendEmailToLandlord(eq(remainingLandlordTwo), emailCaptor.capture())

        val bobEmail = emailCaptor.allValues.single { it.notifiedLandlord == "Bob" }
        assertEquals("Alice", bobEmail.leavingLandlord)
        assertEquals(address.toMultiLineAddress(), bobEmail.address)
        assertEquals(propertyRecordUri.toString(), bobEmail.propertyRecordUrl)

        val carolEmail = emailCaptor.allValues.single { it.notifiedLandlord == "Carol" }
        assertEquals("Alice", carolEmail.leavingLandlord)
        assertEquals(address.toMultiLineAddress(), carolEmail.address)
        assertEquals(propertyRecordUri.toString(), carolEmail.propertyRecordUrl)

        emailCaptor.allValues.forEach {
            assertEquals(EmailTemplate.JOINT_LANDLORD_OTHER_LANDLORD_LEFT_NOTIFICATION, it.template)
        }
    }

    @Test
    fun `sendNotificationToRemainingLandlords sends no emails when there are no remaining landlords`() {
        val previousLandlord = MockLandlordData.createIndividualLandlord(name = "Alice", email = "alice@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                landlords = mutableSetOf(),
            )

        emailService.sendNotificationToRemainingLandlords(propertyOwnership, previousLandlord)

        verify(mockNotificationEmailService, never()).sendEmailToLandlord(any(), any())
    }
}
