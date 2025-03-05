package uk.gov.communities.prsdb.webapp.forms.journeys

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.testHelpers.TestIteratorBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.TestStepModel
import kotlin.test.assertEquals

data class TestStepId(
    override val urlPathSegment: String,
) : StepId

class ReachableStepDetailsIteratorTest {
    @Nested
    inner class HasNextTests {
        @Test
        fun `hasNext returns false if the journey is completed and the current step is the last step`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .initialised()
                    .withFirstStep(TestStepModel("1"))
                    .build()

            // Act && assert
            assertFalse(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns false if the current step hasn't been completed`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .onStep(2)
                    .withFirstStep(TestStepModel("1", isSatisfied = true))
                    .withNextStepWithoutPageData(TestStepModel("2", isSatisfied = true))
                    .withNextStepWithoutPageData(TestStepModel("3", isSatisfied = true))
                    .build()

            // Act && assert
            assertFalse(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns false if the current step has been completed with invalid data`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .initialised()
                    .withFirstStep(TestStepModel("1", isSatisfied = false))
                    .withNextStep(TestStepModel("2", isSatisfied = false))
                    .build()

            // Act && assert
            assertFalse(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns true if the current step has been completed with valid data and isn't the last step in the journey`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .initialised()
                    .withFirstStep(TestStepModel("1", isSatisfied = true))
                    .withNextStep(TestStepModel("2", isSatisfied = false))
                    .build()

            // Act && assert
            assertTrue(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns false if it hasn't been initialised and there is no initial step`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .withNextStep(TestStepModel("1", isSatisfied = true))
                    .withNextStep(TestStepModel("2", isSatisfied = false))
                    .build()

            // Act && assert
            assertFalse(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns false if it hasn't been initialised and there are multiple initial steps`() {
            // Arrange
            val initialStepSegment = "initial step segment"
            val testIterator =
                TestIteratorBuilder()
                    .withFirstStep(TestStepModel(initialStepSegment, isSatisfied = true))
                    .withNextStep(TestStepModel("2", isSatisfied = false))
                    .withNextStep(TestStepModel(initialStepSegment, isSatisfied = true))
                    .build()

            // Act && assert
            assertFalse(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns true if it hasn't been initialised and there is a valid initial step`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .withFirstStep(TestStepModel("1", isSatisfied = true))
                    .withNextStep(TestStepModel("2", isSatisfied = false))
                    .build()

            // Act && assert
            assertTrue(testIterator.hasNext())
        }
    }

    @Nested
    inner class NextTests {
        @Test
        fun `next returns the initial step if it hasn't been initialised`() {
            // Arrange
            val firstUrlSegment = "test step id"
            val testIterator =
                TestIteratorBuilder()
                    .withFirstStep(TestStepModel(firstUrlSegment, isSatisfied = true))
                    .withNextStep(TestStepModel("2", isSatisfied = false))
                    .build()

            // Act && assert
            assertEquals(TestStepId(firstUrlSegment), testIterator.next().step.id)
        }

        @Test
        fun `next returns the the current step's next action if it has initialised`() {
            // Arrange
            val subsequentUrlSegment = "test step id"
            val testIterator =
                TestIteratorBuilder()
                    .initialised()
                    .withFirstStep(TestStepModel("1", isSatisfied = true))
                    .withNextStep(TestStepModel(subsequentUrlSegment, isSatisfied = false))
                    .build()

            // Act
            val nextStepDetails = testIterator.next()

            // Assert
            assertEquals(TestStepId(subsequentUrlSegment), nextStepDetails.step.id)
        }

        @Test
        fun `next adds the next step's data to the filtered journey data of the next step`() {
            // Arrange
            val currentStepModel = TestStepModel("2", isSatisfied = true)
            val builder =
                TestIteratorBuilder()
                    .onStep(1)
                    .withFirstStep(TestStepModel("1", isSatisfied = true))
                    .withNextStep(currentStepModel)
            val pageDataForStep = builder.getDataForStep(currentStepModel.urlPathSegment)

            val testIterator = builder.build()

            // Act
            val nextStepDetails = testIterator.next()

            // Assert
            assertEquals(pageDataForStep, nextStepDetails.filteredJourneyData[currentStepModel.urlPathSegment])
        }

        @Test
        fun `next does not change any of the filtered journey data except the for the current steps data`() {
            // Arrange
            val builder =
                TestIteratorBuilder()
                    .onStep(4)
                    .withFirstStep(TestStepModel("1", isSatisfied = true))
                    .withNextStep(TestStepModel("2", isSatisfied = true))
                    .withNextStep(TestStepModel("3", isSatisfied = true))
                    .withNextStep(TestStepModel("4", isSatisfied = true))
                    .withNextStep(TestStepModel("5", isSatisfied = true))
                    .withNextStep(TestStepModel("6", isSatisfied = true))

            val testIterator = builder.build()

            // Act
            val currentStepDetails = testIterator.next()
            val nextStepDetails = testIterator.next()

            // Assert
            val previousFilteredJourneyData = currentStepDetails.filteredJourneyData
            for (key in previousFilteredJourneyData.keys) {
                if (key != nextStepDetails.step.id.urlPathSegment) {
                    assertEquals(previousFilteredJourneyData[key], nextStepDetails.filteredJourneyData[key])
                }
            }
        }

        @Test
        fun `next throws an exception if the current step does not have valid data`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .initialised()
                    .withFirstStep(TestStepModel("1", isSatisfied = false))
                    .withNextStep(TestStepModel("2", isSatisfied = false))
                    .build()

            // Act && assert
            assertThrows<NoSuchElementException> { testIterator.next() }
        }

        @Test
        fun `next throws an exception if the current step does not have a next action`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .initialised()
                    .withFirstStep(TestStepModel("1"))
                    .build()

            // Act && assert
            assertThrows<NoSuchElementException> { testIterator.next() }
        }
    }
}
