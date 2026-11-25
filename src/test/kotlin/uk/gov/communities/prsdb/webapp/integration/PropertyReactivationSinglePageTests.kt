package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.constants.EIC_ISSUE_DATE
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.BillsIncludedFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckAnswersPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckEicUploadsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckEicrUploadsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.CheckGasCertUploadsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.ConfirmEpcDetailsRetrievedByUprnFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EicExpiryDateFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EicIssueDateFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EicrExpiryDateFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EpcExemptionFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.EpcInDateAtStartOfTenancyCheckPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.FurnishedStatusFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.GasCertIssueDateFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasElectricalCertFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasEpcFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasGasCertFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasGasSupplyFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HasMeesExemptionFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.HmoMandatoryLicenceFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.IsEpcRequiredFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.LicensingTypeFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.MeesExemptionFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.NumberOfHouseholdsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.NumberOfPeopleFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.RentAmountFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.RentFrequencyFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.RentIncludesBillsFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages.SelectiveLicenceFormPagePropertyReactivation
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyReactivationStateSessionBuilder
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

        @Test
        fun `the valid gas certificate change link navigates to the gas certificate page, which links back to check answers`(page: Page) {
            val checkAnswersPage =
                navigator.skipToPropertyReactivationCheckAnswersPage(
                    inactivePropertyOwnershipId,
                    PropertyReactivationStateSessionBuilder.beforeCheckAnswersGasCertUploaded(),
                )

            checkAnswersPage.complianceSummaryList.validGasCertRow.clickFirstActionLinkAndWait()
            val hasGasCertPage = assertPageIs(page, HasGasCertFormPagePropertyReactivation::class, urlArguments)
            hasGasCertPage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `the gas certificate issue date change link navigates to the issue date page, which links back to check answers`(page: Page) {
            val checkAnswersPage =
                navigator.skipToPropertyReactivationCheckAnswersPage(
                    inactivePropertyOwnershipId,
                    PropertyReactivationStateSessionBuilder.beforeCheckAnswersGasCertUploaded(),
                )

            checkAnswersPage.complianceSummaryList.gasCertIssueDateRow.clickFirstActionLinkAndWait()
            val gasCertIssueDatePage = assertPageIs(page, GasCertIssueDateFormPagePropertyReactivation::class, urlArguments)
            gasCertIssueDatePage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `the gas certificate uploads change link navigates to the uploads page, which links back to check answers`(page: Page) {
            val checkAnswersPage =
                navigator.skipToPropertyReactivationCheckAnswersPage(
                    inactivePropertyOwnershipId,
                    PropertyReactivationStateSessionBuilder.beforeCheckAnswersGasCertUploaded(),
                )

            checkAnswersPage.complianceSummaryList.gasCertUploadRow.clickFirstActionLinkAndWait()
            val gasCertUploadsPage = assertPageIs(page, CheckGasCertUploadsFormPagePropertyReactivation::class, urlArguments)
            gasCertUploadsPage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `later gas pages reached by changing an answer from check answers link back to the previous page`(page: Page) {
            var checkAnswersPage =
                navigator.skipToPropertyReactivationCheckAnswersPage(
                    inactivePropertyOwnershipId,
                    PropertyReactivationStateSessionBuilder.beforeCheckAnswersGasCertUploaded(),
                )

            checkAnswersPage.complianceSummaryList.gasSupplyRow.clickFirstActionLinkAndWait()
            assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments).submitHasGasSupply()
            assertPageIs(page, HasGasCertFormPagePropertyReactivation::class, urlArguments).backLink.clickAndWait()
            assertPageIs(page, HasGasSupplyFormPagePropertyReactivation::class, urlArguments).backLink.clickAndWait()
            checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)

            checkAnswersPage.complianceSummaryList.gasCertIssueDateRow.clickFirstActionLinkAndWait()
            assertPageIs(page, GasCertIssueDateFormPagePropertyReactivation::class, urlArguments)
                .submitDate(PropertyRegistrationJourneyTests.validGasSafetyCertIssueDate)
            assertPageIs(page, CheckGasCertUploadsFormPagePropertyReactivation::class, urlArguments).backLink.clickAndWait()
            assertPageIs(page, GasCertIssueDateFormPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `the EIC issue date change link navigates to the issue date page, which links back to check answers`(page: Page) {
            val checkAnswersPage = skipToCheckAnswersPageWithElectricalCertUploaded(CertificateType.Eic)

            checkAnswersPage.complianceSummaryList.electricalCertDateRow(CertificateType.Eic).clickFirstActionLinkAndWait()
            val issueDatePage = assertPageIs(page, EicIssueDateFormPagePropertyReactivation::class, urlArguments)
            issueDatePage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        // TODO PDJB-1679: Remove this test when the EIC_ISSUE_DATE flag is removed
        @Test
        fun `the EIC expiry date change link navigates to the expiry date page, which links back to check answers`(page: Page) {
            featureFlagManager.disableFeature(EIC_ISSUE_DATE)
            var checkAnswersPage =
                navigator.skipToPropertyReactivationCheckAnswersPage(
                    inactivePropertyOwnershipId,
                    PropertyReactivationStateSessionBuilder.beforeCheckAnswersEicWithExpiryDateUploaded(),
                )

            checkAnswersPage.complianceSummaryList.eicExpiryDateRow.clickFirstActionLinkAndWait()
            assertPageIs(page, EicExpiryDateFormPagePropertyReactivation::class, urlArguments).backLink.clickAndWait()
            checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)

            checkAnswersPage.complianceSummaryList.eicExpiryDateRow.clickFirstActionLinkAndWait()
            assertPageIs(page, EicExpiryDateFormPagePropertyReactivation::class, urlArguments)
                .submitDate(PropertyRegistrationJourneyTests.validExpiryDate)
            assertPageIs(page, CheckEicUploadsFormPagePropertyReactivation::class, urlArguments).backLink.clickAndWait()
            assertPageIs(page, EicExpiryDateFormPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `the EICR expiry date change link navigates to the expiry date page, which links back to check answers`(page: Page) {
            val checkAnswersPage = skipToCheckAnswersPageWithElectricalCertUploaded(CertificateType.Eicr)

            checkAnswersPage.complianceSummaryList.electricalCertDateRow(CertificateType.Eicr).clickFirstActionLinkAndWait()
            val expiryDatePage = assertPageIs(page, EicrExpiryDateFormPagePropertyReactivation::class, urlArguments)
            expiryDatePage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = CertificateType::class, names = ["Eic", "Eicr"])
        fun `the electrical certificate uploads change link navigates to the uploads page, which links back to check answers`(
            certificateType: CertificateType,
            page: Page,
        ) {
            val checkAnswersPage = skipToCheckAnswersPageWithElectricalCertUploaded(certificateType)

            checkAnswersPage.complianceSummaryList.electricalCertUploadRow.clickFirstActionLinkAndWait()
            val electricalCertUploadsPage =
                when (certificateType) {
                    CertificateType.Eic -> assertPageIs(page, CheckEicUploadsFormPagePropertyReactivation::class, urlArguments)
                    CertificateType.Eicr -> assertPageIs(page, CheckEicrUploadsFormPagePropertyReactivation::class, urlArguments)
                    else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
                }
            electricalCertUploadsPage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `later electrical pages reached by changing an answer from check answers link back to the previous page`(page: Page) {
            var checkAnswersPage = skipToCheckAnswersPageWithElectricalCertUploaded(CertificateType.Eic)

            checkAnswersPage.complianceSummaryList.electricalCertRow.clickFirstActionLinkAndWait()
            assertPageIs(page, HasElectricalCertFormPagePropertyReactivation::class, urlArguments).submitHasEic()
            assertPageIs(page, EicIssueDateFormPagePropertyReactivation::class, urlArguments)
                .submitDate(PropertyRegistrationJourneyTests.validEicIssueDate)
            assertPageIs(page, CheckEicUploadsFormPagePropertyReactivation::class, urlArguments).backLink.clickAndWait()
            assertPageIs(page, EicIssueDateFormPagePropertyReactivation::class, urlArguments).backLink.clickAndWait()
            assertPageIs(page, HasElectricalCertFormPagePropertyReactivation::class, urlArguments).backLink.clickAndWait()
            checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)

            checkAnswersPage.complianceSummaryList.electricalCertDateRow(CertificateType.Eic).clickFirstActionLinkAndWait()
            assertPageIs(page, EicIssueDateFormPagePropertyReactivation::class, urlArguments)
                .submitDate(PropertyRegistrationJourneyTests.validEicIssueDate)
            assertPageIs(page, CheckEicUploadsFormPagePropertyReactivation::class, urlArguments).backLink.clickAndWait()
            assertPageIs(page, EicIssueDateFormPagePropertyReactivation::class, urlArguments)
        }

        private fun skipToCheckAnswersPageWithElectricalCertUploaded(certificateType: CertificateType) =
            navigator.skipToPropertyReactivationCheckAnswersPage(
                inactivePropertyOwnershipId,
                when (certificateType) {
                    CertificateType.Eic -> PropertyReactivationStateSessionBuilder.beforeCheckAnswersEicUploaded()
                    CertificateType.Eicr -> PropertyReactivationStateSessionBuilder.beforeCheckAnswersEicrUploaded()
                    else -> throw IllegalArgumentException("Unexpected certificate type: $certificateType")
                },
            )

        @Test
        fun `the EPC change link goes to the has EPC page when found by certificate number, which links back to check answers`(
            page: Page,
        ) {
            whenever(epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
            val checkAnswersPage =
                navigator.skipToPropertyReactivationCheckAnswersPage(
                    inactivePropertyOwnershipId,
                    PropertyReactivationStateSessionBuilder.beforeCheckAnswersEpcFoundByCertificateNumber(),
                )

            checkAnswersPage.epcCard
                .getAction("Change")
                .link
                .clickAndWait()
            val hasEpcPage = assertPageIs(page, HasEpcFormPagePropertyReactivation::class, urlArguments)
            hasEpcPage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `the EPC tenancy check change link navigates to the tenancy check page, which links back to check answers`(page: Page) {
            val checkAnswersPage =
                navigator.skipToPropertyReactivationCheckAnswersPage(
                    inactivePropertyOwnershipId,
                    PropertyReactivationStateSessionBuilder.beforeCheckAnswersEpcExpiredInDateAtTenancyStart(),
                )

            checkAnswersPage.complianceSummaryList.epcTenancyCheckRow.clickFirstActionLinkAndWait()
            val tenancyCheckPage = assertPageIs(page, EpcInDateAtStartOfTenancyCheckPagePropertyReactivation::class, urlArguments)
            tenancyCheckPage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `the is EPC required and EPC exemption change links navigate to their pages, which link back to check answers`(page: Page) {
            val checkAnswersPage =
                navigator.skipToPropertyReactivationCheckAnswersPage(
                    inactivePropertyOwnershipId,
                    PropertyReactivationStateSessionBuilder.beforeCheckAnswersNoEpcExempt(),
                )

            checkAnswersPage.complianceSummaryList.isEpcRequiredRow.clickFirstActionLinkAndWait()
            val isEpcRequiredPage = assertPageIs(page, IsEpcRequiredFormPagePropertyReactivation::class, urlArguments)
            isEpcRequiredPage.backLink.clickAndWait()
            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)

            checkAnswersPage.complianceSummaryList.epcExemptionRow.clickFirstActionLinkAndWait()
            val epcExemptionPage = assertPageIs(page, EpcExemptionFormPagePropertyReactivation::class, urlArguments)
            epcExemptionPage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `the registered energy efficiency exemption change link navigates to its page, which links back to check answers`(page: Page) {
            val checkAnswersPage =
                navigator.skipToPropertyReactivationCheckAnswersPage(
                    inactivePropertyOwnershipId,
                    PropertyReactivationStateSessionBuilder.beforeCheckAnswersEpcLowRatingWithExemption(),
                )

            checkAnswersPage.complianceSummaryList.hasMeesExemptionRow.clickFirstActionLinkAndWait()
            val hasMeesExemptionPage = assertPageIs(page, HasMeesExemptionFormPagePropertyReactivation::class, urlArguments)
            hasMeesExemptionPage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `the registered exemption reason change link navigates to the reason page, which links back to check answers`(page: Page) {
            val checkAnswersPage =
                navigator.skipToPropertyReactivationCheckAnswersPage(
                    inactivePropertyOwnershipId,
                    PropertyReactivationStateSessionBuilder.beforeCheckAnswersEpcLowRatingWithExemption(),
                )

            checkAnswersPage.complianceSummaryList.meesExemptionRow.clickFirstActionLinkAndWait()
            val meesExemptionPage = assertPageIs(page, MeesExemptionFormPagePropertyReactivation::class, urlArguments)
            meesExemptionPage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `the number of tenants change link navigates to the number of tenants page, which links back to check answers`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyReactivationCheckAnswersPage(inactivePropertyOwnershipId)

            checkAnswersPage.tenancySummaryList.numberOfTenantsRow.clickFirstActionLinkAndWait()
            val numberOfPeoplePage = assertPageIs(page, NumberOfPeopleFormPagePropertyReactivation::class, urlArguments)
            numberOfPeoplePage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `changing rent includes bills asks which bills are included and returns to check answers with the new answers`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyReactivationCheckAnswersPage(inactivePropertyOwnershipId)
            assertThat(checkAnswersPage.tenancySummaryList.rentIncludesBillsRow.value).containsText("No")

            checkAnswersPage.tenancySummaryList.rentIncludesBillsRow.clickFirstActionLinkAndWait()
            assertPageIs(page, RentIncludesBillsFormPagePropertyReactivation::class, urlArguments).submitIsIncluded()
            val billsIncludedPage = assertPageIs(page, BillsIncludedFormPagePropertyReactivation::class, urlArguments)
            billsIncludedPage.selectGasElectricityWater()
            billsIncludedPage.form.submit()

            val updatedCheckAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
            assertThat(updatedCheckAnswersPage.tenancySummaryList.rentIncludesBillsRow.value).containsText("Yes")
            assertThat(updatedCheckAnswersPage.tenancySummaryList.billsIncludedRow.value).containsText("Gas, Electricity, Water")
        }

        @Test
        fun `the bills included change link navigates to the bills included page, which links back to check answers`(page: Page) {
            val checkAnswersPage =
                navigator.skipToPropertyReactivationCheckAnswersPage(
                    inactivePropertyOwnershipId,
                    PropertyReactivationStateSessionBuilder.beforeCheckAnswersRentIncludesBills(),
                )

            checkAnswersPage.tenancySummaryList.billsIncludedRow.clickFirstActionLinkAndWait()
            val billsIncludedPage = assertPageIs(page, BillsIncludedFormPagePropertyReactivation::class, urlArguments)
            billsIncludedPage.backLink.clickAndWait()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `changing the rent frequency re-asks for the rent amount and returns to check answers with the new answers`(page: Page) {
            val checkAnswersPage = navigator.skipToPropertyReactivationCheckAnswersPage(inactivePropertyOwnershipId)
            assertThat(checkAnswersPage.tenancySummaryList.rentFrequencyRow.value).containsText("Monthly")

            checkAnswersPage.tenancySummaryList.rentFrequencyRow.clickFirstActionLinkAndWait()
            val rentFrequencyPage = assertPageIs(page, RentFrequencyFormPagePropertyReactivation::class, urlArguments)
            rentFrequencyPage.selectRentFrequency(RentFrequency.WEEKLY)
            rentFrequencyPage.form.submit()
            assertPageIs(page, RentAmountFormPagePropertyReactivation::class, urlArguments).submitRentAmount("100")

            val updatedCheckAnswersPage = assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
            assertThat(updatedCheckAnswersPage.tenancySummaryList.rentFrequencyRow.value).containsText("Weekly")
            assertThat(updatedCheckAnswersPage.tenancySummaryList.rentAmountRow.value).containsText("100")
        }
    }

    @Nested
    inner class ConfirmMissingComplianceStep {
        @Test
        fun `the confirm missing compliance page shows only the missing certificates with reactivation content`(page: Page) {
            val confirmPage = navigator.skipToPropertyReactivationConfirmMissingCompliancePage(inactivePropertyOwnershipId)

            assertThatComponent(confirmPage.heading).containsText("Confirm missing compliance certificates")
            assertThatComponent(confirmPage.warning).isVisible()
            assertThatComponent(confirmPage.electricalSectionHeading).isVisible()
            assertThatComponent(confirmPage.gasSectionHeading).isHidden()
            assertThatComponent(confirmPage.epcSectionHeading).isHidden()
            assertThatComponent(confirmPage.areYouSureLegend).containsText("Are you sure you want to continue?")
            assertThatComponent(confirmPage.form.submitButton).containsText("Confirm and continue")
            assertThatComponent(confirmPage.form.sectionHeader).isHidden()

            confirmPage.backLink.clickAndWait()
            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }

        @Test
        fun `submitting with no option selected shows the reactivation error`() {
            val confirmPage = navigator.skipToPropertyReactivationConfirmMissingCompliancePage(inactivePropertyOwnershipId)

            confirmPage.form.submit()

            assertThat(confirmPage.form.getErrorMessage()).containsText("Select whether you want to continue")
        }

        @Test
        fun `selecting no, go back returns to the check answers page`(page: Page) {
            navigator.skipToPropertyReactivationConfirmMissingCompliancePage(inactivePropertyOwnershipId).submitNo()

            assertPageIs(page, CheckAnswersPagePropertyReactivation::class, urlArguments)
        }
    }
}
