package uk.gov.communities.prsdb.webapp.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.AbstractMessageSource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.text.MessageFormat
import java.util.Locale

@Configuration
class MessageSourceConfig {
    @Bean
    fun messageSource(): MessageSource = YamlMessageSource("classpath:messages")
}

class YamlMessageSource(
    private val messagesFolderPath: String,
) : AbstractMessageSource() {
    private val messages: Map<String, String> by lazy { loadAllMessages() }

    private fun loadAllMessages(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val keySources = mutableMapOf<String, String>()
        val resolver = PathMatchingResourcePatternResolver()
        val resources = resolver.getResources("$messagesFolderPath/*.yml")

        for (resource in resources) {
            val fileName = resource.filename?.removeSuffix(".yml") ?: continue
            val yamlMap = loadYamlFile(resource.inputStream)
            val flattenedMessages =
                if (fileName == "default") {
                    // For default.yml, use keys as-is
                    flattenYamlMap(yamlMap)
                } else {
                    // For other files (e.g., addLocalCouncilUser.yml), prefix keys with the filename
                    flattenYamlMap(yamlMap, fileName)
                }

            for ((key, value) in flattenedMessages) {
                if (key in result) {
                    throw IllegalStateException(
                        "Duplicate message key '$key' found in '$fileName.yml' and '${keySources[key]}.yml'",
                    )
                }
                result[key] = value
                keySources[key] = fileName
            }
        }
        return result
    }

    private fun loadYamlFile(inputStream: InputStream): Map<String, Any> {
        val yaml = Yaml()
        return yaml.load(inputStream) ?: emptyMap()
    }

    private fun flattenYamlMap(
        map: Map<String, Any>,
        prefix: String = "",
    ): Map<String, String> {
        val result = mutableMapOf<String, String>()

        for ((key, value) in map) {
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"
            when (value) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    result.putAll(flattenYamlMap(value as Map<String, Any>, fullKey))
                }

                is String -> result[fullKey] = value
                else -> result[fullKey] = value.toString()
            }
        }
        return result
    }

    override fun resolveCode(
        code: String,
        locale: Locale,
    ): MessageFormat? {
        val message = messages[code] ?: return null
        return createMessageFormat(message, locale)
    }

    override fun resolveCodeWithoutArguments(
        code: String,
        locale: Locale,
    ): String? = messages[code]
}
