package uk.gov.communities.prsdb.webapp.forms

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.testHelpers.builders.TestIteratorBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.builders.TestStepModel
import kotlin.test.assertEquals

class ReachableStepDetailsIteratorTests {
    @Nested
    inner class ConstructionTests {
        @Test
        fun `ReachableStepDetailsIterator cannot be constructed if the given initial step is invalid (doesn't exist)`() {
            // Arrange
            val testIteratorBuilder =
                TestIteratorBuilder()
                    .withNextStep(TestStepModel("2", isSatisfied = true))

            // Act && assert
            assertThrows<NoSuchElementException> { testIteratorBuilder.build() }
        }

        @Test
        fun `ReachableStepDetailsIterator cannot be constructed if the given initial step is invalid (not unique)`() {
            // Arrange
            val initialStepUrlSegment = "initial step segment"
            val testIteratorBuilder =
                TestIteratorBuilder()
                    .withInitialStep(TestStepModel(initialStepUrlSegment, isSatisfied = true))
                    .withNextStep(TestStepModel(initialStepUrlSegment, isSatisfied = false))

            // Act && assert
            assertThrows<NoSuchElementException> { testIteratorBuilder.build() }
        }

        @Test
        fun `ReachableStepDetailsIterator can be constructed if the given initial step is valid`() {
            // Arrange
            val testIteratorBuilder =
                TestIteratorBuilder()
                    .withInitialStep(TestStepModel("1", isSatisfied = true))

            // Act && assert
            assertDoesNotThrow { testIteratorBuilder.build() }
        }
    }

    @Nested
    inner class HasNextTests {
        @Test
        fun `hasNext returns false if the current step is a leaf and its ancestors have no unvisited children`() {
            // Arrange
            val leftReachableStepUrlSegment = "leftReachableStepUrlSegment"
            val rightReachableStepUrlSegment = "rightReachableStepUrlSegment"
            val testIterator =
                TestIteratorBuilder()
                    .withInitialStep(
                        TestStepModel(
                            "1",
                            isSatisfied = true,
                            customReachableStepUrlSegments = listOf(leftReachableStepUrlSegment, rightReachableStepUrlSegment),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            leftReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            rightReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).onStep(3)
                    .build()

            // Act && assert
            assertFalse(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns false if the current step is an incomplete parent and its ancestors have no unvisited children`() {
            // Arrange
            val leftReachableStepUrlSegment = "leftReachableStepUrlSegment"
            val rightReachableStepUrlSegment = "rightReachableStepUrlSegment"
            val childOfRightReachableStepUrlSegment = "childOfRightReachableStepUrlSegment"
            val testIterator =
                TestIteratorBuilder()
                    .withInitialStep(
                        TestStepModel(
                            "1",
                            isSatisfied = true,
                            customReachableStepUrlSegments = listOf(leftReachableStepUrlSegment, rightReachableStepUrlSegment),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            leftReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            rightReachableStepUrlSegment,
                            isSatisfied = false,
                            customReachableStepUrlSegments = listOf(childOfRightReachableStepUrlSegment),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            childOfRightReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).onStep(3)
                    .build()

            // Act && assert
            assertFalse(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns true if the current step is a leaf and its ancestors have unvisited children`() {
            // Arrange
            val leftReachableStepUrlSegment = "leftReachableStepUrlSegment"
            val rightReachableStepUrlSegment = "rightReachableStepUrlSegment"
            val testIterator =
                TestIteratorBuilder()
                    .withInitialStep(
                        TestStepModel(
                            "1",
                            isSatisfied = true,
                            customReachableStepUrlSegments = listOf(leftReachableStepUrlSegment, rightReachableStepUrlSegment),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            leftReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            rightReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).onStep(2)
                    .build()

            // Act && assert
            assertTrue(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns true if the current step is an incomplete parent and its ancestors have unvisited children`() {
            // Arrange
            val leftReachableStepUrlSegment = "leftReachableStepUrlSegment"
            val rightReachableStepUrlSegment = "rightReachableStepUrlSegment"
            val childOfLeftReachableStepUrlSegment = "childOfLeftReachableStepUrlSegment"
            val testIterator =
                TestIteratorBuilder()
                    .withInitialStep(
                        TestStepModel(
                            "1",
                            isSatisfied = true,
                            customReachableStepUrlSegments = listOf(leftReachableStepUrlSegment, rightReachableStepUrlSegment),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            leftReachableStepUrlSegment,
                            isSatisfied = false,
                            customReachableStepUrlSegments = listOf(childOfLeftReachableStepUrlSegment),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            rightReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            childOfLeftReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).onStep(2)
                    .build()

            // Act && assert
            assertTrue(testIterator.hasNext())
        }

        @Test
        fun `hasNext returns true if the current step is a completed parent`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .withInitialStep(TestStepModel("1", isSatisfied = true))
                    .withNextStep(TestStepModel("2"))
                    .onStep(1)
                    .build()

            // Act && assert
            assertTrue(testIterator.hasNext())
        }
    }

    @Nested
    inner class NextTests {
        @Test
        fun `next returns the initial step the first time it is called`() {
            // Arrange
            val initialStepUrlSegment = "test step id"
            val testIterator =
                TestIteratorBuilder()
                    .withInitialStep(TestStepModel(initialStepUrlSegment, isSatisfied = true))
                    .withNextStep(TestStepModel("2", isSatisfied = false))
                    .build()

            // Act
            val nextStepDetails = testIterator.next()

            // Assert
            assertEquals(initialStepUrlSegment, nextStepDetails.step.id.urlPathSegment)
        }

        @Test
        fun `next returns an incomplete parent step's closest unvisited reachable step (horizontally) if it has been called before`() {
            // Arrange
            // Arrange
            val leftReachableStepUrlSegment = "leftReachableStepUrlSegment"
            val middleReachableStepUrlSegment = "middleReachableStepUrlSegment"
            val rightReachableStepUrlSegment = "rightReachableStepUrlSegment"
            val childOfLeftReachableStepUrlSegment = "childOfLeftReachableStepUrlSegment"
            val testIterator =
                TestIteratorBuilder()
                    .withInitialStep(
                        TestStepModel(
                            "1",
                            isSatisfied = true,
                            customReachableStepUrlSegments =
                                listOf(
                                    leftReachableStepUrlSegment,
                                    middleReachableStepUrlSegment,
                                    rightReachableStepUrlSegment,
                                ),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            leftReachableStepUrlSegment,
                            isSatisfied = false,
                            customReachableStepUrlSegments = listOf(childOfLeftReachableStepUrlSegment),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            middleReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            rightReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            childOfLeftReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).onStep(2)
                    .build()

            // Act
            val nextStepDetails = testIterator.next()

            // Assert
            assertEquals(middleReachableStepUrlSegment, nextStepDetails.step.id.urlPathSegment)
        }

        @Test
        fun `next returns a leaf step's closest unvisited reachable step (vertically) if it has been called before`() {
            // Arrange
            // Arrange
            val leftReachableStepUrlSegment = "leftReachableStepUrlSegment"
            val rightReachableStepUrlSegment = "rightReachableStepUrlSegment"
            val leftChildOfLeftReachableStepUrlSegment = "leftChildOfLeftReachableStepUrlSegment"
            val rightChildOfLeftReachableStepUrlSegment = "rightChildOfLeftReachableStepUrlSegment"
            val testIterator =
                TestIteratorBuilder()
                    .withInitialStep(
                        TestStepModel(
                            "1",
                            isSatisfied = true,
                            customReachableStepUrlSegments =
                                listOf(
                                    leftReachableStepUrlSegment,
                                    rightReachableStepUrlSegment,
                                ),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            leftReachableStepUrlSegment,
                            isSatisfied = true,
                            customReachableStepUrlSegments =
                                listOf(
                                    leftChildOfLeftReachableStepUrlSegment,
                                    rightChildOfLeftReachableStepUrlSegment,
                                ),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            rightReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            leftChildOfLeftReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            rightChildOfLeftReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).onStep(3)
                    .build()

            // Act
            val nextStepDetails = testIterator.next()

            // Assert
            assertEquals(rightChildOfLeftReachableStepUrlSegment, nextStepDetails.step.id.urlPathSegment)
        }

        @Test
        fun `next returns a completed parent step's leftmost child if it has been called before`() {
            // Arrange
            val leftReachableStepUrlSegment = "leftReachableStepUrlSegment"
            val rightReachableStepUrlSegment = "rightReachableStepUrlSegment"
            val testIterator =
                TestIteratorBuilder()
                    .withInitialStep(
                        TestStepModel(
                            "1",
                            isSatisfied = true,
                            customReachableStepUrlSegments = listOf(leftReachableStepUrlSegment, rightReachableStepUrlSegment),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            leftReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).withNextStep(
                        TestStepModel(
                            rightReachableStepUrlSegment,
                            customReachableStepUrlSegments = emptyList(),
                        ),
                    ).onStep(1)
                    .build()

            // Act
            val nextStepDetails = testIterator.next()

            // Assert
            assertEquals(leftReachableStepUrlSegment, nextStepDetails.step.id.urlPathSegment)
        }

        @Test
        fun `next throws an exception if there are no more reachable steps`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .withInitialStep(TestStepModel("1"))
                    .build()

            val testIteratorSpy = spy(testIterator)
            whenever(testIteratorSpy.hasNext()).thenReturn(false)

            // Act && assert
            assertThrows<NoSuchElementException> { testIteratorSpy.next() }
        }

        @Test
        fun `next adds the next step's data to the filtered journey data of the next step`() {
            // Arrange
            val subsequentStepUrlSegment = "test step id"
            val testIteratorBuilder =
                TestIteratorBuilder()
                    .withInitialStep(TestStepModel("1", isSatisfied = true))
                    .withNextStep(TestStepModel(subsequentStepUrlSegment))
                    .onStep(1)
            val pageDataForStep = testIteratorBuilder.getDataForStep(subsequentStepUrlSegment)
            val testIterator = testIteratorBuilder.build()

            // Act
            val nextStepDetails = testIterator.next()

            // Assert
            assertEquals(pageDataForStep, nextStepDetails.filteredJourneyData[subsequentStepUrlSegment])
        }

        @Test
        fun `next does not change any of the filtered journey data except the for the current steps data`() {
            // Arrange
            val testIterator =
                TestIteratorBuilder()
                    .withInitialStep(TestStepModel("1", isSatisfied = true))
                    .withNextStep(TestStepModel("2", isSatisfied = true))
                    .withNextStep(TestStepModel("3", isSatisfied = true))
                    .withNextStep(TestStepModel("4", isSatisfied = true))
                    .withNextStep(TestStepModel("5", isSatisfied = true))
                    .withNextStep(TestStepModel("6"))
                    .onStep(4)
                    .build()

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
    }
}
