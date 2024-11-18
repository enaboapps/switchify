package com.enaboapps.switchify.service.menu

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import com.enaboapps.switchify.R
import com.enaboapps.switchify.service.methods.nodes.Node

/**
 * This class represents a page of the menu
 * @property context The context of the menu page
 * @property rowsOfMenuItems The rows of menu items
 * @property showNavMenuItems Whether to show navigation menu items
 * @property navRowItems The navigation row items
 * @property pageIndex The index of the page
 * @property maxPageIndex The maximum index of the page
 * @property onMenuPageChanged The action to perform when the page is changed
 */
class MenuPage(
    val context: Context,
    private val rowsOfMenuItems: List<List<MenuItem>>,
    private val showNavMenuItems: Boolean,
    private val navRowItems: List<MenuItem>,
    private val pageIndex: Int,
    private val maxPageIndex: Int,
    val onMenuPageChanged: (pageIndex: Int) -> Unit
) {
    private var baseLayout: LinearLayout = LinearLayout(context)
    private var menuChangeBtn: MenuItem? = null

    init {
        baseLayout.orientation = LinearLayout.VERTICAL
        baseLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * Get the menu items of the page
     * @return The menu items of the page
     */
    fun getMenuItems(): List<MenuItem> {
        val menuItems = mutableListOf<MenuItem>()
        rowsOfMenuItems.forEach { rowItems ->
            rowItems.forEach { menuItem ->
                menuItems.add(menuItem)
            }
        }
        if (showNavMenuItems) {
            menuItems.addAll(navRowItems)
        }
        menuChangeBtn?.let { menuItems.add(it) }
        return menuItems
    }

    /**
     * Translate the menu items to nodes
     * @return The nodes of the menu items
     */
    fun translateMenuItemsToNodes(): List<Node> {
        val nodes = mutableListOf<Node>()
        getMenuItems().forEach { menuItem ->
            nodes.add(
                Node.fromMenuItem(menuItem)
            )
        }
        return nodes
    }

    /**
     * Get the layout of the menu
     * @return The layout of the menu
     */
    fun getMenuLayout(): LinearLayout {
        baseLayout.removeAllViews()

        rowsOfMenuItems.forEach { rowItems ->
            val rowLayout = createRowLayout()
            rowItems.forEach { menuItem ->
                menuItem.inflate(rowLayout)
            }
            baseLayout.addView(rowLayout)
        }

        if (showNavMenuItems) {
            val navButtonView = createNavButtonView()
            navRowItems.forEach { menuItem ->
                menuItem.inflate(navButtonView, 75, 65)
            }

            if (maxPageIndex > 0) {
                menuChangeBtn = MenuItem(
                    id = "change_page",
                    drawableId = R.drawable.ic_change_menu_page,
                    drawableDescription = "Change page",
                    closeOnSelect = false,
                    action = { changePage() }
                )
                menuChangeBtn?.inflate(navButtonView, 75, 65)
            }

            baseLayout.addView(navButtonView)
        }

        return baseLayout
    }

    /**
     * Get the navigation items of the page
     * @return The navigation items of the page
     */
    private fun createNavButtonView(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.gravity = Gravity.CENTER_HORIZONTAL
                it.topMargin = 24
            }
        }
    }

    /**
     * Create a row layout
     * @return The row layout
     */
    private fun createRowLayout(): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.gravity = Gravity.CENTER_HORIZONTAL }
        }
    }

    /**
     * Change the page
     */
    private fun changePage() {
        var newPageIndex = pageIndex
        if (pageIndex == maxPageIndex) {
            newPageIndex = 0
        } else {
            newPageIndex++
        }
        onMenuPageChanged(newPageIndex)
    }
}
