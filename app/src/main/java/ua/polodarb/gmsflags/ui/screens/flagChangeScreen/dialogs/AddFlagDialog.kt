package ua.polodarb.gmsflags.ui.screens.flagChangeScreen.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun AddFlagDialog(
    showDialog: Boolean,
    flagType: Int,
    onFlagTypeChange: (Int) -> Unit,
    flagBoolean: Int,
    onFlagBooleanChange: (Int) -> Unit,
    flagName: String,
    flagNameChange: (String) -> Unit,
    flagValue: String,
    flagValueChange: (String) -> Unit,
    onAddFlag: () -> Unit,
    onDismiss: () -> Unit
) {

    val rowTypes = listOf("Boolean", "Integer", "Float", "String")
    val chipsBooleanSelect = listOf("True", "False")

    val haptic = LocalHapticFeedback.current

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = "Add a flag manually") },
            text = {
                Column {
                    FlagTypeChips(
                        valuesList = rowTypes,
                        selectedChips = flagType,
                        onChipClick = onFlagTypeChange,
                        haptic = haptic
                    )
                    FlagBooleanChips(
                        valuesList = chipsBooleanSelect,
                        selectedValue = flagBoolean,
                        onValueClick = onFlagBooleanChange,
                        enabled = flagType == 0,
                        haptic = haptic
                    )
                    FlagNameInput(
                        flagName = flagName,
                        onQueryChange = flagNameChange,
                        enabled = true
                    )
                    FlagValueInput(
                        flagValue = flagValue,
                        onQueryChange = flagValueChange,
                        enabled = flagType != 0
                    )
                }
            },
            confirmButton = {
                Button(onClick = onAddFlag) {
                    Text(text = "Add flag")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = "Exit")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FlagTypeChips(
    valuesList: List<String>,
    selectedChips: Int,
    onChipClick: (Int) -> Unit,
    haptic: HapticFeedback
) {
    Text(text = "Choose a type of flag")
    FlowRow(
        maxItemsInEachRow = 2,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(-4.dp)
    ) {
        valuesList.forEachIndexed { index, item ->
            FilterChip(
                selected = selectedChips == index,
                onClick = {
                    onChipClick(index)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                colors = SelectableChipColors(
                    containerColor = Color.Transparent,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    leadingIconColor = Color.Transparent,
                    trailingIconColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    disabledLabelColor = Color.Transparent,
                    disabledLeadingIconColor = Color.Transparent,
                    disabledTrailingIconColor = Color.Transparent,
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    disabledSelectedContainerColor = Color.Transparent,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                    selectedLeadingIconColor = Color.Transparent,
                    selectedTrailingIconColor = Color.Transparent
                ),
                label = {
                    Text(
                        text = item,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = null,
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagBooleanChips(
    valuesList: List<String>,
    selectedValue: Int,
    onValueClick: (Int) -> Unit,
    enabled: Boolean,
    haptic: HapticFeedback
) {
    Text(
        text = "Select a value",
        color = if (enabled)
            MaterialTheme.colorScheme.onSurfaceVariant
        else
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(top = 12.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        valuesList.forEachIndexed { index, item ->
            FilterChip(
                selected = selectedValue == index,
                onClick = {
                    onValueClick(index)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                colors = SelectableChipColors(
                    containerColor = Color.Transparent,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    leadingIconColor = Color.Transparent,
                    trailingIconColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                        alpha = 0.3f
                    ),
                    disabledLeadingIconColor = Color.Transparent,
                    disabledTrailingIconColor = Color.Transparent,
                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                    disabledSelectedContainerColor = MaterialTheme.colorScheme.secondary.copy(
                        alpha = 0.2f
                    ),
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                    selectedLeadingIconColor = Color.Transparent,
                    selectedTrailingIconColor = Color.Transparent
                ),
                label = {
                    Text(
                        text = item,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = null,
                modifier = Modifier
                    .weight(1f),
                enabled = enabled
            )
        }
    }
}

@Composable
fun FlagNameInput(
    flagName: String,
    onQueryChange: (String) -> Unit,
    enabled: Boolean
) {
    Text(
        text = "Enter a name",
        color = if (enabled)
            MaterialTheme.colorScheme.onSurfaceVariant
        else
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(top = 12.dp)
    )
    OutlinedTextField(
        value = flagName,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Type a name of flag",
                color = if (enabled)
                    MaterialTheme.colorScheme.outline
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            )
        },
        modifier = Modifier.padding(top = 12.dp),
        shape = MaterialTheme.shapes.medium,
        enabled = enabled
    )
}

@Composable
fun FlagValueInput(
    flagValue: String,
    onQueryChange: (String) -> Unit,
    enabled: Boolean
) {
    Text(
        text = "Enter a value",
        color = if (enabled)
            MaterialTheme.colorScheme.onSurfaceVariant
        else
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(top = 12.dp)
    )
    OutlinedTextField(
        value = flagValue,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Type a value",
                color = if (enabled)
                    MaterialTheme.colorScheme.outline
                else
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            )
        },
        modifier = Modifier.padding(top = 12.dp),
        shape = MaterialTheme.shapes.medium,
        enabled = enabled
    )
}