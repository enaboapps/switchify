package com.enaboapps.switchify.service.methods.nodes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enaboapps.switchify.service.scanning.ScanMethod
import com.enaboapps.switchify.service.scanning.tree.ScanTree
import com.enaboapps.switchify.service.selection.SelectionHandler
import com.enaboapps.switchify.service.utils.ScreenWatcher
import com.enaboapps.switchifykeyboardscanlib.KeyboardSwitchifyLayoutInfo
import com.enaboapps.switchifykeyboardscanlib.KeyboardSwitchifyLink
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * NodeScanner is a class that handles the scanning of nodes.
 * It manages the scanning process using a ScanTree instance and handles updates from NodeExaminer.
 */
class NodeScanner {
    private lateinit var context: Context
    lateinit var scanTree: ScanTree
    private var screenNodes: List<Node> = emptyList()
    private var keyboardNodes: List<Node> = emptyList()
    private var isKeyboardVisible = false
    private val screenWatcher = ScreenWatcher(onScreenSleep = { escapeKeyboardScan() })
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val keyboardLayoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val jsonLayoutInfo =
                intent.getStringExtra(KeyboardSwitchifyLink.EXTRA_KEYBOARD_LAYOUT_INFO)
            val layoutInfo =
                Gson().fromJson(jsonLayoutInfo, KeyboardSwitchifyLayoutInfo::class.java)
            updateNodesWithLayoutInfo(layoutInfo)
        }
    }

    private val keyboardShowReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            isKeyboardVisible = true
            SelectionHandler.setBypassAutoSelect(true)
            SelectionHandler.setStartScanningAction { scanTree.startScanning() }
            val jsonLayoutInfo =
                intent.getStringExtra(KeyboardSwitchifyLink.EXTRA_KEYBOARD_LAYOUT_INFO)
            val layoutInfo =
                Gson().fromJson(jsonLayoutInfo, KeyboardSwitchifyLayoutInfo::class.java)
            updateNodesWithLayoutInfo(layoutInfo)
            println("Keyboard shown")
        }
    }

    private val keyboardHideReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            escapeKeyboardScan()
        }
    }

    /**
     * Starts the NodeScanner.
     * Initializes the scanTree with the context and starts observing node updates.
     *
     * @param context The context in which the NodeScanner is started.
     */
    fun start(context: Context) {
        this.context = context
        startTimeoutToRevertToCursor()
        scanTree = ScanTree(context = context, stopScanningOnSelect = true)
        screenWatcher.register(context)
        registerEventReceivers(context)
    }

    /**
     * Registers the required event receivers.
     *
     * @param context The context in which the receivers are registered.
     */
    private fun registerEventReceivers(context: Context) {
        val localBroadcastManager = LocalBroadcastManager.getInstance(context)
        localBroadcastManager.registerReceiver(
            keyboardLayoutReceiver,
            IntentFilter(KeyboardSwitchifyLink.ACTION_KEYBOARD_LAYOUT_INFO)
        )
        localBroadcastManager.registerReceiver(
            keyboardShowReceiver,
            IntentFilter(KeyboardSwitchifyLink.ACTION_KEYBOARD_SHOW)
        )
        localBroadcastManager.registerReceiver(
            keyboardHideReceiver,
            IntentFilter(KeyboardSwitchifyLink.ACTION_KEYBOARD_HIDE)
        )
    }

    /**
     * Escapes the keyboard scan.
     */
    private fun escapeKeyboardScan() {
        isKeyboardVisible = false
        setScreenNodes(screenNodes)
        setKeyboardNodes(emptyList())
        SelectionHandler.setBypassAutoSelect(false)
        println("Keyboard hidden, updating nodes")
    }

    /**
     * Updates the nodes with the layout info from the keyboard.
     *
     * @param layoutInfo KeyboardLayoutInfo instance.
     */
    private fun updateNodesWithLayoutInfo(layoutInfo: KeyboardSwitchifyLayoutInfo) {
        val newNodes = layoutInfo.keys.map { Node.fromKeyInfo(it) }
        setKeyboardNodes(newNodes)
    }

    /**
     * Starts a timeout that resets the scanTree and changes the state of the ScanMethod
     * if the state is ITEM_SCAN and there are no nodes after 5 seconds.
     */
    fun startTimeoutToRevertToCursor() {
        coroutineScope.launch {
            delay(5000)
            if (ScanMethod.getType() == ScanMethod.MethodType.ITEM_SCAN && screenNodes.isEmpty() && keyboardNodes.isEmpty()) {
                withContext(Dispatchers.Main) {
                    scanTree.reset()
                    ScanMethod.setType(ScanMethod.MethodType.CURSOR)
                    println("ScanMethod changed to cursor")
                }
            } else {
                println("ScanMethod not changed, nodes.size: ${screenNodes.size}")
            }
        }
    }

    /**
     * Sets the keyboard nodes.
     *
     * @param nodes List of new Node instances.
     */
    private fun setKeyboardNodes(nodes: List<Node>) {
        this.keyboardNodes = nodes
        if (isKeyboardVisible) {
            updateNodes(nodes)
        }
    }

    /**
     * Sets the screen nodes.
     *
     * @param nodes List of new Node instances.
     */
    fun setScreenNodes(nodes: List<Node>) {
        this.screenNodes = nodes
        if (!isKeyboardVisible) {
            updateNodes(nodes)
        }
    }

    /**
     * Updates the nodes and rebuilds the scanTree.
     * If no nodes are present, it starts the timeout.
     *
     * @param nodes List of new Node instances.
     */
    private fun updateNodes(nodes: List<Node>) {
        scanTree.buildTree(nodes)
        if (nodes.isEmpty()) {
            startTimeoutToRevertToCursor()
        }
    }

    /**
     * Cleans up the NodeScanner by shutting down the scanTree and cancelling all coroutines.
     */
    fun cleanup() {
        scanTree.shutdown()
        coroutineScope.cancel()
    }
}