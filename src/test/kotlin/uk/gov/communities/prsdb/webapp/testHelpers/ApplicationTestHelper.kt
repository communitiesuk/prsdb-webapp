package uk.gov.communities.prsdb.webapp.testHelpers

import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

class ApplicationTestHelper {
    companion object {
        // Beans added by component annotation scanning use their simple name as the bean name by default
        val KClass<*>.simpleBeanName: String
            get() = this.java.simpleName

        // Beans with scopes use their simple name with the scope prefix as the bean name by default
        val KClass<*>.scopedBeanName: String
            get() = "scopedTarget.${this.java.simpleName}"

        // Beans added by @Import annotations use their fully qualified class name as the bean name by default
        val KClass<*>.importedBeanName: String
            get() = this.java.name

        // Beans with explicit names can retrieve them by reflecting on their annotation
        // e.g. getExplicitBeanName<Component>(YourComponentClassName::class),
        inline fun <reified TAnnotation : Annotation> KClass<*>.getExplicitBeanName(): String =
            when (TAnnotation::class) {
                Component::class -> this.java.getAnnotation(Component::class.java)?.value
                Service::class -> this.java.getAnnotation(Service::class.java)?.value
                Configuration::class -> this.java.getAnnotation(Configuration::class.java)?.value
                else -> throw IllegalArgumentException("Unsupported annotation type: ${TAnnotation::class}")
            } ?: throw IllegalArgumentException("${TAnnotation::class} present on ${this.simpleName} but no explicit name provided")

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
