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
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordAddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordContactTypeFormModel
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
    fun `afterStepIsReached saves the entered name and placeholder email when the contact type is email`() {
        val stepConfig = SaveSuperiorLandlordStepConfig()
        setupStepsWithAnswers(true, SuperiorLandlordContactType.EMAIL, ENTERED_NAME)
        whenever(mockState.superiorLandlordsMap).thenReturn(null)
        whenever(mockState.editingSuperiorLandlordId).thenReturn(null)
        whenever(mockState.nextSuperiorLandlordId).thenReturn(null)

        stepConfig.afterStepIsReached(mockState)

        val expectedSuperiorLandlord =
            SuperiorLandlordDataModel(
                detailsKnown = true,
                name = ENTERED_NAME,
                email = SaveSuperiorLandlordStepConfig.PLACEHOLDER_EMAIL,
            )
        assertEquals(mapOf(1 to expectedSuperiorLandlord), captureSavedMap())
        verify(mockState).nextSuperiorLandlordId = 2
    }

    @Test
    fun `afterStepIsReached saves the entered name and placeholder address when the contact type is postal address`() {
        val stepConfig = SaveSuperiorLandlordStepConfig()
        setupStepsWithAnswers(true, SuperiorLandlordContactType.POSTAL_ADDRESS, ENTERED_NAME)
        whenever(mockState.superiorLandlordsMap).thenReturn(emptyMap())
        whenever(mockState.editingSuperiorLandlordId).thenReturn(null)
        whenever(mockState.nextSuperiorLandlordId).thenReturn(1)

        stepConfig.afterStepIsReached(mockState)

        val expectedSuperiorLandlord =
            SuperiorLandlordDataModel(
                detailsKnown = true,
                name = ENTERED_NAME,
                address = SaveSuperiorLandlordStepConfig.PLACEHOLDER_ADDRESS,
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
        whenever(mockState.superiorLandlordProvideDetailsStep).thenReturn(provideDetailsStep)
        whenever(provideDetailsStep.formModelOrNull).thenReturn(null)

        assertThrows<PrsdbWebException> { stepConfig.afterStepIsReached(mockState) }
    }

    @Test
    fun `afterStepIsReached throws when details are provided but the contact type answer is missing`() {
        val stepConfig = SaveSuperiorLandlordStepConfig()
        whenever(mockState.superiorLandlordProvideDetailsStep).thenReturn(provideDetailsStep)
        whenever(provideDetailsStep.formModelOrNull).thenReturn(SuperiorLandlordProvideDetailsFormModel(provideDetails = true))
        whenever(mockState.superiorLandlordContactTypeStep).thenReturn(contactTypeStep)
        whenever(contactTypeStep.formModelOrNull).thenReturn(null)

        assertThrows<PrsdbWebException> { stepConfig.afterStepIsReached(mockState) }
    }

    @Test
    fun `afterStepIsReached throws when details are provided but the name answer is missing`() {
        val stepConfig = SaveSuperiorLandlordStepConfig()
        whenever(mockState.superiorLandlordProvideDetailsStep).thenReturn(provideDetailsStep)
        whenever(provideDetailsStep.formModelOrNull).thenReturn(SuperiorLandlordProvideDetailsFormModel(provideDetails = true))
        whenever(mockState.superiorLandlordContactTypeStep).thenReturn(contactTypeStep)
        whenever(contactTypeStep.formModelOrNull).thenReturn(
            SuperiorLandlordContactTypeFormModel(contactType = SuperiorLandlordContactType.EMAIL),
        )
        whenever(mockState.superiorLandlordNameStep).thenReturn(nameStep)
        whenever(nameStep.formModelOrNull).thenReturn(null)

        assertThrows<PrsdbWebException> { stepConfig.afterStepIsReached(mockState) }
    }

    // TODO PDJB-1975: add a test that afterStepIsReached throws when the contact type is email but the email answer is missing
    // TODO PDJB-1976: add a test that afterStepIsReached throws when the contact type is postal address but the address answer is missing

    private fun setupStepsWithAnswers(
        provideDetails: Boolean,
        contactType: SuperiorLandlordContactType? = null,
        name: String? = null,
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
        whenever(mockState.superiorLandlordAddressTask).thenReturn(addressTask)
    }

    private fun captureSavedMap(): Map<Int, SuperiorLandlordDataModel> {
        val captor = argumentCaptor<Map<Int, SuperiorLandlordDataModel>>()
        verify(mockState).superiorLandlordsMap = captor.capture()
        return captor.firstValue
    }

    companion object {
        private const val ENTERED_NAME = "Entered superior landlord"
    }
}
