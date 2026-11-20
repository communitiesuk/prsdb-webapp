package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.PEOPLE_RESPONSIBLE_FRAGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.database.repository.FreeholderRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CorrespondenceEmailCyaPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CorrespondenceEmailFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholderLookupAddressFormPageFreeholdersUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholderManualAddressFormPageFreeholdersUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholderProvideDetailsFormPageFreeholdersUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholderSelectAddressFormPageFreeholdersUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholdersListFormPageFreeholdersUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ConfirmRemoveFreeholderFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderContactTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderNameFormPagePropertyRegistration

class PropertyDetailsUpdateFreeholdersJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    // TODO PDJB-2016: Remove ASSOCIATED_PARTIES feature flag setup once the flag is removed.
    @BeforeEach
    fun enableAssociatedPartiesFeature() {
        featureFlagManager.enableFeature(ASSOCIATED_PARTIES)
    }

    @Autowired
    private lateinit var freeholderRepository: FreeholderRepository

    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @Test
    fun `landlord can add email, postal-address, and unknown freeholders in the non-joint update journey`(page: Page) {
        val originalFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        var freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        assertThat(freeholdersListPage.heading).containsText("You’ve added 1 freeholder")
        assertThat(freeholdersListPage.form.submitButton).hasAttribute("data-plausible-event", "Transaction")
        freeholdersListPage.addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitYes()
        FreeholderNameFormPagePropertyRegistration(page).submitName("New Freeholder")
        FreeholderContactTypeFormPagePropertyRegistration(page).submitContactType(FreeholderContactType.EMAIL)
        FreeholderEmailFormPagePropertyRegistration(page).submitEmail("new.freeholder@example.com")
        freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        freeholdersListPage.addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitYes()
        FreeholderNameFormPagePropertyRegistration(page).submitName("Postal Address Freeholder")
        FreeholderContactTypeFormPagePropertyRegistration(page).submitContactType(FreeholderContactType.POSTAL_ADDRESS)
        val lookupAddressPage = assertPageIs(page, FreeholderLookupAddressFormPageFreeholdersUpdate::class, urlArguments)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")
        val selectAddressPage = assertPageIs(page, FreeholderSelectAddressFormPageFreeholdersUpdate::class, urlArguments)
        selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")
        freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        freeholdersListPage.addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitNo()
        freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        assertThat(freeholdersListPage.heading).containsText("You’ve added 4 freeholders")
        freeholdersListPage.submit()

        val updatedPropertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(PEOPLE_RESPONSIBLE_FRAGMENT, updatedPropertyDetailsPage.tabs.activeTabPanelId)
        assertThat(updatedPropertyDetailsPage.freeholderSection.locator("h3")).hasText("Freeholders")
        assertThat(updatedPropertyDetailsPage.freeholderUpdateLink.locator)
            .hasText("Add, change or remove freeholders")
        val freeholderCards = updatedPropertyDetailsPage.freeholderCards
        assertEquals(4, freeholderCards.size)
        assertThat(freeholderCards.single { it.title.getText() == "New Freeholder" })
            .containsText("new.freeholder@example.com")
        assertThat(freeholderCards.single { it.title.getText() == "Postal Address Freeholder" }.bodyText)
            .hasText(
                "A postal address was provided for this freeholder. We’ve used this to tell them that they’re named on this registration.",
            )
        assertThat(freeholderCards.first { it.title.getText() == "Unknown freeholder" }.bodyText)
            .hasText(
                "You told us there was another freeholder, but you cannot currently provide their details. " +
                    "Return and add these details when you can.",
            )
        val savedFreeholders = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId)
        assertEquals(4, savedFreeholders.size)
        assertEquals(1, savedFreeholders.count { it.id == originalFreeholder.id })
        val addedFreeholders = savedFreeholders.filter { it.id != originalFreeholder.id }
        assertEquals("new.freeholder@example.com", addedFreeholders.single { it.name == "New Freeholder" }.email)
        assertNull(addedFreeholders.single { it.name == "Postal Address Freeholder" }.email)
        assertEquals(1, addedFreeholders.count { !it.detailsKnown() })
    }

    @Test
    fun `landlord can add email, postal-address, and unknown freeholders in the joint update journey`(page: Page) {
        val propertyOwnership = propertyOwnershipRepository.findById(propertyOwnershipId).orElseThrow()
        propertyOwnership.ownershipType = OwnershipType.SHARE_OF_JOINT_TENANTS
        propertyOwnershipRepository.save(propertyOwnership)
        val originalFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove joint freeholders"))
            .click()

        var freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        assertThat(freeholdersListPage.heading).containsText("You’ve added 1 joint freeholder")
        freeholdersListPage.addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitYes()
        FreeholderNameFormPagePropertyRegistration(page).submitName("New Joint Freeholder")
        FreeholderContactTypeFormPagePropertyRegistration(page).submitContactType(FreeholderContactType.EMAIL)
        FreeholderEmailFormPagePropertyRegistration(page).submitEmail("new.joint.freeholder@example.com")
        freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        freeholdersListPage.addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitYes()
        FreeholderNameFormPagePropertyRegistration(page).submitName("Joint Postal Address Freeholder")
        FreeholderContactTypeFormPagePropertyRegistration(page).submitContactType(FreeholderContactType.POSTAL_ADDRESS)
        val lookupAddressPage = assertPageIs(page, FreeholderLookupAddressFormPageFreeholdersUpdate::class, urlArguments)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")
        val selectAddressPage = assertPageIs(page, FreeholderSelectAddressFormPageFreeholdersUpdate::class, urlArguments)
        selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")
        freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        freeholdersListPage.addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitNo()
        freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        assertThat(freeholdersListPage.heading).containsText("You’ve added 4 joint freeholders")
        freeholdersListPage.submit()

        val updatedPropertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(PEOPLE_RESPONSIBLE_FRAGMENT, updatedPropertyDetailsPage.tabs.activeTabPanelId)
        assertThat(updatedPropertyDetailsPage.freeholderSection.locator("h3")).hasText("Joint freeholders")
        assertThat(updatedPropertyDetailsPage.freeholderUpdateLink.locator)
            .hasText("Add, change or remove joint freeholders")
        val freeholderCards = updatedPropertyDetailsPage.freeholderCards
        assertEquals(4, freeholderCards.size)
        assertThat(freeholderCards.single { it.title.getText() == "New Joint Freeholder" })
            .containsText("new.joint.freeholder@example.com")
        assertThat(freeholderCards.single { it.title.getText() == "Joint Postal Address Freeholder" }.bodyText)
            .hasText(
                "A postal address was provided for this freeholder. We’ve used this to tell them that they’re named on this registration.",
            )
        assertThat(freeholderCards.first { it.title.getText() == "Unknown freeholder" }.bodyText)
            .hasText(
                "You told us there was another freeholder, but you cannot currently provide their details. " +
                    "Return and add these details when you can.",
            )
        val savedFreeholders = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId)
        assertEquals(4, savedFreeholders.size)
        assertEquals(1, savedFreeholders.count { it.id == originalFreeholder.id })
        val addedFreeholders = savedFreeholders.filter { it.id != originalFreeholder.id }
        assertEquals(
            "new.joint.freeholder@example.com",
            addedFreeholders.single { it.name == "New Joint Freeholder" }.email,
        )
        assertNull(addedFreeholders.single { it.name == "Joint Postal Address Freeholder" }.email)
        assertEquals(1, addedFreeholders.count { !it.detailsKnown() })
    }

    @Test
    fun `invalid email returns the registration format error in the update journey`(page: Page) {
        val initialFreeholderCount = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments).addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitYes()
        FreeholderNameFormPagePropertyRegistration(page).submitName("Email Validation Freeholder")
        FreeholderContactTypeFormPagePropertyRegistration(page).submitContactType(FreeholderContactType.EMAIL)
        val emailPage = FreeholderEmailFormPagePropertyRegistration(page)
        emailPage.submitEmail("not-an-email")

        assertThat(emailPage.form.getErrorMessage())
            .containsText("Enter an email address in the correct format, like name@example.com")
        assertEquals(initialFreeholderCount, freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size)
    }

    @Test
    fun `blank postcode returns the required error on freeholder address lookup during an update`(page: Page) {
        val initialFreeholderCount = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments).addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitYes()
        FreeholderNameFormPagePropertyRegistration(page).submitName("Postcode Validation Freeholder")
        FreeholderContactTypeFormPagePropertyRegistration(page).submitContactType(FreeholderContactType.POSTAL_ADDRESS)
        val lookupAddressPage = assertPageIs(page, FreeholderLookupAddressFormPageFreeholdersUpdate::class, urlArguments)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("", "1")

        assertThat(lookupAddressPage.form.getErrorMessage()).containsText("Enter a postcode")
        assertEquals(initialFreeholderCount, freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size)
    }

    @Test
    fun `changing a known freeholder to details unknown clears their details without replacing the record`(page: Page) {
        val originalFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        originalFreeholder.name = "Existing Freeholder"
        originalFreeholder.email = "existing@example.com"
        freeholderRepository.save(originalFreeholder)
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        var freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        freeholdersListPage.getSummaryCard("Existing Freeholder").getAction("Change").link.clickAndWait()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitNo()
        freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        assertThat(freeholdersListPage.getSummaryCard("Unknown freeholder").title).hasText("Unknown freeholder")
        freeholdersListPage.submit()

        val updatedPropertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        val unknownFreeholderCard = updatedPropertyDetailsPage.freeholderCards.single()
        assertEquals("Unknown freeholder", unknownFreeholderCard.title.getText())
        assertThat(unknownFreeholderCard.bodyText).hasText(
            "You told us there was another freeholder, but you cannot currently provide their details. " +
                "Return and add these details when you can.",
        )
        val savedFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        assertEquals(originalFreeholder.id, savedFreeholder.id)
        assertNull(savedFreeholder.name)
        assertNull(savedFreeholder.email)
        assertFalse(savedFreeholder.detailsKnown())
    }

    @Test
    fun `freeholder update list shows the submission warning`(page: Page) {
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        val freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        assertThat(freeholdersListPage.warning).isVisible()
    }

    @Test
    fun `back from the freeholder update list returns to the people responsible tab`(page: Page) {
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments).backLink.clickAndWait()

        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(PEOPLE_RESPONSIBLE_FRAGMENT, propertyDetailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `a postal address can be provided for a freeholder during an update`(page: Page) {
        val originalFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        originalFreeholder.name = "Existing Freeholder"
        originalFreeholder.email = null
        freeholderRepository.save(originalFreeholder)
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
            .getSummaryCard("Existing Freeholder")
            .getAction("Change")
            .link
            .clickAndWait()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitYes()
        FreeholderNameFormPagePropertyRegistration(page).form.submit()
        val contactTypePage = FreeholderContactTypeFormPagePropertyRegistration(page)
        contactTypePage.submitContactType(FreeholderContactType.POSTAL_ADDRESS)
        val lookupAddressPage = assertPageIs(page, FreeholderLookupAddressFormPageFreeholdersUpdate::class, urlArguments)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")
        val selectAddressPage = assertPageIs(page, FreeholderSelectAddressFormPageFreeholdersUpdate::class, urlArguments)
        selectAddressPage.selectAddressAndSubmit("1 PRSDB Square, EG1 2AA")
        assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments).submit()

        val updatedPropertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        val freeholderCard = updatedPropertyDetailsPage.freeholderCards.single()
        assertEquals("Existing Freeholder", freeholderCard.title.getText())
        assertThat(freeholderCard.bodyText).hasText(
            "A postal address was provided for this freeholder. We’ve used this to tell them that they’re named on this registration.",
        )
        val savedFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        assertEquals(originalFreeholder.id, savedFreeholder.id)
        assertNull(savedFreeholder.email)
    }

    @Test
    fun `a freeholder can be given a manual postal address during an update`(page: Page) {
        val originalFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        originalFreeholder.name = "Existing Freeholder"
        originalFreeholder.email = null
        freeholderRepository.save(originalFreeholder)
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
            .getSummaryCard("Existing Freeholder")
            .getAction("Change")
            .link
            .clickAndWait()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitYes()
        FreeholderNameFormPagePropertyRegistration(page).form.submit()
        FreeholderContactTypeFormPagePropertyRegistration(page).submitContactType(FreeholderContactType.POSTAL_ADDRESS)
        val lookupAddressPage = assertPageIs(page, FreeholderLookupAddressFormPageFreeholdersUpdate::class, urlArguments)
        lookupAddressPage.submitPostcodeAndBuildingNameOrNumber("EG1 2AA", "1")
        val selectAddressPage = assertPageIs(page, FreeholderSelectAddressFormPageFreeholdersUpdate::class, urlArguments)
        selectAddressPage.selectAddressAndSubmit(MANUAL_ADDRESS_CHOSEN)

        val manualAddressPage = assertPageIs(page, FreeholderManualAddressFormPageFreeholdersUpdate::class, urlArguments)
        manualAddressPage.submitAddress(
            addressLineOne = "2 Updated Street",
            addressLineTwo = "Example Court",
            townOrCity = "Example City",
            county = "Exampleshire",
            postcode = "EG1 2AA",
        )
        assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments).submit()

        val updatedPropertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        val freeholderCard = updatedPropertyDetailsPage.freeholderCards.single()
        assertEquals("Existing Freeholder", freeholderCard.title.getText())
        assertThat(freeholderCard.bodyText).hasText(
            "A postal address was provided for this freeholder. We’ve used this to tell them that they’re named on this registration.",
        )
        val savedFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        assertEquals(originalFreeholder.id, savedFreeholder.id)
        assertNull(savedFreeholder.email)
    }

    @Test
    fun `resuming an abandoned freeholder update after another property update discards earlier progress`(page: Page) {
        val initialFreeholderCount = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size
        val landlordAccountEmail = "alex.surname@example.com"
        assertNotEquals(
            landlordAccountEmail,
            propertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnershipId)!!.correspondenceEmail,
        )
        var propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()
        assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments).addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitNo()
        var freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        assertEquals(initialFreeholderCount + 1, freeholdersListPage.removeActionCount)

        propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.propertyDetailsSummaryList.contactEmailAddressRow.clickFirstActionLinkAndWait()
        assertPageIs(page, CorrespondenceEmailFormPagePropertyDetailsUpdate::class, urlArguments).submitAccountEmail()
        assertPageIs(page, CorrespondenceEmailCyaPagePropertyDetailsUpdate::class, urlArguments).confirm()
        propertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()
        freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        assertEquals(initialFreeholderCount, page.locator(".govuk-summary-card").count())
        freeholdersListPage.submit()
        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(initialFreeholderCount, freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size)
    }

    @Test
    fun `landlord can add a freeholder without removing the existing last freeholder`(page: Page) {
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        var freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        assertEquals(0, freeholdersListPage.removeActionCount)

        freeholdersListPage.addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitNo()

        freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        assertEquals(2, freeholdersListPage.removeActionCount)
        freeholdersListPage.submit()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertThat(page.locator("#people-responsible")).isVisible()
        assertEquals(2, page.locator("#people-responsible .govuk-summary-card:has-text('Unknown freeholder')").count())
    }

    @Test
    fun `abandoning a freeholder update does not persist a newly added freeholder`() {
        val initialFreeholderCount = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        propertyDetailsPage.page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        val freeholdersListPage = assertPageIs(propertyDetailsPage.page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        freeholdersListPage.addAnotherButton.click()
        assertPageIs(propertyDetailsPage.page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitNo()

        navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)

        assertEquals(initialFreeholderCount, freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size)
    }

    @Test
    fun `editing a saved freeholder updates the existing record without adding a record`(page: Page) {
        val originalFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        originalFreeholder.name = "Existing Freeholder"
        originalFreeholder.email = "existing@example.com"
        freeholderRepository.save(originalFreeholder)
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        val freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        freeholdersListPage.getSummaryCard("Existing Freeholder").getAction("Change").link.clickAndWait()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitYes()
        val namePage = FreeholderNameFormPagePropertyRegistration(page)
        assertEquals("Existing Freeholder", namePage.form.nameInput.locator.inputValue())
        namePage.submitName("Updated Freeholder")
        val contactTypePage = FreeholderContactTypeFormPagePropertyRegistration(page)
        assertEquals(FreeholderContactType.EMAIL.name, contactTypePage.form.radios.selectedValue)
        contactTypePage.submitContactType(FreeholderContactType.EMAIL)
        val emailPage = FreeholderEmailFormPagePropertyRegistration(page)
        assertEquals("existing@example.com", emailPage.form.emailInput.locator.inputValue())
        emailPage.submitEmail("updated@example.com")
        assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments).submit()

        val updatedPropertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        val freeholderCard = updatedPropertyDetailsPage.freeholderCards.single()
        assertEquals("Updated Freeholder", freeholderCard.title.getText())
        assertThat(freeholderCard).containsText("updated@example.com")
        val savedFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        assertEquals(originalFreeholder.id, savedFreeholder.id)
        assertEquals("Updated Freeholder", savedFreeholder.name)
        assertEquals("updated@example.com", savedFreeholder.email)
    }

    @Test
    fun `persisted freeholder can be removed while the existing freeholder remains`(page: Page) {
        val existingFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        freeholderRepository.save(
            Freeholder(
                propertyOwnership = existingFreeholder.propertyOwnership,
                name = "Freeholder to remove",
                email = "remove@example.com",
            ),
        )
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        var freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        freeholdersListPage.getSummaryCard("Freeholder to remove").getAction("Remove").link.clickAndWait()
        ConfirmRemoveFreeholderFormPagePropertyRegistration(page).submitYes()

        freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        assertEquals(0, freeholdersListPage.removeActionCount)
        freeholdersListPage.submit()

        val remainingFreeholders = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId)
        assertEquals(1, remainingFreeholders.size)
        assertEquals(listOf(existingFreeholder.id), remainingFreeholders.map { it.id })
    }

    @Test
    fun `blank freeholder name returns the registration validation error in the update journey`(page: Page) {
        val initialFreeholderCount = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments).addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitYes()
        val namePage = FreeholderNameFormPagePropertyRegistration(page)
        namePage.submitName("")

        assertThat(namePage.form.getErrorMessage()).containsText("Enter a name")
        assertEquals(initialFreeholderCount, freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size)
    }

    @Test
    fun `adding editing and removing freeholders in one session reconciles saved records`(page: Page) {
        val freeholderA = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        freeholderA.name = "Freeholder A"
        freeholderA.email = "a@example.com"
        freeholderRepository.save(freeholderA)
        val freeholderB =
            freeholderRepository.save(
                Freeholder(
                    propertyOwnership = freeholderA.propertyOwnership,
                    name = "Freeholder B",
                    email = "b@example.com",
                ),
            )

        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        var freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        freeholdersListPage.getSummaryCard("Freeholder A").getAction("Change").link.clickAndWait()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitYes()
        FreeholderNameFormPagePropertyRegistration(page).submitName("Freeholder A")
        FreeholderContactTypeFormPagePropertyRegistration(page).submitContactType(FreeholderContactType.EMAIL)
        FreeholderEmailFormPagePropertyRegistration(page).submitEmail("updated.a@example.com")

        freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        freeholdersListPage.getSummaryCard("Freeholder B").getAction("Remove").link.clickAndWait()
        ConfirmRemoveFreeholderFormPagePropertyRegistration(page).submitYes()
        freeholdersListPage = assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments)
        freeholdersListPage.addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPageFreeholdersUpdate::class, urlArguments).submitNo()
        assertPageIs(page, FreeholdersListFormPageFreeholdersUpdate::class, urlArguments).submit()

        val savedFreeholders = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId)
        assertEquals(2, savedFreeholders.size)
        assertEquals(listOf(freeholderA.id), savedFreeholders.filter { it.name == "Freeholder A" }.map { it.id })
        assertEquals("updated.a@example.com", savedFreeholders.single { it.id == freeholderA.id }.email)
        assertFalse(savedFreeholders.any { it.id == freeholderB.id })
        assertEquals(1, savedFreeholders.count { !it.detailsKnown() })
    }
}
