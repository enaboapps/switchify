package com.enaboapps.switchify.components

import androidx.compose.runtime.Composable

@Composable
fun InfoCard(
    title: String,
    description: String
) {
    UICard(title = title, description = description, onClick = {})
}