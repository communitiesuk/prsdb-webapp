package uk.gov.communities.prsdb.webapp.forms.pages

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import kotlin.test.assertEquals

class PageWithContentProviderTests {
    @Test
    fun `enrichModel adds the provided content to the model`() {
        val providedContent = mapOf("key" to "value")

        val testPage =
            PageWithContentProvider(
                formModel = FormModel::class,
                templateName = "any",
                content = emptyMap(),
            ) { providedContent }

        val modelAndView = ModelAndView()

        testPage.enrichModel(modelAndView, filteredJourneyData = null)

        assertEquals(providedContent, modelAndView.modelMap.toMap())
    }

    @Test
    fun `PageWithContentProvider constructor does not call the content provider`() {
        assertDoesNotThrow {
            PageWithContentProvider(
                formModel = FormModel::class,
                templateName = "any",
                content = emptyMap(),
            ) { throw Exception("contentProvider called during PageWithContentProvider construction") }
        }
    }
}
