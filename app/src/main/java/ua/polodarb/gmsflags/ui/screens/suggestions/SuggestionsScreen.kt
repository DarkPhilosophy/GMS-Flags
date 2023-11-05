package ua.polodarb.gmsflags.ui.screens.suggestions

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel
import ua.polodarb.gmsflags.R
import ua.polodarb.gmsflags.data.remote.flags.dto.FlagInfo
import ua.polodarb.gmsflags.data.remote.flags.dto.FlagType
import ua.polodarb.gmsflags.ui.components.inserts.ErrorLoadScreen
import ua.polodarb.gmsflags.ui.components.inserts.LoadingProgressBar
import ua.polodarb.gmsflags.ui.screens.UiStates
import ua.polodarb.gmsflags.ui.screens.suggestions.dialog.ResetFlagToDefaultDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(
    isFirstStart: Boolean,
    onSettingsClick: () -> Unit,
    onPackagesClick: () -> Unit
) {
    val viewModel = koinViewModel<SuggestionScreenViewModel>()

    val overriddenFlags = viewModel.stateSuggestionsFlags.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.getAllOverriddenBoolFlags()
    }

    var showResetDialog by rememberSaveable {
        mutableStateOf(false)
    }

    // Reset Flags list
    val resetFlagsList: MutableList<FlagInfo> = mutableListOf()
    var resetFlagPackage = ""

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.nav_bar_suggestions),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
//                    IconButton(onClick = {
//                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
//                        Toast.makeText(context, "Search", Toast.LENGTH_SHORT).show()
//                    }) {
//                        Icon(
//                            imageVector = Icons.Filled.Search,
//                            contentDescription = "Localized description"
//                        )
//                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPackagesClick()
                    }) {
                        Icon(
                            painterResource(id = R.drawable.ic_packages),
                            contentDescription = "Localized description"
                        )
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSettingsClick()
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
        ) {
            when (val result = overriddenFlags.value) {
                is UiStates.Success -> {
                    val data = result.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            WarningBanner(isFirstStart)
                        }
                        itemsIndexed(data) { index, item ->
                            SuggestedFlagItem(
                                flagName = item.flag.name,
                                senderName = item.flag.author,
                                flagValue = item.enabled,
                                flagOnCheckedChange = { bool ->
                                    coroutineScope.launch {
                                        withContext(Dispatchers.IO) {
                                            viewModel.updateFlagValue(bool, index)
                                            item.flag.flags.forEach { flag ->
                                                when (flag.type) {
                                                    FlagType.BOOL -> {
                                                        viewModel.overrideFlag(
                                                            packageName = item.flag.packageName,
                                                            name = flag.tag,
                                                            boolVal = if (bool) flag.value else "0"
                                                        )
                                                    }

                                                    FlagType.INTEGER -> {
                                                        viewModel.overrideFlag(
                                                            packageName = item.flag.packageName,
                                                            name = flag.tag,
                                                            intVal = if (bool) flag.value else "0"
                                                        )
                                                    }

                                                    FlagType.FLOAT -> {
                                                        viewModel.overrideFlag(
                                                            packageName = item.flag.packageName,
                                                            name = flag.tag,
                                                            floatVal = if (bool) flag.value else "0"
                                                        )
                                                    }

                                                    FlagType.STRING -> {
                                                        viewModel.overrideFlag(
                                                            packageName = item.flag.packageName,
                                                            name = flag.tag,
                                                            stringVal = if (bool) flag.value else ""
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                onFlagLongClick = {
                                    resetFlagPackage = item.flag.packageName
                                    resetFlagsList.addAll(item.flag.flags)
                                    showResetDialog = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )
                            ResetFlagToDefaultDialog(
                                showDialog = showResetDialog,
                                onDismiss = { showResetDialog = false }
                            ) {
                                showResetDialog = false
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.resetSuggestedFlagValue(resetFlagPackage, resetFlagsList)
                                viewModel.getAllOverriddenBoolFlags()
                            }

                        }
                        item {
                            Spacer(modifier = Modifier.padding(44.dp))
                        }
                    }
                }

                is UiStates.Loading -> {
                    LoadingProgressBar()
                }

                is UiStates.Error -> {
                    ErrorLoadScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SuggestedFlagItem(
    flagName: String,
    senderName: String,
    flagValue: Boolean,
    flagOnCheckedChange: (Boolean) -> Unit,
    onFlagLongClick: () -> Unit
) {
//    NewSuggestedFlagItem(
//        titleText = flagName,
//        sourceText = senderName
//    )
    ListItem(
        headlineContent = { Text(flagName) },
        supportingContent = { Text(stringResource(R.string.finder) + senderName) },
        trailingContent = {
            Row {
                Switch(
                    checked = flagValue,
                    onCheckedChange = {
                        flagOnCheckedChange(it)
                    }
                )
            }
        },
        modifier = Modifier
            .combinedClickable(
                onClick = {},
                onLongClick = onFlagLongClick
            )
    )
}

@Composable
private fun NewSuggestedFlagItem(
    titleText: String,
    sourceText: String
) {

    val context = LocalContext.current

    var expanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(24.dp)) // todo: corner radius
            .background(
                if (!isSystemInDarkTheme())
                    MaterialTheme.colorScheme.surfaceContainerLow
                else
                    MaterialTheme.colorScheme.surfaceContainer
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column() {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleText,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestedFlagItemArrow(
                        expanded = expanded,
                        onExpandedChange = {
                            expanded = !expanded
                        }
                    )
                    Switch(
                        checked = false,
                        onCheckedChange = { },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Text(
                        text = "" + stringResource(R.string.finder) + " " + sourceText,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomEnd = 4.dp,
                                    bottomStart = 4.dp
                                )
                            )
                            .background(
                                if (!isSystemInDarkTheme())
                                    MaterialTheme.colorScheme.surfaceContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 6.dp)
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp,
                                    bottomEnd = 16.dp,
                                    bottomStart = 16.dp
                                )
                            )
                            .background(
                                if (!isSystemInDarkTheme())
                                    MaterialTheme.colorScheme.surfaceContainer
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                    ) {
                        AppContent(
                            appName = "AppName",
                            pkg = "PackageName",
                            appIcon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher_round)
                        )
                        OutlinedButton(
                            onClick = { /*TODO*/ },
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                                .fillMaxWidth(),
                        ) {
                            Text(text = "Open app details settings")
                        }
                        OutlinedButton(
                            onClick = { /*TODO*/ },
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                                .fillMaxWidth(),
                        ) {
                            Text(text = "Open AppName")
                        }
                    }
                    Button(
                        onClick = { /*TODO*/ },
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_telegram),
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            text = "View more information"
                        )
                    }
                    Row(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 16.dp
                        ),
                    ) {
                        Button(
                            onClick = { /*TODO*/ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            modifier = Modifier.weight(0.5f)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_flag_reset_new),
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(
                                text = "Reset"
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = { /*TODO*/ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            modifier = Modifier.weight(0.5f)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_report_fill),
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(
                                text = "Report"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Stable
@Composable
fun AppContent(
    appName: String,
    pkg: String,
    appIcon: Drawable? = null
) {
    ListItem(
        colors = ListItemDefaults.colors(
            containerColor = if (!isSystemInDarkTheme())
                MaterialTheme.colorScheme.surfaceContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        headlineContent = { Text(text = appName, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(text = pkg, fontSize = 13.sp) },
        leadingContent = {
            AsyncImage(
                model = appIcon,
                contentDescription = null,
                modifier = Modifier.size(52.dp)
            )
        }
    )
}

@Composable
fun SuggestedFlagItemArrow(
    expanded: Boolean,
    onExpandedChange: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "Rotation"
    )

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer(
                    rotationX = rotationState
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    onExpandedChange()
                }
        )
    }
}


@Composable
fun WarningBanner(
    isFirstStart: Boolean
) {
    var visibility by rememberSaveable {
        mutableStateOf(isFirstStart)
    }

    AnimatedVisibility(
        visible = visibility,
        enter = fadeIn(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_force_stop),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .padding(8.dp)
                        .size(28.dp)
                )
                Text(
                    text = stringResource(R.string.suggestion_banner),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { visibility = false },
                        modifier = Modifier.padding(8.dp),
                        colors = ButtonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                            disabledContainerColor = MaterialTheme.colorScheme.outline,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(text = stringResource(R.string.close))
                    }
                }
            }
        }
    }
}
