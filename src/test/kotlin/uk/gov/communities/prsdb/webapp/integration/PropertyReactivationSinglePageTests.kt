package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckAnswersPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.FurnishedStatusFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasElectricalCertFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasGasSupplyFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HmoMandatoryLicenceFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.LicensingTypeFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.NumberOfHouseholdsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.NumberOfPeopleFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.RentAmountFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.SelectiveLicenceFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat as assertThatComponent

class PropertyReactivationSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    private val inactivePropertyOwnershipId = 52L
    private val urlArguments = mapOf("propertyOwnershipId" to inactivePropertyOwnershipId.toString())

    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    @Nested
    inner class InfoStep {
        @Test
        fun `Page displays the heading, property address and what happens next`(page: Page) {
            val infoPage = navigator.goToPropertyReactivationInfoPage(inactivePropertyOwnershipId)

            assertThatComponent(infoPage.heading).containsText("Reactivate your property")
            assertThat(infoPage.propertyAddress).containsText("Inactive House")
            assertThat(infoPage.propertyAddress).containsText("IN1 1AA")
            assertThat(infoPage.bulletList).hasCount(5)
            assertThatComponent(infoPage.insetText).containsText("submit in one go")
        }
    }

    @Nested
    inner class AlreadyRegisteredStep {
        @Test
        fun `Page displays the heading and links to register a new property`(page: Page) {
            val alreadyRegisteredPage = navigator.skipToPropertyReactivationAlreadyRegisteredPage(inactivePropertyOwnershipId)

            assertThatComponent(alreadyRegisteredPage.heading).containsText("There is an active registration for this property")
            assertThat(alreadyRegisteredPage.registerNewPropertyLink.locator)
                .hasAttribute("href", RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE)
        }
    }

    @Nested
    inner class CheckAnswersStep {
        @Test
        fun `the check answers page shows the answers for each required task`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyReactivationCheckAnswersPage(inactivePropertyOwnershipId)

            assertThatComponent(checkAnswersPage.heading)
                .containsText("Check your answers for: Inactive House, PRSDB Road, IN1 1AA")
            assertThatComponent(checkAnswersPage.licensingHeading).isVisible()
            assertThatComponent(checkAnswersPage.gasSafetyHeading).isVisible()
            assertThatComponent(checkAnswersPage.electricalSafetyHeading).isVisible()
            assertThatComponent(checkAnswersPage.epcHeading).isVisible()
            assertThatComponent(checkAnswersPage.tenancyHeading).isVisible()
            assertThat(checkAnswersPage.licensingSummaryList.licensingTypeRow.value).containsText("Selective licence")
            assertThat(checkAnswersPage.licensingSummaryList.licensingNumberRow.value).containsText("SL-12345")
            assertThat(checkAnswersPage.complianceSummaryList.gasSupplyRow.value).containsText("No")
            assertThat(checkAnswersPage.tenancySummaryList.numberOfHouseholdsRow.value).containsText("2")
            assertThat(checkAnswersPage.tenancySummaryList.numberOfTenantsRow.value).containsText("4")
            assertThat(checkAnswersPage.tenancySummaryList.rentAmountRow.value).containsText("400")
            assertThatComponent(checkAnswersPage.epcCard).isVisible()
            assertThatComponent(checkAnswersPage.warning).containsText("false or misleading")
            assertThatComponent(checkAnswersPage.form.submitButton).containsText("Confirm details")
        }

        @Test
        fun `changing the licensing type re-asks for the licence number and returns to check answers with the new answers`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyReactivationCheckAnswersPage(inactivePropertyOwnershipId)

            checkAnswersPage.licensingSummaryList.licensingTypeRow.clickFirstActionLinkAndWait()
            assertPageIs(page, LicensingTypeFormPagePropertyReactivation::class, urlArguments)
                .submitLicensingType(LicensingType.HMO_MANDATORY_LICENCE)
            assertPageIs(page, HmoMandatoryLicenceFormPagePropertyReactivation::class, urlArguments).submitLicenseNumber("HMO-67890")

            val updatedCheckAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
            assertThat(updatedCheckAnswersPage.licensingSummaryList.licensingTypeRow.value).containsText("HMO mandatory licence")
            assertThat(updatedCheckAnswersPage.licensingSummaryList.licensingNumberRow.value).containsText("HMO-67890")
        }

        @Test
        fun `changing the licence number returns to check answers with the new answer`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyReactivationCheckAnswersPage(inactivePropertyOwnershipId)

            checkAnswersPage.licensingSummaryList.licensingNumberRow.clickFirstActionLinkAndWait()
            assertPageIs(page, SelectiveLicenceFormPagePropertyReactivation::class, urlArguments).submitLicenseNumber("SL-67890")

            val updatedCheckAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
            assertThat(updatedCheckAnswersPage.licensingSummaryList.licensingTypeRow.value).containsText("Selective licence")
            assertThat(updatedCheckAnswersPage.licensingSummaryList.licensingNumberRow.value).containsText("SL-67890")
        }

        @Test
        fun `the gas supply change link navigates to the gas supply page, which links back to check answers`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyReactivationCheckAnswersPage(inactivePropertyOwnershipId)

            checkAnswersPage.complianceSummaryList.gasSupplyRow.clickFirstActionLinkAndWait()
            val gasSupplyPage = assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments)
            gasSupplyPage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `the electrical certificate change link navigates to the certificate page, which links back to check answers`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyReactivationCheckAnswersPage(inactivePropertyOwnershipId)

            checkAnswersPage.complianceSummaryList.electricalCertRow.clickFirstActionLinkAndWait()
            val hasElectricalCertPage = assertPageIs(page, HasElectricalCertFormPagePropertyReactivation::class, urlArguments)
            hasElectricalCertPage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `changing the number of households returns to check answers with the new answer`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyReactivationCheckAnswersPage(inactivePropertyOwnershipId)

            checkAnswersPage.tenancySummaryList.numberOfHouseholdsRow.clickFirstActionLinkAndWait()
            assertPageIs(page, NumberOfHouseholdsFormPagePropertyReactivation::class, urlArguments).submitNumberOfHouseholds(3)
            assertPageIs(page, NumberOfPeopleFormPagePropertyReactivation::class, urlArguments).submitNumOfPeople(5)

            val updatedCheckAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
            assertThat(updatedCheckAnswersPage.tenancySummaryList.numberOfHouseholdsRow.value).containsText("3")
            assertThat(updatedCheckAnswersPage.tenancySummaryList.numberOfTenantsRow.value).containsText("5")
        }

        @Test
        fun `changing the furnished status returns to check answers with the new answer`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyReactivationCheckAnswersPage(inactivePropertyOwnershipId)
            assertThat(checkAnswersPage.tenancySummaryList.furnishedStatusRow.value).containsText("Fully furnished")

            checkAnswersPage.tenancySummaryList.furnishedStatusRow.clickFirstActionLinkAndWait()
            assertPageIs(page, FurnishedStatusFormPagePropertyReactivation::class, urlArguments)
                .submitFurnishedStatus(FurnishedStatus.UNFURNISHED)

            val updatedCheckAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
            assertThat(updatedCheckAnswersPage.tenancySummaryList.furnishedStatusRow.value).containsText("Unfurnished")
        }

        @Test
        fun `changing the rent amount returns to check answers with the new answer`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyReactivationCheckAnswersPage(inactivePropertyOwnershipId)

            checkAnswersPage.tenancySummaryList.rentAmountRow.clickFirstActionLinkAndWait()
            assertPageIs(page, RentAmountFormPagePropertyReactivation::class, urlArguments).submitRentAmount("500")

            val updatedCheckAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
            assertThat(updatedCheckAnswersPage.tenancySummaryList.rentAmountRow.value).containsText("500")
        }

        @Test
        fun `the EPC change link returns to check answers after confirming the EPC found by UPRN`(page: Page) {
            whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.createEpcRegisterClientEpcFoundResponse())
            val checkAnswersPage = navigator.skipToPropertyReactivationCheckAnswersPage(inactivePropertyOwnershipId)

            checkAnswersPage.epcCard
                .getAction("Change")
                .link
                .clickAndWait()
            assertPageIs(page, ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation::class, urlArguments).submitYes()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }
    }
}
