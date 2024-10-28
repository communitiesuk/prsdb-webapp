package uk.gov.communities.prsdb.webapp.multipageforms

import kotlin.reflect.full.primaryConstructor

interface FormModel {
    // Get the property names in the order they're defined in the primary constructor
    fun getFieldNames(): List<String> = this::class.primaryConstructor?.parameters?.map { it.name!! }!!
}
