package com.pilltip.pilltip.view.search.Logic

fun removeMarkdown(text: String): String {
    return text
        .replace(Regex("(?m)^#{1,6}\\s*"), "") // #### 제목 제거
        .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1") // **볼드체** 제거
        .replace(Regex("\\*(.*?)\\*"), "$1") // *기울임* 제거
        .replace(Regex("`(.*?)`"), "$1") // `코드` 제거
        .replace(Regex("~~(.*?)~~"), "$1") // ~~취소선~~ 제거
}

fun convertDurTitle(title: String): String {
    return when (title) {
        "임부금기" -> "임산부주의"
        "병용금기" -> "병용주의"
        "노인금기" -> "노약자주의"
        "연령금기" -> "연령제한주의"
        "효능군중복주의" -> "중복주의"
        else -> title
    }
}