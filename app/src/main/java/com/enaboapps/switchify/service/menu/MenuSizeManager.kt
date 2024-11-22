package com.enaboapps.switchify.service.menu

import android.content.Context
import com.enaboapps.switchify.preferences.PreferenceManager

class MenuSizeManager(context: Context) {
    private val preferenceManager = PreferenceManager(context)

    data class MenuSize(
        val name: String,
        val textSize: Float,
        val textSizeWithIcon: Float,
        val itemWidth: Int,
        val itemHeight: Int,
        val itemsPerPage: Int
    )

    companion object {
        val menuSizes = listOf(
            MenuSize(
                name = "Small",
                textSize = 10f,
                textSizeWithIcon = 8f,
                itemWidth = 80,
                itemHeight = 70,
                itemsPerPage = 6
            ),
            MenuSize(
                name = "Medium",
                textSize = 14f,
                textSizeWithIcon = 12f,
                itemWidth = 130,
                itemHeight = 75,
                itemsPerPage = 4
            ),
            MenuSize(
                name = "Large",
                textSize = 18f,
                textSizeWithIcon = 16f,
                itemWidth = 170,
                itemHeight = 100,
                itemsPerPage = 4
            )
        )
    }

    fun fromName(name: String): MenuSize {
        return menuSizes.find { it.name == name } ?: menuSizes[0]
    }

    fun getMenuSize(): MenuSize =
        fromName(preferenceManager.getStringValue(PreferenceManager.PREFERENCE_KEY_MENU_SIZE))

    fun setMenuSize(menuSize: MenuSize) {
        preferenceManager.setStringValue(PreferenceManager.PREFERENCE_KEY_MENU_SIZE, menuSize.name)
    }
}