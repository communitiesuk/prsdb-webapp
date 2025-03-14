package uk.gov.communities.prsdb.webapp.forms.pages

import org.junit.jupiter.api.Test
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
}
