package uk.gov.communities.prsdb.webapp.forms.journeys

typealias JourneyData = Map<String, Any?>

typealias PageData = Map<String, Any?>

fun objectToStringKeyedMap(obj: Any?): JourneyData? {
    if (obj == null) return null
    val initialMap: Map<*, *> = obj as? Map<*, *> ?: return null
    return initialMap.map { (key, value) -> (key as? String ?: return null) to value }.associate { it }
}
