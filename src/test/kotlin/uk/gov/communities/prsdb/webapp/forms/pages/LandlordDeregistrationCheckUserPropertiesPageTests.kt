package uk.gov.communities.prsdb.webapp.forms.pages

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock
import org.springframework.validation.BindingResult
import org.springframework.web.servlet.ModelAndView

class LandlordDeregistrationCheckUserPropertiesPageTests {
    @Test
    fun `enrichModel throws an error as this page should not be displayed`() {
        // Arrange
        val modelAndView = ModelAndView()
        modelAndView.addObject(BindingResult.MODEL_KEY_PREFIX + "formModel", mock<BindingResult>())

        // Act
        val page = LandlordDeregistrationCheckUserPropertiesPage()

        // Assert
        assertThrows<IllegalStateException> { page.enrichModel(modelAndView, null) }
    }
}
