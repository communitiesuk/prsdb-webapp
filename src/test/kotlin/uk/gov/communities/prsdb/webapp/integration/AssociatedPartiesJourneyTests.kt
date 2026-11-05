package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.AnyoneInOwnershipChainFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckPropertyManagerFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CorrespondenceEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderContactTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderManualAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderNameFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderNoAddressFoundFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderProvideDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderSelectAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersBackRoutingFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersListFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasAnyFreeholdersFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasAnySuperiorLandlordsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasJointLandlordsFormBasePagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipKindFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnsPropertyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyManagerDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyManagerFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SaveFreeholderFormPagePropertyRegistration
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

            // TODO PDJB-1962: update this usage when the real freeholders intro page is added
            assertPageIs(page, FreeholdersFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1969: remove this usage when has any freeholders becomes an internal step
            assertPageIs(page, HasAnyFreeholdersFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.NO)
            // TODO PDJB-1969: remove this usage when save freeholder becomes an internal step
            assertPageIs(page, SaveFreeholderFormPagePropertyRegistration::class).submit()
            val freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            // TODO PDJB-1969: verify that the information the user submitted is visible
            assertThat(freeholdersListPage.heading).containsText("added 3 joint freeholders")
            assertThat(freeholdersListPage.getSummaryCard(2).title).hasText("Unknown freeholder")
            freeholdersListPage.submit()
            // TODO PDJB-1969: remove this usage when freeholders back routing becomes an internal step
            assertPageIs(page, FreeholdersBackRoutingFormPagePropertyRegistration::class).submit()
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

            // TODO PDJB-1971: update this usage when the real has superior landlords page is added
            assertPageIs(page, AnyoneInOwnershipChainFormPagePropertyRegistration::class).submitAnyoneInOwnershipChain(YesOrNo.NO)
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

            // TODO PDJB-1962: update this usage when the real freeholders intro page is added
            assertPageIs(page, FreeholdersFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1969: remove this usage when has any freeholders becomes an internal step
            assertPageIs(page, HasAnyFreeholdersFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.NO)
            // TODO PDJB-1969: remove this usage when save freeholder becomes an internal step
            assertPageIs(page, SaveFreeholderFormPagePropertyRegistration::class).submit()
            val freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 3 freeholders")
            freeholdersListPage.submit()
            // TODO PDJB-1969: remove this usage when freeholders back routing becomes an internal step
            assertPageIs(page, FreeholdersBackRoutingFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1971: update this usage when the real has superior landlords page is added
            assertPageIs(page, AnyoneInOwnershipChainFormPagePropertyRegistration::class).submitAnyoneInOwnershipChain(YesOrNo.NO)
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

            // TODO PDJB-1962: update this usage when the real freeholders intro page is added
            assertPageIs(page, FreeholdersFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1969: remove this usage when has any freeholders becomes an internal step
            assertPageIs(page, HasAnyFreeholdersFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.NO)
            // TODO PDJB-1969: remove this usage when save freeholder becomes an internal step
            assertPageIs(page, SaveFreeholderFormPagePropertyRegistration::class).submit()
            val freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class)
            assertThat(freeholdersListPage.heading).containsText("added 3 freeholders")
            freeholdersListPage.submit()
            // TODO PDJB-1969: remove this usage when freeholders back routing becomes an internal step
            assertPageIs(page, FreeholdersBackRoutingFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1971: update this usage when the real has superior landlords page is added
            assertPageIs(page, AnyoneInOwnershipChainFormPagePropertyRegistration::class).submitAnyoneInOwnershipChain(YesOrNo.NO)
            val propertyManagerPage = assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
            propertyManagerPage.submitNo()
            assertPageIs(page, CorrespondenceEmailFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class FreeholdersTests {
        @Test
        fun `Choosing email as the freeholder contact type goes through the email page before leaving the task`(page: Page) {
            val freeholdersPage = navigator.skipToPropertyRegistrationFreeholdersPage()
            freeholdersPage.submit()
            // TODO PDJB-1969: remove this usage when has any freeholders becomes an internal step
            assertPageIs(page, HasAnyFreeholdersFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.YES)
            assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class).submitName("Test Freeholder")
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class).submitContactType(FreeholderContactType.EMAIL)
            assertPageIs(page, FreeholderEmailFormPagePropertyRegistration::class).submitEmail("test@example.com")
            // TODO PDJB-1969: remove this usage when save freeholder becomes an internal step
            assertPageIs(page, SaveFreeholderFormPagePropertyRegistration::class).submit()
            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1969: remove this usage when freeholders back routing becomes an internal step
            assertPageIs(page, FreeholdersBackRoutingFormPagePropertyRegistration::class).submit()
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

            // TODO PDJB-1969: remove this usage when save freeholder becomes an internal step
            assertPageIs(page, SaveFreeholderFormPagePropertyRegistration::class).submit()
            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1969: remove this usage when freeholders back routing becomes an internal step
            assertPageIs(page, FreeholdersBackRoutingFormPagePropertyRegistration::class).submit()
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

            // TODO PDJB-1969: remove this usage when save freeholder becomes an internal step
            assertPageIs(page, SaveFreeholderFormPagePropertyRegistration::class).submit()
            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1969: remove this usage when freeholders back routing becomes an internal step
            assertPageIs(page, FreeholdersBackRoutingFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing not to provide freeholder details skips the freeholder detail pages before leaving the task`(page: Page) {
            val freeholdersPage = navigator.skipToPropertyRegistrationFreeholdersPage()
            freeholdersPage.submit()
            // TODO PDJB-1969: remove this usage when has any freeholders becomes an internal step
            assertPageIs(page, HasAnyFreeholdersFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.NO)
            // TODO PDJB-1969: remove this usage when save freeholder becomes an internal step
            assertPageIs(page, SaveFreeholderFormPagePropertyRegistration::class).submit()
            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1969: remove this usage when freeholders back routing becomes an internal step
            assertPageIs(page, FreeholdersBackRoutingFormPagePropertyRegistration::class).submit()
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class SuperiorLandlordsTests {
        @Test
        fun `Answering no to having superior landlords skips the rest of the task`(page: Page) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage()
            anyoneInOwnershipChainPage.submitAnyoneInOwnershipChain(YesOrNo.NO)
            assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
        }

        @Test
        fun `Choosing email as the superior landlord contact type goes through the email page before leaving the task`(page: Page) {
            val anyoneInOwnershipChainPage = navigator.skipToPropertyRegistrationAnyoneInOwnershipChainPage()
            anyoneInOwnershipChainPage.submitAnyoneInOwnershipChain(YesOrNo.YES)
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
            anyoneInOwnershipChainPage.submitAnyoneInOwnershipChain(YesOrNo.YES)
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
            anyoneInOwnershipChainPage.submitAnyoneInOwnershipChain(YesOrNo.YES)
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
            propertyManagerDetailsPage.form.submit()
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
            ownershipTypePage.submitOwnershipType(OwnershipType.FREEHOLD)

            val hasJointLandlordsPage = assertPageIs(page, HasJointLandlordsFormBasePagePropertyRegistration::class)
            hasJointLandlordsPage.submitHasNoJointLandlords()
            assertPageIs(page, CorrespondenceEmailFormPagePropertyRegistration::class)
        }
    }
}
