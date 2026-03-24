package uk.gov.communities.prsdb.webapp.journeys

// TODO PDJB-730: Remove JourneyData
typealias JourneyData = Map<String, Any?>

typealias FormData = Map<String, Any?>

fun objectToStringKeyedMap(obj: Any?): JourneyData? {
    if (obj == null) return null
    val initialMap: Map<*, *> = obj as? Map<*, *> ?: return null
    return initialMap.map { (key, value) -> (key as? String ?: return null) to value }.associate { it }
}

inline fun <reified T> objectToTypedStringKeyedMap(obj: Any?): Map<String, T>? {
    if (obj == null) return null
    val initialMap: Map<*, *> = obj as? Map<*, *> ?: return null
    return initialMap.map { (key, value) -> (key as? String ?: return null) to (value as? T ?: return null) }.associate { it }
}
