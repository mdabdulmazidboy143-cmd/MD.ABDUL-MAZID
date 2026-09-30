package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.MedicineEntity
import com.example.data.model.TimeSlot
import com.example.ui.components.AddEditMedicineDialog
import com.example.ui.components.SlotTimePickerDialog
import com.example.ui.screens.IntakeHistoryScreen
import com.example.ui.screens.MedicineListScreen
import com.example.ui.screens.SlotSettingsScreen
import com.example.ui.screens.TodayScheduleScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MedicineViewModel
import kotlinx.coroutines.launch

sealed class NavItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    data object Today : NavItem("আজকের সূচি", Icons.Default.EventNote, "tab_today")
    data object Medicines : NavItem("ঔষধ তালিকা", Icons.Default.Medication, "tab_medicines")
    data object History : NavItem("সেবনের ইতিহাস", Icons.Default.History, "tab_history")
    data object Settings : NavItem("সময় সেটিংস", Icons.Default.Alarm, "tab_settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MedicineViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Request notification permission for Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (!isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("সতর্কতা: নোটিফিকেশন অনুমোদন না দিলে সময়মত অ্যালার্ট আসবে না।")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val navItems = listOf(
        NavItem.Today,
        NavItem.Medicines,
        NavItem.History,
        NavItem.Settings
    )

    // Handle back button: return to Tab 0 before exiting
    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    // State collections
    val todaySlotsState by viewModel.todaySlotsState.collectAsStateWithLifecycle()
    val allMedicines by viewModel.allMedicines.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val totalTaken by viewModel.totalTakenCount.collectAsStateWithLifecycle()
    val totalMissed by viewModel.totalMissedCount.collectAsStateWithLifecycle()
    val slotSettings by viewModel.slotSettings.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Dialog States
    var showAddEditMedicineDialog by remember { mutableStateOf(false) }
    var medicineToEdit by remember { mutableStateOf<MedicineEntity?>(null) }
    var slotToEditTime by remember { mutableStateOf<TimeSlot?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Medication,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ঔষধ রিমাইন্ডার",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "সকাল • দুপুর • সন্ধ্যা",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        IconButton(onClick = {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "অনুমোদন দিন",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Crossfade(
            targetState = selectedTabIndex,
            label = "tab_crossfade",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tabIndex ->
            when (tabIndex) {
                0 -> TodayScheduleScreen(
                    slotsState = todaySlotsState,
                    onMarkStatus = { slot, status ->
                        viewModel.markSlotStatus(slot, status)
                    },
                    onTestNotification = { slot ->
                        viewModel.triggerTestNotification(slot)
                    },
                    onNavigateToMedicines = {
                        selectedTabIndex = 1
                    },
                    onEditTime = { slot ->
                        slotToEditTime = slot
                    }
                )

                1 -> MedicineListScreen(
                    medicines = allMedicines,
                    onAddMedicine = {
                        medicineToEdit = null
                        showAddEditMedicineDialog = true
                    },
                    onEditMedicine = { med ->
                        medicineToEdit = med
                        showAddEditMedicineDialog = true
                    },
                    onDeleteMedicine = { med ->
                        viewModel.deleteMedicine(med)
                    },
                    onToggleActive = { med ->
                        viewModel.toggleMedicineActive(med)
                    }
                )

                2 -> IntakeHistoryScreen(
                    logs = allLogs,
                    totalTaken = totalTaken,
                    totalMissed = totalMissed,
                    onClearHistory = {
                        viewModel.clearAllHistory()
                    }
                )

                3 -> SlotSettingsScreen(
                    slotSettings = slotSettings,
                    onEditSlotTime = { slot ->
                        slotToEditTime = slot
                    },
                    onToggleAlarm = { slot, isEnabled ->
                        viewModel.toggleSlotAlarm(slot, isEnabled)
                    },
                    onTestNotification = { slot ->
                        viewModel.triggerTestNotification(slot)
                    }
                )
            }
        }
    }

    // Add / Edit Medicine Dialog
    if (showAddEditMedicineDialog) {
        AddEditMedicineDialog(
            medicineToEdit = medicineToEdit,
            onDismiss = {
                showAddEditMedicineDialog = false
                medicineToEdit = null
            },
            onSave = { name, dosage, mealTiming, forMorning, forNoon, forEvening, notes ->
                viewModel.saveMedicine(
                    id = medicineToEdit?.id ?: 0L,
                    name = name,
                    dosage = dosage,
                    mealTiming = mealTiming,
                    forMorning = forMorning,
                    forNoon = forNoon,
                    forEvening = forEvening,
                    notes = notes
                )
                showAddEditMedicineDialog = false
                medicineToEdit = null
            }
        )
    }

    // Time Picker Dialog for changing slot alarm time
    slotToEditTime?.let { slot ->
        val setting = slotSettings.firstOrNull { it.slotId == slot.id }
        val hour = setting?.hour ?: slot.defaultHour
        val minute = setting?.minute ?: slot.defaultMinute

        SlotTimePickerDialog(
            slot = slot,
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { slotToEditTime = null },
            onConfirm = { newHour, newMinute ->
                viewModel.updateSlotTime(slot, newHour, newMinute)
                slotToEditTime = null
            }
        )
    }
}
