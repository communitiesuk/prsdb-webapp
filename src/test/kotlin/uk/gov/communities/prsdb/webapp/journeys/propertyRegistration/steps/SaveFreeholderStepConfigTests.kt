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
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholderAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderContactTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderProvideDetailsFormModel

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
    fun `afterStepIsReached replaces the freeholder being edited without changing the next id`() {
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
    fun `afterStepIsReached saves the entered name and placeholder email when the contact type is email`() {
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
                email = SaveFreeholderStepConfig.PLACEHOLDER_EMAIL,
            )
        assertEquals(mapOf(1 to expectedFreeholder), captureSavedMap())
        verify(mockState).nextFreeholderId = 2
    }

    @Test
    fun `afterStepIsReached saves the entered name and placeholder address when the contact type is postal address`() {
        val stepConfig = SaveFreeholderStepConfig()
        setupStepsWithAnswers(YesOrNo.YES, FreeholderContactType.POSTAL_ADDRESS)
        whenever(mockState.freeholdersMap).thenReturn(emptyMap())
        whenever(mockState.editingFreeholderId).thenReturn(null)
        whenever(mockState.nextFreeholderId).thenReturn(1)

        stepConfig.afterStepIsReached(mockState)

        val expectedFreeholder =
            FreeholderDataModel(
                detailsKnown = true,
                name = ENTERED_NAME,
                address = SaveFreeholderStepConfig.PLACEHOLDER_ADDRESS,
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
        whenever(mockState.freeholderAddressTask).thenReturn(addressTask)
    }

    private fun captureSavedMap(): Map<Int, FreeholderDataModel> {
        val captor = argumentCaptor<Map<Int, FreeholderDataModel>>()
        verify(mockState).freeholdersMap = captor.capture()
        return captor.firstValue
    }

    companion object {
        private const val ENTERED_NAME = "Entered freeholder name"
    }
}
