package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholderAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderContactTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderProvideDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel

@ExtendWith(MockitoExtension::class)
class SaveFreeholderStepConfigTests {
    @Mock
    lateinit var mockState: FreeholdersDetailsState

    @Mock
    lateinit var provideDetailsStep: ProvideFreeholderDetailsStep

    @Mock
    lateinit var nameStep: FreeholderNameStep

    @Mock
    lateinit var contactTypeStep: FreeholderContactTypeStep

    @Mock
    lateinit var emailStep: FreeholderEmailStep

    @Mock
    lateinit var addressTask: FreeholderAddressTask

    @Mock
    lateinit var lookupAddressStep: LookupAddressStep

    @Mock
    lateinit var manualAddressStep: ManualAddressStep

    @Test
    fun `afterStepIsReached adds a new freeholder with the next id when not editing`() {
        val stepConfig = SaveFreeholderStepConfig()
        setupStepsWithAnswers(YesOrNo.NO)
        val existingFreeholder = FreeholderDataModel(detailsKnown = true, name = "Existing freeholder")
        whenever(mockState.freeholdersMap).thenReturn(mapOf(1 to existingFreeholder))
        whenever(mockState.editingFreeholderId).thenReturn(null)
        whenever(mockState.nextFreeholderId).thenReturn(4)

        stepConfig.afterStepIsReached(mockState)

        val savedMap = captureSavedMap()
        assertEquals(mapOf(1 to existingFreeholder, 4 to FreeholderDataModel(detailsKnown = false)), savedMap)
        verify(mockState).nextFreeholderId = 5
        verify(mockState).editingFreeholderId = null
    }

    @Test
    fun `afterStepIsReached replaces the freeholder being edited without changing the next id when editing`() {
        val stepConfig = SaveFreeholderStepConfig()
        setupStepsWithAnswers(YesOrNo.NO)
        val otherFreeholder = FreeholderDataModel(detailsKnown = true, name = "Other freeholder")
        whenever(mockState.freeholdersMap).thenReturn(
            mapOf(1 to otherFreeholder, 2 to FreeholderDataModel(detailsKnown = true, name = "Edited freeholder")),
        )
        whenever(mockState.editingFreeholderId).thenReturn(2)

        stepConfig.afterStepIsReached(mockState)

        val savedMap = captureSavedMap()
        assertEquals(mapOf(1 to otherFreeholder, 2 to FreeholderDataModel(detailsKnown = false)), savedMap)
        verify(mockState, never()).nextFreeholderId = any()
        verify(mockState).editingFreeholderId = null
    }

    @Test
    fun `afterStepIsReached saves the entered name and email when the contact type is email`() {
        val stepConfig = SaveFreeholderStepConfig()
        setupStepsWithAnswers(YesOrNo.YES, FreeholderContactType.EMAIL)
        whenever(mockState.freeholdersMap).thenReturn(null)
        whenever(mockState.editingFreeholderId).thenReturn(null)
        whenever(mockState.nextFreeholderId).thenReturn(null)

        stepConfig.afterStepIsReached(mockState)

        val expectedFreeholder =
            FreeholderDataModel(
                detailsKnown = true,
                name = ENTERED_NAME,
                email = ENTERED_EMAIL,
            )
        assertEquals(mapOf(1 to expectedFreeholder), captureSavedMap())
        verify(mockState).nextFreeholderId = 2
    }

    @Test
    fun `afterStepIsReached saves the selected address and lookup answers when the contact type is postal address`() {
        // Arrange
        val stepConfig = SaveFreeholderStepConfig()
        setupStepsWithAnswers(YesOrNo.YES, FreeholderContactType.POSTAL_ADDRESS)
        val staleManualFormModel = ManualAddressFormModel().apply { addressLineOne = "Previously entered road" }
        setupAddressAnswers(selectedAddress = SELECTED_ADDRESS.singleLineAddress, manualFormModel = staleManualFormModel)
        whenever(addressTask.getAddress()).thenReturn(SELECTED_ADDRESS)
        whenever(mockState.freeholdersMap).thenReturn(emptyMap())
        whenever(mockState.editingFreeholderId).thenReturn(null)
        whenever(mockState.nextFreeholderId).thenReturn(1)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        val expectedFreeholder =
            FreeholderDataModel(
                detailsKnown = true,
                name = ENTERED_NAME,
                address = SELECTED_ADDRESS,
                addressSearchPostcode = LOOKUP_POSTCODE,
                addressSearchHouseNameOrNumber = LOOKUP_HOUSE_NAME_OR_NUMBER,
                selectedAddress = SELECTED_ADDRESS.singleLineAddress,
            )
        assertEquals(mapOf(1 to expectedFreeholder), captureSavedMap())
    }

    @Test
    fun `afterStepIsReached saves the manual address answers when the address was entered manually`() {
        // Arrange
        val stepConfig = SaveFreeholderStepConfig()
        setupStepsWithAnswers(YesOrNo.YES, FreeholderContactType.POSTAL_ADDRESS)
        val manualFormModel =
            ManualAddressFormModel().apply {
                addressLineOne = "2 Manual Road"
                addressLineTwo = "Flat 3"
                townOrCity = "Manual Town"
                county = "Manual County"
                postcode = "MA1 1AA"
            }
        setupAddressAnswers(selectedAddress = MANUAL_ADDRESS_CHOSEN, manualFormModel = manualFormModel)
        val manualAddress = AddressDataModel.fromManualAddressData("2 Manual Road", "Manual Town", "MA1 1AA")
        whenever(addressTask.getAddress()).thenReturn(manualAddress)
        whenever(mockState.freeholdersMap).thenReturn(emptyMap())
        whenever(mockState.editingFreeholderId).thenReturn(null)
        whenever(mockState.nextFreeholderId).thenReturn(1)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        val expectedFreeholder =
            FreeholderDataModel(
                detailsKnown = true,
                name = ENTERED_NAME,
                address = manualAddress,
                addressSearchPostcode = LOOKUP_POSTCODE,
                addressSearchHouseNameOrNumber = LOOKUP_HOUSE_NAME_OR_NUMBER,
                selectedAddress = MANUAL_ADDRESS_CHOSEN,
                manualAddressLineOne = "2 Manual Road",
                manualAddressLineTwo = "Flat 3",
                manualTownOrCity = "Manual Town",
                manualCounty = "Manual County",
                manualPostcode = "MA1 1AA",
            )
        assertEquals(mapOf(1 to expectedFreeholder), captureSavedMap())
    }

    @Test
    fun `afterStepIsReached clears the freeholder form data so the next freeholder starts empty`() {
        val stepConfig = SaveFreeholderStepConfig()
        setupStepsWithAnswers(YesOrNo.NO)
        whenever(mockState.freeholdersMap).thenReturn(emptyMap())
        whenever(mockState.editingFreeholderId).thenReturn(null)
        whenever(mockState.nextFreeholderId).thenReturn(1)

        stepConfig.afterStepIsReached(mockState)

        verify(provideDetailsStep).clearFormData()
        verify(nameStep).clearFormData()
        verify(contactTypeStep).clearFormData()
        verify(emailStep).clearFormData()
        verify(addressTask).clearFormData()
    }

    @Test
    fun `afterStepIsReached throws when the provide details answer is missing`() {
        val stepConfig = SaveFreeholderStepConfig()
        whenever(mockState.freeholderProvideDetailsStep).thenReturn(provideDetailsStep)
        whenever(provideDetailsStep.formModelOrNull).thenReturn(null)

        assertThrows<PrsdbWebException> { stepConfig.afterStepIsReached(mockState) }
    }

    @Test
    fun `afterStepIsReached throws when details are provided but the contact type answer is missing`() {
        val stepConfig = SaveFreeholderStepConfig()
        whenever(mockState.freeholderProvideDetailsStep).thenReturn(provideDetailsStep)
        whenever(provideDetailsStep.formModelOrNull).thenReturn(FreeholderProvideDetailsFormModel(provideDetails = YesOrNo.YES))
        whenever(mockState.freeholderContactTypeStep).thenReturn(contactTypeStep)
        whenever(contactTypeStep.formModelOrNull).thenReturn(null)

        assertThrows<PrsdbWebException> { stepConfig.afterStepIsReached(mockState) }
    }

    @Test
    fun `afterStepIsReached throws when details are provided but the name answer is missing`() {
        val stepConfig = SaveFreeholderStepConfig()
        whenever(mockState.freeholderProvideDetailsStep).thenReturn(provideDetailsStep)
        whenever(provideDetailsStep.formModelOrNull).thenReturn(FreeholderProvideDetailsFormModel(provideDetails = YesOrNo.YES))
        whenever(mockState.freeholderContactTypeStep).thenReturn(contactTypeStep)
        whenever(contactTypeStep.formModelOrNull).thenReturn(FreeholderContactTypeFormModel(contactType = FreeholderContactType.EMAIL))
        whenever(mockState.freeholderNameStep).thenReturn(nameStep)
        whenever(nameStep.formModelOrNull).thenReturn(null)

        assertThrows<PrsdbWebException> { stepConfig.afterStepIsReached(mockState) }
    }

    private fun setupStepsWithAnswers(
        provideDetails: YesOrNo,
        contactType: FreeholderContactType? = null,
    ) {
        whenever(mockState.freeholderProvideDetailsStep).thenReturn(provideDetailsStep)
        whenever(provideDetailsStep.formModelOrNull).thenReturn(FreeholderProvideDetailsFormModel(provideDetails = provideDetails))
        whenever(mockState.freeholderNameStep).thenReturn(nameStep)
        whenever(mockState.freeholderContactTypeStep).thenReturn(contactTypeStep)
        if (contactType != null) {
            whenever(contactTypeStep.formModelOrNull).thenReturn(FreeholderContactTypeFormModel(contactType = contactType))
            whenever(nameStep.formModelOrNull).thenReturn(FreeholderNameFormModel().apply { name = ENTERED_NAME })
        }
        whenever(mockState.freeholderEmailStep).thenReturn(emailStep)
        if (contactType == FreeholderContactType.EMAIL) {
            whenever(emailStep.formModelOrNull).thenReturn(FreeholderEmailFormModel().apply { emailAddress = ENTERED_EMAIL })
        }
        whenever(mockState.freeholderAddressTask).thenReturn(addressTask)
    }

    private fun setupAddressAnswers(
        selectedAddress: String?,
        manualFormModel: ManualAddressFormModel?,
    ) {
        whenever(addressTask.lookupAddressStep).thenReturn(lookupAddressStep)
        whenever(lookupAddressStep.formModelOrNull).thenReturn(
            LookupAddressFormModel().apply {
                postcode = LOOKUP_POSTCODE
                houseNameOrNumber = LOOKUP_HOUSE_NAME_OR_NUMBER
            },
        )
        whenever(addressTask.cachedSelectedAddress).thenReturn(selectedAddress)
        whenever(addressTask.manualAddressStep).thenReturn(manualAddressStep)
        whenever(manualAddressStep.formModelOrNull).thenReturn(manualFormModel)
    }

    private fun captureSavedMap(): Map<Int, FreeholderDataModel> {
        val captor = argumentCaptor<Map<Int, FreeholderDataModel>>()
        verify(mockState).freeholdersMap = captor.capture()
        return captor.firstValue
    }

    companion object {
        private const val ENTERED_NAME = "Entered freeholder name"
        private const val ENTERED_EMAIL = "entered.freeholder@example.com"
        private const val LOOKUP_POSTCODE = "FA1 1AA"
        private const val LOOKUP_HOUSE_NAME_OR_NUMBER = "1"
        private val SELECTED_ADDRESS = AddressDataModel(singleLineAddress = "1 Fictional Road, FA1 1AA", postcode = "FA1 1AA")
    }
}
