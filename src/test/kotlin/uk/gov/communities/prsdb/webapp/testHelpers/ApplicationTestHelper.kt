package uk.gov.communities.prsdb.webapp.testHelpers

import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.type.classreading.CachingMetadataReaderFactory
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.util.ClassUtils
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.TaskHasName
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.TaskOnly
import kotlin.reflect.KClass

class ApplicationTestHelper {
    companion object {
        private const val BASE_PACKAGE = "uk.gov.communities.prsdb.webapp"

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
                        ?.contains(BASE_PACKAGE) ?: false
                }.map { it.lowercase() }
                .toSet()

        fun getProcessOnlyBeanTypes(): Set<Class<*>> {
            val classLoader = ApplicationTestHelper::class.java.classLoader
            val resourceResolver = PathMatchingResourcePatternResolver(classLoader)
            val metadataReaderFactory = CachingMetadataReaderFactory(resourceResolver)
            val resourcePath = ClassUtils.convertClassNameToResourcePath(BASE_PACKAGE)

            // Component scanning evaluates conditions so wouldn't find TaskOnly beans
            return resourceResolver
                .getResources("classpath*:$resourcePath/**/*.class")
                .asSequence()
                .map { metadataReaderFactory.getMetadataReader(it).annotationMetadata }
                .filter { it.isConcrete && !it.isAnnotation }
                .filter { metadata ->
                    metadata.annotations.stream(Conditional::class.java).anyMatch { annotation ->
                        annotation.getClassArray("value").any { condition ->
                            condition == TaskOnly::class.java || TaskHasName::class.java.isAssignableFrom(condition)
                        }
                    }
                }.map { Class.forName(it.className, false, classLoader) }
                .toSet()
        }
    }
}
