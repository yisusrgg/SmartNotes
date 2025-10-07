package com.example.smartnotes.iu.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartnotes.data.Item

@Composable
fun ItemCard(
    item: Item,
    onCheckedChange: (Item) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.completed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Checkbox(
                checked = item.completed,
                onCheckedChange = {
                    val updated = when (item) {
                        is Item.Task -> item.copy(completed = it)
                        is Item.Note -> item.copy(completed = it)
                    }
                    onCheckedChange(updated)
                }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = item.title,
                    maxLines = 2,
                    style = if (item.completed) MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ) else MaterialTheme.typography.bodyLarge,
                    fontWeight = if (item.completed) FontWeight.Normal else FontWeight.Bold
                )
                if (item is Item.Task) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.dateTimeText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (item.attachments.isNotEmpty()) {
                    BadgedBox(
                        badge = {
                            if (item.attachments.size > 1) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) { Text("${item.attachments.size}", fontSize = 10.sp) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Adjuntos (${item.attachments.size})",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (item.audios.isNotEmpty()) {
                    BadgedBox(
                        badge = {
                            if (item.audios.size > 1) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) { Text("${item.audios.size}", fontSize = 10.sp) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Audios (${item.audios.size})",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}