package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ConfirmEpcDetailsRetrievedByUprnFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasElectricalCertFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasEpcFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasGasSupplyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HmoAdditionalLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LicensingTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectiveLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyStateSessionBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import kotlin.test.assertTrue

// Covers the property registration journey with the restructure-and-skipping feature flag OFF.
// TODO PDJB-1340: delete every PropertyRegistrationPreRestructure*SinglePageTests file when
// PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING is removed. Every class here has an equivalent
// in the flag-on files, so they can all be removed wholesale.
class PropertyRegistrationPreRestructureCheckAnswersSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    @MockitoSpyBean
    private lateinit var savedJourneyStateRepository: SavedJourneyStateRepository

    @BeforeEach
    fun disableRestructureAndSkippingFlag() {
        featureFlagManager.disableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
    }

    @Nested
    inner class Confirmation {
        @Test
        fun `Navigating here with an incomplete form returns a 400 error page`(page: Page) {
            navigator.navigateToPropertyRegistrationConfirmationPage()
            val errorPage = assertPageIs(page, ErrorPage::class)
            BaseComponent.assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
        }
    }

    @Nested
    inner class PropertyRegistrationStepCheckAnswers {
        @Test
        fun `After changing an answer, submitting a full section saves the state and returns the CYA page`(page: Page) {
            var checkAnswersPage = navigator.skipToPropertyRegistrationCheckAnswersPage()

            checkAnswersPage.summaryList.ownershipRowLegacy.actions.firstActionLink
                .clickAndWait()
            val ownershipPage = assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class)

            ownershipPage.submitOwnershipType(OwnershipType.LEASEHOLD)
            checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

            checkAnswersPage.summaryList.licensingRow.actions.firstActionLink
                .clickAndWait()
            val licensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyRegistration::class)

            licensingTypePage.submitLicensingType(LicensingType.HMO_ADDITIONAL_LICENCE)
            val licenceNumberPage = assertPageIs(page, HmoAdditionalLicenceFormPagePropertyRegistration::class)
            licenceNumberPage.submitLicenseNumber("licence number")
            assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

            // Confirmation - verify record saved
            val savedJourneyStateCaptor = captor<SavedJourneyState>()
            verify(savedJourneyStateRepository, times(2)).save(savedJourneyStateCaptor.capture())
            val savedJourneyStateAfterOwnershipUpdate = savedJourneyStateCaptor.allValues[0]
            val savedJourneyStateAfterLicensingUpdate = savedJourneyStateCaptor.allValues[1]
            assertTrue(savedJourneyStateAfterOwnershipUpdate.serializedState.contains("ownershipType\":\"LEASEHOLD\""))
            assertTrue(savedJourneyStateAfterOwnershipUpdate.serializedState.contains("licensingType\":\"NO_LICENSING\""))
            assertTrue(savedJourneyStateAfterLicensingUpdate.serializedState.contains("licensingType\":\"HMO_ADDITIONAL_LICENCE\""))
            assertTrue(savedJourneyStateAfterLicensingUpdate.serializedState.contains("licenceNumber\":\"licence number\""))
        }

        @Test
        fun `the gas supply change link starts a CYA sub-journey that returns to the property registration CYA on submit`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyRegistrationCheckAnswersPage()
            checkAnswersPage.complianceSummaryList.gasSupplyRow.clickFirstActionLinkAndWait()
            val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPagePropertyRegistration::class)
            hasGasSupplyPage.submitHasNoGasSupply()
            assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
        }

        @Test
        fun `the electrical certificate change link navigates to the has electrical certificate page`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyRegistrationCheckAnswersPage()
            checkAnswersPage.complianceSummaryList.electricalCertRow.clickFirstActionLinkAndWait()
            assertPageIs(page, HasElectricalCertFormPagePropertyRegistration::class)
        }

        @Test
        fun `the EPC change link takes the user to the confirm epc step if epc is found by uprn`(page: Page) {
            whenever(epcRegisterClient.getByUprn(PropertyRegistrationJourneyTests.uprnForSelectedAddress))
                .thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse())

            val cyaPage =
                navigator.skipToPropertyRegistrationCheckEpcAnswers(
                    PropertyStateSessionBuilder.beforePropertyRegistrationCheckEpcAnswersCompliantEpc(),
                )

            cyaPage.epcCard
                .getAction("Change")
                .link
                .clickAndWait()
            assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPagePropertyRegistration::class)
        }

        @Test
        fun `the EPC change link takes the user to the has epc step if epc is found by certificate number`(page: Page) {
            whenever(epcRegisterClient.getByUprn(PropertyRegistrationJourneyTests.uprnForSelectedAddress))
                .thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)

            val cyaPage =
                navigator.skipToPropertyRegistrationCheckEpcAnswers(
                    PropertyStateSessionBuilder.beforePropertyRegistrationCheckAnswersEpcFoundByCertificateNumber(),
                )
            cyaPage.epcCard
                .getAction("Change")
                .link
                .clickAndWait()
            assertPageIs(page, HasEpcFormPagePropertyRegistration::class)
        }

        @Test
        fun `the licensing number change link navigates to the licensing page`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyRegistrationCheckAnswersPageWithSelectiveLicence()
            checkAnswersPage.summaryList.licensingNumberRow.clickFirstActionLinkAndWait()
            val selectiveLicencePage = assertPageIs(page, SelectiveLicenceFormPagePropertyRegistration::class)
            selectiveLicencePage.submitLicenseNumber("SL-99999")
            assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class ConfirmMissingComplianceStep {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val confirmPage = navigator.skipToPropertyRegistrationConfirmMissingCompliancePage()
            confirmPage.form.submit()
            assertThat(confirmPage.form.getErrorMessage()).containsText("Select whether you want to submit this registration")
        }

        @Test
        fun `Selecting no, go back redirects to the check answers page`(page: Page) {
            val confirmPage = navigator.skipToPropertyRegistrationConfirmMissingCompliancePage()
            confirmPage.form.radios.selectValue("false")
            confirmPage.form.submit()
            assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
        }
    }
}
