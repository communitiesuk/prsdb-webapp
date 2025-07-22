package uk.gov.communities.prsdb.webapp.forms.pages

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.validation.BindingResult
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
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
    fun `enrichModel adds filteredJourneyData to the model, then calls furtherEnrichModel`() {
        val modelAndView = ModelAndView()
        val filteredJourneyData = mapOf("furtherEnrichModelKey" to "furtherEnrichModelValue")

        checkAnswersPage.enrichModel(modelAndView, filteredJourneyData)

        assertEquals(modelAndView.modelMap["submittedFilteredJourneyData"], CheckAnswersFormModel.serializeJourneyData(filteredJourneyData))
        assertEquals(modelAndView.modelMap["furtherEnrichModelKey"], "furtherEnrichModelValue")
    }

    @Test
    fun `bindDataToFormModel adds journeyData to the formModel`() {
        val summaryListData = mapOf(CheckAnswersFormModel::submittedFilteredJourneyData.name to "{}")
        val journeyData = mapOf("key1" to "value1")
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        val bindingResult = checkAnswersPage.bindDataToFormModel(AlwaysTrueValidator(), summaryListData)

        val formModel = bindingResult.target as CheckAnswersFormModel
        assertEquals(journeyData, formModel.storedJourneyData)
    }

    // TODO PRSD-1298: Update 'isSatisfied' tests to match implementation
    @Test
    fun `isSatisfied returns true if the bindingResult doesn't contain errors`() {
        whenever(mockBindingResult.hasErrors()).thenReturn(false)

        assertTrue(checkAnswersPage.isSatisfied(mockBindingResult))
        verify(mockJourneyDataService, never()).removeJourneyDataAndContextIdFromSession()
    }

    @Test
    fun `isSatisfied removes journey context from session and throws an error if the binding result contains errors`() {
        whenever(mockBindingResult.hasErrors()).thenReturn(true)

        assertThrows<PrsdbWebException> { checkAnswersPage.isSatisfied(mockBindingResult) }
        verify(mockJourneyDataService).removeJourneyDataAndContextIdFromSession()
    }

    class TestCheckAnswersPage(
        journeyDataService: JourneyDataService,
    ) : CheckAnswersPage(content = emptyMap(), journeyDataService) {
        override fun furtherEnrichModel(
            modelAndView: ModelAndView,
            filteredJourneyData: JourneyData,
        ) {
            filteredJourneyData.entries.forEach { (key, value) -> modelAndView.addObject(key, value) }
        }
    }
}
