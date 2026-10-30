package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckPropertyManagerFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CorrespondenceEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderContactTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderNameFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderProvideDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholdersListFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasJointLandlordsFormBasePagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipKindFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnsPropertyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyManagerDetailsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.PropertyManagerFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RemoveFreeholderFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordsFormPagePropertyRegistration
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
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.NO)
            val freeholderNamePage = assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class)
            assertThat(freeholderNamePage.heading).containsText("What is this joint freeholder’s name?")
            freeholderNamePage.submitName("Jane Smith")
            // TODO PDJB-1965: update this usage when the real contact type page is added
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class).submitContactType(FreeholderContactType.EMAIL)
            // TODO PDJB-1966: update this usage when the real freeholder email page is added
            assertPageIs(page, FreeholderEmailFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1967: update this usage when the real freeholder address pages are added
            assertPageIs(page, FreeholderAddressFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1968: update this usage when the real freeholders list page is added
            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1970: update this usage when the real remove freeholder page is added
            assertPageIs(page, RemoveFreeholderFormPagePropertyRegistration::class).submit()
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

            // TODO PDJB-2008: update this usage when the real superior landlords pages are added
            val superiorLandlordsPage = assertPageIs(page, SuperiorLandlordsFormPagePropertyRegistration::class)
            superiorLandlordsPage.submit()
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
            // TODO PDJB-1963: update this usage when the real provide details page is added
            assertPageIs(page, FreeholderProvideDetailsFormPagePropertyRegistration::class).submitProvideDetails(YesOrNo.NO)
            val freeholderNamePage = assertPageIs(page, FreeholderNameFormPagePropertyRegistration::class)
            assertThat(freeholderNamePage.heading).containsText("What is this freeholder’s name?")
            freeholderNamePage.submitName("Jane Smith")
            // TODO PDJB-1965: update this usage when the real contact type page is added
            assertPageIs(page, FreeholderContactTypeFormPagePropertyRegistration::class).submitContactType(FreeholderContactType.EMAIL)
            // TODO PDJB-1966: update this usage when the real freeholder email page is added
            assertPageIs(page, FreeholderEmailFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1967: update this usage when the real freeholder address pages are added
            assertPageIs(page, FreeholderAddressFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1968: update this usage when the real freeholders list page is added
            assertPageIs(page, FreeholdersListFormPagePropertyRegistration::class).submit()
            // TODO PDJB-1970: update this usage when the real remove freeholder page is added
            assertPageIs(page, RemoveFreeholderFormPagePropertyRegistration::class).submit()
            // TODO PDJB-2008: update this usage when the real superior landlords pages are added
            val superiorLandlordsPage = assertPageIs(page, SuperiorLandlordsFormPagePropertyRegistration::class)
            superiorLandlordsPage.submit()
            val propertyManagerPage = assertPageIs(page, PropertyManagerFormPagePropertyRegistration::class)
            propertyManagerPage.submitNo()
            assertPageIs(page, CorrespondenceEmailFormPagePropertyRegistration::class)
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
