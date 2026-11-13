package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.controllers.LeavePropertyController
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.leavePropertyJourneyPages.ConfirmationPageLeaveProperty
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordYouLeftConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.JointLandlordYouLeftConfirmationAccountDeactivated
import uk.gov.communities.prsdb.webapp.services.ActiveInactiveService
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import kotlin.test.assertEquals

class LeavePropertyJourneyTests : IntegrationTestWithMutableData("data-mockuser-landlord-with-sole-and-joint-properties.sql") {
    private val jointPropertyOwnershipId = 2L
    private val solePropertyOwnershipId = 1L
    private val leavingLandlordId = 1L
    private val remainingLandlordId = 2L

    @Autowired
    private lateinit var landlordRepository: LandlordRepository

    @Autowired
    private lateinit var activeInactiveService: ActiveInactiveService

    @MockitoBean
    private lateinit var confirmationEmailSender: EmailNotificationService<JointLandlordYouLeftConfirmation>

    @MockitoBean
    private lateinit var accountDeactivatedConfirmationEmailSender:
        EmailNotificationService<JointLandlordYouLeftConfirmationAccountDeactivated>

    @Test
    fun `confirming reaches the confirmation page`(page: Page) {
        val confirmPage = navigator.goToLeavePropertyConfirmPage(jointPropertyOwnershipId)
        assertThat(confirmPage.heading).containsText("3 Imaginary Street")
        confirmPage.submitConfirm()

        val confirmationPage =
            assertPageIs(
                page,
                ConfirmationPageLeaveProperty::class,
                mapOf("propertyOwnershipId" to jointPropertyOwnershipId.toString()),
            )
        BaseComponent
            .assertThat(confirmationPage.confirmationBanner)
            .containsText("No longer registered as a landlord for 3 Imaginary Street")

        confirmationPage.goToDashboardLink.clickAndWait()
        val dashboard = assertPageIs(page, LandlordDashboardPage::class)

        dashboard.viewPropertyRecordsButton.clickAndWait()
        val properties = assertPageIs(page, LandlordDetailsPage::class)

        BaseComponent.assertThat(properties.activePropertiesTable.rows).not().containsText("3 Imaginary Street")
    }

    @Test
    fun `leaving a property keeps the landlord active when they have another active property`(page: Page) {
        val confirmPage = navigator.goToLeavePropertyConfirmPage(jointPropertyOwnershipId)
        confirmPage.submitConfirm()
        assertPageIs(page, ConfirmationPageLeaveProperty::class, mapOf("propertyOwnershipId" to jointPropertyOwnershipId.toString()))

        assertTrue(landlordRepository.findById(leavingLandlordId).get().isActive)
        verify(confirmationEmailSender).sendEmail(any(), any())
        verify(accountDeactivatedConfirmationEmailSender, never()).sendEmail(any(), any())
    }

    @Test
    fun `leaving the last active property deactivates the landlord and sends the account deactivated email`(page: Page) {
        featureFlagManager.enable(ACTIVE_INACTIVE)
        activeInactiveService.deactivateProperty(solePropertyOwnershipId)

        val confirmPage = navigator.goToLeavePropertyConfirmPage(jointPropertyOwnershipId)
        confirmPage.submitConfirm()
        assertPageIs(page, ConfirmationPageLeaveProperty::class, mapOf("propertyOwnershipId" to jointPropertyOwnershipId.toString()))

        assertFalse(landlordRepository.findById(leavingLandlordId).get().isActive)
        assertTrue(landlordRepository.findById(remainingLandlordId).get().isActive)
        verify(accountDeactivatedConfirmationEmailSender).sendEmail(any(), any())
        verify(confirmationEmailSender, never()).sendEmail(any(), any())
    }

    @Test
    fun `confirm page shows the reinvite inset text and no warning when ACTIVE_INACTIVE is enabled`(page: Page) {
        featureFlagManager.enable(ACTIVE_INACTIVE)

        val confirmPage = navigator.goToLeavePropertyConfirmPage(jointPropertyOwnershipId)

        assertThat(confirmPage.insetText)
            .containsText("To become a landlord for this property again, another landlord will need to invite you.")
        assertThat(confirmPage.warningText).isHidden()
    }

    // TODO PDJB-2149: Remove this test when the ACTIVE_INACTIVE flag is removed
    @Test
    fun `confirm page shows the warning and no inset text when ACTIVE_INACTIVE is disabled`(page: Page) {
        featureFlagManager.disable(ACTIVE_INACTIVE)

        val confirmPage = navigator.goToLeavePropertyConfirmPage(jointPropertyOwnershipId)

        assertThat(confirmPage.warningText).isVisible()
        assertThat(confirmPage.insetText).isHidden()
    }

    @Test
    fun `a landlord on a property without joint landlords receives a 404`() {
        val response = navigator.navigate(LeavePropertyController.getLeavePropertyPath(solePropertyOwnershipId))
        assertEquals(404, response?.status())
    }
}

@WithOrgLandlordProfile
class LeavePropertyOrgLandlordJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    private val jointPropertyOwnershipId = 48L

    @Test
    fun `An org landlord can leave a joint property and reach the confirmation page`(page: Page) {
        val confirmPage = navigator.goToLeavePropertyConfirmPage(jointPropertyOwnershipId)
        assertThat(confirmPage.heading).containsText("Joint Org House")
        confirmPage.submitConfirm()

        val confirmationPage =
            assertPageIs(
                page,
                ConfirmationPageLeaveProperty::class,
                mapOf("propertyOwnershipId" to jointPropertyOwnershipId.toString()),
            )
        BaseComponent
            .assertThat(confirmationPage.confirmationBanner)
            .containsText("No longer registered as a landlord for Joint Org House")

        confirmationPage.goToDashboardLink.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }
}
