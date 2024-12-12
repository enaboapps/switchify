package com.enaboapps.switchify.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> Picker(
    title: String,
    selectedItem: T?,
    items: List<T>,
    modifier: Modifier = Modifier,
    onItemSelected: (T) -> Unit,
    onDelete: (() -> Unit)? = null,
    itemToString: (T) -> String,
    itemDescription: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    UICard(
        modifier = modifier.padding(bottom = 8.dp),
        title = title,
        description = if (selectedItem != null) itemToString(selectedItem) else "",
        extraDescription = if (selectedItem != null) itemDescription(selectedItem) else "",
        rightActionButton = {
            onDelete?.let {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        rightIcon = Icons.Default.KeyboardArrowDown,
        onClick = {
            expanded = true
        }
    )

    if (expanded) {
        DropdownMenu(
            modifier = modifier,
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = itemToString(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}