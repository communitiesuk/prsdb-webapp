package uk.gov.communities.prsdb.webapp.testHelpers

import org.springframework.context.ConfigurableApplicationContext
import kotlin.reflect.KClass

class ApplicationTestHelper {
    companion object {
        // Beans added by component annotation scanning use their simple name as the bean name by default
        val KClass<*>.simpleBeanName: String
            get() = this.java.simpleName

        fun getAvailableBeanNames(context: ConfigurableApplicationContext): Set<String> =
            context
                .beanDefinitionNames
                .filter {
                    context
                        .beanFactory
                        .getBeanDefinition(it)
                        .beanClassName
                        ?.contains("uk.gov.communities.prsdb.webapp") ?: false
                }.map { it.lowercase() }
                .toSet()
    }
}
