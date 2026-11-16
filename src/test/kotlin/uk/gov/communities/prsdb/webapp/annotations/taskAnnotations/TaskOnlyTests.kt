package uk.gov.communities.prsdb.webapp.annotations.taskAnnotations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.context.annotation.ConditionContext
import org.springframework.mock.env.MockEnvironment

class TaskOnlyTests {
    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("profileCombinations")
    fun `matches only when running a task`(
        activeProfiles: List<String>,
        expected: Boolean,
    ) {
        val environment = MockEnvironment().apply { setActiveProfiles(*activeProfiles.toTypedArray()) }
        val context = mock<ConditionContext> { on { this.environment } doReturn environment }

        assertEquals(expected, TaskOnly().matches(context, mock()))
    }

    companion object {
        @JvmStatic
        fun profileCombinations() =
            listOf(
                Arguments.of(listOf("default", "integration"), false),
                Arguments.of(listOf("web-server-deactivated", "scheduled-task", "integration"), true),
                Arguments.of(listOf("web-server-deactivated", "email-worker", "integration"), false),
                Arguments.of(listOf("web-server-deactivated", "scan-result-worker", "integration"), false),
            )
    }
}
