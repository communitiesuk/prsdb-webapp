package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipTypeBeforePdjb955
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.AnyoneInOwnershipChainFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckPropertyManagerFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ConfirmRemoveFreeholderFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CorrespondenceEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderContactTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderNameFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderProvideDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersInfoFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersListFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasAnySuperiorLandlordsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasJointLandlordsFormBasePagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipKindFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnsPropertyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyManagerDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyManagerFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SaveSuperiorLandlordFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordContactTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordNameFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordProvideDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordsBackRoutingFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordsListFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import kotlin.test.assertEquals

class AssociatedPartiesJourneyTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class TaskRoutingTests {
        @Test
        fun `Sole freehold owners are not asked about freeholders or superior landlords`(page: Page) {
            navigator.skipToPropertyRegistrationHasJointLandlordsPage().submitHasNoJointLandlords()

            // TODO PDJB-1959: update this usage when the real owns property page is added
            val ownsPropertyPage = assertPageIs(page, OwnsPropertyFormPagePropertyRegistration::class)
            ownsPropertyPage.submitOwnsProperty(true)
            // TODO PDJB-1960: update this usage when the real ownership kind page is added
            val ownershipKindPage = assertPageIs(page, OwnershipKindFormPagePropertyRegistration::class)
            ownershipKindPage.submitOwnershipKind(OwnershipKind.FREEHOLD_ONLY)
            // TODO PDJB-1961: update this usage when the real freehold type page is added
            val freeholdTypePage = assertPageIs(page, FreeholdTypeFormPagePropertyRegistration::class)
            freeholdTypePage.submitFreeholdType(FreeholdType.SOLE_OWNERSHIP)

            val propertyManagerPage = assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
            propertyManagerPage.submitNo()
            assertPageIs(page, CorrespondenceEmailFormPagePropertyRegistration::class)
        }

        @Test
        fun `Joint freehold owners are asked about freeholders but not superior landlords`(page: Page) {
            navigator.skipToPropertyRegistrationHasJointLandlordsPage().submitHasNoJointLandlords()

            // TODO PDJB-1959: update this usage when the real owns property page is added
            val ownsPropertyPage = assertPageIs(page, OwnsPropertyFormPagePropertyRegistration::class)
            ownsPropertyPage.submitOwnsProperty(true)
            // TODO PDJB-1960: update this usage when the real ownership kind page is added
            val ownershipKindPage = assertPageIs(page, OwnershipKindFormPagePropertyRegistration::class)
            ownershipKindPage.submitOwnershipKind(OwnershipKind.FREEHOLD_ONLY)
            // TODO PDJB-1961: update this usage when the real freehold type page is added
            val freeholdTypePage = assertPageIs(page, FreeholdTypeFormPagePropertyRegistration::class)
            freeholdTypePage.submitFreeholdType(FreeholdType.JOINT_TENANTS)

            assertPageIs(page, FreeholdersInfoFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.NO)
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

            // TODO PDJB-1959: update this usage when the real owns property page is added
            val ownsPropertyPage = assertPageIs(page, OwnsPropertyFormPagePropertyRegistration::class)
            ownsPropertyPage.submitOwnsProperty(true)
            // TODO PDJB-1960: update this usage when the real ownership kind page is added
            val ownershipKindPage = assertPageIs(page, OwnershipKindFormPagePropertyRegistration::class)
            ownershipKindPage.submitOwnershipKind(OwnershipKind.FREEHOLD_AND_LEASEHOLD)
            // TODO PDJB-1961: update this usage when the real freehold type page is added
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

            // TODO PDJB-1959: update this usage when the real owns property page is added
            val ownsPropertyPage = assertPageIs(page, OwnsPropertyFormPagePropertyRegistration::class)
            ownsPropertyPage.submitOwnsProperty(false)

            assertPageIs(page, FreeholdersInfoFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.NO)
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

            // TODO PDJB-1959: update this usage when the real owns property page is added
            val ownsPropertyPage = assertPageIs(page, OwnsPropertyFormPagePropertyRegistration::class)
            ownsPropertyPage.submitOwnsProperty(true)
            // TODO PDJB-1960: update this usage when the real ownership kind page is added
            val ownershipKindPage = assertPageIs(page, OwnershipKindFormPagePropertyRegistration::class)
            ownershipKindPage.submitOwnershipKind(OwnershipKind.LEASEHOLD_ONLY)

            assertPageIs(page, FreeholdersInfoFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.NO)
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
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.YES)
            assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class).submitName("Test Freeholder")
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class).submitContactType(FreeholderContactType.EMAIL)
            assertPageIs(page, FreeholderEmailFormPagePropertyRegistration::class).submitEmail("test@example.com")
            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing postal address as the freeholder contact type goes through the address page before leaving the task`(page: Page) {
            val freeholdersPage = navigator.skipToPropertyRegistrationFreeholdersInfoPage()
            freeholdersPage.submit()
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.YES)
            assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class).submitName("Test Freeholder")
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class)
                .submitContactType(FreeholderContactType.POSTAL_ADDRESS)
            // TODO PDJB-1967: update this usage when the real freeholder address pages are added
            assertPageIs(page, FreeholderAddressFormPagePropertyRegistration::class).submit()
            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing not to provide freeholder details skips the freeholder detail pages before leaving the task`(page: Page) {
            val freeholdersPage = navigator.skipToPropertyRegistrationFreeholdersInfoPage()
            freeholdersPage.submit()
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.NO)
            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Adding several freeholders shows each of them in the list`(page: Page) {
            val freeholdersPage = navigator.skipToPropertyRegistrationFreeholdersInfoPage()
            freeholdersPage.submit()
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.NO)

            var freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 1 joint freeholder")
            freeholdersListPage.addAnotherButton.click()
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.YES)
            assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class).submitName("Email freeholder")
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class).submitContactType(FreeholderContactType.EMAIL)
            assertPageIs(page, FreeholderEmailFormPagePropertyRegistration::class).submitEmail("email.freeholder@example.com")

            freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 2 joint freeholders")
            freeholdersListPage.addAnotherButton.click()
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.YES)
            val secondNamePage = assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class)
            assertThat(secondNamePage.form.nameInput).hasValue("")
            secondNamePage.submitName("Address freeholder")
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class)
                .submitContactType(FreeholderContactType.POSTAL_ADDRESS)
            // TODO PDJB-1967: update this usage when the real freeholder address pages are added
            assertPageIs(page, FreeholderAddressFormPagePropertyRegistration::class).submit()

            freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 3 joint freeholders")
            assertThat(freeholdersListPage.getSummaryCard(0).title).hasText("Unknown freeholder")
            assertThat(freeholdersListPage.getSummaryCard(1).title).hasText("Email freeholder")
            assertThat(freeholdersListPage.getSummaryCard(1).summaryList).containsText("email.freeholder@example.com")
            assertThat(freeholdersListPage.getSummaryCard(2).title).hasText("Address freeholder")
            // TODO PDJB-1967: assert the address the user entered once the real address pages are added
            assertThat(freeholdersListPage.getSummaryCard(2).summaryList).containsText("1 Placeholder Street")
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
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.YES)
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
        fun `Removing freeholders one by one updates the list then returns to the freeholders page`(page: Page) {
            val freeholdersListPage = navigator.skipToPropertyRegistrationFreeholdersListPage()
            assertThat(freeholdersListPage.heading).containsText("added 3 joint freeholders")

            freeholdersListPage
                .getSummaryCard(1)
                .getAction("Remove")
                .link
                .clickAndWait()
            // TODO PDJB-1970: update this usage when the real remove freeholder confirmation page is added
            assertPageIs(page, ConfirmRemoveFreeholderFormPagePropertyRegistration::class).submit()
            val afterFirstRemoval = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(afterFirstRemoval.heading).containsText("added 2 joint freeholders")
            assertThat(afterFirstRemoval.getSummaryCard(0).title).hasText("Freeholder With Address")
            assertThat(afterFirstRemoval.getSummaryCard(1).title).hasText("Unknown freeholder")

            afterFirstRemoval
                .getSummaryCard(0)
                .getAction("Remove")
                .link
                .clickAndWait()
            // TODO PDJB-1970: update this usage when the real remove freeholder confirmation page is added
            assertPageIs(page, ConfirmRemoveFreeholderFormPagePropertyRegistration::class).submit()
            val afterSecondRemoval = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(afterSecondRemoval.heading).containsText("added 1 joint freeholder")
            assertThat(afterSecondRemoval.getSummaryCard(0).title).hasText("Unknown freeholder")

            afterSecondRemoval
                .getSummaryCard(0)
                .getAction("Remove")
                .link
                .clickAndWait()
            // TODO PDJB-1970: update this usage when the real remove freeholder confirmation page is added
            assertPageIs(page, ConfirmRemoveFreeholderFormPagePropertyRegistration::class).submit()
            assertPageIs(page, FreeholdersInfoFormPagePropertyRegistration::class)
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
            // TODO PDJB-1978: remove this usage when has any superior landlords becomes an internal step
            assertPageIs(page, HasAnySuperiorLandlordsFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1972: update this usage when the real provide details page is added
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.YES)
            // TODO PDJB-1973: update this usage when the real superior landlord name page is added
            assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1974: update this usage when the real contact type page is added
            assertPageIs(page, SuperiorLandlordContactTypeFormPagePropertyRegistration::class)
                .submitContactType(SuperiorLandlordContactType.EMAIL)
            // TODO PDJB-1975: update this usage when the real superior landlord email page is added
            assertPageIs(page, SuperiorLandlordEmailFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1978: remove this usage when save superior landlord becomes an internal step
            assertPageIs(page, SaveSuperiorLandlordFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1977: update this usage when the real superior landlords list page is added
            assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1978: remove this usage when superior landlords back routing becomes an internal step
            assertPageIs(page, SuperiorLandlordsBackRoutingFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing postal address as the superior landlord contact type goes through the address page before leaving the task`(
            page: Page,
        ) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage()
            anyoneInOwnershipChainPage.submitYes()
            // TODO PDJB-1978: remove this usage when has any superior landlords becomes an internal step
            assertPageIs(page, HasAnySuperiorLandlordsFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1972: update this usage when the real provide details page is added
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.YES)
            // TODO PDJB-1973: update this usage when the real superior landlord name page is added
            assertPageIs(page, SuperiorLandlordNameFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1974: update this usage when the real contact type page is added
            assertPageIs(page, SuperiorLandlordContactTypeFormPagePropertyRegistration::class)
                .submitContactType(SuperiorLandlordContactType.POSTAL_ADDRESS)
            // TODO PDJB-1976: update this usage when the real superior landlord address pages are added
            assertPageIs(page, SuperiorLandlordAddressFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1978: remove this usage when save superior landlord becomes an internal step
            assertPageIs(page, SaveSuperiorLandlordFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1977: update this usage when the real superior landlords list page is added
            assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1978: remove this usage when superior landlords back routing becomes an internal step
            assertPageIs(page, SuperiorLandlordsBackRoutingFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing not to provide superior landlord details skips the superior landlord detail pages before leaving the task`(
            page: Page,
        ) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage()
            anyoneInOwnershipChainPage.submitYes()
            // TODO PDJB-1978: remove this usage when has any superior landlords becomes an internal step
            assertPageIs(page, HasAnySuperiorLandlordsFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1972: update this usage when the real provide details page is added
            assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.NO)
            // TODO PDJB-1978: remove this usage when save superior landlord becomes an internal step
            assertPageIs(page, SaveSuperiorLandlordFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1977: update this usage when the real superior landlords list page is added
            assertPageIs(page, SuperiorLandlordsListFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1978: remove this usage when superior landlords back routing becomes an internal step
            assertPageIs(page, SuperiorLandlordsBackRoutingFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
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
