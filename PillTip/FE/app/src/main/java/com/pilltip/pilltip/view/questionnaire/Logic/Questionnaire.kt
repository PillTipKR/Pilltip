package com.pilltip.pilltip.view.questionnaire.Logic

data class DosageEntry(
    var amPm: String? = null,
    var hour: Int? = null,
    var minute: Int? = null,
    var alarm_on_off : Boolean? = true,
    val dosageUnit: String = "회"
)

fun String.toKoreanGender(): String {
    return when (this) {
        "MALE" -> "남"
        "FEMALE" -> "여"
        else -> "기타"
    }
}