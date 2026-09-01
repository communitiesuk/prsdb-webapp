package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordPropertyUpdateNotificationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordPropertyUpdateWithLettingAgentRemovedNotification
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateWithLettingAgentRemovedConfirmation
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class PropertyUpdateEmailServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockUserToLandlordService: UserToLandlordService

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockConfirmationEmailService: EmailNotificationService<PropertyUpdateConfirmation>

    @Mock
    private lateinit var mockNotificationEmailService: EmailNotificationService<JointLandlordPropertyUpdateNotificationEmail>

    @Mock
    private lateinit var mockLettingAgentRemovedEmailService:
        EmailNotificationService<PropertyUpdateWithLettingAgentRemovedConfirmation>

    @Mock
    private lateinit var mockJointLandlordLettingAgentRemovedEmailService:
        EmailNotificationService<JointLandlordPropertyUpdateWithLettingAgentRemovedNotification>

    private val propertyId = 123L
    private val bullets = listOf("The ownership type")
    private val updatedMessage = "The property was made unoccupied"

    private lateinit var notifier: PropertyUpdateEmailService

    @BeforeEach
    fun setUp() {
        notifier =
            PropertyUpdateEmailService(
                mockPropertyOwnershipService,
                mockUserToLandlordService,
                mockAbsoluteUrlProvider,
                mockConfirmationEmailService,
                mockNotificationEmailService,
                mockLettingAgentRemovedEmailService,
                mockJointLandlordLettingAgentRemovedEmailService,
            )
    }

    @Test
    fun `sendUpdateEmails sends the confirmation to the acting landlord with the given bullets`() {
        val baseUserId = "acting-user"
        val actor =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                email = "actor@example.com",
            )
        val other = MockLandlordData.createIndividualLandlord(email = "other@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(id = propertyId, landlords = mutableSetOf(actor, other))
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(actor)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://dashboard"))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(propertyId)).thenReturn(URI("http://property"))

        notifier.sendUpdateEmails(propertyId, bullets)

        verify(mockConfirmationEmailService).sendEmail(
            eq(actor.email),
            argThat<PropertyUpdateConfirmation> { this.updatedBullets == bullets },
        )
    }

    @Test
    fun `sendUpdateEmails notifies every other landlord but not the acting landlord`() {
        val baseUserId = "acting-user"
        val actor =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                email = "actor@example.com",
            )
        val other = MockLandlordData.createIndividualLandlord(name = "Lois", email = "other@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(id = propertyId, landlords = mutableSetOf(actor, other))
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(actor)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://dashboard"))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(propertyId)).thenReturn(URI("http://property"))

        notifier.sendUpdateEmails(propertyId, bullets)

        verify(mockNotificationEmailService).sendEmail(
            eq(other.email),
            argThat<JointLandlordPropertyUpdateNotificationEmail> {
                this.recipientName == other.name && this.updatedBullets == bullets &&
                    this.propertyRecordUrl == "http://property"
            },
        )
        verify(mockNotificationEmailService, never()).sendEmail(eq(actor.email), any())
    }

    @Test
    fun `sendUpdateEmails sends no notification when there are no other landlords`() {
        val baseUserId = "acting-user"
        val actor =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                email = "actor@example.com",
            )
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(id = propertyId, landlords = mutableSetOf(actor))
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(actor)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://dashboard"))

        notifier.sendUpdateEmails(propertyId, bullets)

        verify(mockNotificationEmailService, never()).sendEmail(any(), any())
    }

    @Test
    fun `sendUpdateWithLettingAgentRemovedEmails sends confirmation to acting landlord`() {
        val baseUserId = "acting-user"
        val lettingAgentEmail = "agent@example.com"
        val actor =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                email = "actor@example.com",
            )
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(id = propertyId, landlords = mutableSetOf(actor))
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(actor)
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(propertyId)).thenReturn(URI("http://property"))

        notifier.sendUpdateWithLettingAgentRemovedEmails(propertyId, updatedMessage, lettingAgentEmail)

        verify(mockLettingAgentRemovedEmailService).sendEmail(
            eq(actor.email),
            argThat<PropertyUpdateWithLettingAgentRemovedConfirmation> {
                this.lettingAgentEmail == lettingAgentEmail && this.updatedMessage == updatedMessage
            },
        )
        verify(mockJointLandlordLettingAgentRemovedEmailService, never()).sendEmail(any(), any())
    }

    @Test
    fun `sendUpdateWithLettingAgentRemovedEmails sends notification to other landlords`() {
        val baseUserId = "acting-user"
        val lettingAgentEmail = "agent@example.com"
        val actor =
            MockLandlordData.createIndividualLandlord(
                baseUser = MockLandlordData.createPrsdbUser(baseUserId),
                email = "actor@example.com",
            )
        val other = MockLandlordData.createIndividualLandlord(name = "Lois", email = "other@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(id = propertyId, landlords = mutableSetOf(actor, other))
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser()).thenReturn(actor)
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(propertyId)).thenReturn(URI("http://property"))

        notifier.sendUpdateWithLettingAgentRemovedEmails(propertyId, updatedMessage, lettingAgentEmail)

        verify(mockJointLandlordLettingAgentRemovedEmailService).sendEmail(
            eq(other.email),
            argThat<JointLandlordPropertyUpdateWithLettingAgentRemovedNotification> {
                this.recipientName == other.name && this.lettingAgentEmail == lettingAgentEmail
            },
        )
        verify(mockJointLandlordLettingAgentRemovedEmailService, never()).sendEmail(eq(actor.email), any())
    }
}
