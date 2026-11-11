package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordContactTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordProvideDetailsFormModel

@ExtendWith(MockitoExtension::class)
class SaveSuperiorLandlordStepConfigTests {
    @Mock
    lateinit var mockState: SuperiorLandlordsDetailsState

    @Mock
    lateinit var provideDetailsStep: ProvideSuperiorLandlordDetailsStep

    @Mock
    lateinit var nameStep: SuperiorLandlordNameStep

    @Mock
    lateinit var contactTypeStep: SuperiorLandlordContactTypeStep

    @Mock
    lateinit var emailStep: SuperiorLandlordEmailStep

    @Mock
    lateinit var addressTask: SuperiorLandlordAddressTask

    @Mock
    lateinit var lookupAddressStep: LookupAddressStep

    @Mock
    lateinit var manualAddressStep: ManualAddressStep

    @Mock
    lateinit var selectAddressStep: SelectAddressStep

    @Test
    fun `afterStepIsReached adds a new superior landlord with the next id when not editing`() {
        val stepConfig = SaveSuperiorLandlordStepConfig()
        setupStepsWithAnswers(false)
        val existingSuperiorLandlord = SuperiorLandlordDataModel(detailsKnown = true, name = "Existing superior landlord")
        whenever(mockState.superiorLandlordsMap).thenReturn(mapOf(1 to existingSuperiorLandlord))
        whenever(mockState.editingSuperiorLandlordId).thenReturn(null)
        whenever(mockState.nextSuperiorLandlordId).thenReturn(4)

        stepConfig.afterStepIsReached(mockState)

        val savedMap = captureSavedMap()
        assertEquals(mapOf(1 to existingSuperiorLandlord, 4 to SuperiorLandlordDataModel(detailsKnown = false)), savedMap)
        verify(mockState).nextSuperiorLandlordId = 5
        verify(mockState).editingSuperiorLandlordId = null
    }

    @Test
    fun `afterStepIsReached replaces the superior landlord being edited without changing the next id when editing`() {
        val stepConfig = SaveSuperiorLandlordStepConfig()
        setupStepsWithAnswers(false)
        val otherSuperiorLandlord = SuperiorLandlordDataModel(detailsKnown = true, name = "Other superior landlord")
        whenever(mockState.superiorLandlordsMap).thenReturn(
            mapOf(1 to otherSuperiorLandlord, 2 to SuperiorLandlordDataModel(detailsKnown = true, name = "Edited superior landlord")),
        )
        whenever(mockState.editingSuperiorLandlordId).thenReturn(2)

        stepConfig.afterStepIsReached(mockState)

        val savedMap = captureSavedMap()
        assertEquals(mapOf(1 to otherSuperiorLandlord, 2 to SuperiorLandlordDataModel(detailsKnown = false)), savedMap)
        verify(mockState, never()).nextSuperiorLandlordId = any()
        verify(mockState).editingSuperiorLandlordId = null
    }

    @Test
    fun `afterStepIsReached saves the name and email when the contact type is email`() {
        val stepConfig = SaveSuperiorLandlordStepConfig()
        setupStepsWithAnswers(true, SuperiorLandlordContactType.EMAIL, ENTERED_NAME, email = "superior.landlord@example.com")
        whenever(mockState.superiorLandlordsMap).thenReturn(null)
        whenever(mockState.editingSuperiorLandlordId).thenReturn(null)
        whenever(mockState.nextSuperiorLandlordId).thenReturn(null)

        stepConfig.afterStepIsReached(mockState)

        val expectedSuperiorLandlord =
            SuperiorLandlordDataModel(
                detailsKnown = true,
                name = ENTERED_NAME,
                email = "superior.landlord@example.com",
            )
        assertEquals(mapOf(1 to expectedSuperiorLandlord), captureSavedMap())
        verify(mockState).nextSuperiorLandlordId = 2
    }

    @Test
    fun `afterStepIsReached saves the selected address and lookup answers when the contact type is postal address`() {
        // Arrange
        val stepConfig = SaveSuperiorLandlordStepConfig()
        setupStepsWithAnswers(true, SuperiorLandlordContactType.POSTAL_ADDRESS, ENTERED_NAME)
        val staleManualFormModel = ManualAddressFormModel().apply { addressLineOne = "Previously entered road" }
        setupAddressAnswers(selectedAddress = SELECTED_ADDRESS.singleLineAddress, manualFormModel = staleManualFormModel)
        whenever(addressTask.getAddress()).thenReturn(SELECTED_ADDRESS)
        whenever(mockState.superiorLandlordsMap).thenReturn(emptyMap())
        whenever(mockState.editingSuperiorLandlordId).thenReturn(null)
        whenever(mockState.nextSuperiorLandlordId).thenReturn(1)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        val expectedSuperiorLandlord =
            SuperiorLandlordDataModel(
                detailsKnown = true,
                name = ENTERED_NAME,
                address = SELECTED_ADDRESS,
                addressSearchPostcode = LOOKUP_POSTCODE,
                addressSearchHouseNameOrNumber = LOOKUP_HOUSE_NAME_OR_NUMBER,
                selectedAddress = SELECTED_ADDRESS.singleLineAddress,
            )
        assertEquals(mapOf(1 to expectedSuperiorLandlord), captureSavedMap())
    }

    @Test
    fun `afterStepIsReached saves the manual address answers when the address was entered manually`() {
        // Arrange
        val stepConfig = SaveSuperiorLandlordStepConfig()
        setupStepsWithAnswers(true, SuperiorLandlordContactType.POSTAL_ADDRESS, ENTERED_NAME)
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
        whenever(mockState.superiorLandlordsMap).thenReturn(emptyMap())
        whenever(mockState.editingSuperiorLandlordId).thenReturn(null)
        whenever(mockState.nextSuperiorLandlordId).thenReturn(1)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        val expectedSuperiorLandlord =
            SuperiorLandlordDataModel(
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
        assertEquals(mapOf(1 to expectedSuperiorLandlord), captureSavedMap())
    }

    @Test
    fun `afterStepIsReached saves manual address answers after a failed search despite an earlier selected address`() {
        // Arrange
        val stepConfig = SaveSuperiorLandlordStepConfig()
        setupStepsWithAnswers(true, SuperiorLandlordContactType.POSTAL_ADDRESS, ENTERED_NAME)
        val previousSelection = SELECTED_ADDRESS.singleLineAddress
        val manualFormModel =
            ManualAddressFormModel().apply {
                addressLineOne = "2 Manual Road"
                townOrCity = "Manual Town"
                postcode = "MA1 1AA"
            }
        setupAddressAnswers(selectedAddress = previousSelection, manualFormModel = manualFormModel)
        val selectFormModel = SelectAddressFormModel().apply { address = previousSelection }
        whenever(addressTask.selectAddressStep).thenReturn(selectAddressStep)
        whenever(selectAddressStep.formModelOrNull).thenReturn(selectFormModel)
        whenever(addressTask.cachedAddresses).thenReturn(emptyList())
        whenever(addressTask.getMatchingAddress(any())).thenCallRealMethod()
        whenever(addressTask.getManualAddressOrNull()).thenCallRealMethod()
        whenever(addressTask.getAddress()).thenCallRealMethod()

        var cachedSelectedAddress: String? = previousSelection
        whenever(addressTask.cachedSelectedAddress).thenAnswer { cachedSelectedAddress }
        doAnswer { cachedSelectedAddress = it.getArgument(0) }
            .whenever(addressTask)
            .cachedSelectedAddress = anyOrNull()

        whenever(mockState.superiorLandlordsMap).thenReturn(emptyMap())
        whenever(mockState.editingSuperiorLandlordId).thenReturn(null)
        whenever(mockState.nextSuperiorLandlordId).thenReturn(1)

        // Act
        NoAddressFoundStepConfig().afterStepDataIsAdded(addressTask)
        stepConfig.afterStepIsReached(mockState)

        // Assert
        val expectedSuperiorLandlord =
            SuperiorLandlordDataModel(
                detailsKnown = true,
                name = ENTERED_NAME,
                address = AddressDataModel.fromManualAddressData("2 Manual Road", "Manual Town", "MA1 1AA"),
                addressSearchPostcode = LOOKUP_POSTCODE,
                addressSearchHouseNameOrNumber = LOOKUP_HOUSE_NAME_OR_NUMBER,
                selectedAddress = MANUAL_ADDRESS_CHOSEN,
                manualAddressLineOne = "2 Manual Road",
                manualTownOrCity = "Manual Town",
                manualPostcode = "MA1 1AA",
            )
        assertEquals(mapOf(1 to expectedSuperiorLandlord), captureSavedMap())
    }

    @Test
    fun `afterStepIsReached clears the superior landlord form data so the next superior landlord starts empty`() {
        val stepConfig = SaveSuperiorLandlordStepConfig()
        setupStepsWithAnswers(false)
        whenever(mockState.superiorLandlordsMap).thenReturn(emptyMap())
        whenever(mockState.editingSuperiorLandlordId).thenReturn(null)
        whenever(mockState.nextSuperiorLandlordId).thenReturn(1)

        stepConfig.afterStepIsReached(mockState)

        verify(provideDetailsStep).clearFormData()
        verify(nameStep).clearFormData()
        verify(contactTypeStep).clearFormData()
        verify(emailStep).clearFormData()
        verify(addressTask).clearFormData()
    }

    @Test
    fun `afterStepIsReached throws when the provide details answer is missing`() {
        val stepConfig = SaveSuperiorLandlordStepConfig()
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockState.superiorLandlordProvideDetailsStep).thenReturn(provideDetailsStep)
        whenever(provideDetailsStep.formModelOrNull).thenReturn(null)

        val exception = assertThrows<UnrecoverableJourneyStateException> { stepConfig.afterStepIsReached(mockState) }

        assertTrue(exception.message!!.contains(JOURNEY_ID))
    }

    @Test
    fun `afterStepIsReached throws when details are provided but the contact type answer is missing`() {
        val stepConfig = SaveSuperiorLandlordStepConfig()
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockState.superiorLandlordProvideDetailsStep).thenReturn(provideDetailsStep)
        whenever(provideDetailsStep.formModelOrNull).thenReturn(SuperiorLandlordProvideDetailsFormModel(provideDetails = true))
        whenever(mockState.superiorLandlordContactTypeStep).thenReturn(contactTypeStep)
        whenever(contactTypeStep.formModelOrNull).thenReturn(null)

        val exception = assertThrows<UnrecoverableJourneyStateException> { stepConfig.afterStepIsReached(mockState) }

        assertTrue(exception.message!!.contains(JOURNEY_ID))
    }

    @Test
    fun `afterStepIsReached throws when the contact type is email but the email answer is missing`() {
        val stepConfig = SaveSuperiorLandlordStepConfig()
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockState.superiorLandlordProvideDetailsStep).thenReturn(provideDetailsStep)
        whenever(provideDetailsStep.formModelOrNull).thenReturn(SuperiorLandlordProvideDetailsFormModel(provideDetails = true))
        whenever(mockState.superiorLandlordContactTypeStep).thenReturn(contactTypeStep)
        whenever(contactTypeStep.formModelOrNull).thenReturn(
            SuperiorLandlordContactTypeFormModel(contactType = SuperiorLandlordContactType.EMAIL),
        )
        whenever(mockState.superiorLandlordNameStep).thenReturn(nameStep)
        whenever(nameStep.formModelOrNull).thenReturn(SuperiorLandlordNameFormModel().apply { name = ENTERED_NAME })
        whenever(mockState.superiorLandlordEmailStep).thenReturn(emailStep)
        whenever(emailStep.formModelOrNull).thenReturn(null)

        val exception = assertThrows<UnrecoverableJourneyStateException> { stepConfig.afterStepIsReached(mockState) }

        assertTrue(exception.message!!.contains(JOURNEY_ID))
    }

    @Test
    fun `afterStepIsReached throws when details are provided but the name answer is missing`() {
        val stepConfig = SaveSuperiorLandlordStepConfig()
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockState.superiorLandlordProvideDetailsStep).thenReturn(provideDetailsStep)
        whenever(provideDetailsStep.formModelOrNull).thenReturn(SuperiorLandlordProvideDetailsFormModel(provideDetails = true))
        whenever(mockState.superiorLandlordContactTypeStep).thenReturn(contactTypeStep)
        whenever(contactTypeStep.formModelOrNull).thenReturn(
            SuperiorLandlordContactTypeFormModel(contactType = SuperiorLandlordContactType.EMAIL),
        )
        whenever(mockState.superiorLandlordNameStep).thenReturn(nameStep)
        whenever(nameStep.formModelOrNull).thenReturn(null)

        val exception = assertThrows<UnrecoverableJourneyStateException> { stepConfig.afterStepIsReached(mockState) }

        assertTrue(exception.message!!.contains(JOURNEY_ID))
    }

    @Test
    fun `afterStepIsReached throws when the contact type is postal address but the address answer is missing`() {
        // Arrange
        val stepConfig = SaveSuperiorLandlordStepConfig()
        whenever(mockState.superiorLandlordProvideDetailsStep).thenReturn(provideDetailsStep)
        whenever(provideDetailsStep.formModelOrNull).thenReturn(SuperiorLandlordProvideDetailsFormModel(provideDetails = true))
        whenever(mockState.superiorLandlordContactTypeStep).thenReturn(contactTypeStep)
        whenever(contactTypeStep.formModelOrNull).thenReturn(
            SuperiorLandlordContactTypeFormModel(contactType = SuperiorLandlordContactType.POSTAL_ADDRESS),
        )
        whenever(mockState.superiorLandlordNameStep).thenReturn(nameStep)
        whenever(nameStep.formModelOrNull).thenReturn(SuperiorLandlordNameFormModel().apply { name = ENTERED_NAME })
        whenever(mockState.superiorLandlordAddressTask).thenReturn(addressTask)
        whenever(addressTask.getAddress()).thenThrow(NotNullFormModelValueIsNullException("No address found in AddressState"))

        // Act & Assert
        assertThrows<NotNullFormModelValueIsNullException> { stepConfig.afterStepIsReached(mockState) }
    }

    private fun setupStepsWithAnswers(
        provideDetails: Boolean,
        contactType: SuperiorLandlordContactType? = null,
        name: String? = null,
        email: String? = null,
    ) {
        whenever(mockState.superiorLandlordProvideDetailsStep).thenReturn(provideDetailsStep)
        whenever(provideDetailsStep.formModelOrNull).thenReturn(SuperiorLandlordProvideDetailsFormModel(provideDetails = provideDetails))
        whenever(mockState.superiorLandlordNameStep).thenReturn(nameStep)
        whenever(mockState.superiorLandlordContactTypeStep).thenReturn(contactTypeStep)
        if (contactType != null) {
            whenever(contactTypeStep.formModelOrNull).thenReturn(SuperiorLandlordContactTypeFormModel(contactType = contactType))
        }
        if (name != null) {
            whenever(nameStep.formModelOrNull).thenReturn(SuperiorLandlordNameFormModel().apply { this.name = name })
        }
        whenever(mockState.superiorLandlordEmailStep).thenReturn(emailStep)
        if (email != null) {
            whenever(emailStep.formModelOrNull).thenReturn(SuperiorLandlordEmailFormModel().apply { emailAddress = email })
        }
        whenever(mockState.superiorLandlordAddressTask).thenReturn(addressTask)
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

    private fun captureSavedMap(): Map<Int, SuperiorLandlordDataModel> {
        val captor = argumentCaptor<Map<Int, SuperiorLandlordDataModel>>()
        verify(mockState).superiorLandlordsMap = captor.capture()
        return captor.firstValue
    }

    companion object {
        private const val ENTERED_NAME = "Entered superior landlord"
        private const val JOURNEY_ID = "test-journey-id"
        private const val LOOKUP_POSTCODE = "FA1 1AA"
        private const val LOOKUP_HOUSE_NAME_OR_NUMBER = "1"
        private val SELECTED_ADDRESS = AddressDataModel(singleLineAddress = "1 Fictional Road, FA1 1AA", postcode = "FA1 1AA")
    }
}
