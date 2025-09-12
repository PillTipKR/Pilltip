package com.pilltip.pilltip.view.friend.logic

import java.time.LocalDate

fun calculateBirthDate(year: Int, month: Int, day: Int): Pair<String, Int>  {
    val dateStr = "%04d-%02d-%02d".format(year, month, day)
    val calculatedAge = calculateRealAge(year, month, day)
    return dateStr to calculatedAge
}

fun calculateRealAge(year: Int, month: Int, day: Int): Int {
    val today = LocalDate.now()
    val birthDate = LocalDate.of(year, month, day)
    var age = today.year - birthDate.year

    if (today < birthDate.plusYears(age.toLong()))
        age--

    return age
}