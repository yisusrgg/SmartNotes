package com.example.smartnotes.iu.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.smartnotes.data.Task
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic


@Composable
fun TaskCard(
    task: Task,
    onCheckedChange: (Task) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = {
                    val updated = task.copy(completed = it)
                    onCheckedChange(updated)
                }
            )
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
            ) {
                Text(text = task.title, maxLines = 2)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = task.dateTimeText, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (task.hasAttachment) {
                    Icon(
                        imageVector = Icons.Filled.AttachFile,
                        contentDescription = "Adjunto",
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (task.hasAudio) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Audio",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
