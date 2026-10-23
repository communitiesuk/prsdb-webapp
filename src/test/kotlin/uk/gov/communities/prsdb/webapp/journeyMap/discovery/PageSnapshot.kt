package uk.gov.communities.prsdb.webapp.journeyMap.discovery

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PageSnapshot(
    val url: String,
    val title: String = "",
    val h1: String = "",
    val hasForm: Boolean = false,
    val hasTaskList: Boolean = false,
    val action: String? = null,
    val enctype: String? = null,
    val fields: List<FormField> = emptyList(),
    val buttons: List<FormButton> = emptyList(),
    val errors: List<ErrorLink> = emptyList(),
    val changeLinks: List<PageLink> = emptyList(),
    val links: List<PageLink> = emptyList(),
    val contentLinks: List<PageLink> = emptyList(),
) {
    val hasFileInput: Boolean get() = fields.any { it.type == "file" }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(value: String): PageSnapshot = json.decodeFromString(value)
    }
}

@Serializable
data class FormField(
    val tag: String = "input",
    val type: String = "text",
    val name: String? = null,
    val id: String? = null,
    val value: String? = null,
    val checked: Boolean = false,
    val label: String = "",
    val autocomplete: String? = null,
    val inputmode: String? = null,
    val controls: String? = null,
    val conditional: String? = null,
    val dateGroup: String? = null,
    val options: List<SelectOption> = emptyList(),
)

@Serializable
data class SelectOption(
    val value: String,
    val label: String = "",
)

@Serializable
data class FormButton(
    val name: String? = null,
    val value: String? = null,
    val text: String = "",
    val type: String = "submit",
)

@Serializable
data class ErrorLink(
    val href: String? = null,
    val text: String = "",
)

@Serializable
data class PageLink(
    val href: String,
    val text: String = "",
)
