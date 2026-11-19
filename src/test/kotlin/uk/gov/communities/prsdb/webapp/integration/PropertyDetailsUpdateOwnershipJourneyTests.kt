package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.constants.PEOPLE_RESPONSIBLE_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipController
import uk.gov.communities.prsdb.webapp.database.repository.FreeholderRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.SuperiorLandlordRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.AnyoneInOwnershipChainFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholdTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholderContactTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholderEmailFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholderNameFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholderProvideDetailsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholdersInfoFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholdersListFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OwnershipCheckAnswersPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OwnershipKindFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.OwnsPropertyFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.SuperiorLandlordContactTypeFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.SuperiorLandlordEmailFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.SuperiorLandlordNameFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.SuperiorLandlordProvideDetailsFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.SuperiorLandlordsListFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownership.InitialiseOwnershipUpdateStep
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PropertyDetailsUpdateOwnershipJourneyTests :
    IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Autowired
    lateinit var freeholderRepository: FreeholderRepository

    @Autowired
    lateinit var superiorLandlordRepository: SuperiorLandlordRepository

    // Seeded as LEASEHOLD_ONLY with one "no details known" freeholder and no superior landlords
    private val leaseholdPropertyId = 1L

    // Seeded as SOLE_FREEHOLD with no freeholders or superior landlords
    private val soleFreeholdPropertyId = 33L

    // Seeded as LEASEHOLD_ONLY with two named freeholders, one "no details known" freeholder and one named superior landlord
    private val propertyWithPartiesId = 55L

    @Test
    fun `a landlord can change to a share of joint tenants, keeping the existing freeholder and adding new parties`(page: Page) {
        val urlArguments = urlArgumentsFor(leaseholdPropertyId)
        startJourney(leaseholdPropertyId)

        assertPageIs(page, OwnsPropertyFormPagePropertyDetailsUpdate::class, urlArguments).submitOwnsProperty(true)
        assertPageIs(page, OwnershipKindFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitOwnershipKind(OwnershipKind.FREEHOLD_AND_LEASEHOLD)
        assertPageIs(page, FreeholdTypeFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitFreeholdType(FreeholdType.JOINT_TENANTS)

        // The info page is always shown, in the version for the new ownership type
        val freeholdersInfoPage = assertPageIs(page, FreeholdersInfoFormPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(freeholdersInfoPage.heading).containsText("Add joint freeholders")
        freeholdersInfoPage.submit()

        // The existing freeholder has been loaded into the list
        var freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(freeholdersListPage.getSummaryCard(0).title).containsText("Unknown freeholder")
        freeholdersListPage.addAnotherButton.click()

        assertPageIs(page, FreeholderProvideDetailsFormPagePropertyDetailsUpdate::class, urlArguments).submitYes()
        assertPageIs(page, FreeholderNameFormPagePropertyDetailsUpdate::class, urlArguments).submitName("New Freeholder")
        assertPageIs(page, FreeholderContactTypeFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitContactType(FreeholderContactType.EMAIL)
        assertPageIs(page, FreeholderEmailFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitEmail("new.freeholder@example.com")

        freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(freeholdersListPage.getSummaryCard(1).title).containsText("New Freeholder")
        freeholdersListPage.submit()

        val anyoneInOwnershipChainPage =
            assertPageIs(page, AnyoneInOwnershipChainFormPagePropertyDetailsUpdate::class, urlArguments)
        assertFalse(anyoneInOwnershipChainPage.form.radios.isAnyOptionSelected)
        anyoneInOwnershipChainPage.submitYes()

        assertPageIs(page, SuperiorLandlordProvideDetailsFormPagePropertyDetailsUpdate::class, urlArguments).submitYes()
        assertPageIs(page, SuperiorLandlordNameFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitName("New Superior Landlord")
        assertPageIs(page, SuperiorLandlordContactTypeFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitContactType(SuperiorLandlordContactType.EMAIL)
        assertPageIs(page, SuperiorLandlordEmailFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitEmail("new.superior.landlord@example.com")
        assertPageIs(page, SuperiorLandlordsListFormPagePropertyDetailsUpdate::class, urlArguments).submit()

        val checkAnswersPage = assertPageIs(page, OwnershipCheckAnswersPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(checkAnswersPage.summaryList.ownershipTypeRow.value).containsText("Freehold and leasehold")
        assertThat(checkAnswersPage.summaryList.freeholdTypeRow.value).containsText("Joint tenants")
        assertThat(checkAnswersPage.changeJointFreeholdersLink).isVisible()
        assertThat(checkAnswersPage.changeSuperiorLandlordsLink).isVisible()
        assertThat(checkAnswersPage.insetTexts.first()).containsText("We’ll notify anyone you add.")
        assertThat(checkAnswersPage.summaryCards).hasCount(3)
        checkAnswersPage.submit()

        val propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertContains(page.url(), "#$PEOPLE_RESPONSIBLE_FRAGMENT")
        val ownershipSummaryList = propertyDetailsPage.peopleResponsibleOwnershipSummaryList
        assertThat(ownershipSummaryList.ownsPropertyRow.value).containsText("Yes")
        assertThat(ownershipSummaryList.ownershipTypeRow.value).containsText("Freehold and leasehold")
        assertThat(ownershipSummaryList.freeholdTypeRow.value).containsText("Joint tenants")
        assertThat(propertyDetailsPage.freeholderSection.locator("h3")).hasText("Joint freeholders")
        assertEquals(
            setOf("Unknown freeholder", "New Freeholder"),
            propertyDetailsPage.freeholderCards.map { it.title.getText() }.toSet(),
        )

        assertEquals(OwnershipType.SHARE_OF_JOINT_TENANTS, ownershipTypeOf(leaseholdPropertyId))
        val freeholders = freeholderRepository.findAllByPropertyOwnership_Id(leaseholdPropertyId)
        assertEquals(
            setOf(null to null, "New Freeholder" to "new.freeholder@example.com"),
            freeholders.map { it.name to it.email }.toSet(),
        )
        val superiorLandlords = superiorLandlordRepository.findAllByPropertyOwnership_Id(leaseholdPropertyId)
        assertEquals(
            listOf("New Superior Landlord" to "new.superior.landlord@example.com"),
            superiorLandlords.map { it.name to it.email },
        )
    }

    @Test
    fun `a landlord who does not own the property is asked about freeholders and the ownership chain`(page: Page) {
        val urlArguments = urlArgumentsFor(soleFreeholdPropertyId)
        startJourney(soleFreeholdPropertyId)

        assertPageIs(page, OwnsPropertyFormPagePropertyDetailsUpdate::class, urlArguments).submitOwnsProperty(false)

        val freeholdersInfoPage = assertPageIs(page, FreeholdersInfoFormPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(freeholdersInfoPage.heading).containsText("Add freeholders")
        freeholdersInfoPage.submit()
        assertPageIs(page, FreeholderProvideDetailsFormPagePropertyDetailsUpdate::class, urlArguments).submitNo()
        assertPageIs(page, FreeholdersListFormPagePropertyDetailsUpdate::class, urlArguments).submit()

        assertPageIs(page, AnyoneInOwnershipChainFormPagePropertyDetailsUpdate::class, urlArguments).submitNo()

        val checkAnswersPage = assertPageIs(page, OwnershipCheckAnswersPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(checkAnswersPage.summaryList.ownsPropertyRow.value).containsText("No")
        assertThat(checkAnswersPage.changeFreeholdersLink).isVisible()
        assertThat(checkAnswersPage.insetTexts.first()).containsText("We’ll notify anyone you add.")
        assertThat(checkAnswersPage.summaryCards).hasCount(1)
        assertThat(checkAnswersPage.summaryList.anyoneInOwnershipChainRow.value).containsText("No")
        checkAnswersPage.submit()

        val propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertThat(propertyDetailsPage.peopleResponsibleOwnershipSummaryList.ownsPropertyRow.value).containsText("No")
        assertEquals(OwnershipType.NONE, ownershipTypeOf(soleFreeholdPropertyId))
        assertEquals(1, freeholderRepository.findAllByPropertyOwnership_Id(soleFreeholdPropertyId).size)
        assertEquals(0, superiorLandlordRepository.findAllByPropertyOwnership_Id(soleFreeholdPropertyId).size)
    }

    @Test
    fun `choosing leasehold only skips the freehold type question`(page: Page) {
        val urlArguments = urlArgumentsFor(soleFreeholdPropertyId)
        startJourney(soleFreeholdPropertyId)

        assertPageIs(page, OwnsPropertyFormPagePropertyDetailsUpdate::class, urlArguments).submitOwnsProperty(true)
        assertPageIs(page, OwnershipKindFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitOwnershipKind(OwnershipKind.LEASEHOLD_ONLY)

        val freeholdersInfoPage = assertPageIs(page, FreeholdersInfoFormPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(freeholdersInfoPage.heading).containsText("Add freeholders")
    }

    @Test
    fun `changing to sole freehold skips the freeholder and ownership chain questions and clears existing parties`(page: Page) {
        val urlArguments = urlArgumentsFor(propertyWithPartiesId)
        startJourney(propertyWithPartiesId)

        assertPageIs(page, OwnsPropertyFormPagePropertyDetailsUpdate::class, urlArguments).submitOwnsProperty(true)
        assertPageIs(page, OwnershipKindFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitOwnershipKind(OwnershipKind.FREEHOLD_ONLY)
        assertPageIs(page, FreeholdTypeFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitFreeholdType(FreeholdType.SOLE_OWNERSHIP)

        val checkAnswersPage = assertPageIs(page, OwnershipCheckAnswersPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(checkAnswersPage.summaryCards).hasCount(0)
        assertThat(checkAnswersPage.changeFreeholdersLink.locator).hasCount(0)
        assertThat(checkAnswersPage.changeJointFreeholdersLink.locator).hasCount(0)
        checkAnswersPage.submit()

        val propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertThat(propertyDetailsPage.freeholderSection).hasCount(0)
        assertEquals(OwnershipType.SOLE_FREEHOLD, ownershipTypeOf(propertyWithPartiesId))
        assertEquals(0, freeholderRepository.findAllByPropertyOwnership_Id(propertyWithPartiesId).size)
        assertEquals(0, superiorLandlordRepository.findAllByPropertyOwnership_Id(propertyWithPartiesId).size)
    }

    @Test
    fun `changing a property with no freeholders to joint tenants shows the joint freeholders info page`(page: Page) {
        val urlArguments = urlArgumentsFor(soleFreeholdPropertyId)
        startJourney(soleFreeholdPropertyId)

        assertPageIs(page, OwnsPropertyFormPagePropertyDetailsUpdate::class, urlArguments).submitOwnsProperty(true)
        assertPageIs(page, OwnershipKindFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitOwnershipKind(OwnershipKind.FREEHOLD_ONLY)
        assertPageIs(page, FreeholdTypeFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitFreeholdType(FreeholdType.JOINT_TENANTS)

        val freeholdersInfoPage = assertPageIs(page, FreeholdersInfoFormPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(freeholdersInfoPage.heading).containsText("Add joint freeholders")
        freeholdersInfoPage.submit()

        // With no existing freeholders, the first one is added straight away
        assertPageIs(page, FreeholderProvideDetailsFormPagePropertyDetailsUpdate::class, urlArguments).submitNo()
        assertPageIs(page, FreeholdersListFormPagePropertyDetailsUpdate::class, urlArguments).submit()
        assertPageIs(page, OwnershipCheckAnswersPagePropertyDetailsUpdate::class, urlArguments).submit()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(OwnershipType.JOINT_TENANTS, ownershipTypeOf(soleFreeholdPropertyId))
        assertEquals(1, freeholderRepository.findAllByPropertyOwnership_Id(soleFreeholdPropertyId).size)
    }

    @Test
    fun `answering no to the ownership chain question clears the existing superior landlords`(page: Page) {
        val urlArguments = urlArgumentsFor(propertyWithPartiesId)
        val checkAnswersPage = completeJourneyToCheckAnswersAsShareOfJointTenantsWithNoOwnershipChain(page, urlArguments)

        assertThat(checkAnswersPage.summaryList.anyoneInOwnershipChainRow.value).containsText("No")
        checkAnswersPage.submit()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(OwnershipType.SHARE_OF_JOINT_TENANTS, ownershipTypeOf(propertyWithPartiesId))
        val freeholders = freeholderRepository.findAllByPropertyOwnership_Id(propertyWithPartiesId)
        assertEquals(listOf("Alex Non-Joint", "Taylor Non-Joint", null), freeholders.map { it.name }.sortedWith(nullsLast()))
        assertEquals(0, superiorLandlordRepository.findAllByPropertyOwnership_Id(propertyWithPartiesId).size)
    }

    @Test
    fun `changing freeholders from check answers goes to the list, whose back link returns to check answers`(page: Page) {
        val urlArguments = urlArgumentsFor(propertyWithPartiesId)
        var checkAnswersPage = completeJourneyToCheckAnswersAsShareOfJointTenantsWithNoOwnershipChain(page, urlArguments)

        checkAnswersPage.changeJointFreeholdersLink.clickAndWait()
        val freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyDetailsUpdate::class, urlArguments)
        val cardTitles = (0..2).map { freeholdersListPage.getSummaryCard(it).title.getText() }
        assertContains(cardTitles, "Taylor Non-Joint")

        freeholdersListPage.backLink.clickAndWait()
        checkAnswersPage = assertPageIs(page, OwnershipCheckAnswersPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(checkAnswersPage.summaryList.freeholdTypeRow.value).containsText("Joint tenants")
    }

    @Test
    fun `the postal address note is rendered as HTML on the check answers and freeholders list pages`(page: Page) {
        val urlArguments = urlArgumentsFor(propertyWithPartiesId)
        val checkAnswersPage = completeJourneyToCheckAnswersAsShareOfJointTenantsWithNoOwnershipChain(page, urlArguments)
        assertThat(postalAddressNoteEmphasis(page, "Alex Non-Joint")).hasText("postal address")

        checkAnswersPage.changeJointFreeholdersLink.clickAndWait()
        assertPageIs(page, FreeholdersListFormPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(postalAddressNoteEmphasis(page, "Alex Non-Joint")).hasText("postal address")
    }

    // TODO PDJB-2016: delete this test along with the ASSOCIATED_PARTIES flag
    @Test
    fun `the ownership update journey is not available when the associated parties flag is disabled`() {
        featureFlagManager.disableFeature(ASSOCIATED_PARTIES)

        val response =
            navigator.navigate(
                UpdateOwnershipController.getUpdateOwnershipRoute(leaseholdPropertyId) +
                    "/${InitialiseOwnershipUpdateStep.ROUTE_SEGMENT}",
            )

        assertEquals(404, response?.status())
    }

    private fun urlArgumentsFor(propertyOwnershipId: Long) = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    private fun postalAddressNoteEmphasis(
        page: Page,
        cardTitle: String,
    ) = page
        .locator(".govuk-summary-card", Page.LocatorOptions().setHasText(cardTitle))
        .locator(".govuk-summary-card__content p strong")

    private fun startJourney(propertyOwnershipId: Long) {
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        propertyDetailsPage.peopleResponsibleOwnershipSummaryList.ownsPropertyRow.clickFirstActionLinkAndWait()
    }

    private fun completeJourneyToCheckAnswersAsShareOfJointTenantsWithNoOwnershipChain(
        page: Page,
        urlArguments: Map<String, String>,
    ): OwnershipCheckAnswersPagePropertyDetailsUpdate {
        startJourney(propertyWithPartiesId)

        assertPageIs(page, OwnsPropertyFormPagePropertyDetailsUpdate::class, urlArguments).submitOwnsProperty(true)
        assertPageIs(page, OwnershipKindFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitOwnershipKind(OwnershipKind.FREEHOLD_AND_LEASEHOLD)
        assertPageIs(page, FreeholdTypeFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitFreeholdType(FreeholdType.JOINT_TENANTS)
        assertPageIs(page, FreeholdersInfoFormPagePropertyDetailsUpdate::class, urlArguments).submit()
        assertPageIs(page, FreeholdersListFormPagePropertyDetailsUpdate::class, urlArguments).submit()

        // The ownership chain question is not prefilled, even though the property has superior landlords
        val anyoneInOwnershipChainPage =
            assertPageIs(page, AnyoneInOwnershipChainFormPagePropertyDetailsUpdate::class, urlArguments)
        assertFalse(anyoneInOwnershipChainPage.form.radios.isAnyOptionSelected)
        anyoneInOwnershipChainPage.submitNo()

        return assertPageIs(page, OwnershipCheckAnswersPagePropertyDetailsUpdate::class, urlArguments)
    }

    private fun ownershipTypeOf(propertyOwnershipId: Long) =
        propertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnershipId)!!.ownershipType
}
