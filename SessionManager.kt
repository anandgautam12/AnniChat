package com.anand.annichat.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val sharedPref: SharedPreferences = context.getSharedPreferences("AnniChatPrefs", Context.MODE_PRIVATE)

    fun saveUser(id: String, name: String, email: String) {
        val editor = sharedPref.edit()
        editor.putString("user_id", id)
        editor.putString("user_name", name)
        editor.putString("user_email", email)
        editor.apply()
    }

    fun getUserId(): String? = sharedPref.getString("user_id", null)
    fun getUserName(): String? = sharedPref.getString("user_name", null)
    fun getUserEmail(): String? = sharedPref.getString("user_email", null)

    fun logout() {
        sharedPref.edit().clear().apply()
    }
}