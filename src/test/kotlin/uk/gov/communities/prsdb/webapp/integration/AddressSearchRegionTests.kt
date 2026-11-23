package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SelectAddressFormPageUpdateLandlordDetails
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CorrespondenceLookupAddressFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CorrespondenceSelectAddressFormPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.SelectAddressFormPagePropertyRegistration

class AddressSearchRegionTests : IntegrationTestWithImmutableData("data-local.sql") {
    private val postcode = "UK1 1AA"
    private val englishWithCouncil = "1 English Council Road, UK1 1AA"
    private val englishWithoutCouncil = "2 English No Council Road, UK1 1AA"
    private val welsh = "3 Welsh Road, UK1 1AA"
    private val scottish = "4 Scottish Road, UK1 1AA"

    @Test
    fun `Property registration address search only lists addresses with a local council`(page: Page) {
        navigator.goToPropertyRegistrationLookupAddressPage().submitPostcodeAndBuildingNameOrNumber(postcode, "1")
        assertPageIs(page, SelectAddressFormPagePropertyRegistration::class)

        assertListedAddresses(
            page,
            listed = listOf(englishWithCouncil),
            notListed = listOf(englishWithoutCouncil, welsh, scottish),
        )
    }

    @Test
    fun `Correspondence address search only lists addresses in England or Wales`(page: Page) {
        val propertyOwnershipId = 1L
        val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())
        navigator
            .goToPropertyDetailsLandlordView(propertyOwnershipId)
            .propertyDetailsSummaryList.contactAddressRow
            .clickFirstActionLinkAndWait()
        assertPageIs(page, CorrespondenceLookupAddressFormPagePropertyDetailsUpdate::class, urlArguments)
            .submitPostcodeAndBuildingNameOrNumber(postcode, "1")
        assertPageIs(page, CorrespondenceSelectAddressFormPagePropertyDetailsUpdate::class, urlArguments)

        assertListedAddresses(
            page,
            listed = listOf(englishWithCouncil, englishWithoutCouncil, welsh),
            notListed = listOf(scottish),
        )
    }

    @Test
    fun `Landlord address search lists addresses across the UK`(page: Page) {
        navigator.goToUpdateLandlordDetailsUpdateLookupAddressPage().submitPostcodeAndBuildingNameOrNumber(postcode, "1")
        assertPageIs(page, SelectAddressFormPageUpdateLandlordDetails::class)

        assertListedAddresses(
            page,
            listed = listOf(englishWithCouncil, englishWithoutCouncil, welsh, scottish),
            notListed = emptyList(),
        )
    }

    private fun assertListedAddresses(
        page: Page,
        listed: List<String>,
        notListed: List<String>,
    ) {
        listed.forEach { assertThat(page.locator("input[value='$it']")).hasCount(1) }
        notListed.forEach { assertThat(page.locator("input[value='$it']")).hasCount(0) }
    }
}
