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
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
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
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LettingAgentEmailPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LicensingTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NumberOfHouseholdsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OccupancyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ProvideTenancyDetailsLaterFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectiveLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LettingAgentEmailStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AllowLettingAgentEmailFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyStateSessionBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import kotlin.test.assertTrue

class PropertyRegistrationCheckAnswersSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    @MockitoSpyBean
    private lateinit var savedJourneyStateRepository: SavedJourneyStateRepository

    @BeforeEach
    fun enabledFeatureFlags() {
        featureFlagManager.enableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        featureFlagManager.enableFeature(DELEGATE_TO_LETTING_AGENT)
    }

    @Nested
    inner class Confirmation {
        @Test
        fun `navigating here with an incomplete form returns a 400 error page`(page: Page) {
            navigator.navigateToPropertyRegistrationConfirmationPage()
            val errorPage = assertPageIs(page, ErrorPage::class)
            BaseComponent.assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
        }
    }

    @Nested
    inner class PropertyRegistrationStepCheckAnswers {
        @Test
        fun `after changing an answer, submitting a full section saves the state and returns the CYA page`(page: Page) {
            val taskListPage =
                navigator.goToRestructuredPropertyRegistrationTaskList(
                    PropertyStateSessionBuilder
                        .beforePropertyRegistrationCheckAnswers()
                        .withBedrooms(),
                )
            taskListPage.clickSubmitYourRegistrationTaskWithName("Check and submit your answers")
            var checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

            checkAnswersPage.summaryList.ownershipRow.actions.firstActionLink
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
            val taskListPage =
                navigator.goToRestructuredPropertyRegistrationTaskList(
                    PropertyStateSessionBuilder
                        .beforePropertyRegistrationCheckAnswers()
                        .withBedrooms(),
                )
            taskListPage.clickSubmitYourRegistrationTaskWithName("Check and submit your answers")
            val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
            checkAnswersPage.complianceSummaryList.gasSupplyRow.clickFirstActionLinkAndWait()
            val hasGasSupplyPage = assertPageIs(page, HasGasSupplyFormPagePropertyRegistration::class)
            hasGasSupplyPage.submitHasNoGasSupply()
            assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
        }

        @Test
        fun `the occupancy change link navigates to the occupancy page and changing from occupied to unoccupied returns to the CYA page`(
            page: Page,
        ) {
            val checkAnswersPage = navigator.skipToPropertyRegistrationCheckAnswersPageOccupied()
            assertThat(checkAnswersPage.summaryList.occupancyQuestionRow.value).containsText("Yes")

            checkAnswersPage.summaryList.occupancyQuestionRow.actions.firstActionLink
                .clickAndWait()
            val occupancyPage = assertPageIs(page, OccupancyFormPagePropertyRegistration::class)
            occupancyPage.submitIsVacant()
            val updatedCheckAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
            assertThat(updatedCheckAnswersPage.summaryList.occupancyQuestionRow.value).containsText("No")
        }

        @Test
        fun `when landlord provides details, rented out section is shown and email row is hidden`(page: Page) {
            val taskListPage =
                navigator.goToRestructuredPropertyRegistrationTaskList(
                    PropertyStateSessionBuilder
                        .beforePropertyRegistrationCheckAnswersOccupied()
                        .withLandlordProvidesRentalDetails()
                        .withBedrooms(),
                )
            taskListPage.clickSubmitYourRegistrationTaskWithName("Check and submit your answers")
            val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

            BaseComponent.assertThat(checkAnswersPage.lettingAgentDelegationHeading).isVisible()
            BaseComponent.assertThat(checkAnswersPage.lettingAgentDelegationSubheading).isVisible()
            assertThat(checkAnswersPage.summaryList.whoProvidesRentalDetailsRow.value).containsText("I will provide these details")
            assertThat(checkAnswersPage.summaryList.lettingAgentEmailRow.key).hasCount(0)
            BaseComponent.assertThat(checkAnswersPage.lettingAgentDelegationBodyText).isHidden()
        }

        @Test
        fun `when letting agent provides details, rented out section shows email row with change link`(page: Page) {
            val taskListPage =
                navigator.goToRestructuredPropertyRegistrationTaskList(
                    PropertyStateSessionBuilder
                        .beforePropertyRegistrationCheckAnswersOccupied()
                        .withLettingAgentProvidesRentalDetails()
                        .withSubmittedValue(
                            LettingAgentEmailStep.ROUTE_SEGMENT,
                            AllowLettingAgentEmailFormModel().apply { emailAddress = "letting.agent@example.com" },
                        )
                        .withBedrooms(),
                )
            taskListPage.clickSubmitYourRegistrationTaskWithName("Check and submit your answers")
            val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

            assertThat(checkAnswersPage.summaryList.whoProvidesRentalDetailsRow.value).containsText("My letting agent or property manager")
            assertThat(
                checkAnswersPage.summaryList.lettingAgentEmailRow.key,
            ).containsText("Letting agent or property manager’s email address")
            assertThat(checkAnswersPage.summaryList.lettingAgentEmailRow.value).containsText("letting.agent@example.com")
            BaseComponent.assertThat(checkAnswersPage.lettingAgentDelegationBodyText).isVisible()
            checkAnswersPage.summaryList.lettingAgentEmailRow.clickFirstActionLinkAndWait()
            val emailPage = assertPageIs(page, LettingAgentEmailPagePropertyRegistration::class)

            emailPage.submitEmail("new.agent@example.com")

            val updatedCheckAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
            assertThat(updatedCheckAnswersPage.summaryList.lettingAgentEmailRow.value)
                .containsText("new.agent@example.com")
        }

        @Test
        fun `rented out section appears after occupied and before licensing when landlord provides details`(page: Page) {
            val taskListPage =
                navigator.goToRestructuredPropertyRegistrationTaskList(
                    PropertyStateSessionBuilder
                        .beforePropertyRegistrationCheckAnswersOccupied()
                        .withLandlordProvidesRentalDetails()
                        .withBedrooms(),
                )
            taskListPage.clickSubmitYourRegistrationTaskWithName("Check and submit your answers")
            assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

            val headings =
                page
                    .locator("main h2.govuk-heading-m, main h3.govuk-heading-s")
                    .allInnerTexts()
                    .map { it.trim() }
            val occupancyIndex = headings.indexOf("Tell us if your property’s occupied")
            val rentedOutIndex = headings.indexOf("How your property’s rented out")
            val licensingIndex = headings.indexOf("Tell us if your property needs a license")

            assertTrue(occupancyIndex >= 0 && rentedOutIndex >= 0 && licensingIndex >= 0)
            assertTrue(rentedOutIndex > occupancyIndex, "Rented-out section should appear after occupied section")
            assertTrue(rentedOutIndex < licensingIndex, "Rented-out section should appear before licensing section")
        }

        @Test
        fun `when delegate to letting agent feature is disabled, rented out section is not displayed`(page: Page) {
            featureFlagManager.disableFeature(DELEGATE_TO_LETTING_AGENT)
            val taskListPage =
                navigator.goToRestructuredPropertyRegistrationTaskList(
                    PropertyStateSessionBuilder
                        .beforePropertyRegistrationCheckAnswersOccupied()
                        .withLandlordProvidesRentalDetails()
                        .withBedrooms(),
                )
            taskListPage.clickSubmitYourRegistrationTaskWithName("Check and submit your answers")
            val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

            BaseComponent.assertThat(checkAnswersPage.lettingAgentDelegationHeading).isHidden()
        }

        @Test
        fun `the occupancy change link navigates to the occupancy page and changing from unoccupied to occupied returns to the CYA page`(
            page: Page,
        ) {
            val checkAnswersPage = navigator.skipToPropertyRegistrationCheckAnswersPageUnoccupiedWithTenancyDetails()
            assertThat(checkAnswersPage.summaryList.occupancyQuestionRow.value).containsText("No")

            checkAnswersPage.summaryList.occupancyQuestionRow.actions.firstActionLink
                .clickAndWait()
            val occupancyPage = assertPageIs(page, OccupancyFormPagePropertyRegistration::class)
            occupancyPage.submitIsOccupied()
            val updatedCheckAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
            assertThat(updatedCheckAnswersPage.summaryList.occupancyQuestionRow.value).containsText("Yes")
        }

        @Test
        fun `the electrical certificate change link navigates to the has electrical certificate page`(page: Page) {
            val taskListPage =
                navigator.goToRestructuredPropertyRegistrationTaskList(
                    PropertyStateSessionBuilder
                        .beforePropertyRegistrationCheckAnswers()
                        .withBedrooms(),
                )
            taskListPage.clickSubmitYourRegistrationTaskWithName("Check and submit your answers")
            val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
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
            val taskListPage =
                navigator.goToRestructuredPropertyRegistrationTaskList(
                    PropertyStateSessionBuilder
                        .beforePropertyRegistrationCheckAnswersWithSelectiveLicence()
                        .withBedrooms(),
                )
            taskListPage.clickSubmitYourRegistrationTaskWithName("Check and submit your answers")
            val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
            checkAnswersPage.summaryList.licensingNumberRow.clickFirstActionLinkAndWait()
            val selectiveLicencePage = assertPageIs(page, SelectiveLicenceFormPagePropertyRegistration::class)
            selectiveLicencePage.submitLicenseNumber("SL-99999")
            assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
        }

        @Test
        fun `the tenancy details change link navigates to the households page when tenancy details have been provided later`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyRegistrationCheckAnswersPageWithProvideTenancyDetailsLater()
            checkAnswersPage.summaryList.tenancyDetailsRow.clickFirstActionLinkAndWait()
            assertPageIs(page, NumberOfHouseholdsFormPagePropertyRegistration::class)
        }

        @Test
        fun `selecting provide later again in the tenancy details CYA sub-journey returns to the property registration CYA`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyRegistrationCheckAnswersPageWithProvideTenancyDetailsLater()
            checkAnswersPage.summaryList.tenancyDetailsRow.clickFirstActionLinkAndWait()
            val householdsPage = assertPageIs(page, NumberOfHouseholdsFormPagePropertyRegistration::class)
            householdsPage.submitProvideThisLater()
            val confirmationPage = assertPageIs(page, ProvideTenancyDetailsLaterFormPagePropertyRegistration::class)
            confirmationPage.form.submit()
            assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class ConfirmMissingComplianceStep {
        @Test
        fun `submitting with no option selected returns an error`(page: Page) {
            val confirmPage = navigator.skipToPropertyRegistrationConfirmMissingCompliancePage()
            confirmPage.form.submit()
            assertThat(confirmPage.form.getErrorMessage()).containsText("Select whether you want to submit this registration")
        }

        @Test
        fun `selecting no, go back redirects to the check answers page`(page: Page) {
            val confirmPage = navigator.skipToPropertyRegistrationConfirmMissingCompliancePage()
            confirmPage.form.radios.selectValue("false")
            confirmPage.form.submit()
            assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
        }
    }
}
