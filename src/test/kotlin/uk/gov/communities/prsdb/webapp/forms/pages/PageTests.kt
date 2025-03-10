package uk.gov.communities.prsdb.webapp.forms.pages

import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import jakarta.validation.constraints.NotNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.validation.beanvalidation.SpringValidatorAdapter
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
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
                mapOf("testKey" to "testValue"),
                shouldDisplaySectionHeader = true,
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
    fun `getModelAndView populates the model and returns the template name`() {
        // Arrange
        val formData = mapOf("testProperty" to "testPropertyValue")
        val previousUrl = "/previous"
        val sectionHeader = SectionHeaderViewModel("testSectionHeader", 3, 5)

        // Act
        val result = testPage.getModelAndView(validator, formData, previousUrl, null, sectionHeader)

        // Assert
        assertIs<BindingResult>(result.model[BindingResult.MODEL_KEY_PREFIX + "formModel"])
        val bindingResult: BindingResult = result.model[BindingResult.MODEL_KEY_PREFIX + "formModel"] as BindingResult
        assertContains(result.model, "testKey")
        assertEquals("testValue", result.model["testKey"])
        assertContains(result.model, "backUrl")
        assertEquals(previousUrl, result.model["backUrl"])
        val propertyValue = bindingResult.getRawFieldValue("testProperty")
        assertEquals("testPropertyValue", propertyValue)
        assertEquals("index", result.viewName)

        assertEquals(sectionHeader, result.model["sectionHeaderInfo"])
    }

    @Test
    fun `getModelAndView does not add a section header if the page does not need one`() {
        // Arrange
        val formData = mapOf("testProperty" to "testPropertyValue")
        val previousUrl = "/previous"
        val sectionHeader = SectionHeaderViewModel("testSectionHeader", 3, 5)

        val testPageWithoutSectionHeader =
            Page(
                TestFormModel::class,
                "index",
                mapOf("testKey" to "testValue"),
                shouldDisplaySectionHeader = false,
            )
        // Act
        val result = testPageWithoutSectionHeader.getModelAndView(validator, formData, previousUrl, null, sectionHeader)

        assertNull(result.model["sectionHeaderInfo"])
    }

    @Test
    fun `getModelAndView throws an error if the section heading is requested but not found`() {
        // Arrange
        val formData = mapOf("testProperty" to "testPropertyValue")
        val previousUrl = "/previous"
        val filteredJourneyData: JourneyData = mapOf()
        val sectionHeaderInfo = null

        // Act, Assert
        assertThrows<PrsdbWebException> {
            testPage.getModelAndView(validator, formData, previousUrl, filteredJourneyData, sectionHeaderInfo)
        }
    }
}
