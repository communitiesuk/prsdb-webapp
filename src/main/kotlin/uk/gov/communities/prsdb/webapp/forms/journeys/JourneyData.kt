package uk.gov.communities.prsdb.webapp.forms.journeys

typealias JourneyData = MutableMap<String, Any?>

typealias PageData = MutableMap<String, Any?>

fun objectToStringKeyedMap(obj: Any?): JourneyData? {
    if (obj == null) return null
    val initialMap: MutableMap<*, *> = obj as? MutableMap<*, *> ?: return null
    val result: JourneyData = mutableMapOf()
    for ((key, value) in initialMap) {
        val stringKey = key as? String ?: return null
        result[stringKey] = value
    }
    return result
}
