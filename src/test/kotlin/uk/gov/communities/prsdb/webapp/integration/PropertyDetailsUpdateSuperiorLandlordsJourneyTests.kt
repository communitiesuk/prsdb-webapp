package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.constants.PEOPLE_RESPONSIBLE_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.AnyoneInOwnershipChainFormPageSuperiorLandlordsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.ConfirmRemoveSuperiorLandlordFormPageSuperiorLandlordsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.SuperiorLandlordProvideDetailsFormPageSuperiorLandlordsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.SuperiorLandlordsListFormPageSuperiorLandlordsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.UpdateSuperiorLandlordsCheckYourAnswersPageSuperiorLandlordsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordContactTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SuperiorLandlordNameFormPagePropertyRegistration

class PropertyDetailsUpdateSuperiorLandlordsJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    // TODO PDJB-2016: Remove ASSOCIATED_PARTIES feature flag setup once the flag is removed.
    @BeforeEach
    fun enableAssociatedPartiesFeature() {
        featureFlagManager.enableFeature(ASSOCIATED_PARTIES)
    }

    private val propertyWithSuperiorLandlordsId = 57L
    private val propertyWithoutSuperiorLandlordsId = 1L
    private val urlArgumentsWithSuperiorLandlords = mapOf("propertyOwnershipId" to propertyWithSuperiorLandlordsId.toString())
    private val urlArgumentsWithoutSuperiorLandlords = mapOf("propertyOwnershipId" to propertyWithoutSuperiorLandlordsId.toString())

    @Test
    fun `the list is pre-populated with existing superior landlords and the landlord can add one`(page: Page) {
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyWithSuperiorLandlordsId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        propertyDetailsPage.superiorLandlordUpdateLink.clickAndWait()

        var superiorLandlordsListPage =
            assertPageIs(page, SuperiorLandlordsListFormPageSuperiorLandlordsUpdate::class, urlArgumentsWithSuperiorLandlords)
        assertThat(superiorLandlordsListPage.heading).containsText("You’ve added 3 people in the ownership chain")
        assertThat(superiorLandlordsListPage.getSummaryCard("Sam Superior")).containsText("superior.landlord@example.com")
        assertThat(superiorLandlordsListPage.form.submitButton).hasAttribute("data-plausible-event", "Transaction")
        superiorLandlordsListPage.addAnotherButton.click()
        assertPageIs(page, SuperiorLandlordProvideDetailsFormPageSuperiorLandlordsUpdate::class, urlArgumentsWithSuperiorLandlords)
            .submitYes()
        SuperiorLandlordNameFormPagePropertyRegistration(page).submitName("New Superior")
        SuperiorLandlordContactTypeFormPagePropertyRegistration(page).submitContactType(SuperiorLandlordContactType.EMAIL)
        SuperiorLandlordEmailFormPagePropertyRegistration(page).submitEmail("new.superior@example.com")
        superiorLandlordsListPage =
            assertPageIs(page, SuperiorLandlordsListFormPageSuperiorLandlordsUpdate::class, urlArgumentsWithSuperiorLandlords)
        assertThat(superiorLandlordsListPage.heading).containsText("You’ve added 4 people in the ownership chain")
        superiorLandlordsListPage.submit()

        val updatedPropertyDetailsPage =
            assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArgumentsWithSuperiorLandlords)
        assertEquals(PEOPLE_RESPONSIBLE_FRAGMENT, updatedPropertyDetailsPage.tabs.activeTabPanelId)
        val superiorLandlordCards = updatedPropertyDetailsPage.superiorLandlordCards
        assertEquals(4, superiorLandlordCards.size)
        assertThat(superiorLandlordCards.single { it.title.getText() == "New Superior" })
            .containsText("new.superior@example.com")
    }

    @Test
    fun `removing every superior landlord returns to the ownership chain question and answering no removes them all`(page: Page) {
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyWithSuperiorLandlordsId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        propertyDetailsPage.superiorLandlordUpdateLink.clickAndWait()

        repeat(3) {
            assertPageIs(page, SuperiorLandlordsListFormPageSuperiorLandlordsUpdate::class, urlArgumentsWithSuperiorLandlords)
                .getSummaryCard(0)
                .getAction("Remove")
                .link
                .clickAndWait()
            assertPageIs(page, ConfirmRemoveSuperiorLandlordFormPageSuperiorLandlordsUpdate::class, urlArgumentsWithSuperiorLandlords)
                .submitWantsToProceed()
        }

        assertPageIs(page, AnyoneInOwnershipChainFormPageSuperiorLandlordsUpdate::class, urlArgumentsWithSuperiorLandlords).submitNo()

        val checkYourAnswersPage =
            assertPageIs(page, UpdateSuperiorLandlordsCheckYourAnswersPageSuperiorLandlordsUpdate::class, urlArgumentsWithSuperiorLandlords)
        assertThat(checkYourAnswersPage.summaryList.anyoneInOwnershipChainRow.value).hasText("No")
        checkYourAnswersPage.confirm()

        val updatedPropertyDetailsPage =
            assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArgumentsWithSuperiorLandlords)
        assertEquals(PEOPLE_RESPONSIBLE_FRAGMENT, updatedPropertyDetailsPage.tabs.activeTabPanelId)
        assertEquals(0, updatedPropertyDetailsPage.superiorLandlordCards.size)
        assertThat(updatedPropertyDetailsPage.noSuperiorLandlordsSummaryList.anyoneInOwnershipChainRow.value).hasText("No")
    }

    @Test
    fun `a property without superior landlords starts at the ownership chain question and can add one`(page: Page) {
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyWithoutSuperiorLandlordsId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        propertyDetailsPage.noSuperiorLandlordsSummaryList.anyoneInOwnershipChainRow.clickFirstActionLinkAndWait()

        assertPageIs(page, AnyoneInOwnershipChainFormPageSuperiorLandlordsUpdate::class, urlArgumentsWithoutSuperiorLandlords)
            .submitYes()
        assertPageIs(page, SuperiorLandlordProvideDetailsFormPageSuperiorLandlordsUpdate::class, urlArgumentsWithoutSuperiorLandlords)
            .submitNo()
        assertPageIs(page, SuperiorLandlordsListFormPageSuperiorLandlordsUpdate::class, urlArgumentsWithoutSuperiorLandlords).submit()

        val updatedPropertyDetailsPage =
            assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArgumentsWithoutSuperiorLandlords)
        assertEquals(PEOPLE_RESPONSIBLE_FRAGMENT, updatedPropertyDetailsPage.tabs.activeTabPanelId)
        assertEquals(1, updatedPropertyDetailsPage.superiorLandlordCards.size)
    }

    @Test
    fun `the ownership chain question back link returns to the people responsible tab`(page: Page) {
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyWithoutSuperiorLandlordsId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        propertyDetailsPage.noSuperiorLandlordsSummaryList.anyoneInOwnershipChainRow.clickFirstActionLinkAndWait()

        assertPageIs(page, AnyoneInOwnershipChainFormPageSuperiorLandlordsUpdate::class, urlArgumentsWithoutSuperiorLandlords)
            .backLink
            .clickAndWait()

        val returnedPropertyDetailsPage =
            assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArgumentsWithoutSuperiorLandlords)
        assertEquals(PEOPLE_RESPONSIBLE_FRAGMENT, returnedPropertyDetailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `the change link on the check your answers page returns to the ownership chain question`(page: Page) {
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyWithoutSuperiorLandlordsId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        propertyDetailsPage.noSuperiorLandlordsSummaryList.anyoneInOwnershipChainRow.clickFirstActionLinkAndWait()

        assertPageIs(page, AnyoneInOwnershipChainFormPageSuperiorLandlordsUpdate::class, urlArgumentsWithoutSuperiorLandlords)
            .submitNo()
        assertPageIs(page, UpdateSuperiorLandlordsCheckYourAnswersPageSuperiorLandlordsUpdate::class, urlArgumentsWithoutSuperiorLandlords)
            .clickChangeAndWait()

        assertPageIs(page, AnyoneInOwnershipChainFormPageSuperiorLandlordsUpdate::class, urlArgumentsWithoutSuperiorLandlords)
    }
}
