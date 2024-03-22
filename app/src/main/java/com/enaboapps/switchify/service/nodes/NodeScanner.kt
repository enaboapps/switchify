package com.enaboapps.switchify.service.nodes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enaboapps.switchify.keyboard.KeyboardAccessibilityManager
import com.enaboapps.switchify.keyboard.KeyboardLayoutInfo
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.service.scanning.tree.ScanTree
import com.enaboapps.switchify.service.utils.KeyboardBridge
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * NodeScanner is a class that handles the scanning of nodes.
 * It implements the NodeUpdateDelegate interface.
 * It uses a ScanTree instance to manage the scanning process.
 */
class NodeScanner : NodeUpdateDelegate {
    /**
     * Context in which the NodeScanner is started.
     */
    private lateinit var context: Context

    /**
     * ScanTree instance used for managing the scanning process.
     */
    lateinit var scanTree: ScanTree

    /**
     * List of Node instances that are currently being managed.
     */
    private var nodes: List<Node> = emptyList()

    /**
     * BroadcastReceiver that listens for keyboard layout updates.
     */
    private val keyboardLayoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val jsonLayoutInfo =
                intent.getStringExtra(KeyboardAccessibilityManager.EXTRA_KEYBOARD_LAYOUT_INFO)
            val layoutInfo = Gson().fromJson(jsonLayoutInfo, KeyboardLayoutInfo::class.java)
            updateNodesWithLayoutInfo(layoutInfo)
        }
    }

    /**
     * Starts the NodeScanner.
     * Sets the nodeUpdateDelegate of the NodeExaminer to this instance and starts the timeout.
     * Also starts listening for keyboard layout updates.
     *
     * @param context The context in which the NodeScanner is started.
     */
    fun start(context: Context) {
        this.context = context
        NodeExaminer.nodeUpdateDelegate = this
        startTimeoutToRevertToCursor()
        startListeningForKeyboardLayoutInfo(context)
        scanTree = ScanTree(
            context = context,
            stopScanningOnSelect = true,
            individualHighlightingItemsInTreeItem = false
        )
    }

    /**
     * Registers the keyboardLayoutReceiver to listen for keyboard layout updates.
     *
     * @param context The context in which the receiver is registered.
     */
    private fun startListeningForKeyboardLayoutInfo(context: Context) {
        LocalBroadcastManager.getInstance(context).registerReceiver(
            keyboardLayoutReceiver,
            IntentFilter(KeyboardAccessibilityManager.ACTION_KEYBOARD_LAYOUT_INFO)
        )
    }

    /**
     * Unregisters the keyboardLayoutReceiver.
     *
     * @param context The context in which the receiver is unregistered.
     */
    private fun stopListeningForKeyboardLayoutInfo(context: Context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(keyboardLayoutReceiver)
    }

    /**
     * Updates the nodes with the layout info from the keyboard.
     * It creates a new list of nodes from the layout info and updates the nodes.
     *
     * @param layoutInfo KeyboardLayoutInfo instance.
     */
    private fun updateNodesWithLayoutInfo(layoutInfo: KeyboardLayoutInfo) {
        val newNodes = layoutInfo.keys.map { Node.fromKeyInfo(it) }
        newNodes.forEach { println(it) }
        updateNodes(newNodes)
    }

    /**
     * Starts a timeout that resets the scanTree and changes the state of the ScanMethod
     * if the state is ITEM_SCAN and there are no nodes after 5 seconds.
     */
    fun startTimeoutToRevertToCursor() {
        val timeoutJob = Job()
        val timeoutScope = CoroutineScope(Dispatchers.Default + timeoutJob)

        timeoutScope.launch {
            delay(5000)
            if (ScanMethod.getType() == ScanMethod.MethodType.ITEM_SCAN && this@NodeScanner.nodes.isEmpty()) {
                withContext(Dispatchers.Main) {
                    scanTree.reset()
                    ScanMethod.setType(ScanMethod.MethodType.CURSOR)
                    println("ScanMethod changed to cursor")
                }
            } else {
                println("ScanMethod not changed, nodes.size: ${this@NodeScanner.nodes.size}")
            }
        }
    }

    /**
     * Updates the nodes and rebuilds the scanTree.
     * If no nodes are present, it starts the timeout.
     *
     * @param nodes List of new Node instances.
     */
    private fun updateNodes(nodes: List<Node>) {
        this.nodes = nodes
        scanTree.buildTree(nodes)
        if (nodes.isEmpty()) {
            startTimeoutToRevertToCursor()
        }
    }

    /**
     * Cleans up the NodeScanner by shutting down the scanTree.
     */
    fun cleanup() {
        scanTree.shutdown()
    }

    /**
     * Called when the nodes are updated.
     * Updates the nodes if our keyboard is not active and a keyboard is not visible.
     *
     * @param nodes List of new Node instances.
     */
    override fun onNodesUpdated(nodes: List<Node>) {
        if (!KeyboardBridge.isOurKeyboardActive(context) && !KeyboardBridge.isKeyboardVisible) {
            updateNodes(nodes)
        }
    }
}