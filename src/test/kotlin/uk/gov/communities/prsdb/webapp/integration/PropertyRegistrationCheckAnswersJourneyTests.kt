package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.CORRESPONDENCE_ADDRESS
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_PHASE_TWO
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.controllers.CustomErrorController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckJointLandlordsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.InviteAnotherJointLandlordFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PaymentSummaryFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationCyaStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.CYA_SNAPSHOT_DELEGATE_KEY_PREFIX
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyStateSessionBuilder
import kotlin.test.assertContains

class PropertyRegistrationCheckAnswersJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @BeforeEach
    fun enableFeatureFlags() {
        featureFlagManager.enableFeature(CORRESPONDENCE_ADDRESS)
        featureFlagManager.enableFeature(PROPERTY_REGISTRATION_PHASE_TWO)
    }

    @Test
    fun `changing an answer from a CYA page loaded before the CYA was submitted in another tab returns to CYA with the new answer`(
        page: Page,
        browserContext: BrowserContext,
    ) {
        val staleCheckAnswersPage = navigator.goToPropertyRegistrationCheckAnswersPageWithPayments()
        val otherTab = browserContext.newPage()
        otherTab.navigate(page.url())
        assertPageIs(otherTab, CheckAnswersPagePropertyRegistration::class).confirm()
        assertPageIs(otherTab, PaymentSummaryFormPagePropertyRegistration::class)

        staleCheckAnswersPage.summaryList.ownershipRow.actions.firstActionLink
            .clickAndWait()
        assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class).submitOwnershipType(OwnershipType.LEASEHOLD)

        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
        assertThat(checkAnswersPage.summaryList.ownershipRow.value).containsText("Leasehold")
    }

    @Test
    fun `submitting a CYA page after its answers were changed in another tab shows the CYA error page`(
        page: Page,
        browserContext: BrowserContext,
    ) {
        val staleCheckAnswersPage = navigator.goToPropertyRegistrationCheckAnswersPageWithPayments()
        val otherTab = browserContext.newPage()
        otherTab.navigate(page.url())
        assertPageIs(otherTab, CheckAnswersPagePropertyRegistration::class)
            .summaryList.ownershipRow.actions.firstActionLink
            .clickAndWait()
        assertPageIs(otherTab, OwnershipTypeFormPagePropertyRegistration::class).submitOwnershipType(OwnershipType.LEASEHOLD)
        assertPageIs(otherTab, CheckAnswersPagePropertyRegistration::class)

        staleCheckAnswersPage.confirm()

        assertPageIs(page, ErrorPage::class)
        assertContains(page.url(), CustomErrorController.CYA_ERROR_ROUTE)
    }

    @Test
    fun `changing an answer after a joint landlord was invited in another tab shows the CYA error page instead of losing the invite`(
        page: Page,
        browserContext: BrowserContext,
    ) {
        val taskListPage =
            navigator.goToPropertyRegistrationTaskList(
                PropertyStateSessionBuilder.beforePropertyRegistrationCheckAnswers().withCheckedJointLandlords(),
            )
        taskListPage.clickSubmitYourRegistrationTaskWithName("Submit and pay")
        val checkAnswersUrl = page.url()
        assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
            .summaryList.ownershipRow.actions.firstActionLink
            .clickAndWait()
        val ownershipTypePage = assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class)

        val otherTab = browserContext.newPage()
        otherTab.navigate(checkAnswersUrl.replace(PropertyRegistrationCyaStep.ROUTE_SEGMENT, CheckJointLandlordsStep.ROUTE_SEGMENT))
        assertPageIs(otherTab, CheckJointLandlordsFormPagePropertyRegistration::class)
            .form.addAnotherButton
            .clickAndWait()
        assertPageIs(otherTab, InviteAnotherJointLandlordFormPagePropertyRegistration::class).submitEmail("another@example.com")
        assertPageIs(otherTab, CheckJointLandlordsFormPagePropertyRegistration::class)

        ownershipTypePage.submitOwnershipType(OwnershipType.LEASEHOLD)

        assertPageIs(page, ErrorPage::class)
        assertContains(page.url(), CustomErrorController.CYA_ERROR_ROUTE)
    }

    @Test
    fun `the CYA answers snapshot covers every delegate in the journey except those that aren't answers`() {
        val checkAnswersPage = navigator.goToPropertyRegistrationCheckAnswersPageWithPayments()

        val snapshotDelegateKeys =
            CheckAnswersFormModel
                .deserializeJourneyDataKeys(checkAnswersPage.answersSnapshot)
                .filter { it.startsWith(CYA_SNAPSHOT_DELEGATE_KEY_PREFIX) }
                .map { it.removePrefix(CYA_SNAPSHOT_DELEGATE_KEY_PREFIX) }
                .toSortedSet()

        // When adding a delegate to this journey, add its key here if it's an answer. Otherwise, add it to
        // PropertyRegistrationJourney.cyaSnapshotExcludedDelegateKeys so that changing it can't stop the CYA page being submitted
        assertEquals(
            sortedSetOf(
                "acceptedEpc",
                "cachedAddresses",
                "cachedOccupied",
                "cachedSelectedAddress",
                "cachedWhoProvidesRentalDetails",
                "checkingAnswersFor",
                "correspondence-address/cachedAddresses",
                "correspondence-address/cachedSelectedAddress",
                "correspondence-address/isAddressAlreadyRegistered",
                "cyaRouteSegment",
                "electricalUploadMap",
                "epcRetrievedByCertificateNumber",
                "epcRetrievedByCertificateNumberUpdatedSinceUserReview",
                "epcRetrievedByUprn",
                "epcRetrievedByUprnUpdatedSinceUserReview",
                "gasUploadMap",
                "highestAssignedElectricalMemberId",
                "highestGasUploadMemberId",
                "invitedJointLandlordEmails",
                "isAddressAlreadyRegistered",
                "isStateInitialized",
                "loggedInLandlordEmailAtStartOfJourney",
                "nextJointLandlordMemberId",
                "originalCyaAnswers",
                "registrationNumberValue",
                "updatedEpcRetrievedByCertificateNumber",
            ),
            snapshotDelegateKeys,
        )
    }
}
