package com.enaboapps.switchify.service.scanning.tree

import com.enaboapps.switchify.service.scanning.ScanDirection
import com.enaboapps.switchify.service.scanning.ScanNodeInterface
import com.enaboapps.switchify.service.scanning.ScanSettings

/**
 * This class is responsible for navigating through the ScanTree structure.
 * It manages the current position and provides methods for moving between items, groups, and nodes.
 * The class supports both row-column scanning and sequential (non-row-column) scanning modes.
 *
 * @property tree The list of ScanTreeItems that make up the scanning tree.
 * @property scanSettings The settings for scanning behavior.
 */
class ScanTreeNavigator(
    private val tree: List<ScanTreeItem>,
    private val scanSettings: ScanSettings
) {
    /** The index of the current tree item being scanned. */
    var currentTreeItem = 0

    /** The index of the current group within the current tree item. */
    var currentGroup = 0

    /** The index of the current column within the current group or the current node in non-row-column mode. */
    var currentColumn = 0

    /** Indicates whether the scanning is currently within a tree item. */
    var isInTreeItem = false

    /* Tracks the current cycle of the scanning tree */
    var currentCycle = 0

    /** Indicates whether we're scanning groups or items within a group. */
    var isScanningGroups = true

    /** The current direction of scanning. */
    var scanDirection = ScanDirection.DOWN

    /** Indicates whether the current item should be escaped. */
    private var shouldEscapeItem = false

    /** Indicates whether the current group should be escaped. */
    private var shouldEscapeGroup = false

    /** Indicates whether row-column scanning is enabled based on scan settings. */
    private val isRowColumnScanEnabled: Boolean
        get() = scanSettings.isRowColumnScanEnabled()

    /**
     * A flattened list of all nodes in the tree, used when row-column scanning is disabled.
     * Computed lazily to avoid unnecessary processing when row-column scanning is enabled.
     */
    private val flattenedNodes: List<ScanNodeInterface> by lazy {
        tree.flatMap { it.children }
    }

    /**
     * Moves the selection to the next or previous element based on the current state and settings.
     * @return True if the movement was successful, false if an escape condition was met.
     */
    fun moveSelectionToNextOrPrevious(): Boolean {
        return when (scanDirection) {
            ScanDirection.DOWN, ScanDirection.RIGHT -> moveSelectionToNext()
            ScanDirection.UP, ScanDirection.LEFT -> moveSelectionToPrevious()
        }
    }

    /**
     * Moves the selection to the next element based on the current state and settings.
     * In non-row-column mode, it moves to the next node in the flattened list.
     * In row-column mode, it navigates through the tree structure.
     * @return True if the movement was successful, false if an escape condition was met.
     */
    fun moveSelectionToNext(): Boolean {
        if (!isRowColumnScanEnabled) {
            if (currentColumn < flattenedNodes.size - 1) {
                currentColumn++
            } else {
                currentColumn = 0
            }
            return true
        }

        return when {
            !isInTreeItem -> moveSelectionToNextTreeItem()
            isCurrentItemSingleGroup() -> moveSelectionToNextWithinGroup()
            scanSettings.isGroupScanEnabled() && isScanningGroups -> moveSelectionToNextGroup()
            else -> moveSelectionToNextWithinGroup()
        }
    }

    /**
     * Moves the selection to the previous element based on the current state and settings.
     * In non-row-column mode, it moves to the previous node in the flattened list.
     * In row-column mode, it navigates through the tree structure.
     * @return True if the movement was successful, false if an escape condition was met.
     */
    fun moveSelectionToPrevious(): Boolean {
        if (!isRowColumnScanEnabled) {
            if (currentColumn > 0) {
                currentColumn--
            } else {
                currentColumn = flattenedNodes.size - 1
            }
            return true
        }

        return when {
            !isInTreeItem -> moveSelectionToPreviousTreeItem()
            isCurrentItemSingleGroup() -> moveSelectionToPreviousWithinGroup()
            scanSettings.isGroupScanEnabled() && isScanningGroups -> moveSelectionToPreviousGroup()
            else -> moveSelectionToPreviousWithinGroup()
        }
    }

    /**
     * Moves the selection to the next element within the current group.
     * @return True if the movement was successful, false if an escape condition was met.
     */
    private fun moveSelectionToNextWithinGroup(): Boolean {
        val currentItem = getCurrentItem()
        return when {
            currentColumn < currentItem.getNodeCount(currentGroup) - 1 -> {
                currentColumn++
                true
            }

            isCurrentItemSingleGroup() -> {
                shouldEscapeItem = true
                false
            }

            scanSettings.isGroupScanEnabled() -> {
                shouldEscapeGroup = true
                false
            }

            else -> {
                shouldEscapeItem = true
                false
            }
        }
    }

    /**
     * Moves the selection to the previous element within the current group.
     * @return True if the movement was successful, false if an escape condition was met.
     */
    private fun moveSelectionToPreviousWithinGroup(): Boolean {
        return when {
            currentColumn > 0 -> {
                currentColumn--
                true
            }

            isCurrentItemSingleGroup() -> {
                shouldEscapeItem = true
                false
            }

            scanSettings.isGroupScanEnabled() -> {
                shouldEscapeGroup = true
                false
            }

            else -> {
                shouldEscapeItem = true
                false
            }
        }
    }

    /**
     * Moves the selection to the next group within the current tree item.
     * @return True if the movement was successful, false if an escape condition was met.
     */
    private fun moveSelectionToNextGroup(): Boolean {
        val currentItem = getCurrentItem()
        return when {
            currentGroup < currentItem.getGroupCount() - 1 -> {
                currentGroup++
                true
            }

            else -> {
                shouldEscapeItem = true
                false
            }
        }
    }

    /**
     * Moves the selection to the previous group within the current tree item.
     * @return True if the movement was successful, false if an escape condition was met.
     */
    private fun moveSelectionToPreviousGroup(): Boolean {
        return when {
            currentGroup > 0 -> {
                currentGroup--
                true
            }

            else -> {
                shouldEscapeItem = true
                false
            }
        }
    }

    /**
     * Moves the selection to the next tree item.
     * @return Always returns true as it wraps around to the first item if at the end.
     */
    private fun moveSelectionToNextTreeItem(): Boolean {
        if (currentTreeItem < tree.size - 1) {
            currentTreeItem++
        } else {
            currentTreeItem = 0
            currentCycle++
        }
        resetGroupAndColumn()
        return true
    }

    /**
     * Moves the selection to the previous tree item.
     * @return Always returns true as it wraps around to the last item if at the beginning.
     */
    private fun moveSelectionToPreviousTreeItem(): Boolean {
        if (currentTreeItem > 0) {
            currentTreeItem--
        } else {
            currentTreeItem = tree.size - 1
            currentCycle++
        }
        resetGroupAndColumn()
        return true
    }

    /**
     * Finds out if the current item has only one group.
     * @return True if the current item has only one group, false otherwise.
     */
    private fun isCurrentItemSingleGroup(): Boolean = getCurrentItem().getGroupCount() == 1

    /**
     * Resets the group and column indices to their initial values.
     * In non-row-column mode, it only resets the group index.
     */
    private fun resetGroupAndColumn() {
        currentGroup = 0
        currentColumn = if (isRowColumnScanEnabled) 0 else currentColumn
        isScanningGroups = scanSettings.isGroupScanEnabled()
    }

    /**
     * Handles the escape logic for items and groups.
     * @return True if an escape was handled, false otherwise.
     */
    fun handleEscape(): Boolean = shouldEscapeItem || shouldEscapeGroup

    /**
     * Handles the number of cycles for the scanning tree
     * @return True if cycles value has reached the user defined value, false otherwise.
     */
    fun handleCycles(): Boolean {
        val userDefinedCycles = scanSettings.getScanCycles()
        println("Cycles: $currentCycle, user defined: $userDefinedCycles")
        return currentCycle == userDefinedCycles
    }

    /**
     * Confirms the escape action and updates the navigation state accordingly.
     * @return True if the escape was confirmed, false otherwise.
     */
    fun confirmEscape(): Boolean {
        if (shouldEscapeItem) {
            shouldEscapeItem = false
            isInTreeItem = false
            scanDirection = ScanDirection.DOWN
            if (!isRowColumnScanEnabled) {
                currentColumn = 0
            }
            currentCycle = 0
            return true
        }

        if (shouldEscapeGroup) {
            shouldEscapeGroup = false
            isScanningGroups = true
            scanDirection = ScanDirection.RIGHT
            currentColumn = 0
            currentGroup = 0
            currentCycle = 0
            return true
        }

        return false
    }

    /**
     * Denies the escape action and resets the escape flags.
     * @return True if an escape was denied, false otherwise.
     */
    fun denyEscape(): Boolean {
        val setColumn: () -> Unit = {
            currentColumn = if (scanDirection == ScanDirection.RIGHT) {
                0
            } else {
                if (isRowColumnScanEnabled) getCurrentItem().getNodeCount(currentGroup) - 1
                else flattenedNodes.size - 1
            }
        }

        if (shouldEscapeItem) {
            shouldEscapeItem = false
            setColumn()
            currentCycle++
            if (isRowColumnScanEnabled) {
                currentGroup = if (scanDirection == ScanDirection.RIGHT) {
                    0
                } else {
                    getCurrentItem().getGroupCount() - 1
                }
            }
            return true
        }

        if (shouldEscapeGroup) {
            shouldEscapeGroup = false
            setColumn()
            return true
        }

        return false
    }

    /**
     * Gets the current ScanTreeItem.
     * @return The current ScanTreeItem.
     */
    private fun getCurrentItem(): ScanTreeItem = tree[currentTreeItem]

    /**
     * Swaps the scanning direction between vertical and horizontal.
     */
    fun swapScanDirection() {
        scanDirection = when (scanDirection) {
            ScanDirection.DOWN -> ScanDirection.UP
            ScanDirection.UP -> ScanDirection.DOWN
            ScanDirection.RIGHT -> ScanDirection.LEFT
            ScanDirection.LEFT -> ScanDirection.RIGHT
        }
    }

    /**
     * Selects the current group and switches to scanning items within the group.
     * Only applicable in row-column scanning mode.
     */
    fun selectGroup() {
        if (isRowColumnScanEnabled && scanSettings.isGroupScanEnabled()) {
            isScanningGroups = false
            currentColumn = 0
        }
    }

    /**
     * Resets the navigator to its initial state.
     */
    fun reset() {
        currentTreeItem = 0
        currentGroup = 0
        currentColumn = 0
        currentCycle = 0
        isInTreeItem = false
        isScanningGroups = scanSettings.isGroupScanEnabled()
        shouldEscapeItem = false
        shouldEscapeGroup = false
        scanDirection = ScanDirection.DOWN
    }

    /**
     * Gets the current ScanNodeInterface based on the scanning mode.
     * @return The current ScanNodeInterface, or null if not available.
     */
    fun getCurrentNode(): ScanNodeInterface? {
        return if (isRowColumnScanEnabled) {
            getCurrentItem().children.getOrNull(currentColumn)
        } else {
            flattenedNodes.getOrNull(currentColumn)
        }
    }
}