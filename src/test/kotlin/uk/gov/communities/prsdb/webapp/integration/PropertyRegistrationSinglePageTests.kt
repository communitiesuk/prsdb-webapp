package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.communities.prsdb.webapp.constants.GOV_LEGAL_ADVICE_URL
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.AlreadyRegisteredFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckGasSafetyAnswersFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckJointLandlordsFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.EpcExemptionFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HasJointLandlordsFormBasePagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HmoAdditionalLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.HmoMandatoryLicenceFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.InviteAnotherJointLandlordFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LicensingTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.LookupAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.ManualAddressFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.MeesExemptionFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NoAddressFoundFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.NumberOfPeopleFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OccupancyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OwnershipTypeFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RemoveJointLandlordAreYouSureFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

class PropertyRegistrationSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Nested
    inner class TaskListStep {
        @BeforeEach
        fun enableJointLandlordsFlag() {
            featureFlagManager.enableFeature(JOINT_LANDLORDS)
        }

        @Test
        fun `Completing preceding steps will show a task as not started and completed steps as complete`(page: Page) {
            navigator.skipToPropertyRegistrationHasJointLandlordsPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            assert(taskListPage.taskHasStatus("Enter the property address", "Complete"))
            assert(taskListPage.taskHasStatus("Select the type of property", "Complete"))
            assert(taskListPage.taskHasStatus("Tell us how you own the property", "Complete"))
            assert(taskListPage.taskHasStatus("Add information about any property licensing", "Complete"))
            assert(taskListPage.taskHasStatus("Add tenancy and rental information for the property", "Complete"))
            assert(taskListPage.taskHasStatus("Add information about any additional landlords", "Not started"))
        }

        @Test
        fun `Completing first step of a task will show a task as in progress and completed steps as complete`(page: Page) {
            navigator.skipToPropertyRegistrationRentFrequencyPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            assert(taskListPage.taskHasStatus("Enter the property address", "Complete"))
            assert(taskListPage.taskHasStatus("Select the type of property", "Complete"))
            assert(taskListPage.taskHasStatus("Tell us how you own the property", "Complete"))
            assert(taskListPage.taskHasStatus("Add information about any property licensing", "Complete"))
            assert(taskListPage.taskHasStatus("Add tenancy and rental information for the property", "In progress"))
            assert(taskListPage.taskHasStatus("Add information about any additional landlords", "Cannot start"))
        }
    }

    @Nested
    inner class TaskListStepWithFeatureFlagDisabled {
        @Test
        fun `the joint landlords task is not shown in the task list when the feature flag is disabled`(page: Page) {
            featureFlagManager.disableFeature(JOINT_LANDLORDS)
            navigator.skipToPropertyRegistrationRentFrequencyPage()
            val taskListPage = navigator.goToPropertyRegistrationTaskList()
            BaseComponent.assertThat(taskListPage.getRegisterTask("Add information about any additional landlords")).isHidden()
        }
    }

    @Nested
    inner class LookupAddressAndNoAddressFoundSteps {
        @Test
        fun `Submitting with empty data fields returns an error`(page: Page) {
            val lookupAddressPage = navigator.goToPropertyRegistrationLookupAddressPage()
            lookupAddressPage.clearForm() // There may be form answers in the journey state
            lookupAddressPage.form.submit()
            assertThat(lookupAddressPage.form.getErrorMessage("postcode")).containsText("Enter a postcode")
            assertThat(lookupAddressPage.form.getErrorMessage("houseNameOrNumber")).containsText("Enter a house name or number")
        }

        @Test
        fun `If no English addresses are found, user can search again or enter address manually via the No Address Found step`(page: Page) {
            // Lookup address finds no English results
            val houseNumber = "NOT A HOUSE NUMBER"
            val postcode = "NOT A POSTCODE"
            val lookupAddressPage = navigator.goToPropertyRegistrationLookupAddressPage()
            lookupAddressPage.submitPostcodeAndBuildingNameOrNumber(postcode, houseNumber)

            // redirect to noAddressFoundPage
            val noAddressFoundPage = BasePage.assertPageIs(page, NoAddressFoundFormPagePropertyRegistration::class)
            BaseComponent
                .assertThat(noAddressFoundPage.heading)
                .containsText("No matching address in England found for $postcode and $houseNumber")

            // Search Again
            noAddressFoundPage.searchAgain.clickAndWait()
            val lookupAddressPageAgain = BasePage.assertPageIs(page, LookupAddressFormPagePropertyRegistration::class)
            lookupAddressPageAgain.submitPostcodeAndBuildingNameOrNumber(postcode, houseNumber)

            // Submit no address found page
            val noAddressFoundPageAgain = BasePage.assertPageIs(page, NoAddressFoundFormPagePropertyRegistration::class)
            noAddressFoundPageAgain.form.submit()
            BasePage.assertPageIs(page, ManualAddressFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class SelectAddressStep {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val selectAddressPage = navigator.skipToPropertyRegistrationSelectAddressPage()
            selectAddressPage.form.submit()
            assertThat(selectAddressPage.form.getErrorMessage()).containsText("Select an address")
        }

        @Test
        fun `Clicking Search Again navigates to the previous step`(page: Page) {
            val selectAddressPage = navigator.skipToPropertyRegistrationSelectAddressPage()
            selectAddressPage.searchAgain.clickAndWait()
            BasePage.assertPageIs(page, LookupAddressFormPagePropertyRegistration::class)
        }

        @Test
        fun `Selecting an already-registered address navigates to the AlreadyRegistered step`(page: Page) {
            val alreadyRegisteredAddress = AddressDataModel("1 Example Road", uprn = 1123456)
            val selectAddressPage = navigator.skipToPropertyRegistrationSelectAddressPage(listOf(alreadyRegisteredAddress))
            selectAddressPage.selectAddressAndSubmit(alreadyRegisteredAddress.singleLineAddress)
            BasePage.assertPageIs(page, AlreadyRegisteredFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class ManualAddressEntryStep {
        @Test
        fun `Submitting empty data fields returns errors`(page: Page) {
            val manualAddressPage = navigator.skipToPropertyRegistrationManualAddressPage()
            manualAddressPage.submitAddress()
            assertThat(manualAddressPage.form.getErrorMessage("addressLineOne"))
                .containsText("Enter the first line of an address, typically the building and street")
            assertThat(manualAddressPage.form.getErrorMessage("townOrCity")).containsText("Enter town or city")
            assertThat(manualAddressPage.form.getErrorMessage("postcode")).containsText("Enter postcode")
        }
    }

    @Nested
    inner class SelectLocalCouncilStep {
        @Test
        fun `Submitting without selecting an LA return an error`(page: Page) {
            val selectLocalCouncilPage = navigator.skipToPropertyRegistrationSelectLocalCouncilPage()
            selectLocalCouncilPage.form.submit()
            assertThat(selectLocalCouncilPage.form.getErrorMessage("localCouncilId"))
                .containsText("Select a local council to continue")
        }
    }

    @Nested
    inner class PropertyTypeStep {
        @Test
        fun `Submitting with no propertyType selected returns an error`(page: Page) {
            val propertyTypePage = navigator.skipToPropertyRegistrationPropertyTypePage()
            propertyTypePage.form.submit()
            assertThat(propertyTypePage.form.getErrorMessage()).containsText("Select the type of property")
        }

        @Test
        fun `Submitting with the Other propertyType selected but an empty customPropertyType field returns an error`(page: Page) {
            val propertyTypePage = navigator.skipToPropertyRegistrationPropertyTypePage()
            propertyTypePage.submitCustomPropertyType("")
            assertThat(propertyTypePage.form.getErrorMessage()).containsText("Enter the property type")
        }
    }

    @Nested
    inner class OwnershipTypeStep {
        @Test
        fun `Submitting with no ownershipType selected returns an error`(page: Page) {
            val ownershipTypePage = navigator.skipToPropertyRegistrationOwnershipTypePage()
            ownershipTypePage.form.submit()
            assertThat(ownershipTypePage.form.getErrorMessage()).containsText("Select the ownership type")
        }
    }

    @Nested
    inner class LicensingTypeStep {
        @Test
        fun `Submitting with no licensingType selected returns an error`(page: Page) {
            val licensingTypePage = navigator.skipToPropertyRegistrationLicensingTypePage()
            licensingTypePage.form.submit()
            assertThat(licensingTypePage.form.getErrorMessage()).containsText("Select the type of licensing for the property")
        }

        @Test
        fun `Submitting with an HMO mandatory licence redirects to the next step`(page: Page) {
            val licensingTypePage = navigator.skipToPropertyRegistrationLicensingTypePage()
            licensingTypePage.submitLicensingType(LicensingType.HMO_MANDATORY_LICENCE)
            val licenseNumberPage = BasePage.assertPageIs(page, HmoMandatoryLicenceFormPagePropertyRegistration::class)
            BaseComponent
                .assertThat(licenseNumberPage.form.sectionHeader)
                .containsText("Section 1 of 5 \u2014 Register your property details")
        }

        @Test
        fun `Submitting with an HMO additional licence redirects to the next step`(page: Page) {
            val licensingTypePage = navigator.skipToPropertyRegistrationLicensingTypePage()
            licensingTypePage.submitLicensingType(LicensingType.HMO_ADDITIONAL_LICENCE)
            val licenseNumberPage = BasePage.assertPageIs(page, HmoAdditionalLicenceFormPagePropertyRegistration::class)
            BaseComponent
                .assertThat(licenseNumberPage.form.sectionHeader)
                .containsText("Section 1 of 5 \u2014 Register your property details")
        }
    }

    @Nested
    inner class SelectiveLicenceStep {
        @Test
        fun `Submitting with no licence number returns an error`(page: Page) {
            val selectiveLicencePage = navigator.skipToPropertyRegistrationSelectiveLicencePage()
            selectiveLicencePage.form.submit()
            assertThat(selectiveLicencePage.form.getErrorMessage()).containsText("Enter the selective licence number")
        }

        @Test
        fun `Submitting with a very long licence number returns an error`(page: Page) {
            val selectiveLicencePage = navigator.skipToPropertyRegistrationSelectiveLicencePage()
            val aVeryLongString =
                "This string is very long, so long that it is not feasible that it is a real licence number " +
                    "- therefore if it is submitted there will in fact be an error rather than a successful submission." +
                    " It is actually quite difficult for a string to be long enough to trigger this error, because the" +
                    " maximum length has been selected to be permissive of id numbers we do not expect while still having " +
                    "a cap reachable with a little effort."
            selectiveLicencePage.submitLicenseNumber(aVeryLongString)
            assertThat(selectiveLicencePage.form.getErrorMessage()).containsText("The licensing number is too long")
        }
    }

    @Nested
    inner class HmoMandatoryLicenceStep {
        @Test
        fun `Submitting with a licence number redirects to the next step`(page: Page) {
            val hmoMandatoryLicencePage = navigator.skipToPropertyRegistrationHmoMandatoryLicencePage()
            hmoMandatoryLicencePage.submitLicenseNumber("licence number")
            BasePage.assertPageIs(page, OccupancyFormPagePropertyRegistration::class)
        }

        @Test
        fun `Submitting with no licence number returns an error`(page: Page) {
            val hmoMandatoryLicencePage = navigator.skipToPropertyRegistrationHmoMandatoryLicencePage()
            hmoMandatoryLicencePage.form.submit()
            assertThat(hmoMandatoryLicencePage.form.getErrorMessage()).containsText("Enter the HMO Mandatory licence number")
        }

        @Test
        fun `Submitting with a very long licence number returns an error`(page: Page) {
            val hmoMandatoryLicencePage = navigator.skipToPropertyRegistrationHmoMandatoryLicencePage()
            val aVeryLongString =
                "This string is very long, so long that it is not feasible that it is a real licence number " +
                    "- therefore if it is submitted there will in fact be an error rather than a successful submission." +
                    " It is actually quite difficult for a string to be long enough to trigger this error, because the" +
                    " maximum length has been selected to be permissive of id numbers we do not expect while still having " +
                    "a cap reachable with a little effort."
            hmoMandatoryLicencePage.submitLicenseNumber(aVeryLongString)
            assertThat(hmoMandatoryLicencePage.form.getErrorMessage()).containsText("The licensing number is too long")
        }
    }

    @Nested
    inner class HmoAdditionalLicenceStep {
        @Test
        fun `Submitting with a licence number redirects to the next step`(page: Page) {
            val hmoAdditionalLicencePage = navigator.skipToPropertyRegistrationHmoAdditionalLicencePage()
            hmoAdditionalLicencePage.submitLicenseNumber("licence number")
            BasePage.assertPageIs(page, OccupancyFormPagePropertyRegistration::class)
        }

        @Test
        fun `Submitting with no licence number returns an error`(page: Page) {
            val hmoAdditionalLicencePage = navigator.skipToPropertyRegistrationHmoAdditionalLicencePage()
            hmoAdditionalLicencePage.form.submit()
            assertThat(hmoAdditionalLicencePage.form.getErrorMessage()).containsText("Enter the HMO additional licence number")
        }

        @Test
        fun `Submitting with a very long licence number returns an error`(page: Page) {
            val hmoAdditionalLicencePage = navigator.skipToPropertyRegistrationHmoAdditionalLicencePage()
            val aVeryLongString =
                "This string is very long, so long that it is not feasible that it is a real licence number " +
                    "- therefore if it is submitted there will in fact be an error rather than a successful submission." +
                    " It is actually quite difficult for a string to be long enough to trigger this error, because the" +
                    " maximum length has been selected to be permissive of id numbers we do not expect while still having " +
                    "a cap reachable with a little effort."
            hmoAdditionalLicencePage.submitLicenseNumber(aVeryLongString)
            assertThat(hmoAdditionalLicencePage.form.getErrorMessage()).containsText("The licensing number is too long")
        }
    }

    @Nested
    inner class OccupancyStep {
        @Test
        fun `Submitting with no occupancy option selected returns an error`(page: Page) {
            val occupancyPage = navigator.skipToPropertyRegistrationOccupancyPage()
            occupancyPage.form.submit()
            assertThat(occupancyPage.form.getErrorMessage()).containsText("Select whether the property is occupied")
        }
    }

    @Nested
    inner class NumberOfHouseholdsStep {
        @Test
        fun `Submitting with a blank numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.skipToPropertyRegistrationHouseholdsPage()
            householdsPage.form.submit()
            assertThat(householdsPage.form.getErrorMessage()).containsText("Enter how many separate households, like 1 or 2")
        }

        @Test
        fun `Submitting with a non-numerical value in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.skipToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds("not-a-number")
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Enter how many separate households, like 1 or 2")
        }

        @Test
        fun `Submitting with a non-integer number in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.skipToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds("2.3")
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Enter how many separate households, like 1 or 2")
        }

        @Test
        fun `Submitting with a negative integer in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.skipToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds(-2)
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Enter how many separate households, like 1 or 2")
        }

        @Test
        fun `Submitting with a zero integer in the numberOfHouseholds field returns an error`(page: Page) {
            val householdsPage = navigator.skipToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds(0)
            assertThat(householdsPage.form.getErrorMessage())
                .containsText("Enter how many separate households, like 1 or 2")
        }
    }

    @Nested
    inner class NumberOfPeopleStep {
        @Test
        fun `Submitting with a blank numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.skipToPropertyRegistrationPeoplePage()
            peoplePage.form.submit()
            assertThat(peoplePage.form.getErrorMessage()).containsText("Enter how many people, like 2 or 5")
        }

        @Test
        fun `Submitting with a non-numerical value in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.skipToPropertyRegistrationPeoplePage()
            peoplePage.submitNumOfPeople("not-a-number")
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Enter how many people, like 2 or 5")
        }

        @Test
        fun `Submitting with a non-integer number in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.skipToPropertyRegistrationPeoplePage()
            peoplePage.submitNumOfPeople("2.3")
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Enter how many people, like 2 or 5")
        }

        @Test
        fun `Submitting with a negative integer in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.skipToPropertyRegistrationPeoplePage()
            peoplePage.submitNumOfPeople("-2")
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Enter how many people, like 2 or 5")
        }

        @Test
        fun `Submitting with a zero integer in the numberOfPeople field returns an error`(page: Page) {
            val peoplePage = navigator.skipToPropertyRegistrationPeoplePage()
            peoplePage.submitNumOfPeople(0)
            assertThat(peoplePage.form.getErrorMessage())
                .containsText("Enter how many people, like 2 or 5")
        }

        @Test
        fun `Submitting with an integer in the numberOfPeople field that is less than the numberOfHouseholds returns an error`(
            page: Page,
        ) {
            val householdsPage = navigator.skipToPropertyRegistrationHouseholdsPage()
            householdsPage.submitNumberOfHouseholds(3)
            val peoplePage = BasePage.assertPageIs(page, NumberOfPeopleFormPagePropertyRegistration::class)
            peoplePage.submitNumOfPeople(2)
            assertThat(peoplePage.form.getErrorMessage())
                .containsText(
                    "The number of people in the property must be the same as or higher than the number of households in the property",
                )
        }
    }

    @Nested
    inner class NumberOfBedroomsStep {
        val numberOfBedroomsErrorMessage = "Enter the number of bedrooms, like 3 or 8"

        @Test
        fun `Submitting with a blank numberOfBedrooms field returns an error`(page: Page) {
            val bedroomsPage = navigator.skipToPropertyRegistrationBedroomsPage()
            bedroomsPage.form.submit()
            assertThat(bedroomsPage.form.getErrorMessage()).containsText(numberOfBedroomsErrorMessage)
        }

        @Test
        fun `Submitting with a non-numerical value in the numberOfBedrooms field returns an error`(page: Page) {
            val bedroomsPage = navigator.skipToPropertyRegistrationBedroomsPage()
            bedroomsPage.submitNumOfBedrooms("not-a-number")
            assertThat(bedroomsPage.form.getErrorMessage()).containsText(numberOfBedroomsErrorMessage)
        }

        @Test
        fun `Submitting with a non-integer number in the numberOfBedrooms field returns an error`(page: Page) {
            val bedroomsPage = navigator.skipToPropertyRegistrationBedroomsPage()
            bedroomsPage.submitNumOfBedrooms("2.3")
            assertThat(bedroomsPage.form.getErrorMessage()).containsText(numberOfBedroomsErrorMessage)
        }

        @Test
        fun `Submitting with a negative integer in the numberOfBedrooms field returns an error`(page: Page) {
            val bedroomsPage = navigator.skipToPropertyRegistrationBedroomsPage()
            bedroomsPage.submitNumOfBedrooms("-2")
            assertThat(bedroomsPage.form.getErrorMessage()).containsText(numberOfBedroomsErrorMessage)
        }

        @Test
        fun `Submitting with a zero integer in the numberOfBedrooms field returns an error`(page: Page) {
            val bedroomsPage = navigator.skipToPropertyRegistrationBedroomsPage()
            bedroomsPage.submitNumOfBedrooms(0)
            assertThat(bedroomsPage.form.getErrorMessage()).containsText(numberOfBedroomsErrorMessage)
        }
    }

    @Nested
    inner class RentIncludesBillsStep {
        @Test
        fun `Submitting with no rent included option selected returns an error`(page: Page) {
            val rentIncludesBillsPage = navigator.skipToPropertyRegistrationRentIncludesBillsPage()
            rentIncludesBillsPage.form.submit()
            assertThat(rentIncludesBillsPage.form.getErrorMessage()).containsText("Select whether the rent includes bills")
        }
    }

    @Nested
    inner class BillsIncludedStep {
        @Test
        fun `Submitting with no bills included selected returns an error`(page: Page) {
            val billsIncludedPage = navigator.skipToPropertyRegistrationBillsIncludedPage()
            billsIncludedPage.form.submit()
            assertThat(billsIncludedPage.form.getErrorMessage()).containsText("Select what you include in the rent")
        }

        @Test
        fun `Submitting with something else selected but no text entered returns an error`(page: Page) {
            val billsIncludedPage = navigator.skipToPropertyRegistrationBillsIncludedPage()
            billsIncludedPage.selectGasElectricityWater()
            billsIncludedPage.selectSomethingElseCheckbox()
            billsIncludedPage.form.submit()
            assertThat(billsIncludedPage.form.getErrorMessage()).containsText("Enter the bills and services you include in the rent")
        }

        @Test
        fun `Submitting with a very long something else text returns an error`(page: Page) {
            val billsIncludedPage = navigator.skipToPropertyRegistrationBillsIncludedPage()
            billsIncludedPage.selectGasElectricityWater()
            billsIncludedPage.selectSomethingElseCheckbox()
            val aVeryLongString =
                "This string is very long, so long that it is not feasible that it is a real description " +
                    "- therefore if it is submitted there will in fact be an error rather than a successful submission." +
                    " It is actually quite difficult for a string to be long enough to trigger this error, because the" +
                    " maximum length has been selected to be permissive of descriptions we do not expect while still having " +
                    "a cap reachable with a little effort."
            billsIncludedPage.fillCustomBills(aVeryLongString)
            billsIncludedPage.form.submit()
            assertThat(billsIncludedPage.form.getErrorMessage("customBillsIncluded"))
                .containsText("The description of other bills and services must be 200 characters or fewer")
        }
    }

    @Nested
    inner class FurnishedStatusStep {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val furnishedStatusPage = navigator.skipToPropertyRegistrationFurnishedStatusPage()
            furnishedStatusPage.form.submit()
            assertThat(
                furnishedStatusPage.form.getErrorMessage(),
            ).containsText("Select whether the property is furnished, partly furnished or unfurnished")
        }
    }

    @Nested
    inner class RentFrequencyStep {
        @Test
        fun `Submitting with no rentFrequency selected returns an error`(page: Page) {
            val rentFrequencyPage = navigator.skipToPropertyRegistrationRentFrequencyPage()
            rentFrequencyPage.form.submit()
            assertThat(rentFrequencyPage.form.getErrorMessage()).containsText("Select how often you charge rent")
        }

        @Test
        fun `Submitting with other rent frequency selected but no text entered returns an error`(page: Page) {
            val rentFrequencyPage = navigator.skipToPropertyRegistrationRentFrequencyPage()
            rentFrequencyPage.selectRentFrequency(RentFrequency.OTHER)
            rentFrequencyPage.form.submit()
            assertThat(rentFrequencyPage.form.getErrorMessage()).containsText("Enter how often you charge rent")
        }
    }

    @Nested
    inner class RentAmountStep {
        @Test
        fun `Submitting no rentAmount returns an error`(page: Page) {
            val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage()
            rentAmountPage.form.submit()
            assertThat(
                rentAmountPage.form.getErrorMessage(),
            ).containsText("Rent amount must only include numbers (and a decimal point), like 600 or 193.54")
        }

        @Test
        fun `Submitting a rentAmount greater than two decimals returns an error`(page: Page) {
            val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage()
            rentAmountPage.submitRentAmount("400.123")
            assertThat(
                rentAmountPage.form.getErrorMessage(),
            ).containsText("Rent amount must only include numbers (and a decimal point), like 600 or 193.54")
        }

        @Test
        fun `Submitting a negative rentAmount returns an error`(page: Page) {
            val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage()
            rentAmountPage.submitRentAmount("-400.12")
            assertThat(
                rentAmountPage.form.getErrorMessage(),
            ).containsText("Rent amount must only include numbers (and a decimal point), like 600 or 193.54")
        }

        @Test
        fun `Submitting a non-numerical rentAmount returns an error`(page: Page) {
            val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage()
            rentAmountPage.submitRentAmount("not-a-number")
            assertThat(
                rentAmountPage.form.getErrorMessage(),
            ).containsText("Rent amount must only include numbers (and a decimal point), like 600 or 193.54")
        }

        @Nested
        inner class ConditionalContentPerRentFrequency {
            @Test
            fun `Page renders correctly for weekly rent frequency`(page: Page) {
                val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage(RentFrequency.WEEKLY)
                BaseComponent
                    .assertThat(rentAmountPage.header)
                    .containsText("What is the weekly rent?")
                BaseComponent
                    .assertThat(rentAmountPage.subheading)
                    .containsText("Weekly rent")
                BaseComponent
                    .assertThat(rentAmountPage.billsExplanationForRentFrequency)
                    .containsText("The amount you enter must be the total weekly rent agreed with the tenant.")
                BaseComponent.assertThat(rentAmountPage.rentCalculationParagraph).isHidden()
            }

            @Test
            fun `Page renders correctly for four weekly rent frequency`(page: Page) {
                val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage(RentFrequency.FOUR_WEEKLY)
                BaseComponent
                    .assertThat(rentAmountPage.header)
                    .containsText("What is the 4-weekly rent?")
                BaseComponent
                    .assertThat(rentAmountPage.subheading)
                    .containsText("4-weekly rent")
                BaseComponent
                    .assertThat(rentAmountPage.billsExplanationForRentFrequency)
                    .containsText("The amount you enter must be the total 4-weekly rent agreed with the tenant.")
                BaseComponent.assertThat(rentAmountPage.rentCalculationParagraph).isHidden()
            }

            @Test
            fun `Page renders correctly for monthly rent frequency`(page: Page) {
                val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage(RentFrequency.MONTHLY)
                BaseComponent
                    .assertThat(rentAmountPage.header)
                    .containsText("What is the monthly rent?")
                BaseComponent
                    .assertThat(rentAmountPage.subheading)
                    .containsText("Monthly rent")
                BaseComponent
                    .assertThat(rentAmountPage.billsExplanationForRentFrequency)
                    .containsText("The amount you enter must be the total monthly rent agreed with the tenant.")
                BaseComponent.assertThat(rentAmountPage.rentCalculationParagraph).isHidden()
            }

            @Test
            fun `Page renders correctly for 'other' rent frequency`(page: Page) {
                val rentAmountPage = navigator.skipToPropertyRegistrationRentAmountPage(RentFrequency.OTHER)
                BaseComponent
                    .assertThat(rentAmountPage.header)
                    .containsText("What is the monthly rent?")
                BaseComponent
                    .assertThat(rentAmountPage.subheading)
                    .containsText("Monthly rent")
                BaseComponent
                    .assertThat(rentAmountPage.billsExplanationForRentFrequency)
                    .containsText("The amount you enter must be the total monthly rent agreed with the tenant.")
                BaseComponent.assertThat(rentAmountPage.rentCalculationParagraph).isVisible()
            }
        }
    }

    @Nested
    inner class HasJointLandlordsStep {
        @BeforeEach
        fun enableJointLandlordsFlag() {
            featureFlagManager.enableFeature(JOINT_LANDLORDS)
        }

        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val hasJointLandlordsPage = navigator.skipToPropertyRegistrationHasJointLandlordsPage()
            hasJointLandlordsPage.form.submit()
            assertThat(hasJointLandlordsPage.form.getErrorMessage())
                .containsText("Select if there are any other landlords for this property")
        }

        @Test
        fun `The link renders correctly`(page: Page) {
            val hasJointLandlordsPage = navigator.skipToPropertyRegistrationHasJointLandlordsPage()
            BaseComponent.Companion.assertThat(hasJointLandlordsPage.legalAdviceLink).hasAttribute("href", GOV_LEGAL_ADVICE_URL)
            BaseComponent.Companion.assertThat(hasJointLandlordsPage.legalAdviceLink).hasAttribute("rel", "noreferrer noopener")
            BaseComponent.Companion.assertThat(hasJointLandlordsPage.legalAdviceLink).hasAttribute("target", "_blank")
        }
    }

    @Nested
    inner class ManagingJointLandlords {
        @BeforeEach
        fun enableJointLandlordsFlag() {
            featureFlagManager.enableFeature(JOINT_LANDLORDS)
        }

        @Test
        fun `Submitting remove a joint landlord with no option selected returns an error`(page: Page) {
            val inviteJointLandlordsPage = navigator.skipToPropertyRegistrationInviteJointLandlordPage()
            inviteJointLandlordsPage.submitEmail("alpha@example.com")

            val firstCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            firstCheckJointLandlordPage.summaryList.firstRow.clickNamedActionLinkAndWait("Remove")

            val removeJointLandlordPage = assertPageIs(page, RemoveJointLandlordAreYouSureFormPagePropertyRegistration::class)
            removeJointLandlordPage.form.submit()
            assertThat(removeJointLandlordPage.form.getErrorMessage())
                .containsText("Select if you want to remove this joint landlord")
        }

        @Test
        fun `Submitting remove a joint landlord with No selected returns to the check page without removing the landlord`(page: Page) {
            val inviteJointLandlordsPage = navigator.skipToPropertyRegistrationInviteJointLandlordPage()
            inviteJointLandlordsPage.submitEmail("alpha@example.com")

            val checkJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            checkJointLandlordPage.summaryList.firstRow.clickNamedActionLinkAndWait("Remove")

            val removeJointLandlordPage = assertPageIs(page, RemoveJointLandlordAreYouSureFormPagePropertyRegistration::class)
            removeJointLandlordPage.submitDoesNotWantToProceed()

            val finalCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            BaseComponent.assertThat(finalCheckJointLandlordPage.title).containsText("You’ve added 1 joint landlord")
            assertThat(finalCheckJointLandlordPage.summaryList.firstRow.value).containsText("alpha@example.com")
        }

        @Test
        fun `Clicking cancel returns to the check page regardless of selected remove answer`(page: Page) {
            val inviteJointLandlordsPage = navigator.skipToPropertyRegistrationInviteJointLandlordPage()
            inviteJointLandlordsPage.submitEmail("alpha@example.com")

            val firstCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            firstCheckJointLandlordPage.form.addAnotherButton.clickAndWait()

            val inviteAnotherJointLandlordPage = assertPageIs(page, InviteAnotherJointLandlordFormPagePropertyRegistration::class)
            inviteAnotherJointLandlordPage.submitEmail("beta@example.com")

            val secondCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            secondCheckJointLandlordPage.summaryList.firstRow.clickNamedActionLinkAndWait("Remove")

            val removeWithYesSelectedPage = assertPageIs(page, RemoveJointLandlordAreYouSureFormPagePropertyRegistration::class)
            removeWithYesSelectedPage.form.areYouSureRadios.selectValue("true")
            removeWithYesSelectedPage.cancelLink.clickAndWait()

            val checkAfterYesCancelPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            BaseComponent.assertThat(checkAfterYesCancelPage.title).containsText("You’ve added 2 joint landlords")
            assertThat(checkAfterYesCancelPage.summaryList.firstRow.value).containsText("alpha@example.com")
            assertThat(checkAfterYesCancelPage.summaryList.getRowByIndex(1).value).containsText("beta@example.com")
            checkAfterYesCancelPage.summaryList.firstRow.clickNamedActionLinkAndWait("Remove")

            val removeWithNoSelectedPage = assertPageIs(page, RemoveJointLandlordAreYouSureFormPagePropertyRegistration::class)
            removeWithNoSelectedPage.form.areYouSureRadios.selectValue("false")
            removeWithNoSelectedPage.cancelLink.clickAndWait()

            val checkAfterNoCancelPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            BaseComponent.assertThat(checkAfterNoCancelPage.title).containsText("You’ve added 2 joint landlords")
            assertThat(checkAfterNoCancelPage.summaryList.firstRow.value).containsText("alpha@example.com")
            assertThat(checkAfterNoCancelPage.summaryList.getRowByIndex(1).value).containsText("beta@example.com")
        }

        @Test
        fun `Removing joint landlords works as expected`(page: Page) {
            val inviteJointLandlordsPage = navigator.skipToPropertyRegistrationInviteJointLandlordPage()
            inviteJointLandlordsPage.submitEmail("alpha@example.com")

            val firstCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            firstCheckJointLandlordPage.form.addAnotherButton.clickAndWait()

            val inviteAnotherJointLandlordPage = assertPageIs(page, InviteAnotherJointLandlordFormPagePropertyRegistration::class)
            inviteAnotherJointLandlordPage.submitEmail("beta@example.com")

            val secondCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            assertThat(secondCheckJointLandlordPage.summaryList.firstRow.value).containsText("alpha@example.com")
            secondCheckJointLandlordPage.summaryList.firstRow.clickNamedActionLinkAndWait("Remove")

            val firstRemoveJointLandlordPage = assertPageIs(page, RemoveJointLandlordAreYouSureFormPagePropertyRegistration::class)
            firstRemoveJointLandlordPage.submitWantsToProceed()

            val finalCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            assertThat(finalCheckJointLandlordPage.summaryList.firstRow.value).containsText("beta@example.com")
            finalCheckJointLandlordPage.summaryList.firstRow.clickNamedActionLinkAndWait("Remove")

            val secondRemoveJointLandlordPage = assertPageIs(page, RemoveJointLandlordAreYouSureFormPagePropertyRegistration::class)
            secondRemoveJointLandlordPage.submitWantsToProceed()

            assertPageIs(page, HasJointLandlordsFormBasePagePropertyRegistration::class)
        }

        @Test
        fun `Editing joint landlords works as expected`(page: Page) {
            val inviteJointLandlordsPage = navigator.skipToPropertyRegistrationInviteJointLandlordPage()
            inviteJointLandlordsPage.submitEmail("alpha@example.com")

            val firstCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            firstCheckJointLandlordPage.form.addAnotherButton.clickAndWait()

            val inviteAnotherJointLandlordPage = assertPageIs(page, InviteAnotherJointLandlordFormPagePropertyRegistration::class)
            inviteAnotherJointLandlordPage.submitEmail("beta@example.com")

            val secondCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            assertThat(secondCheckJointLandlordPage.summaryList.firstRow.value).containsText("alpha@example.com")
            secondCheckJointLandlordPage.summaryList.firstRow.clickNamedActionLinkAndWait("Change")

            val firstEditJointLandlordPage = assertPageIs(page, InviteAnotherJointLandlordFormPagePropertyRegistration::class)
            BaseComponent.assertThat(firstEditJointLandlordPage.form.emailInput).hasValue("alpha@example.com")
            firstEditJointLandlordPage.submitEmail("gamma@example.com")

            val finalCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            assertThat(finalCheckJointLandlordPage.summaryList.firstRow.value).containsText("gamma@example.com")
        }

        @Test
        fun `Numbering on page and tables is correct`(page: Page) {
            val inviteJointLandlordsPage = navigator.skipToPropertyRegistrationInviteJointLandlordPage()
            inviteJointLandlordsPage.submitEmail("alpha@example.com")

            val firstCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            BaseComponent.assertThat(firstCheckJointLandlordPage.title).containsText("You’ve added 1 joint landlord")
            assertThat(firstCheckJointLandlordPage.summaryList.firstRow.key).containsText("Joint landlord 1")
            assertThat(firstCheckJointLandlordPage.summaryList.firstRow.value).containsText("alpha@example.com")
            firstCheckJointLandlordPage.form.addAnotherButton.clickAndWait()

            val inviteAnotherJointLandlordPage = assertPageIs(page, InviteAnotherJointLandlordFormPagePropertyRegistration::class)
            inviteAnotherJointLandlordPage.submitEmail("beta@example.com")

            val secondCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            BaseComponent.assertThat(secondCheckJointLandlordPage.title).containsText("You’ve added 2 joint landlords")
            assertThat(secondCheckJointLandlordPage.summaryList.getRowByIndex(1).key).containsText("Joint landlord 2")
            assertThat(secondCheckJointLandlordPage.summaryList.getRowByIndex(1).value).containsText("beta@example.com")
            secondCheckJointLandlordPage.summaryList.firstRow.clickNamedActionLinkAndWait("Remove")

            val firstRemoveJointLandlordPage = assertPageIs(page, RemoveJointLandlordAreYouSureFormPagePropertyRegistration::class)
            firstRemoveJointLandlordPage.submitWantsToProceed()

            val finalCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            BaseComponent.assertThat(finalCheckJointLandlordPage.title).containsText("You’ve added 1 joint landlord")
            assertThat(finalCheckJointLandlordPage.summaryList.firstRow.key).containsText("Joint landlord 1")
            assertThat(firstCheckJointLandlordPage.summaryList.firstRow.value).containsText("beta@example.com")
        }
    }

    @Nested
    inner class InviteJointLandlordsStep {
        @BeforeEach
        fun enableJointLandlordsFlag() {
            featureFlagManager.enableFeature(JOINT_LANDLORDS)
        }

        @Test
        fun `Submitting with no email returns an error`(page: Page) {
            val inviteJointLandlordsPage = navigator.skipToPropertyRegistrationInviteJointLandlordPage()
            inviteJointLandlordsPage.submitEmail("")
            assertThat(inviteJointLandlordsPage.form.getErrorMessage())
                .containsText("Enter an email address in the correct format, like name@example.com")
        }

        @Test
        fun `Submitting with an invalid email returns an error`(page: Page) {
            val inviteJointLandlordsPage = navigator.skipToPropertyRegistrationInviteJointLandlordPage()
            inviteJointLandlordsPage.submitEmail("not-an-email")
            assertThat(inviteJointLandlordsPage.form.getErrorMessage())
                .containsText("Enter an email address in the correct format, like name@example.com")
        }
    }

    @Nested
    inner class InviteAnotherJointLandlordsStep {
        @BeforeEach
        fun enableJointLandlordsFlag() {
            featureFlagManager.enableFeature(JOINT_LANDLORDS)
        }

        @Test
        fun `Submitting with an already invited email returns an error`(page: Page) {
            val alreadyInvitedEmail = "already@invited.com"
            val inviteJointLandlordsPage =
                navigator.skipToPropertyRegistrationInviteAnotherJointLandlordPage(mutableListOf(alreadyInvitedEmail))
            inviteJointLandlordsPage.submitEmail(alreadyInvitedEmail)
            assertThat(inviteJointLandlordsPage.form.getErrorMessage())
                .containsText("You have already invited this email address")
        }

        @Test
        fun `Submitting with an invited email in edit mode is permitted`(page: Page) {
            val alreadyInvitedEmail = "already@invited.com"

            val inviteJointLandlordsPage = navigator.skipToPropertyRegistrationInviteJointLandlordPage()
            inviteJointLandlordsPage.submitEmail(alreadyInvitedEmail)

            val checkJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            checkJointLandlordPage.summaryList.firstRow.clickNamedActionLinkAndWait("Change")

            val editJointLandlordPage = assertPageIs(page, InviteAnotherJointLandlordFormPagePropertyRegistration::class)
            BaseComponent.assertThat(editJointLandlordPage.form.emailInput).hasValue(alreadyInvitedEmail)
            inviteJointLandlordsPage.submitEmail(alreadyInvitedEmail)

            val finalCheckJointLandlordPage = assertPageIs(page, CheckJointLandlordsFormPagePropertyRegistration::class)
            assertThat(finalCheckJointLandlordPage.summaryList.firstRow.value).containsText(alreadyInvitedEmail)
        }
    }

    @Nested
    inner class HasGasSupplyStep {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val hasGasSupplyPage = navigator.skipToPropertyRegistrationHasGasSupplyPage()
            hasGasSupplyPage.form.submit()
            assertThat(hasGasSupplyPage.form.getErrorMessage()).containsText("Select whether you have a gas supply or any gas appliances")
        }

        @Test
        fun `Submitting No navigates to the check you gas answers step`(page: Page) {
            val hasGasSupplyPage = navigator.skipToPropertyRegistrationHasGasSupplyPage()
            hasGasSupplyPage.submitHasNoGasSupply()
            assertPageIs(page, CheckGasSafetyAnswersFormPagePropertyRegistration::class)
        }
    }

    @Nested
    inner class HasGasSafetyCertStep {
        @Test
        fun `Submitting with the Continue button with no option selected returns an error`(page: Page) {
            val hasGasSafetyCertPage = navigator.skipToPropertyRegistrationHasGasCertPage()
            hasGasSafetyCertPage.form.submitPrimaryButton()
            assertThat(
                hasGasSafetyCertPage.form.getErrorMessage(),
            ).containsText("Select whether you have a gas safety certificate")
        }
    }

    @Nested
    inner class GasSafetyIssueDateStepTests {
        @ParameterizedTest(name = "{0}")
        @Suppress("ktlint:standard:max-line-length")
        @MethodSource(
            "uk.gov.communities.prsdb.webapp.testHelpers.parameterProviders.TodayOrPastDateValidationTestParameterProvider#provideInvalidDateStrings",
        )
        fun `Submitting returns a corresponding error when`(
            dayMonthYear: Triple<String, String, String>,
            expectedErrorMessage: String,
        ) {
            val (day, month, year) = dayMonthYear
            val gasSafetyIssueDatePage = navigator.skipToPropertyRegistrationGasCertIssueDatePage()
            gasSafetyIssueDatePage.submitDate(day, month, year)
            assertThat(gasSafetyIssueDatePage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }
    }

    @Nested
    inner class HasElectricalCertStep {
        @Test
        fun `Submitting with the Continue button with no option selected returns an error`(page: Page) {
            val hasElectricalCertPage = navigator.skipToPropertyRegistrationHasElectricalCertPage()
            hasElectricalCertPage.form.submitPrimaryButton()
            assertThat(
                hasElectricalCertPage.form.getErrorMessage(),
            ).containsText("Select which electrical safety certificate you have")
        }
    }

    @Nested
    inner class ElectricalCertExpiryDateStepTests {
        @ParameterizedTest(name = "{0}")
        @Suppress("ktlint:standard:max-line-length")
        @MethodSource(
            "uk.gov.communities.prsdb.webapp.testHelpers.parameterProviders.AnyDateValidationTestParameterProvider#provideInvalidDateStrings",
        )
        fun `Submitting returns a corresponding error when`(
            dayMonthYear: Triple<String, String, String>,
            expectedErrorMessage: String,
        ) {
            val (day, month, year) = dayMonthYear
            val electricalCertExpiryDatePage = navigator.skipToPropertyRegistrationElectricalCertExpiryDatePage()
            electricalCertExpiryDatePage.submitDate(day, month, year)
            assertThat(electricalCertExpiryDatePage.form.getErrorMessage()).containsText(expectedErrorMessage)
        }
    }

    @Nested
    inner class HasEpcStepTests {
        @Test
        fun `Submitting with the Continue button with no option selected returns an error`(page: Page) {
            val hasEpcPage = navigator.skipToPropertyRegistrationHasEpcPage()
            hasEpcPage.form.submitPrimaryButton()
            assertThat(hasEpcPage.form.getErrorMessage())
                .containsText("Select whether you have an EPC for this property")
        }
    }

    @Nested
    inner class FindYourEpcStepTests {
        @Test
        fun `Submitting with no option selected returns an error`(page: Page) {
            val findYourEpcPage = navigator.skipToPropertyRegistrationFindYourEpcPage()
            findYourEpcPage.form.submit()
            assertThat(findYourEpcPage.form.getErrorMessage())
                .containsText("Enter your EPC certificate number")
        }
    }

    @Nested
    inner class ConfirmEpcDetailsRetrievedByCertificateNumberStepTests {
        @Test
        fun `User sees a validation error when they do not select an answer`(page: Page) {
            val confirmEpcDetailsPage =
                navigator.skipToPropertyRegistrationConfirmEpcDetailsRetrievedByCertificateNumberPage()
            confirmEpcDetailsPage.form.submit()
            assertThat(confirmEpcDetailsPage.form.getErrorMessage())
                .containsText("Select Yes or No to continue")
        }
    }

    @Nested
    inner class IsEpcRequiredStepTests {
        @Test
        fun `Submitting with no option selected returns a validation error`(page: Page) {
            val isEpcRequiredPage = navigator.skipToPropertyRegistrationIsEpcRequiredPage()
            isEpcRequiredPage.form.submit()
            assertThat(isEpcRequiredPage.form.getErrorMessage())
                .containsText("Select whether an EPC is required for this property")
        }
    }

    @Nested
    inner class ConfirmEpcDetailsByUprnStepTests {
        @Test
        fun `User sees a validation error when they do not select an answer`(page: Page) {
            val confirmEpcDetailsPage =
                navigator.skipToPropertyRegistrationConfirmEpcDetailsByUprnPage()
            confirmEpcDetailsPage.form.submit()
            assertThat(confirmEpcDetailsPage.form.getErrorMessage())
                .containsText("Select Yes or No to continue")
        }
    }

    @Nested
    inner class MeesExemptionStepTests {
        @Test
        fun `User sees a validation error when they do not select a MEES exemption reason`(page: Page) {
            val meesExemptionPage = navigator.skipToPropertyRegistrationMeesExemptionPage()

            meesExemptionPage.form.submit()

            assertPageIs(page, MeesExemptionFormPagePropertyRegistration::class)
            assertThat(meesExemptionPage.form.getErrorMessage()).isVisible()
        }
    }

    @Nested
    inner class EpcExemptionStepTests {
        @Test
        fun `User sees a validation error when they do not select an EPC exemption reason`(page: Page) {
            val epcExemptionPage = navigator.skipToPropertyRegistrationEpcExemptionPage()

            epcExemptionPage.form.submit()

            assertPageIs(page, EpcExemptionFormPagePropertyRegistration::class)
            assertThat(epcExemptionPage.form.getErrorMessage()).isVisible()
        }
    }

    @Nested
    inner class Confirmation {
        @Test
        fun `Navigating here with an incomplete form returns a 400 error page`(page: Page) {
            navigator.navigateToPropertyRegistrationConfirmationPage()
            val errorPage = assertPageIs(page, ErrorPage::class)
            BaseComponent.assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
        }
    }

    @Nested
    inner class PropertyRegistrationStepCheckAnswers {
        @Test
        fun `After changing an answer, submitting a full section returns the CYA page`(page: Page) {
            var checkAnswersPage = navigator.skipToPropertyRegistrationCheckAnswersPage()

            checkAnswersPage.summaryList.ownershipRow.actions.firstActionLink
                .clickAndWait()
            val ownershipPage = assertPageIs(page, OwnershipTypeFormPagePropertyRegistration::class)

            ownershipPage.submitOwnershipType(OwnershipType.LEASEHOLD)
            checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

            checkAnswersPage.summaryList.licensingRow.actions.firstActionLink
                .clickAndWait()
            val licensingTypePage = assertPageIs(page, LicensingTypeFormPagePropertyRegistration::class)

            licensingTypePage.submitLicensingType(LicensingType.HMO_ADDITIONAL_LICENCE)
            val licenceNumberPage = assertPageIs(page, HmoAdditionalLicenceFormPagePropertyRegistration::class)
            licenceNumberPage.submitLicenseNumber("licence number")
            assertPageIs(page, CheckAnswersPagePropertyRegistration::class)
        }

        @Test
        fun `the joint landlords section is not shown on the check answers page when the feature flag is disabled`(page: Page) {
            featureFlagManager.disableFeature(JOINT_LANDLORDS)
            val checkAnswersPage = navigator.skipToPropertyRegistrationCheckAnswersPage()
            BaseComponent.assertThat(checkAnswersPage.jointLandlordsHeading).isHidden()
        }
    }

    @Nested
    inner class HasMeesExemptionStep {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val hasMeesExemptionPage = navigator.skipToPropertyRegistrationHasMeesExemptionPage()
            hasMeesExemptionPage.form.submit()
            assertThat(hasMeesExemptionPage.form.getErrorMessage())
                .containsText("Select if you have registered an energy efficiency exemption for this property")
        }
    }

    @Nested
    inner class EpcMissingStep {
        @Test
        fun `The page renders the occupied variant for an occupied property`(page: Page) {
            val epcMissingPage = navigator.skipToPropertyRegistrationEpcMissingPage(propertyIsOccupied = true)
            BaseComponent.assertThat(epcMissingPage.heading).containsText("Your property is missing an EPC")
            BaseComponent.assertThat(epcMissingPage.warning).isVisible()
            BaseComponent.assertThat(epcMissingPage.continueAnywayButton).hasText("Continue anyway")
        }

        @Test
        fun `The page renders the unoccupied variant for an unoccupied property`(page: Page) {
            val epcMissingPage = navigator.skipToPropertyRegistrationEpcMissingPage(propertyIsOccupied = false)
            BaseComponent.assertThat(epcMissingPage.heading).containsText("Your property is missing an EPC")
            BaseComponent.assertThat(epcMissingPage.warning).isHidden()
            BaseComponent.assertThat(epcMissingPage.continueButton).hasText("Continue")
        }
    }

    @Nested
    inner class EpcExpiredStep {
        @Test
        fun `The page renders the occupied variant for an occupied property`(page: Page) {
            val epcExpiredPage = navigator.skipToPropertyRegistrationEpcExpiredPage(propertyIsOccupied = true)
            BaseComponent.assertThat(epcExpiredPage.heading).containsText("This property’s EPC has expired")
            BaseComponent.assertThat(epcExpiredPage.warning).isVisible()
            BaseComponent.assertThat(epcExpiredPage.submitButton).hasText("Continue anyway")
        }

        @Test
        fun `The page renders the unoccupied variant for an unoccupied property`(page: Page) {
            val epcExpiredPage = navigator.skipToPropertyRegistrationEpcExpiredPage(propertyIsOccupied = false)
            BaseComponent.assertThat(epcExpiredPage.heading).containsText("This property’s EPC has expired")
            BaseComponent.assertThat(epcExpiredPage.warning).isHidden()
            BaseComponent.assertThat(epcExpiredPage.submitButton).hasText("Continue")
        }
    }

    @Nested
    inner class LowEnergyRatingStep {
        @Test
        fun `The page renders the occupied variant for an occupied property`(page: Page) {
            val lowEnergyRatingPage = navigator.skipToPropertyRegistrationLowEnergyRatingPage(propertyIsOccupied = true)
            BaseComponent.assertThat(lowEnergyRatingPage.heading).containsText(
                "This property does not meet energy efficiency requirements for letting",
            )
            BaseComponent.assertThat(lowEnergyRatingPage.continueAnywayButton).containsText("Continue anyway")
        }

        @Test
        fun `The page renders the unoccupied variant for an unoccupied property`(page: Page) {
            val lowEnergyRatingPage = navigator.skipToPropertyRegistrationLowEnergyRatingPage(propertyIsOccupied = false)
            BaseComponent.assertThat(lowEnergyRatingPage.heading).containsText(
                "You’ll need to get a new EPC before letting this property",
            )
            BaseComponent.assertThat(lowEnergyRatingPage.continueButton).containsText("Continue")
        }
    }

    @Nested
    inner class ProvideEpcLaterStep {
        @Test
        fun `The page renders the occupied variant for an occupied property`(page: Page) {
            val provideEpcLaterPage = navigator.skipToPropertyRegistrationProvideEpcLaterPage(propertyIsOccupied = true)
            BaseComponent.assertThat(provideEpcLaterPage.heading).containsText("Provide your EPC details later")
            BaseComponent.assertThat(provideEpcLaterPage.insetText).containsText(
                "To keep the property registered, we need all its compliance certificates within 28 days.",
            )
        }

        @Test
        fun `The page renders the unoccupied variant for an unoccupied property`(page: Page) {
            val provideEpcLaterPage = navigator.skipToPropertyRegistrationProvideEpcLaterPage(propertyIsOccupied = false)
            BaseComponent.assertThat(provideEpcLaterPage.heading).containsText("Provide your EPC details later")
            BaseComponent.assertThat(provideEpcLaterPage.insetText).isHidden()
        }
    }
}
