package uk.gov.communities.prsdb.webapp.forms.journeys

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
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
                    .addStepToEnd(TestStepModel("1"))
                    .build()

            // Act && assert
            assertFalse(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns false if the current step hasn't been completed`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .initialised()
                    .addStepToEndWithoutJourneyData(TestStepModel("1", isSatisfied = true))
                    .addStepToEndWithoutJourneyData(TestStepModel("2", isSatisfied = true))
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
                    .addStepToEnd(TestStepModel("1", isSatisfied = false))
                    .addStepToEnd(TestStepModel("2", isSatisfied = false))
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
                    .addStepToEnd(TestStepModel("1", isSatisfied = true))
                    .addStepToEnd(TestStepModel("2", isSatisfied = false))
                    .build()

            // Act && assert
            assertTrue(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns false if it hasn't been initialised and there is no initial step`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .withMissingFirstStep()
                    .addStepToEnd(TestStepModel("1", isSatisfied = true))
                    .addStepToEnd(TestStepModel("2", isSatisfied = false))
                    .withMissingFirstStep()
                    .build()

            // Act && assert
            assertFalse(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns false if it hasn't been initialised and there are multiple initial steps`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .addStepToEnd(TestStepModel("1", isSatisfied = true))
                    .addStepToEnd(TestStepModel("2", isSatisfied = false))
                    .addStepToEnd(TestStepModel("1", isSatisfied = true))
                    .build()

            // Act && assert
            assertFalse(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns true if it hasn't been initialised and there is a valid initial step`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .addStepToEnd(TestStepModel("1", isSatisfied = true))
                    .addStepToEnd(TestStepModel("2", isSatisfied = false))
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
                    .addStepToEnd(TestStepModel(firstUrlSegment, isSatisfied = true))
                    .addStepToEnd(TestStepModel("2", isSatisfied = false))
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
                    .addStepToEnd(TestStepModel("1", isSatisfied = true))
                    .addStepToEnd(TestStepModel(subsequentUrlSegment, isSatisfied = false))
                    .build()

            // Act
            val nextStepDetails = testIterator.next()

            // Assert
            assertEquals(TestStepId(subsequentUrlSegment), nextStepDetails.step.id)
        }

        @Test
        fun `next adds the current step's data to the filtered journey data of the next step`() {
            // Arrange
            val currentStepModel = TestStepModel("1", isSatisfied = true)
            val builder =
                TestIteratorBuilder()
                    .onStep(1)
                    .addStepToEnd(currentStepModel)
                    .addStepToEnd(TestStepModel("2", isSatisfied = true))
            val pageDataForStep = builder.getDataForStep(currentStepModel.urlPathSegment)

            val testIterator = builder.build()

            // Act
            val nextStepDetails = testIterator.next()

            // Assert
            assertEquals(pageDataForStep, nextStepDetails.filteredJourneyData[currentStepModel.urlPathSegment])
        }

        @Test
        fun `next does not add the next step's data to the filtered journey data of the next step`() {
            // Arrange
            val stepModel = TestStepModel("1", isSatisfied = true)
            val builder = TestIteratorBuilder().addStepToEnd(stepModel)

            val testIterator = builder.build()

            // Act
            val nextStepDetails = testIterator.next()

            // Assert
            assertNull(nextStepDetails.filteredJourneyData[stepModel.urlPathSegment])
        }

        @Test
        fun `next does not change any of the filtered journey data except the for the current steps data`() {
            // Arrange
            val builder =
                TestIteratorBuilder()
                    .onStep(4)
                    .addStepToEnd(TestStepModel("1", isSatisfied = true))
                    .addStepToEnd(TestStepModel("2", isSatisfied = true))
                    .addStepToEnd(TestStepModel("3", isSatisfied = true))
                    .addStepToEnd(TestStepModel("4", isSatisfied = true))
                    .addStepToEnd(TestStepModel("5", isSatisfied = true))
                    .addStepToEnd(TestStepModel("6", isSatisfied = true))

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
                    .addStepToEnd(TestStepModel("1", isSatisfied = false))
                    .addStepToEnd(TestStepModel("2", isSatisfied = false))
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
                    .addStepToEnd(TestStepModel("1"))
                    .build()

            // Act && assert
            assertThrows<NoSuchElementException> { testIterator.next() }
        }
    }

    @Nested
    inner class ImmutableJourneyDataTests {
        @Test
        fun `Changing the initialising journey data after the iterator is created does not change the iterator`() {
            // Arrange
            val previousModel = TestStepModel("1")
            val builder =
                TestIteratorBuilder()
                    .onStep(2)
                    .addStepToEnd(previousModel)
                    .addStepToEnd(TestStepModel("2"))
                    .addStepToEnd(TestStepModel("3"))

            val testIterator = builder.build()

            // Act
            builder.clearJourneyData()
            val testStepDetails = testIterator.next()

            // Assert
            assertTrue(testStepDetails.filteredJourneyData.containsKey(previousModel.urlPathSegment))
        }

        @Test
        fun `Changing the current step's filtered journey data does not affect the next step's journey data`() {
            // Arrange
            val previousModel = TestStepModel("1")
            val testIterator =
                TestIteratorBuilder()
                    .onStep(1)
                    .addStepToEnd(previousModel)
                    .addStepToEnd(TestStepModel("2"))
                    .addStepToEnd(TestStepModel("3"))
                    .build()
            val currentStepDetails = testIterator.next()

            // Act
            currentStepDetails.filteredJourneyData.clear()
            val nextStepDetails = testIterator.next()

            // Assert
            assertTrue(nextStepDetails.filteredJourneyData.containsKey(previousModel.urlPathSegment))
        }

        @Test
        fun `If a nextStep function updates the journey data, that does not affect the next step's journey data`() {
            // Arrange
            val previousModel = TestStepModel("1")
            val testIterator =
                TestIteratorBuilder()
                    .onStep(2)
                    .addStepToEnd(previousModel)
                    .addStepToEnd(TestStepModel("2", customNextActionAddition = { journeyData -> journeyData.clear() }))
                    .addStepToEnd(TestStepModel("3"))
                    .build()

            // Act
            val nextStepDetails = testIterator.next()

            // Assert
            assertTrue(nextStepDetails.filteredJourneyData.containsKey(previousModel.urlPathSegment))
        }
    }
}
