package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TaskType
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DoorDostViewModel

enum class NavigationTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Filled.Home),
    DOST_CAM("Dost-Cam", Icons.Filled.CameraEnhance),
    NEIGHBOR_PASS("Emergency", Icons.Filled.HealthAndSafety),
    DIRECTORY("Dosts", Icons.Filled.People),
    CHAT("Dost AI", Icons.Filled.AutoAwesome),
    COMMUNITY("Community", Icons.Filled.Groups)
}

class MainActivity : ComponentActivity() {

    private val viewModel: DoorDostViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DoorDostApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoorDostApp(viewModel: DoorDostViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allRunners by viewModel.allRunners.collectAsStateWithLifecycle()
    val activeTasks by viewModel.activeTasks.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val meetups by viewModel.meetups.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val dailyDrops by viewModel.dailyDrops.collectAsStateWithLifecycle()
    val rentABros by viewModel.rentABros.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var showNeighborhoodDialog by remember { mutableStateOf(false) }
    var showStoryboardDialog by remember { mutableStateOf(false) }
    var showAddDailyDropDialog by remember { mutableStateOf(false) }
    var showManageRentABroDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.fetchCurrentNeighborhoodLocation()
        }
    }

    val requestLocationAndFetch = {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            viewModel.fetchCurrentNeighborhoodLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Display user notification snackbars
    LaunchedEffect(uiState.userNotification) {
        uiState.userNotification?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            DoorDostTopBar(
                currentNeighborhood = uiState.currentNeighborhood,
                referralCredits = uiState.referralCredits,
                isGpsLive = uiState.currentCoordinates.isGpsLive,
                onSelectNeighborhood = { showNeighborhoodDialog = true },
                onOpenReferrals = { currentTab = NavigationTab.COMMUNITY }
            )
        },
        floatingActionButton = {
            if (currentTab == NavigationTab.HOME || currentTab == NavigationTab.DIRECTORY) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openCreateTaskDialog(true) },
                    containerColor = SaffronPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("New Errand", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("fab_new_errand")
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.HOME -> {
                    HomeScreen(
                        activeTasks = activeTasks,
                        runners = allRunners,
                        dailyDrops = dailyDrops,
                        rentABroSubscription = rentABros.firstOrNull(),
                        currentNeighborhood = uiState.currentNeighborhood,
                        onNavigateToDostCam = { currentTab = NavigationTab.DOST_CAM },
                        onNavigateToNeighborPass = { currentTab = NavigationTab.NEIGHBOR_PASS },
                        onNavigateToDostDirectory = { currentTab = NavigationTab.DIRECTORY },
                        onNavigateToChat = { currentTab = NavigationTab.CHAT },
                        onNavigateToCommunity = { currentTab = NavigationTab.COMMUNITY },
                        onHireDost = { runner -> viewModel.openHireDostDialog(runner) },
                        onOpenUpiPay = { task -> viewModel.openUpiSheet(task) },
                        onToggleDailyDrop = { id, active -> viewModel.toggleDailyDrop(id, active) },
                        onAddDailyDrop = { showAddDailyDropDialog = true },
                        onManageRentABro = { showManageRentABroDialog = true },
                        onOpenStoryboard = { showStoryboardDialog = true }
                    )
                }
                NavigationTab.DOST_CAM -> {
                    DostCamVerifyScreen(
                        activeVerificationImageBase64 = uiState.activeVerificationImageBase64,
                        shelfResult = uiState.shelfVerificationResult,
                        isAnalyzing = uiState.isAnalyzingImage,
                        onSelectPresetSample = { sample -> viewModel.selectPresetShelfSample(sample) },
                        onAnalyzeCustomBitmap = { bitmap, prompt -> viewModel.analyzeShelfPhoto(bitmap, prompt) },
                        onPayUpi = {
                            val targetTask = activeTasks.firstOrNull()
                            if (targetTask != null) {
                                viewModel.openUpiSheet(targetTask)
                            } else {
                                viewModel.completeUpiPayment("sample_task")
                            }
                        }
                    )
                }
                NavigationTab.NEIGHBOR_PASS -> {
                    NeighborPassScreen(
                        runners = allRunners,
                        activeTasks = activeTasks,
                        onDispatchChore = { title, choreType, desc, addr, urgency, cost, runner ->
                            viewModel.createEmergencyPassChore(title, choreType, desc, addr, urgency, cost, runner)
                            currentTab = NavigationTab.HOME
                        }
                    )
                }
                NavigationTab.DIRECTORY -> {
                    DostDirectoryScreen(
                        runners = allRunners,
                        selectedCategory = uiState.selectedDostCategory,
                        onSelectCategory = { cat -> viewModel.filterDostCategory(cat) },
                        onHireDost = { runner -> viewModel.openHireDostDialog(runner) }
                    )
                }
                NavigationTab.CHAT -> {
                    DostChatScreen(
                        messages = chatMessages,
                        selectedModel = uiState.selectedModel,
                        selectedRole = uiState.selectedRole,
                        isLoading = uiState.isChatLoading,
                        onSelectModel = { model -> viewModel.setModel(model) },
                        onSelectRole = { role -> viewModel.setRole(role) },
                        onSendMessage = { text, bitmap -> viewModel.sendChatMessage(text, bitmap) }
                    )
                }
                NavigationTab.COMMUNITY -> {
                    CommunityScreen(
                        meetups = meetups,
                        referralCredits = uiState.referralCredits,
                        referralCode = uiState.referralCode,
                        referredFriendsCount = uiState.referredFriendsCount,
                        onToggleRsvp = { id, isRsvpd -> viewModel.toggleMeetupRsvp(id, isRsvpd) },
                        onClaimReward = { viewModel.claimReferralReward() }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showNeighborhoodDialog) {
        NeighborhoodPickerDialog(
            currentNeighborhood = uiState.currentNeighborhood,
            isDetectingLocation = uiState.isFetchingLocation,
            onDetectLocation = {
                requestLocationAndFetch()
            },
            onDismiss = { showNeighborhoodDialog = false },
            onSelectNeighborhood = {
                viewModel.setNeighborhood(it)
                showNeighborhoodDialog = false
            }
        )
    }

    if (uiState.showCreateTaskDialog) {
        CreateTaskDialog(
            onDismiss = { viewModel.openCreateTaskDialog(false) },
            onCreateTask = { title, type, items, address, urgency, cost, runner ->
                viewModel.createEmergencyPassChore(title, type, items, address, urgency, cost, runner)
            },
            runners = allRunners
        )
    }

    uiState.showHireDostDialog?.let { runner ->
        HireDostDialog(
            runner = runner,
            onDismiss = { viewModel.openHireDostDialog(null) },
            onConfirmHire = { desc, addr, cost ->
                viewModel.createEmergencyPassChore(
                    title = "Errand with ${runner.name}",
                    choreType = TaskType.RENT_A_BRO_CHORE,
                    itemsDescription = desc,
                    address = addr,
                    urgency = "Standard",
                    cost = cost,
                    selectedRunner = runner
                )
            }
        )
    }

    uiState.showUpiSheetForTask?.let { task ->
        UpiPaymentDialog(
            task = task,
            onDismiss = { viewModel.openUpiSheet(null) },
            onConfirmPayment = { viewModel.completeUpiPayment(task.id) }
        )
    }

    if (showStoryboardDialog) {
        ExplainerStoryboardDialog(
            scenes = viewModel.explainerScenes,
            onDismiss = { showStoryboardDialog = false }
        )
    }

    if (showAddDailyDropDialog) {
        AddDailyDropDialog(
            onDismiss = { showAddDailyDropDialog = false },
            onConfirmAdd = { title, items, time, floorNote, budget ->
                viewModel.addDailyDrop(title, items, time, floorNote, budget)
                showAddDailyDropDialog = false
            }
        )
    }

    if (showManageRentABroDialog) {
        val currentRetainer = rentABros.firstOrNull()
        ManageRentABroDialog(
            currentWage = currentRetainer?.dailyWage ?: 300,
            currentPetrol = currentRetainer?.petrolAllowancePerDay ?: 100,
            currentMargin = currentRetainer?.platformMarginMonthly ?: 500,
            onDismiss = { showManageRentABroDialog = false },
            onSaveRetainer = { w, p, m ->
                viewModel.updateRentABroSubscription(w, p, m)
                showManageRentABroDialog = false
            }
        )
    }
}
