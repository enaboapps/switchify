package com.enaboapps.switchify.service.scanning

/**
 * This interface represents a node in the scanning tree
 */
interface ScanNodeInterface {
    /**
     * This function gets the left coordinate of the node
     * @return The left coordinate of the node
     */
    fun getLeft(): Int

    /**
     * This function gets the top coordinate of the node
     * @return The top coordinate of the node
     */
    fun getTop(): Int

    /**
     * This function gets the mid x coordinate of the node
     * @return The mid x coordinate of the node
     */
    fun getMidX(): Int

    /**
     * This function gets the mid y coordinate of the node
     * @return The mid y coordinate of the node
     */
    fun getMidY(): Int

    /**
     * This function gets the width of the node
     * @return The width of the node
     */
    fun getWidth(): Int

    /**
     * This function gets the height of the node
     * @return The height of the node
     */
    fun getHeight(): Int

    /**
     * This function gets the content description of the node
     * @return The content description of the node
     */
    fun getContentDescription(): String

    /**
     * This function highlights the node
     */
    fun highlight()

    /**
     * This function unhighlights the node
     */
    fun unhighlight()

    /**
     * This function selects the node
     */
    fun select()
}