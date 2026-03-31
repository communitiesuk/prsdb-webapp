package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import uk.gov.communities.prsdb.webapp.integration.IntegrationTest
import kotlin.reflect.KClass
import kotlin.test.assertEquals

class JourneyFrameworkComponentTests : IntegrationTest() {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    companion object {
        @JvmStatic
        val typesToTest =
            listOf(
                AbstractStepConfig::class,
                Task::class,
                JourneyState::class,
                JourneyStep::class,
            )
    }

    @ParameterizedTest
    @MethodSource("getTypesToTest")
    fun `All journey framework component types are prototype scoped`(kClass: KClass<Any>) {
        val beanFactory = applicationContext.autowireCapableBeanFactory
        val beanNames = applicationContext.getBeanNamesForType(kClass.java)

        val incorrectScopeBeans = mutableListOf<String>()
        for (beanName in beanNames) {
            if (!beanFactory.isPrototype(beanName)) {
                incorrectScopeBeans.add(beanName)
            }
        }

        assertEquals(
            emptyList(),
            incorrectScopeBeans,
            "The following ${kClass.simpleName} beans do not have prototype scope: ${incorrectScopeBeans.joinToString("\n -" ,"\n -")}",
        )
    }
}
