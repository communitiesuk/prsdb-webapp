package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordPropertyUpdateNotificationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateConfirmation
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class PropertyUpdateEmailNotifierTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockLandlordService: LandlordService

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockConfirmationEmailService: EmailNotificationService<PropertyUpdateConfirmation>

    @Mock
    private lateinit var mockNotificationEmailService: EmailNotificationService<JointLandlordPropertyUpdateNotificationEmail>

    private val propertyId = 123L
    private val bullets = listOf("The ownership type")

    private lateinit var notifier: PropertyUpdateEmailNotifier

    @BeforeEach
    fun setUp() {
        notifier =
            PropertyUpdateEmailNotifier(
                mockPropertyOwnershipService,
                mockLandlordService,
                mockAbsoluteUrlProvider,
                mockConfirmationEmailService,
                mockNotificationEmailService,
            )
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `sendUpdateEmails throws PrsdbWebException when the acting landlord is not found`() {
        val baseUserId = "unknown-user"
        val actor = MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId))
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId, primaryLandlord = actor)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        setMockPrincipal(baseUserId)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(null)

        assertThrows<PrsdbWebException> { notifier.sendUpdateEmails(propertyId, bullets) }
    }

    @Test
    fun `sendUpdateEmails sends the confirmation to the acting landlord with the given bullets`() {
        val baseUserId = "acting-user"
        val actor =
            MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId), email = "actor@example.com")
        val other = MockLandlordData.createLandlord(email = "other@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(id = propertyId, primaryLandlord = actor, otherLandlords = mutableSetOf(other))
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        setMockPrincipal(baseUserId)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(actor)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://dashboard"))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(propertyId)).thenReturn(URI("http://property"))

        notifier.sendUpdateEmails(propertyId, bullets)

        verify(mockConfirmationEmailService).sendEmail(
            eq("actor@example.com"),
            argThat<PropertyUpdateConfirmation> { this.updatedBullets == bullets },
        )
    }

    @Test
    fun `sendUpdateEmails notifies every other landlord but not the acting landlord`() {
        val baseUserId = "acting-user"
        val actor =
            MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId), email = "actor@example.com")
        val other = MockLandlordData.createLandlord(name = "Lois", email = "other@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(id = propertyId, primaryLandlord = actor, otherLandlords = mutableSetOf(other))
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        setMockPrincipal(baseUserId)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(actor)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://dashboard"))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(propertyId)).thenReturn(URI("http://property"))

        notifier.sendUpdateEmails(propertyId, bullets)

        verify(mockNotificationEmailService).sendEmail(
            eq("other@example.com"),
            argThat<JointLandlordPropertyUpdateNotificationEmail> {
                this.recipientName == "Lois" && this.updatedBullets == bullets && this.propertyRecordUrl == "http://property"
            },
        )
        verify(mockNotificationEmailService, never()).sendEmail(eq("actor@example.com"), any())
    }

    @Test
    fun `sendUpdateEmails sends the confirmation to the acting landlord even when they are not the primary landlord`() {
        val baseUserId = "joint-user"
        val primary = MockLandlordData.createLandlord(email = "primary@example.com")
        val actor =
            MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId), email = "actor@example.com")
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(id = propertyId, primaryLandlord = primary, otherLandlords = mutableSetOf(actor))
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        setMockPrincipal(baseUserId)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(actor)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://dashboard"))
        whenever(mockAbsoluteUrlProvider.buildPropertyDetailsUri(propertyId)).thenReturn(URI("http://property"))

        notifier.sendUpdateEmails(propertyId, bullets)

        verify(mockConfirmationEmailService).sendEmail(eq("actor@example.com"), any<PropertyUpdateConfirmation>())
        verify(mockConfirmationEmailService, never()).sendEmail(eq("primary@example.com"), any())
        verify(mockNotificationEmailService).sendEmail(eq("primary@example.com"), any<JointLandlordPropertyUpdateNotificationEmail>())
    }

    @Test
    fun `sendUpdateEmails sends no notification when there are no other landlords`() {
        val baseUserId = "acting-user"
        val actor =
            MockLandlordData.createLandlord(baseUser = MockLandlordData.createPrsdbUser(baseUserId), email = "actor@example.com")
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId, primaryLandlord = actor)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        setMockPrincipal(baseUserId)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId)).thenReturn(actor)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://dashboard"))

        notifier.sendUpdateEmails(propertyId, bullets)

        verify(mockNotificationEmailService, never()).sendEmail(any(), any())
    }

    private fun setMockPrincipal(name: String) {
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn(name)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }
}
