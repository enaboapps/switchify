package com.enaboapps.switchify.service.scanning.tree

import android.content.Context
import android.util.Log
import com.enaboapps.switchify.service.scanning.ScanMethodBase
import com.enaboapps.switchify.service.scanning.ScanNodeInterface
import com.enaboapps.switchify.service.scanning.ScanSettings
import com.enaboapps.switchify.service.scanning.ScanningScheduler

/**
 * This class represents the main scanning tree for switch access functionality.
 * It orchestrates the interactions between various components to manage the scanning process.
 *
 * @property context The application context.
 * @property stopScanningOnSelect Whether to stop scanning after a selection is made.
 */
class ScanTree(
    private val context: Context,
    private var stopScanningOnSelect: Boolean = false
) : ScanMethodBase {

    /** The settings for scanning behavior. */
    private val scanSettings = ScanSettings(context)

    /** The list of ScanTreeItems that make up the scanning tree. */
    private val tree: MutableList<ScanTreeItem> = mutableListOf()

    /** The builder responsible for constructing the scanning tree. */
    private val builder = ScanTreeBuilder(context, scanSettings)

    /** The navigator responsible for traversing the scanning tree. */
    private lateinit var navigator: ScanTreeNavigator

    /** The selector responsible for handling selection actions. */
    private lateinit var selector: ScanTreeSelector

    /** The highlighter responsible for visual feedback during scanning. */
    private lateinit var highlighter: ScanTreeHighlighter

    /** The scheduler for automatic scanning. */
    private var scanningScheduler: ScanningScheduler? = null

    /** The flag to track if manual scanning is active. */
    private var isManualScanActive = false

    init {
        initializeComponents()
    }

    /**
     * Initializes the navigator, selector, and highlighter components.
     */
    private fun initializeComponents() {
        navigator = ScanTreeNavigator(tree, scanSettings)
        selector = ScanTreeSelector(tree, navigator, scanSettings, stopScanningOnSelect)
        highlighter = ScanTreeHighlighter(tree, scanSettings)
    }

    /**
     * Builds the scanning tree from a list of scan nodes.
     *
     * @param nodes The list of ScanNodeInterface objects to build the tree from.
     * @param itemThreshold The threshold for determining if a node is in the same item (in dp).
     */
    fun buildTree(nodes: List<ScanNodeInterface>, itemThreshold: Int = 40) {
        tree.clear()
        tree.addAll(builder.buildTree(nodes, itemThreshold))
        initializeComponents() // Reinitialize components with the new tree
    }

    /**
     * Checks if the manual scan setup is valid.
     *
     * @return True if the manual scan setup is valid, false otherwise.
     */
    private fun checkManualScanSetup(): Boolean {
        if (scanSettings.isManualScanMode() && !isManualScanActive) {
            isManualScanActive = true
            highlighter.unhighlightAll()
            highlightCurrent()
            return true
        }
        return false
    }

    /**
     * Performs the selection action based on the current scanning state.
     * This method handles the main logic flow of the scanning process.
     */
    override fun performSelectionAction() {
        try {
            if (checkManualScanSetup()) {
                return
            }

            if (scanSettings.isAutoScanMode()) {
                setup()
            }

            if (scanningScheduler?.isScanning() == false && scanSettings.isAutoScanMode()) {
                startScanning()
                Log.d("ScanTree", "Scanning started")
                return
            }

            unhighlightCurrent()

            if (handleEscape(true)) {
                return
            }

            val selectionMade = selector.performSelection()

            if (selectionMade && stopScanningOnSelect) {
                stopScanning()
            } else {
                pauseScanning()
                resumeScanning()
            }
            if (!selectionMade) {
                highlightCurrent() // Ensure we highlight after selecting an item or group
            }
        } catch (e: Exception) {
            Log.e("ScanTree", "Error performing selection: ${e.message}")
        }
    }

    /**
     * Checks if the escape should be highlighted.
     * @param highlight Whether to highlight the escape.
     * @return True if the escape should be highlighted, false otherwise.
     */
    private fun highlightEscape(highlight: Boolean = true): Boolean {
        if (highlight) {
            highlighter.highlightEscape(
                navigator.currentTreeItem,
                navigator.currentGroup,
                navigator.currentColumn,
                navigator.isInTreeItem,
                !navigator.isScanningGroups
            )
        }
        return highlight
    }

    /**
     * Handles the escape logic for items and groups.
     * This method is called when an escape condition is met during scanning.
     * @param confirm Whether to confirm the escape action.
     * @return True if the escape was confirmed, false otherwise.
     */
    private fun handleEscape(confirm: Boolean = false): Boolean {
        var actionWasTaken = false
        if (navigator.handleEscape()) {
            highlighter.unhighlightEscape(
                navigator.currentTreeItem,
                navigator.currentGroup,
                navigator.currentColumn,
                navigator.isInTreeItem,
                !navigator.isScanningGroups
            )
            actionWasTaken = if (confirm) {
                navigator.confirmEscape()
            } else {
                navigator.denyEscape()
            }
        }
        if (actionWasTaken) {
            highlightCurrent()
        }
        return actionWasTaken
    }

    /**
     * Manually steps forward in the scanning tree.
     * This method is used for manual navigation through the tree.
     */
    override fun stepForward() {
        if (checkManualScanSetup()) {
            return
        }

        unhighlightCurrent()
        val movementSuccessful = navigator.moveSelectionToNext()
        if (highlightEscape(!movementSuccessful)) {
            return
        }
        highlightCurrent()
    }

    /**
     * Manually steps backward in the scanning tree.
     * This method is used for manual navigation through the tree.
     */
    override fun stepBackward() {
        if (checkManualScanSetup()) {
            return
        }

        unhighlightCurrent()
        val movementSuccessful = navigator.moveSelectionToPrevious()
        if (highlightEscape(!movementSuccessful)) {
            return
        }
        highlightCurrent()
    }

    /**
     * Swaps the scanning direction between vertical and horizontal.
     * This method is called when the user wants to change the scanning direction.
     */
    override fun swapScanDirection() {
        navigator.swapScanDirection()
        if (scanSettings.isAutoScanMode()) {
            resumeScanning()
        }

        // If scanning is active, highlight the current item
        if (scanningScheduler?.isScanning() == true) {
            highlightCurrent()
        }
    }

    /**
     * Sets up the scanning scheduler if required.
     * This method initializes the scanning scheduler if it hasn't been set up yet.
     */
    private fun setup() {
        if (scanningScheduler == null) {
            reset()
            scanningScheduler = ScanningScheduler(context) {
                stepAutoScanning()
            }
        }
    }

    /**
     * Steps through the scanning tree automatically.
     * This method is called by the scanning scheduler during automatic scanning.
     */
    private fun stepAutoScanning() {
        unhighlightCurrent()

        if (handleEscape()) {
            return
        }

        val movementSuccessful = navigator.moveSelectionToNextOrPrevious()

        if (highlightEscape(!movementSuccessful)) {
            return
        }

        highlightCurrent()
    }

    /**
     * Highlights the current item, group, or node based on the current state.
     */
    private fun highlightCurrent() {
        Log.d(
            "ScanTree",
            "Highlighting current: treeItem=${navigator.currentTreeItem}, group=${navigator.currentGroup}, column=${navigator.currentColumn}, isInTreeItem=${navigator.isInTreeItem}, isScanningGroups=${navigator.isScanningGroups}"
        )
        highlighter.highlightCurrent(
            navigator.currentTreeItem,
            navigator.currentGroup,
            navigator.currentColumn,
            navigator.isInTreeItem,
            navigator.isScanningGroups
        )
    }

    /**
     * Unhighlights the current item, group, or node.
     */
    private fun unhighlightCurrent() {
        highlighter.unhighlightCurrent(
            navigator.currentTreeItem,
            navigator.currentGroup,
            navigator.currentColumn,
            navigator.isInTreeItem,
            navigator.isScanningGroups
        )
    }

    /**
     * Starts the scanning process.
     */
    override fun startScanning() {
        setup()
        if (tree.isNotEmpty()) {
            reset()
            highlightCurrent() // Highlight the first item
            if (scanSettings.isAutoScanMode()) {
                Log.d("ScanTree", "startScanning")
                scanningScheduler?.startScanning()
            }
        }
    }

    /**
     * Pauses the scanning process.
     */
    override fun pauseScanning() {
        scanningScheduler?.pauseScanning()
    }

    /**
     * Resumes the scanning process.
     */
    override fun resumeScanning() {
        scanningScheduler?.resumeScanning()
    }

    /**
     * Stops the scanning process.
     */
    override fun stopScanning() {
        scanningScheduler?.stopScanning()
    }

    /**
     * Resets the scanning tree to its initial state.
     */
    fun reset() {
        stopScanning()
        highlighter.unhighlightAll()
        navigator.reset()
        isManualScanActive = false
    }

    /**
     * Shuts down the scanning scheduler.
     */
    fun shutdown() {
        reset()
        scanningScheduler?.shutdown()
        scanningScheduler = null
    }

    /**
     * Clears the scanning tree.
     */
    fun clearTree() {
        reset() // Reset the scanning state
        tree.clear() // Clear the tree
    }

    override fun cleanup() {
        shutdown()
    }
}