package uk.gov.communities.prsdb.webapp.forms.pages

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.validation.BindingResult
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class CheckAnswersPageTests {
    @Mock
    private lateinit var mockJourneyDataService: JourneyDataService

    @InjectMocks
    private lateinit var checkAnswersPage: TestCheckAnswersPage

    @Mock
    private lateinit var mockBindingResult: BindingResult

    @Test
    fun `enrichModel adds formData and filteredJourneyData to the model, then calls furtherEnrichModel`() {
        val modelAndView = ModelAndView()
        val filteredJourneyData = mapOf(FORM_DATA_ROW_KEY to "formDataRowValue", "furtherEnrichModelKey" to "furtherEnrichModelValue")

        checkAnswersPage.enrichModel(modelAndView, filteredJourneyData)

        assertEquals(modelAndView.modelMap["formData"], createFormData(filteredJourneyData))
        assertEquals(modelAndView.modelMap["submittedFilteredJourneyData"], Json.encodeToString(filteredJourneyData))
        assertEquals(modelAndView.modelMap["furtherEnrichModelKey"], "furtherEnrichModelValue")
    }

    // TODO PRSD-1298: Update 'isSatisfied' tests to match implementation
    @Test
    fun `isSatisfied returns true if the submittedFilteredJourneyData values match the current journeyData`() {
        val submittedFilteredJourneyData = mapOf("key1" to "value1", "key2" to "value2")
        val formModel = createCheckAnswersFormModel(submittedFilteredJourneyData)
        whenever(mockBindingResult.target).thenReturn(formModel)

        val journeyData = submittedFilteredJourneyData + ("otherKey" to "otherValue")
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        assertTrue(checkAnswersPage.isSatisfied(mockBindingResult))
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun `isSatisfied removes journeyData from session and throws an error if the submittedFilteredJourneyData values don't match the current journeyData`() {
        val submittedFilteredJourneyData = mapOf("key1" to "value1", "key2" to "value2")
        val formModel = createCheckAnswersFormModel(submittedFilteredJourneyData)
        whenever(mockBindingResult.target).thenReturn(formModel)

        val journeyData = submittedFilteredJourneyData + ("key1" to "differentValue")
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        assertThrows<PrsdbWebException> { checkAnswersPage.isSatisfied(mockBindingResult) }
        verify(mockJourneyDataService).removeJourneyDataFromSession()
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun `isSatisfied removes journeyData from session and throws an error if the submittedFilteredJourneyData values don't exist in the current journeyData`() {
        val submittedFilteredJourneyData = mapOf("key1" to "value1", "key2" to "value2")
        val formModel = createCheckAnswersFormModel(submittedFilteredJourneyData)
        whenever(mockBindingResult.target).thenReturn(formModel)

        val journeyData = submittedFilteredJourneyData - "key1"
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        assertThrows<PrsdbWebException> { checkAnswersPage.isSatisfied(mockBindingResult) }
        verify(mockJourneyDataService).removeJourneyDataFromSession()
    }

    @Test
    fun `CheckAnswersPage can handle validation for answers with different types`() {
        val modelAndView = ModelAndView()
        val filteredJourneyData =
            mapOf(
                FORM_DATA_ROW_KEY to "anyValue",
                "stringKey" to "stringValue",
                "numberKey" to 123,
                "booleanKey" to true,
                "dateKey" to LocalDate.of(2021, 1, 1),
                "mapKey" to
                    mapOf(
                        "stringKey" to "stringValue",
                        "numberKey" to 123,
                        "booleanKey" to true,
                        "dateKey" to LocalDate.of(2021, 1, 1),
                    ),
            )
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(filteredJourneyData)

        checkAnswersPage.enrichModel(modelAndView, filteredJourneyData)
        val submittedFilteredJourneyData = modelAndView.modelMap["submittedFilteredJourneyData"]
        val formData = mapOf("submittedFilteredJourneyData" to submittedFilteredJourneyData)
        val bindingResult = checkAnswersPage.bindDataToFormModel(AlwaysTrueValidator(), formData)

        assertTrue(checkAnswersPage.isSatisfied(bindingResult))
    }

    class TestCheckAnswersPage(
        journeyDataService: JourneyDataService,
    ) : CheckAnswersPage(content = emptyMap(), journeyDataService) {
        override fun getFormData(filteredJourneyData: JourneyData): List<SummaryListRowViewModel> = createFormData(filteredJourneyData)

        override fun furtherEnrichModel(
            modelAndView: ModelAndView,
            filteredJourneyData: JourneyData?,
        ) {
            filteredJourneyData?.entries?.forEach { (key, value) ->
                if (key != FORM_DATA_ROW_KEY) {
                    modelAndView.addObject(key, value)
                }
            }
        }
    }

    companion object {
        const val FORM_DATA_ROW_KEY = "formDataRowKey"

        private fun createFormData(journeyData: JourneyData) =
            listOf(SummaryListRowViewModel(FORM_DATA_ROW_KEY, journeyData[FORM_DATA_ROW_KEY]!!, changeUrl = null))

        private fun createCheckAnswersFormModel(submittedFilteredJourneyData: Map<String, String>): CheckAnswersFormModel =
            CheckAnswersFormModel().apply {
                this.submittedFilteredJourneyData = Json.encodeToString(submittedFilteredJourneyData)
            }
    }
}
