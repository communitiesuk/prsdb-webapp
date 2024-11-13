package uk.gov.communities.prsdb.webapp.forms

import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import jakarta.validation.constraints.NotNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.validation.beanvalidation.SpringValidatorAdapter
import org.springframework.validation.support.BindingAwareModelMap
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TestFormModel : FormModel {
    @NotNull
    var testProperty: String? = null
}

class PageTests {
    private lateinit var testPage: Page
    private lateinit var validatorFactory: ValidatorFactory
    private lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        testPage =
            Page(
                TestFormModel::class,
                "index",
                mutableMapOf("testKey" to "testValue"),
            )
        validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = SpringValidatorAdapter(validatorFactory.validator)
    }

    @AfterEach
    fun tearDown() {
        validatorFactory.close()
    }

    @Test
    fun `isSatisfied returns true when validation is satisfied`() {
        // Arrange
        val formData = mapOf("testProperty" to "testPropertyValue")

        // Act
        val result = testPage.isSatisfied(validator, formData)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `isSatisfied returns false when validation is not satisfied`() {
        // Arrange
        val formData = mapOf("anotherProperty" to "testPropertyValue", "testProperty" to null)

        // Act
        val result = testPage.isSatisfied(validator, formData)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `populateModelAndGetTemplateName populates the model and returns the template name`() {
        // Arrange
        val formData = mapOf("testProperty" to "testPropertyValue")
        val model = BindingAwareModelMap()
        val previousUrl = "/previous"

        // Act
        val result = testPage.populateModelAndGetTemplateName(validator, model, formData, previousUrl)

        // Assert
        assertIs<BindingResult>(model[BindingResult.MODEL_KEY_PREFIX + "formModel"])
        val bindingResult: BindingResult = model[BindingResult.MODEL_KEY_PREFIX + "formModel"] as BindingResult
        assertContains(model, "testKey")
        assertEquals("testValue", model["testKey"])
        assertContains(model, "backUrl")
        assertEquals(previousUrl, model["backUrl"])
        val propertyValue = bindingResult.getRawFieldValue("testProperty")
        assertEquals("testPropertyValue", propertyValue)
        assertEquals("index", result)
    }
}
