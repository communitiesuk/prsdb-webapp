package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.controllers.DelegateToLettingAgentController
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.delegateToLettingAgentJourneyPages.AllowLettingAgentPageDelegateToLettingAgent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.delegateToLettingAgentJourneyPages.ConfirmationPageDelegateToLettingAgent
import kotlin.test.assertEquals

class DelegateToLettingAgentJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    companion object {
        const val PROPERTY_OWNERSHIP_ID_OWNED_BY_CURRENT_USER = 4L
        const val PROPERTY_OWNERSHIP_ID_OWNED_BY_ANOTHER_LANDLORD = 3L
    }

    @Autowired
    lateinit var lettingAgentAccessRepository: LettingAgentAccessRepository

    @BeforeEach
    fun enableDelegateToLettingAgentFlag() {
        featureFlagManager.enableFeature(DELEGATE_TO_LETTING_AGENT)
    }

    @Test
    fun `a landlord can walk the journey from the first step to the confirmation page`(page: Page) {
        val propertyOwnershipId = PROPERTY_OWNERSHIP_ID_OWNED_BY_CURRENT_USER
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)

        propertyDetailsPage.delegateToLettingAgentLink.clickAndWait()

        val allowLettingAgentPage =
            assertPageIs(
                page,
                AllowLettingAgentPageDelegateToLettingAgent::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )

        assertThat(allowLettingAgentPage.heading).containsText("Allow your letting agent or property manager to provide details")

        allowLettingAgentPage.submitEmail("agent@example.com")

        val confirmationPage =
            assertPageIs(
                page,
                ConfirmationPageDelegateToLettingAgent::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        BaseComponent
            .assertThat(confirmationPage.confirmationBanner)
            .containsText("Letting agent or property manager can make updates")
        assertThat(confirmationPage.invitedEmailAddress).hasText("agent@example.com")
        assertThat(confirmationPage.propertyAddress).containsText("4 Imaginary Street")
        assertThat(confirmationPage.propertyAddress).containsText("FA1 1AH")

        val invitation = lettingAgentAccessRepository.findByPropertyOwnershipId(propertyOwnershipId)
        assertEquals("agent@example.com", invitation!!.invitedEmail)
    }

    @Test
    fun `the go back to the property record link on the confirmation page returns to the property record`(page: Page) {
        val propertyOwnershipId = PROPERTY_OWNERSHIP_ID_OWNED_BY_CURRENT_USER
        val allowLettingAgentPage = navigator.goToDelegateToLettingAgentAllowLettingAgentPage(propertyOwnershipId)
        allowLettingAgentPage.submitEmail("agent@example.com")

        val confirmationPage =
            assertPageIs(
                page,
                ConfirmationPageDelegateToLettingAgent::class,
                mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
            )
        confirmationPage.goBackToPropertyRecordLink.clickAndWait()

        assertPageIs(
            page,
            PropertyDetailsPageLandlordView::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    @Test
    fun `navigating directly to the confirmation page without completing the journey returns a 404`() {
        val response =
            navigator.navigate(
                "${DelegateToLettingAgentController.getDelegateToLettingAgentBasePath(
                    PROPERTY_OWNERSHIP_ID_OWNED_BY_CURRENT_USER,
                )}/$CONFIRMATION_PATH_SEGMENT",
            )

        assertEquals(404, response?.status())
    }

    @Test
    fun `navigating directly to the first step when already delegated redirects to the property record`(page: Page) {
        val propertyOwnershipId = PROPERTY_OWNERSHIP_ID_OWNED_BY_CURRENT_USER
        val allowLettingAgentPage = navigator.goToDelegateToLettingAgentAllowLettingAgentPage(propertyOwnershipId)
        allowLettingAgentPage.submitEmail("agent@example.com")
        assertPageIs(
            page,
            ConfirmationPageDelegateToLettingAgent::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )

        navigator.navigate(DelegateToLettingAgentController.getDelegateToLettingAgentPath(propertyOwnershipId))

        assertPageIs(
            page,
            PropertyDetailsPageLandlordView::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    @Test
    fun `the property record does not show the delegate to letting agent link when the feature flag is disabled`() {
        featureFlagManager.disableFeature(DELEGATE_TO_LETTING_AGENT)

        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(PROPERTY_OWNERSHIP_ID_OWNED_BY_CURRENT_USER)

        assertThat(propertyDetailsPage.delegateToLettingAgentLink.locator).hasCount(0)
    }

    @Test
    fun `the back link on the first step returns to the property record`(page: Page) {
        val propertyOwnershipId = PROPERTY_OWNERSHIP_ID_OWNED_BY_CURRENT_USER
        val allowLettingAgentPage = navigator.goToDelegateToLettingAgentAllowLettingAgentPage(propertyOwnershipId)

        allowLettingAgentPage.backLink.clickAndWait()

        assertPageIs(
            page,
            PropertyDetailsPageLandlordView::class,
            mapOf("propertyOwnershipId" to propertyOwnershipId.toString()),
        )
    }

    @Test
    fun `a landlord who does not own the property receives a 404`() {
        val response =
            navigator.navigate(
                DelegateToLettingAgentController.getDelegateToLettingAgentPath(PROPERTY_OWNERSHIP_ID_OWNED_BY_ANOTHER_LANDLORD),
            )
        assertEquals(404, response?.status())
    }

    @Test
    fun `the journey is not reachable when the delegate to letting agent feature flag is disabled`() {
        featureFlagManager.disableFeature(DELEGATE_TO_LETTING_AGENT)

        val response =
            navigator.navigate(
                DelegateToLettingAgentController.getDelegateToLettingAgentPath(PROPERTY_OWNERSHIP_ID_OWNED_BY_CURRENT_USER),
            )
        assertEquals(404, response?.status())
    }
}
