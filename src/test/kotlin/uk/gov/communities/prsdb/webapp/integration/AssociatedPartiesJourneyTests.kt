package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipTypeBeforePdjb955
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.AnyoneInOwnershipChainFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckPropertyManagerFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ConfirmRemoveFreeholderFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ConfirmRemoveSuperiorLandlordFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CorrespondenceEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderContactTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderLookupAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderManualAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderNameFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderNoAddressFoundFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderProvideDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderSelectAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersInfoFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersListFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasJointLandlordsFormBasePagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipKindFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnsPropertyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyManagerDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyManagerFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordContactTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordLookupAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordManualAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordNameFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordNoAddressFoundFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordProvideDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordSelectAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordsListFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import kotlin.test.assertEquals
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat as assertThatLocator

class AssociatedPartiesJourneyTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class TaskRoutingTests {
        @Test
        fun `Sole freehold owners are not asked about freeholders or superior landlords`(page: Page) {
            navigator.skipToPropertyRegistrationHasJointLandlordsPage().submitHasNoJointLandlords()

            val ownsPropertyPage = assertPageIs(page, OwnsPropertyFormPagePropertyRegistration::class)
            ownsPropertyPage.submitOwnsProperty(true)
            val ownershipKindPage = assertPageIs(page, OwnershipKindFormPagePropertyRegistration::class)
            ownershipKindPage.submitOwnershipKind(OwnershipKind.FREEHOLD_ONLY)
            val freeholdTypePage = assertPageIs(page, FreeholdTypeFormPagePropertyRegistration::class)
            freeholdTypePage.submitFreeholdType(FreeholdType.SOLE_OWNERSHIP)

            val propertyManagerPage = assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
            propertyManagerPage.submitNo()
            assertPageIs(page, CorrespondenceEmailFormPagePropertyRegistration::class)
        }

        @Test
        fun `Joint freehold owners are asked about freeholders but not superior landlords`(page: Page) {
            navigator.skipToPropertyRegistrationHasJointLandlordsPage().submitHasNoJointLandlords()

            val ownsPropertyPage = assertPageIs(page, OwnsPropertyFormPagePropertyRegistration::class)
            ownsPropertyPage.submitOwnsProperty(true)
            val ownershipKindPage = assertPageIs(page, OwnershipKindFormPagePropertyRegistration::class)
            ownershipKindPage.submitOwnershipKind(OwnershipKind.FREEHOLD_ONLY)
            val freeholdTypePage = assertPageIs(page, FreeholdTypeFormPagePropertyRegistration::class)
            freeholdTypePage.submitFreeholdType(FreeholdType.JOINT_TENANTS)

            assertPageIs(page, FreeholdersInfoFormPagePropertyRegistration::class).submit()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitNo()
            val freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 1 joint freeholder")
            assertThat(freeholdersListPage.getSummaryCard(0).title).hasText("Unknown freeholder")
            freeholdersListPage.submit()
            val propertyManagerPage = assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
            propertyManagerPage.submitNo()
            assertPageIs(page, CorrespondenceEmailFormPagePropertyRegistration::class)
        }

        @Test
        fun `Sole freehold and leasehold owners are asked about superior landlords but not freeholders`(page: Page) {
            navigator.skipToPropertyRegistrationHasJointLandlordsPage().submitHasNoJointLandlords()

            val ownsPropertyPage = assertPageIs(page, OwnsPropertyFormPagePropertyRegistration::class)
            ownsPropertyPage.submitOwnsProperty(true)
            val ownershipKindPage = assertPageIs(page, OwnershipKindFormPagePropertyRegistration::class)
            ownershipKindPage.submitOwnershipKind(OwnershipKind.FREEHOLD_AND_LEASEHOLD)
            val freeholdTypePage = assertPageIs(page, FreeholdTypeFormPagePropertyRegistration::class)
            freeholdTypePage.submitFreeholdType(FreeholdType.SOLE_OWNERSHIP)

            assertPageIs(page, AnyoneInOwnershipChainFormPagePropertyRegistration::class).submitNo()
            val propertyManagerPage = assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
            propertyManagerPage.submitNo()
            assertPageIs(page, CorrespondenceEmailFormPagePropertyRegistration::class)
        }

        @Test
        fun `Landlords who do not own the property are asked about freeholders and superior landlords`(page: Page) {
            navigator.skipToPropertyRegistrationHasJointLandlordsPage().submitHasNoJointLandlords()

            val ownsPropertyPage = assertPageIs(page, OwnsPropertyFormPagePropertyRegistration::class)
            ownsPropertyPage.submitOwnsProperty(false)

            assertPageIs(page, FreeholdersInfoFormPagePropertyRegistration::class).submit()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitNo()
            val freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 1 freeholder")
            freeholdersListPage.submit()
            assertPageIs(page, AnyoneInOwnershipChainFormPagePropertyRegistration::class).submitNo()
            val propertyManagerPage = assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
            propertyManagerPage.submitNo()
            assertPageIs(page, CorrespondenceEmailFormPagePropertyRegistration::class)
        }

        @Test
        fun `Leasehold only owners are asked about freeholders and superior landlords`(page: Page) {
            navigator.skipToPropertyRegistrationHasJointLandlordsPage().submitHasNoJointLandlords()

            val ownsPropertyPage = assertPageIs(page, OwnsPropertyFormPagePropertyRegistration::class)
            ownsPropertyPage.submitOwnsProperty(true)
            val ownershipKindPage = assertPageIs(page, OwnershipKindFormPagePropertyRegistration::class)
            ownershipKindPage.submitOwnershipKind(OwnershipKind.LEASEHOLD_ONLY)

            assertPageIs(page, FreeholdersInfoFormPagePropertyRegistration::class).submit()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitNo()
            val freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 1 freeholder")
            freeholdersListPage.submit()
            assertPageIs(page, AnyoneInOwnershipChainFormPagePropertyRegistration::class).submitNo()
            val propertyManagerPage = assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
            propertyManagerPage.submitNo()
            assertPageIs(page, CorrespondenceEmailFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class FreeholdersTests {
        @Test
        fun `Choosing email as the freeholder contact type goes through the email page before leaving the task`(page: Page) {
            val freeholdersPage = navigator.skipToPropertyRegistrationFreeholdersInfoPage()
            freeholdersPage.submit()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitYes()
            assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class).submitName("Test Freeholder")
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class).submitContactType(FreeholderContactType.EMAIL)
            assertPageIs(page, FreeholderEmailFormPagePropertyRegistration::class).submitEmail("test@example.com")
            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing postal address allows the freeholder address to be selected`(page: Page) {
            val lookupAddressPage = navigator.skipToPropertyRegistrationFreeholderLookupAddressPage()
            assertThat(lookupAddressPage.heading).containsText("Test Freeholder")
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("FA1 1AA", "1")

            val selectAddressPage = assertPageIs(page, FreeholderSelectAddressFormPagePropertyRegistration::class)
            assertThat(selectAddressPage.heading).containsText("Test Freeholder")
            selectAddressPage.selectAddressAndSubmit("1 Fictional Road, FA1 1AA")

            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing to enter a freeholder address manually from search results opens manual entry`(page: Page) {
            val selectAddressPage = navigator.skipToPropertyRegistrationFreeholderSelectAddressPage()
            selectAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)

            val manualAddressPage = assertPageIs(page, FreeholderManualAddressFormPagePropertyRegistration::class)
            assertThat(manualAddressPage.heading).containsText("What is Test Freeholder’s address?")
            manualAddressPage.submitAddress(
                addressLineOne = "4 Freeholder Road",
                addressLineTwo = "Suite 2",
                townOrCity = "London",
                postcode = "SW1A 1AA",
            )

            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Freeholder address search with no results allows manual entry`(page: Page) {
            val lookupAddressPage = navigator.skipToPropertyRegistrationFreeholderLookupAddressPage()
            val postcode = "NOT A POSTCODE"
            val houseNameOrNumber = "NOT A HOUSE NUMBER"
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber(postcode, houseNameOrNumber)

            val noAddressFoundPage = assertPageIs(page, FreeholderNoAddressFoundFormPagePropertyRegistration::class)
            assertThat(noAddressFoundPage.heading).containsText(postcode)
            assertThat(noAddressFoundPage.heading).containsText(houseNameOrNumber)
            noAddressFoundPage.form.submit()

            val manualAddressPage = assertPageIs(page, FreeholderManualAddressFormPagePropertyRegistration::class)
            assertThat(manualAddressPage.heading).containsText("What is Test Freeholder’s address?")
            manualAddressPage.submitAddress(
                addressLineOne = "4 Freeholder Road",
                addressLineTwo = "Suite 2",
                townOrCity = "London",
                postcode = "SW1A 1AA",
            )

            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing not to provide freeholder details skips the freeholder detail pages before leaving the task`(page: Page) {
            val freeholdersPage = navigator.skipToPropertyRegistrationFreeholdersInfoPage()
            freeholdersPage.submit()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitNo()
            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Adding several freeholders shows each of them in the list`(page: Page) {
            val freeholdersPage = navigator.skipToPropertyRegistrationFreeholdersInfoPage()
            freeholdersPage.submit()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitNo()

            var freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 1 joint freeholder")
            freeholdersListPage.addAnotherButton.click()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitYes()
            assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class).submitName("Email freeholder")
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class).submitContactType(FreeholderContactType.EMAIL)
            assertPageIs(page, FreeholderEmailFormPagePropertyRegistration::class).submitEmail("email.freeholder@example.com")

            freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 2 joint freeholders")
            freeholdersListPage.addAnotherButton.click()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitYes()
            val secondNamePage = assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class)
            assertThat(secondNamePage.form.nameInput).hasValue("")
            secondNamePage.submitName("Address freeholder")
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class)
                .submitContactType(FreeholderContactType.POSTAL_ADDRESS)
            val lookupAddressPage = assertPageIs(page, FreeholderLookupAddressFormPagePropertyRegistration::class)
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("FA1 1AA", "1")
            assertPageIs(page, FreeholderSelectAddressFormPagePropertyRegistration::class)
                .selectAddressAndSubmit("1 Fictional Road, FA1 1AA")

            freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 3 joint freeholders")
            assertThat(freeholdersListPage.getSummaryCard(0).title).hasText("Unknown freeholder")
            assertThat(freeholdersListPage.getSummaryCard(1).title).hasText("Email freeholder")
            assertThat(freeholdersListPage.getSummaryCard(1).summaryList).containsText("email.freeholder@example.com")
            assertThat(freeholdersListPage.getSummaryCard(2).title).hasText("Address freeholder")
            assertThat(freeholdersListPage.getSummaryCard(2).summaryList).containsText("1 Fictional Road")
        }

        @Test
        fun `Changing a freeholder updates their details in the list`(page: Page) {
            val freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage()
            assertThat(freeholdersListPage.getSummaryCard(1).title).hasText("Freeholder With Email")

            freeholdersListPage
                .getSummaryCard(1)
                .getAction("Change")
                .link
                .clickAndWait()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitYes()
            val namePage = assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class)
            assertThat(namePage.form.nameInput).hasValue("Freeholder With Email")
            namePage.submitName("Changed freeholder")
            val contactTypePage = assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class)
            assertEquals(FreeholderContactType.EMAIL.name, contactTypePage.form.radios.selectedValue)
            contactTypePage.form.submit()
            val emailPage = assertPageIs(page, FreeholderEmailFormPagePropertyRegistration::class)
            assertThat(emailPage.form.emailInput).hasValue("freeholder@example.com")
            emailPage.submitEmail("changed.freeholder@example.com")

            val updatedListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(updatedListPage.heading).containsText("added 3 joint freeholders")
            assertThat(updatedListPage.getSummaryCard(0).title).hasText("Freeholder With Address")
            assertThat(updatedListPage.getSummaryCard(1).title).hasText("Changed freeholder")
            assertThat(updatedListPage.getSummaryCard(1).summaryList).containsText("changed.freeholder@example.com")
            assertThat(updatedListPage.getSummaryCard(2).title).hasText("Unknown freeholder")
        }

        @Test
        fun `Changing a freeholder with a postal address pre-fills their address answers`(page: Page) {
            val freeholdersListPage =
                navigator.skipToPropertyRegistrationFreeholdersListPage(
                    freeholders =
                        mapOf(
                            1 to
                                FreeholderDataModel(
                                    detailsKnown = true,
                                    name = "Freeholder With Address",
                                    address = AddressDataModel.fromManualAddressData("2 Manual Road", "Manual Town", "MA1 1AA"),
                                    addressSearchPostcode = "FA1 1AA",
                                    addressSearchHouseNameOrNumber = "1",
                                    selectedAddress = MANUAL_ADDRESS_CHOSEN,
                                    manualAddressLineOne = "2 Manual Road",
                                    manualAddressLineTwo = "Flat 3",
                                    manualTownOrCity = "Manual Town",
                                    manualCounty = "Manual County",
                                    manualPostcode = "MA1 1AA",
                                ),
                            2 to FreeholderDataModel(detailsKnown = false),
                        ),
                )

            freeholdersListPage
                .getSummaryCard(0)
                .getAction("Change")
                .link
                .clickAndWait()
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitYes()
            assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class).form.submit()
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class).form.submit()

            val lookupAddressPage = assertPageIs(page, FreeholderLookupAddressFormPagePropertyRegistration::class)
            assertThat(lookupAddressPage.form.postcodeInput).hasValue("FA1 1AA")
            assertThat(lookupAddressPage.form.houseNameOrNumberInput).hasValue("1")
            lookupAddressPage.form.submit()

            val selectAddressPage = assertPageIs(page, FreeholderSelectAddressFormPagePropertyRegistration::class)
            assertEquals(MANUAL_ADDRESS_CHOSEN, selectAddressPage.form.addressRadios.selectedValue)
            selectAddressPage.form.submit()

            val manualAddressPage = assertPageIs(page, FreeholderManualAddressFormPagePropertyRegistration::class)
            assertThat(manualAddressPage.form.addressLineOneInput).hasValue("2 Manual Road")
            assertThat(manualAddressPage.form.addressLineTwoInput).hasValue("Flat 3")
            assertThat(manualAddressPage.form.townOrCityInput).hasValue("Manual Town")
            assertThat(manualAddressPage.form.countyInput).hasValue("Manual County")
            assertThat(manualAddressPage.form.postcodeInput).hasValue("MA1 1AA")
            manualAddressPage.form.addressLineOneInput.fill("3 Changed Road")
            manualAddressPage.form.submit()

            val updatedListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(updatedListPage.getSummaryCard(0).title).hasText("Freeholder With Address")
            assertThat(updatedListPage.getSummaryCard(0).summaryList).containsText("3 Changed Road")
        }

        @Test
        fun `Cancelling removal returns to the unchanged freeholder list`(page: Page) {
            var freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage()
            freeholdersListPage
                .getSummaryCard(1)
                .getAction("Remove")
                .link
                .clickAndWait()

            val removeFreeholderPage = assertPageIs(page, ConfirmRemoveFreeholderFormPagePropertyRegistration::class)
            removeFreeholderPage.cancelLink.clickAndWait()

            freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 3 joint freeholders")
            assertThat(freeholdersListPage.getSummaryCard(1).title).hasText("Freeholder With Email")
            assertThat(freeholdersListPage.getSummaryCard(2).title).hasText("Unknown freeholder")
        }

        @Test
        fun `Answering no returns to the unchanged freeholder list`(page: Page) {
            var freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage()
            freeholdersListPage
                .getSummaryCard(1)
                .getAction("Remove")
                .link
                .clickAndWait()

            val removeFreeholderPage = assertPageIs(page, ConfirmRemoveFreeholderFormPagePropertyRegistration::class)
            removeFreeholderPage.submitNo()

            freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 3 joint freeholders")
            assertThat(freeholdersListPage.getSummaryCard(1).title).hasText("Freeholder With Email")
            assertThat(freeholdersListPage.getSummaryCard(2).title).hasText("Unknown freeholder")
        }

        @Test
        fun `Confirming removal of the last non-joint freeholder returns to the freeholders page variant`(page: Page) {
            val freeholdersListPage =
                navigator.skipToPropertyRegistrationFreeholdersListPage(
                    isJointFreeholder = false,
                    freeholders =
                        mapOf(
                            1 to
                                FreeholderDataModel(
                                    detailsKnown = true,
                                    name = "Test Freeholder",
                                    email = "test@example.com",
                                ),
                        ),
                )
            freeholdersListPage
                .getSummaryCard(0)
                .getAction("Remove")
                .link
                .clickAndWait()

            val removeFreeholderPage = assertPageIs(page, ConfirmRemoveFreeholderFormPagePropertyRegistration::class)
            removeFreeholderPage.submitYes()

            val freeholdersInfoPage = assertPageIs(page, FreeholdersInfoFormPagePropertyRegistration::class)
            assertThat(freeholdersInfoPage.heading).containsText("Add freeholders")
        }

        @Test
        fun `Removing freeholders one by one updates the list then returns to the freeholders page`(page: Page) {
            var freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage()
            assertThat(freeholdersListPage.heading).containsText("added 3 joint freeholders")

            freeholdersListPage
                .getSummaryCard(1)
                .getAction("Remove")
                .link
                .clickAndWait()
            var removeFreeholderPage = assertPageIs(page, ConfirmRemoveFreeholderFormPagePropertyRegistration::class)
            removeFreeholderPage.submitYes()
            freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 2 joint freeholders")
            assertThat(freeholdersListPage.getSummaryCard(0).title).hasText("Freeholder With Address")
            assertThat(freeholdersListPage.getSummaryCard(1).title).hasText("Unknown freeholder")

            freeholdersListPage
                .getSummaryCard(0)
                .getAction("Remove")
                .link
                .clickAndWait()
            removeFreeholderPage = assertPageIs(page, ConfirmRemoveFreeholderFormPagePropertyRegistration::class)
            assertThatLocator(removeFreeholderPage.page.locator(".govuk-radios input:checked")).hasCount(0)
            removeFreeholderPage.submitYes()
            freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 1 joint freeholder")
            assertThat(freeholdersListPage.getSummaryCard(0).title).hasText("Unknown freeholder")

            freeholdersListPage
                .getSummaryCard(0)
                .getAction("Remove")
                .link
                .clickAndWait()
            removeFreeholderPage = assertPageIs(page, ConfirmRemoveFreeholderFormPagePropertyRegistration::class)
            assertThat(removeFreeholderPage.heading)
                .containsText("Are you sure you want to remove this unknown freeholder?")
            removeFreeholderPage.submitYes()

            val freeholdersInfoPage = assertPageIs(page, FreeholdersInfoFormPagePropertyRegistration::class)
            assertThat(freeholdersInfoPage.heading).containsText("Add joint freeholders")
        }
    }

    @Nested
    inner class SuperiorLandlordsTests {
        @Test
        fun `Answering no to having superior landlords skips the rest of the task`(page: Page) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage()
            anyoneInOwnershipChainPage.submitNo()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing email as the superior landlord contact type goes through the email page before leaving the task`(page: Page) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage()
            anyoneInOwnershipChainPage.submitYes()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitYes()
            assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class).submitName("Test Superior Landlord")
            assertPageIs(page, SuperiorLandlordContactTypeFormPagePropertyRegistration::class)
                .submitContactType(SuperiorLandlordContactType.EMAIL)
            assertPageIs(page, SuperiorLandlordEmailFormPagePropertyRegistration::class).submitEmail("superior.landlord@example.com")
            val superiorLandlordsListPage = assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class)
            assertThat(superiorLandlordsListPage.heading).containsText("added 1 person in the ownership chain")
            assertThat(superiorLandlordsListPage.getSummaryCard(0).title).hasText("Test Superior Landlord")
            assertThat(superiorLandlordsListPage.getSummaryCard(0).summaryList).containsText("superior.landlord@example.com")
            superiorLandlordsListPage.submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing postal address allows the superior landlord address to be selected`(page: Page) {
            val lookupAddressPage = navigator.skipToPropertyRegistrationSuperiorLandlordLookupAddressPage()
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("FA1 1AA", "1")

            assertPageIs(page, SuperiorLandlordSelectAddressFormPagePropertyRegistration::class)
                .selectAddressAndSubmit("1 Fictional Road, FA1 1AA")

            val superiorLandlordsListPage = assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class)
            assertThat(superiorLandlordsListPage.getSummaryCard(0).summaryList).containsText("1 Fictional Road")
            superiorLandlordsListPage.submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing to enter a superior landlord address manually from search results opens manual entry`(page: Page) {
            val selectAddressPage = navigator.skipToPropertyRegistrationSuperiorLandlordSelectAddressPage()
            selectAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)

            assertPageIs(page, SuperiorLandlordManualAddressFormPagePropertyRegistration::class).submitAddress(
                addressLineOne = "4 Superior Road",
                addressLineTwo = "Suite 2",
                townOrCity = "London",
                postcode = "SW1A 1AA",
            )

            assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Superior landlord address search with no results allows manual entry`(page: Page) {
            val lookupAddressPage = navigator.skipToPropertyRegistrationSuperiorLandlordLookupAddressPage()
            val postcode = "NOT A POSTCODE"
            val houseNameOrNumber = "NOT A HOUSE NUMBER"
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber(postcode, houseNameOrNumber)

            val noAddressFoundPage = assertPageIs(page, SuperiorLandlordNoAddressFoundFormPagePropertyRegistration::class)
            assertThat(noAddressFoundPage.heading).containsText(postcode)
            assertThat(noAddressFoundPage.heading).containsText(houseNameOrNumber)
            noAddressFoundPage.form.submit()

            assertPageIs(page, SuperiorLandlordManualAddressFormPagePropertyRegistration::class).submitAddress(
                addressLineOne = "4 Superior Road",
                addressLineTwo = "Suite 2",
                townOrCity = "London",
                postcode = "SW1A 1AA",
            )

            assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing not to provide superior landlord details skips the superior landlord detail pages before leaving the task`(
            page: Page,
        ) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage()
            anyoneInOwnershipChainPage.submitYes()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitNo()
            assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Adding several superior landlords shows each of them in the list`(page: Page) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage()
            anyoneInOwnershipChainPage.submitYes()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitNo()

            var superiorLandlordsListPage = assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class)
            assertThat(superiorLandlordsListPage.heading).containsText("added 1 person in the ownership chain")
            superiorLandlordsListPage.addAnotherButton.click()
            val provideDetailsPage = assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class)
            provideDetailsPage.submitYes()
            assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class).submitName("Email superior landlord")
            assertPageIs(page, SuperiorLandlordContactTypeFormPagePropertyRegistration::class)
                .submitContactType(SuperiorLandlordContactType.EMAIL)
            assertPageIs(page, SuperiorLandlordEmailFormPagePropertyRegistration::class).submitEmail("second.person@example.com")

            superiorLandlordsListPage = assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class)
            assertThat(superiorLandlordsListPage.heading).containsText("added 2 people in the ownership chain")
            superiorLandlordsListPage.addAnotherButton.click()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitYes()
            val secondNamePage = assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class)
            secondNamePage.submitName("Address superior landlord")
            assertPageIs(page, SuperiorLandlordContactTypeFormPagePropertyRegistration::class)
                .submitContactType(SuperiorLandlordContactType.POSTAL_ADDRESS)
            assertPageIs(page, SuperiorLandlordLookupAddressFormPagePropertyRegistration::class)
                .submitPostcodeAndBuildingNameOrNumber("FA1 1AA", "1")
            assertPageIs(page, SuperiorLandlordSelectAddressFormPagePropertyRegistration::class)
                .selectAddressAndSubmit("1 Fictional Road, FA1 1AA")

            superiorLandlordsListPage = assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class)
            assertThat(superiorLandlordsListPage.heading).containsText("added 3 people in the ownership chain")
            assertThat(superiorLandlordsListPage.getSummaryCard(0).title).hasText("Unknown person")
            assertThat(superiorLandlordsListPage.getSummaryCard(1).title).hasText("Email superior landlord")
            assertThat(superiorLandlordsListPage.getSummaryCard(1).summaryList).containsText("second.person@example.com")
            assertThat(superiorLandlordsListPage.getSummaryCard(2).title).hasText("Address superior landlord")
            assertThat(superiorLandlordsListPage.getSummaryCard(2).summaryList).containsText("1 Fictional Road")
        }

        @Test
        fun `Changing a superior landlord updates their details in the list`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            assertThat(superiorLandlordsListPage.getSummaryCard(1).title).hasText("Superior Landlord With Email")

            superiorLandlordsListPage
                .getSummaryCard(1)
                .getAction("Change")
                .link
                .clickAndWait()
            val provideDetailsPage = assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class)
            provideDetailsPage.submitYes()
            val namePage = assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class)
            namePage.submitName("Changed superior landlord")
            assertPageIs(page, SuperiorLandlordContactTypeFormPagePropertyRegistration::class).form.submit()
            assertPageIs(
                page,
                SuperiorLandlordEmailFormPagePropertyRegistration::class,
            ).submitEmail("changed.superior.landlord@example.com")

            val updatedListPage = assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class)
            assertThat(updatedListPage.heading).containsText("added 3 people in the ownership chain")
            assertThat(updatedListPage.getSummaryCard(0).title).hasText("Superior Landlord With Address")
            assertThat(updatedListPage.getSummaryCard(1).title).hasText("Changed superior landlord")
            assertThat(updatedListPage.getSummaryCard(1).summaryList).containsText("changed.superior.landlord@example.com")
            assertThat(updatedListPage.getSummaryCard(2).title).hasText("Unknown person")
        }

        @Test
        fun `Changing a superior landlord with a postal address pre-fills their address answers`(page: Page) {
            val superiorLandlordsListPage =
                navigator.skipToPropertyRegistrationSuperiorLandlordsListPage(
                    superiorLandlords =
                        mapOf(
                            1 to
                                SuperiorLandlordDataModel(
                                    detailsKnown = true,
                                    name = "Superior Landlord With Address",
                                    address = AddressDataModel.fromManualAddressData("2 Manual Road", "Manual Town", "MA1 1AA"),
                                    addressSearchPostcode = "FA1 1AA",
                                    addressSearchHouseNameOrNumber = "1",
                                    selectedAddress = MANUAL_ADDRESS_CHOSEN,
                                    manualAddressLineOne = "2 Manual Road",
                                    manualAddressLineTwo = "Flat 3",
                                    manualTownOrCity = "Manual Town",
                                    manualCounty = "Manual County",
                                    manualPostcode = "MA1 1AA",
                                ),
                            2 to SuperiorLandlordDataModel(detailsKnown = false),
                        ),
                )

            superiorLandlordsListPage
                .getSummaryCard(0)
                .getAction("Change")
                .link
                .clickAndWait()
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitYes()
            val namePage = assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class)
            assertThat(namePage.form.nameInput).hasValue("Superior Landlord With Address")
            namePage.form.submit()
            assertPageIs(page, SuperiorLandlordContactTypeFormPagePropertyRegistration::class)
                .submitContactType(SuperiorLandlordContactType.POSTAL_ADDRESS)

            val lookupAddressPage = assertPageIs(page, SuperiorLandlordLookupAddressFormPagePropertyRegistration::class)
            assertThat(lookupAddressPage.form.postcodeInput).hasValue("FA1 1AA")
            assertThat(lookupAddressPage.form.houseNameOrNumberInput).hasValue("1")
            lookupAddressPage.form.submit()

            val selectAddressPage = assertPageIs(page, SuperiorLandlordSelectAddressFormPagePropertyRegistration::class)
            assertEquals(MANUAL_ADDRESS_CHOSEN, selectAddressPage.form.addressRadios.selectedValue)
            selectAddressPage.form.submit()

            val manualAddressPage = assertPageIs(page, SuperiorLandlordManualAddressFormPagePropertyRegistration::class)
            assertThat(manualAddressPage.form.addressLineOneInput).hasValue("2 Manual Road")
            assertThat(manualAddressPage.form.addressLineTwoInput).hasValue("Flat 3")
            assertThat(manualAddressPage.form.townOrCityInput).hasValue("Manual Town")
            assertThat(manualAddressPage.form.countyInput).hasValue("Manual County")
            assertThat(manualAddressPage.form.postcodeInput).hasValue("MA1 1AA")
            manualAddressPage.form.addressLineOneInput.fill("3 Changed Road")
            manualAddressPage.form.submit()

            val updatedListPage = assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class)
            assertThat(updatedListPage.getSummaryCard(0).title).hasText("Superior Landlord With Address")
            assertThat(updatedListPage.getSummaryCard(0).summaryList).containsText("3 Changed Road")
        }

        @Test
        fun `Removing superior landlords one by one updates the list then returns to the anyone in ownership chain page`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()
            assertThat(superiorLandlordsListPage.heading).containsText("added 3 people in the ownership chain")

            superiorLandlordsListPage
                .getSummaryCard(1)
                .getAction("Remove")
                .link
                .clickAndWait()
            var confirmRemovePage = assertPageIs(page, ConfirmRemoveSuperiorLandlordFormPagePropertyRegistration::class)
            assertThat(confirmRemovePage.heading).containsText("Are you sure you want to remove Superior Landlord With Email?")
            confirmRemovePage.submitWantsToProceed()
            val afterFirstRemoval = assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class)
            assertThat(afterFirstRemoval.heading).containsText("added 2 people in the ownership chain")
            assertThat(afterFirstRemoval.getSummaryCard(0).title).hasText("Superior Landlord With Address")
            assertThat(afterFirstRemoval.getSummaryCard(1).title).hasText("Unknown person")

            afterFirstRemoval
                .getSummaryCard(0)
                .getAction("Remove")
                .link
                .clickAndWait()
            confirmRemovePage = assertPageIs(page, ConfirmRemoveSuperiorLandlordFormPagePropertyRegistration::class)
            confirmRemovePage.submitWantsToProceed()
            val afterSecondRemoval = assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class)
            assertThat(afterSecondRemoval.heading).containsText("added 1 person in the ownership chain")
            assertThat(afterSecondRemoval.getSummaryCard(0).title).hasText("Unknown person")

            afterSecondRemoval
                .getSummaryCard(0)
                .getAction("Remove")
                .link
                .clickAndWait()
            confirmRemovePage = assertPageIs(page, ConfirmRemoveSuperiorLandlordFormPagePropertyRegistration::class)
            assertThat(confirmRemovePage.heading).containsText("Are you sure you want to remove this unknown person?")
            confirmRemovePage.submitWantsToProceed()
            assertPageIs(page, AnyoneInOwnershipChainFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing not to remove a superior landlord keeps them in the list`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()

            superiorLandlordsListPage
                .getSummaryCard(1)
                .getAction("Remove")
                .link
                .clickAndWait()
            assertPageIs(page, ConfirmRemoveSuperiorLandlordFormPagePropertyRegistration::class).submitDoesNotWantToProceed()

            val updatedListPage = assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class)
            assertThat(updatedListPage.heading).containsText("added 3 people in the ownership chain")
            assertThat(updatedListPage.getSummaryCard(1).title).hasText("Superior Landlord With Email")
        }

        @Test
        fun `Cancelling removal of a superior landlord keeps them in the list`(page: Page) {
            val superiorLandlordsListPage = navigator.skipToPropertyRegistrationSuperiorLandlordsListPage()

            superiorLandlordsListPage
                .getSummaryCard(1)
                .getAction("Remove")
                .link
                .clickAndWait()
            assertPageIs(page, ConfirmRemoveSuperiorLandlordFormPagePropertyRegistration::class).cancelLink.clickAndWait()

            val updatedListPage = assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class)
            assertThat(updatedListPage.heading).containsText("added 3 people in the ownership chain")
            assertThat(updatedListPage.getSummaryCard(1).title).hasText("Superior Landlord With Email")
        }
    }

    @Nested
    inner class PropertyManagerTests {
        @Test
        fun `Landlords using a property manager provide its details and check them before leaving the task`(page: Page) {
            val propertyManagerPage = navigator.skipToPropertyRegistrationPropertyManagerPage()
            propertyManagerPage.submitYes()

            val propertyManagerDetailsPage = assertPageIs(page, PropertyManagerDetailsFormPagePropertyRegistration::class)
            propertyManagerDetailsPage.submitDetails(name = "Best Property Managers Ltd", email = "manager@example.com")
            val checkPropertyManagerPage = assertPageIs(page, CheckPropertyManagerFormPagePropertyRegistration::class)
            checkPropertyManagerPage.form.submit()
            assertPageIs(page, CorrespondenceEmailFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class OwnershipAndLandlordsBeforeAssociatedPartiesTests {
        // TODO PDJB-2016: remove this nested class once ASSOCIATED_PARTIES is released
        @BeforeEach
        fun disableAssociatedPartiesFlag() {
            featureFlagManager.disableFeature(ASSOCIATED_PARTIES)
        }

        @Test
        fun `Landlords select an ownership type then are asked about joint landlords before the correspondence task`(page: Page) {
            val ownershipTypePage = navigator.skipToPropertyRegistrationOwnershipTypePage()
            ownershipTypePage.submitOwnershipType(OwnershipTypeBeforePdjb955.FREEHOLD)

            val hasJointLandlordsPage = assertPageIs(page, HasJointLandlordsFormBasePagePropertyRegistration::class)
            hasJointLandlordsPage.submitHasNoJointLandlords()
            assertPageIs(page, CorrespondenceEmailFormPagePropertyRegistration::class)
        }
    }
}
