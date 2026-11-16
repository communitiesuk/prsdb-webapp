package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.ASSOCIATED_PARTIES
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.database.repository.FreeholderRepository
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ConfirmRemoveFreeholderFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderContactTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderEmailFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.FreeholderNameFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholderProvideDetailsFormPagePropertyUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.FreeholdersListFormPagePropertyUpdate
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.services.AddressService

class PropertyDetailsUpdateFreeholdersJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var freeholderRepository: FreeholderRepository

    @Autowired
    private lateinit var addressService: AddressService

    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @Test
    fun `landlord can add a freeholder without removing the existing last freeholder`(page: Page) {
        featureFlagManager.enableFeature(ASSOCIATED_PARTIES)
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        var freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyUpdate::class, urlArguments)
        assertEquals(0, freeholdersListPage.removeActionCount)

        freeholdersListPage.addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPagePropertyUpdate::class, urlArguments).submitNo()

        freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyUpdate::class, urlArguments)
        assertEquals(2, freeholdersListPage.removeActionCount)
        freeholdersListPage.submit()

        val updatedPropertyDetailsPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        updatedPropertyDetailsPage.tabs.goToPeopleResponsible()
        assertEquals(2, page.locator("#people-responsible .govuk-summary-card:has-text('Unknown freeholder')").count())
    }

    @Test
    fun `abandoning a freeholder update does not persist a newly added freeholder`() {
        featureFlagManager.enableFeature(ASSOCIATED_PARTIES)
        val initialFreeholderCount = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        propertyDetailsPage.page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        val freeholdersListPage = assertPageIs(propertyDetailsPage.page, FreeholdersListFormPagePropertyUpdate::class, urlArguments)
        freeholdersListPage.addAnotherButton.click()
        assertPageIs(propertyDetailsPage.page, FreeholderProvideDetailsFormPagePropertyUpdate::class, urlArguments).submitNo()

        navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)

        assertEquals(initialFreeholderCount, freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size)
    }

    @Test
    fun `editing a saved freeholder updates the existing record without adding a record`(page: Page) {
        featureFlagManager.enableFeature(ASSOCIATED_PARTIES)
        val originalFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        originalFreeholder.name = "Existing Freeholder"
        originalFreeholder.email = "existing@example.com"
        originalFreeholder.address =
            addressService.findOrCreateAddress(
                AddressDataModel.fromManualAddressData(
                    addressLineOne = "1 Example Street",
                    addressLineTwo = "Example Court",
                    townOrCity = "Example City",
                    county = "Exampleshire",
                    postcode = "EX1 1AB",
                ),
            )
        freeholderRepository.save(originalFreeholder)
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        val freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyUpdate::class, urlArguments)
        assertEquals(1, page.getByText("1 Example Street").count())
        assertEquals(1, page.getByText("Example Court").count())
        freeholdersListPage.getSummaryCard("Existing Freeholder").getAction("Change").link.clickAndWait()
        assertPageIs(page, FreeholderProvideDetailsFormPagePropertyUpdate::class, urlArguments).submitYes()
        val namePage = FreeholderNameFormPagePropertyRegistration(page)
        assertEquals("Existing Freeholder", namePage.form.nameInput.locator.inputValue())
        namePage.submitName("Updated Freeholder")
        val contactTypePage = FreeholderContactTypeFormPagePropertyRegistration(page)
        assertEquals(FreeholderContactType.EMAIL.name, contactTypePage.form.radios.selectedValue)
        contactTypePage.submitContactType(FreeholderContactType.EMAIL)
        val emailPage = FreeholderEmailFormPagePropertyRegistration(page)
        assertEquals("existing@example.com", emailPage.form.emailInput.locator.inputValue())
        emailPage.submitEmail("updated@example.com")
        assertPageIs(page, FreeholdersListFormPagePropertyUpdate::class, urlArguments).submit()

        val savedFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        assertEquals(originalFreeholder.id, savedFreeholder.id)
        assertEquals("Updated Freeholder", savedFreeholder.name)
        assertEquals("updated@example.com", savedFreeholder.email)
        assertNull(savedFreeholder.address)
    }

    @Test
    fun `persisted freeholder can be removed while the existing freeholder remains`(page: Page) {
        featureFlagManager.enableFeature(ASSOCIATED_PARTIES)
        val existingFreeholder = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).single()
        freeholderRepository.save(
            Freeholder(
                propertyOwnership = existingFreeholder.propertyOwnership,
                name = "Freeholder to remove",
                email = "remove@example.com",
            )
        )
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        var freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyUpdate::class, urlArguments)
        freeholdersListPage.getSummaryCard("Freeholder to remove").getAction("Remove").link.clickAndWait()
        ConfirmRemoveFreeholderFormPagePropertyRegistration(page).submitYes()

        freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyUpdate::class, urlArguments)
        assertEquals(0, freeholdersListPage.removeActionCount)
        freeholdersListPage.submit()

        val remainingFreeholders = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId)
        assertEquals(1, remainingFreeholders.size)
        assertEquals(listOf(existingFreeholder.id), remainingFreeholders.map { it.id })
    }

    @Test
    fun `blank freeholder name returns the registration validation error in the update journey`(page: Page) {
        featureFlagManager.enableFeature(ASSOCIATED_PARTIES)
        val initialFreeholderCount = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size
        val propertyDetailsPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        propertyDetailsPage.tabs.goToPeopleResponsible()
        page
            .getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Add, change or remove freeholders"))
            .click()

        assertPageIs(page, FreeholdersListFormPagePropertyUpdate::class, urlArguments).addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPagePropertyUpdate::class, urlArguments).submitYes()
        val namePage = FreeholderNameFormPagePropertyRegistration(page)
        namePage.submitName("")

        assertThat(namePage.form.getErrorMessage()).containsText("Enter a name")
        assertEquals(initialFreeholderCount, freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId).size)
    }

    @Test
    fun `adding editing and removing freeholders in one session reconciles saved records`(page: Page) {
        featureFlagManager.enableFeature(ASSOCIATED_PARTIES)
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

        var freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyUpdate::class, urlArguments)
        freeholdersListPage.getSummaryCard("Freeholder A").getAction("Change").link.clickAndWait()
        assertPageIs(page, FreeholderProvideDetailsFormPagePropertyUpdate::class, urlArguments).submitYes()
        FreeholderNameFormPagePropertyRegistration(page).submitName("Freeholder A")
        FreeholderContactTypeFormPagePropertyRegistration(page).submitContactType(FreeholderContactType.EMAIL)
        FreeholderEmailFormPagePropertyRegistration(page).submitEmail("updated.a@example.com")

        freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyUpdate::class, urlArguments)
        freeholdersListPage.getSummaryCard("Freeholder B").getAction("Remove").link.clickAndWait()
        ConfirmRemoveFreeholderFormPagePropertyRegistration(page).submitYes()
        freeholdersListPage = assertPageIs(page, FreeholdersListFormPagePropertyUpdate::class, urlArguments)
        freeholdersListPage.addAnotherButton.click()
        assertPageIs(page, FreeholderProvideDetailsFormPagePropertyUpdate::class, urlArguments).submitNo()
        assertPageIs(page, FreeholdersListFormPagePropertyUpdate::class, urlArguments).submit()

        val savedFreeholders = freeholderRepository.findAllByPropertyOwnership_Id(propertyOwnershipId)
        assertEquals(2, savedFreeholders.size)
        assertEquals(listOf(freeholderA.id), savedFreeholders.filter { it.name == "Freeholder A" }.map { it.id })
        assertEquals("updated.a@example.com", savedFreeholders.single { it.id == freeholderA.id }.email)
        assertFalse(savedFreeholders.any { it.id == freeholderB.id })
        assertEquals(1, savedFreeholders.count { !it.detailsKnown() })
    }
}
