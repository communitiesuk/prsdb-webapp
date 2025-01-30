package uk.gov.communities.prsdb.webapp.helpers

import jakarta.servlet.http.HttpServletRequest
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.util.UriComponentsBuilder

class URIQueryBuilder private constructor(
    private val uriComponentsBuilder: UriComponentsBuilder,
) {
    companion object {
        fun fromHTTPServletRequest(httpServletRequest: HttpServletRequest): URIQueryBuilder =
            URIQueryBuilder(
                UriComponentsBuilder
                    .fromUriString(httpServletRequest.requestURI)
                    .queryParams(LinkedMultiValueMap(httpServletRequest.parameterMap.mapValues { it.value.toList() })),
            )

        private fun getHiddenParam(name: String) = "_$name"
    }

    fun build() = uriComponentsBuilder.encode().build()

    fun updateParam(
        name: String,
        value: Any,
    ): URIQueryBuilder {
        uriComponentsBuilder.replaceQueryParam(name, value)
        return this
    }

    fun updateParam(
        name: String,
        values: Collection<Any>,
    ): URIQueryBuilder {
        uriComponentsBuilder.replaceQueryParam(name, values)
        return this
    }

    fun removeParam(name: String): URIQueryBuilder {
        uriComponentsBuilder
            .replaceQueryParam(name)
            .replaceQueryParam(getHiddenParam(name))
        return this
    }

    fun removeParams(names: Collection<String>): URIQueryBuilder {
        names.forEach { removeParam(it) }
        return this
    }

    fun removeParamValue(
        name: String,
        value: Any,
    ): URIQueryBuilder {
        val currentURI = build()

        val selectedParamValues = currentURI.queryParams[name]?.toMutableList() ?: return this
        selectedParamValues.remove(value)

        return if (selectedParamValues.isEmpty()) {
            removeParam(name)
        } else {
            updateParam(name, selectedParamValues)
        }
    }
}
