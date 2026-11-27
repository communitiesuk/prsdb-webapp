package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.controllers.DeactivatePropertyController
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.leavePropertyJourneyPages.ConfirmPageLeaveProperty
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeactivationJourneyPages.CannotDeactivatePropertyJointLandlordsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeactivationJourneyPages.CheckInvitationsPagePropertyDeactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeactivationJourneyPages.ConfirmPagePropertyDeactivation
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LettingAgentPropertyDeactivatedNotificationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeactivationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyDeactivationInviteeCancellationEmail
import uk.gov.communities.prsdb.webapp.services.DeactivationService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService

class PropertyDeactivationJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val propertyOwnershipId = 1L
    private val propertyWithPendingInvitationsId = 38L

    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    private lateinit var lettingAgentAccessRepository: LettingAgentAccessRepository

    @Autowired
    private lateinit var jointLandlordInvitationRepository: JointLandlordInvitationRepository

    @MockitoBean
    private lateinit var confirmationEmailSender: EmailNotificationService<PropertyDeactivationConfirmationEmail>

    @MockitoBean
    private lateinit var lettingAgentDeactivatedEmailSender: EmailNotificationService<LettingAgentPropertyDeactivatedNotificationEmail>

    @MockitoBean
    private lateinit var inviteeCancellationEmailSender: EmailNotificationService<PropertyDeactivationInviteeCancellationEmail>

    @Test
    fun `landlord with no pending joint landlord invitations goes straight to the deactivation confirm page and can deactivate`(
        page: Page,
    ) {
        assertTrue(jointLandlordInvitationRepository.findByRegisteredOwnershipId(propertyOwnershipId).isEmpty())

        val detailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        BaseComponent.assertThat(detailsPage.deregisterPropertyLink).isHidden()
        detailsPage.deactivatePropertyLink.clickAndWait()

        val confirmPage =
            assertPageIs(
                page,
                ConfirmPagePropertyDeactivation::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        assertThat(confirmPage.heading).containsText("Are you sure you want to deactivate this property?")
        confirmPage.submitConfirm()

        assertFalse(propertyOwnershipRepository.findById(propertyOwnershipId).get().isActive)
        assertNull(lettingAgentAccessRepository.findByPropertyOwnershipId(propertyOwnershipId))
        verify(confirmationEmailSender).sendEmail(any(), argThat { !landlordDeactivated })
        verify(lettingAgentDeactivatedEmailSender).sendEmail(eq("letting.agent.one@example.com"), any())

        val deactivatedDetailsPage =
            assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to propertyOwnershipId.toString()))
        assertThat(deactivatedDetailsPage.deactivationSuccessBanner).containsText("Property deactivated.")
        assertThat(deactivatedDetailsPage.caption).hasText("Deactivated property")
    }

    @Test
    fun `cancel returns to the property record without deactivating`(page: Page) {
        val confirmPage = navigator.goToPropertyDeactivationConfirmPage(propertyOwnershipId)
        confirmPage.cancelLink.clickAndWait()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to propertyOwnershipId.toString()))
        assertTrue(propertyOwnershipRepository.findById(propertyOwnershipId).get().isActive)
    }

    @Test
    fun `back link on the confirm page returns to the property record`(page: Page) {
        val confirmPage = navigator.goToPropertyDeactivationConfirmPage(propertyOwnershipId)
        confirmPage.backLink.clickAndWait()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to propertyOwnershipId.toString()))
    }

    @Test
    fun `confirm page hides the landlord account section when the landlord has other active properties`(page: Page) {
        val confirmPage = navigator.goToPropertyDeactivationConfirmPage(propertyOwnershipId)

        assertThat(confirmPage.landlordAccountHeading).isHidden()
    }

    @Test
    fun `landlord is warned about pending joint landlord invitations before deactivating and invitees are emailed`(page: Page) {
        val checkInvitationsPage = navigator.goToPropertyDeactivationCheckInvitationsPage(propertyWithPendingInvitationsId)
        assertThat(checkInvitationsPage.heading).containsText("Check these actions before you deactivate")
        assertThat(checkInvitationsPage.invitationsHeading).containsText("Cancel 2 invitations")
        assertThat(checkInvitationsPage.invitationsBody).containsText("We’ll cancel these invitations")
        assertThat(checkInvitationsPage.invitationEmails).hasCount(2)
        checkInvitationsPage.submitContinue()

        val confirmPage =
            assertPageIs(
                page,
                ConfirmPagePropertyDeactivation::class,
                mapOf("propertyOwnershipId" to propertyWithPendingInvitationsId.toString()),
            )
        confirmPage.submitConfirm()

        assertFalse(propertyOwnershipRepository.findById(propertyWithPendingInvitationsId).get().isActive)
        assertTrue(jointLandlordInvitationRepository.findByRegisteredOwnershipId(propertyWithPendingInvitationsId).isEmpty())
        verify(inviteeCancellationEmailSender).sendEmail(eq("jl.pending.three@example.com"), any())
        verify(inviteeCancellationEmailSender).sendEmail(eq("jl.pending.four@example.com"), any())
    }

    @Test
    fun `landlord with one pending joint landlord invitation sees singular content on the check invitations page`(page: Page) {
        val invitations = jointLandlordInvitationRepository.findByRegisteredOwnershipId(propertyWithPendingInvitationsId)
        jointLandlordInvitationRepository.delete(invitations.single { it.invitedEmail == "jl.pending.four@example.com" })

        val checkInvitationsPage = navigator.goToPropertyDeactivationCheckInvitationsPage(propertyWithPendingInvitationsId)

        assertThat(checkInvitationsPage.invitationsHeading).hasText("Cancel 1 invitation to a joint landlord")
        assertThat(checkInvitationsPage.invitationsBody)
            .hasText("We’ll cancel this invitation to a joint landlord to register onto the property:")
        assertThat(checkInvitationsPage.invitationEmails).hasCount(1)
    }

    @Test
    fun `back link on the confirm page returns to the check invitations page when there are pending invitations`(page: Page) {
        navigator.goToPropertyDeactivationCheckInvitationsPage(propertyWithPendingInvitationsId).submitContinue()
        val confirmPage =
            assertPageIs(
                page,
                ConfirmPagePropertyDeactivation::class,
                mapOf("propertyOwnershipId" to propertyWithPendingInvitationsId.toString()),
            )
        confirmPage.backLink.clickAndWait()

        assertPageIs(
            page,
            CheckInvitationsPagePropertyDeactivation::class,
            mapOf("propertyOwnershipId" to propertyWithPendingInvitationsId.toString()),
        )
    }

    @Test
    fun `back link on the check invitations page returns to the property record`(page: Page) {
        val checkInvitationsPage = navigator.goToPropertyDeactivationCheckInvitationsPage(propertyWithPendingInvitationsId)
        checkInvitationsPage.backLink.clickAndWait()

        assertPageIs(
            page,
            PropertyDetailsPageLandlordView::class,
            mapOf("propertyOwnershipId" to propertyWithPendingInvitationsId.toString()),
        )
    }

    @Test
    fun `cancel on the check invitations page returns to the property record without deactivating`(page: Page) {
        val checkInvitationsPage = navigator.goToPropertyDeactivationCheckInvitationsPage(propertyWithPendingInvitationsId)
        checkInvitationsPage.cancelLink.clickAndWait()

        assertPageIs(
            page,
            PropertyDetailsPageLandlordView::class,
            mapOf("propertyOwnershipId" to propertyWithPendingInvitationsId.toString()),
        )
        assertTrue(propertyOwnershipRepository.findById(propertyWithPendingInvitationsId).get().isActive)
        assertEquals(2, jointLandlordInvitationRepository.findByRegisteredOwnershipId(propertyWithPendingInvitationsId).size)
    }
}

class PropertyDeactivationJointLandlordJourneyTests :
    IntegrationTestWithMutableData("data-mockuser-landlord-with-sole-and-joint-properties.sql") {
    private val solePropertyOwnershipId = 1L
    private val jointPropertyOwnershipId = 2L
    private val landlordId = 1L

    @Autowired
    private lateinit var landlordRepository: LandlordRepository

    @Autowired
    private lateinit var deactivationService: DeactivationService

    @MockitoBean
    private lateinit var confirmationEmailSender: EmailNotificationService<PropertyDeactivationConfirmationEmail>

    @Test
    fun `joint landlord property shows the cannot deactivate page linking to leave property`(page: Page) {
        navigator.navigate(DeactivatePropertyController.getPropertyDeactivationPath(jointPropertyOwnershipId))

        val cannotDeactivatePage =
            assertPageIs(
                page,
                CannotDeactivatePropertyJointLandlordsPage::class,
                mapOf("propertyOwnershipId" to jointPropertyOwnershipId.toString()),
            )
        cannotDeactivatePage.leavePropertyLink.clickAndWait()

        assertPageIs(page, ConfirmPageLeaveProperty::class, mapOf("propertyOwnershipId" to jointPropertyOwnershipId.toString()))
    }

    @Test
    fun `back link on the cannot deactivate page returns to the property record`(page: Page) {
        navigator.navigate(DeactivatePropertyController.getPropertyDeactivationPath(jointPropertyOwnershipId))
        val cannotDeactivatePage =
            assertPageIs(
                page,
                CannotDeactivatePropertyJointLandlordsPage::class,
                mapOf("propertyOwnershipId" to jointPropertyOwnershipId.toString()),
            )
        cannotDeactivatePage.backLink.clickAndWait()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, mapOf("propertyOwnershipId" to jointPropertyOwnershipId.toString()))
    }

    @Test
    fun `deactivating the landlord's last active property shows the account section and deactivates the landlord`(page: Page) {
        deactivationService.deactivateProperty(jointPropertyOwnershipId)

        val confirmPage = navigator.goToPropertyDeactivationConfirmPage(solePropertyOwnershipId)
        assertThat(confirmPage.landlordAccountHeading).isVisible()
        confirmPage.submitConfirm()

        assertFalse(landlordRepository.findById(landlordId).get().isActive)
        verify(confirmationEmailSender).sendEmail(any(), argThat { landlordDeactivated })
    }
}
