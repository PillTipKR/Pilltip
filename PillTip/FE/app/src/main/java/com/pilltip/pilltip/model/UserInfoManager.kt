package com.pilltip.pilltip.model

import android.content.Context
import com.google.gson.Gson
import com.pilltip.pilltip.model.signUp.UserData

object UserInfoManager {
    private const val PREF_NAME = "user_info"
    private const val KEY_USER_DATA = "user_data"

    private val gson = Gson()

    fun saveUserData(context: Context, userData: UserData) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(userData)
        prefs.edit().putString(KEY_USER_DATA, json).apply()
    }

    fun getUserData(context: Context): UserData? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_USER_DATA, null)
        return json?.let {
            try {
                gson.fromJson(it, UserData::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun selectProfile(context: Context, selectedId: Long) {
        val current = getUserData(context) ?: return
        val updatedList = current.userList?.map {
            it.copy(isSelected = it.userId == selectedId)
        }
        saveUserData(context, current.copy(userList = updatedList ?: listOf()))
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_USER_DATA)
            .apply()
    }
}