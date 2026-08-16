package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.db.AppDatabase
import com.example.data.location.NeighborhoodCoordinates
import com.example.data.location.NeighborhoodLocationService
import com.example.data.model.*
import com.example.data.repository.DoorDostRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiState(
    val currentNeighborhood: String = "Indiranagar 4th Block, Bengaluru",
    val currentCoordinates: NeighborhoodCoordinates = NeighborhoodCoordinates(
        latitude = 12.9719,
        longitude = 77.6412,
        neighborhoodName = "Indiranagar 4th Block, Bengaluru",
        city = "Bengaluru",
        accuracyMeters = 15f,
        isGpsLive = false
    ),
    val isFetchingLocation: Boolean = false,
    val selectedDostCategory: DostCategory? = null,
    val selectedModel: String = "gemini-3.5-flash",
    val selectedRole: String = "Neighborhood Concierge",
    val isChatLoading: Boolean = false,
    val isAnalyzingImage: Boolean = false,
    val shelfVerificationResult: ShelfVerificationResult? = null,
    val activeVerificationImageBase64: String? = null,
    val showUpiSheetForTask: TaskOrder? = null,
    val showCreateTaskDialog: Boolean = false,
    val showHireDostDialog: DostRunner? = null,
    val referralCredits: Int = 500,
    val referralCode: String = "DOST50",
    val referredFriendsCount: Int = 5,
    val userNotification: String? = null
)

class DoorDostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DoorDostRepository(AppDatabase.getDatabase(application))
    private val locationService = NeighborhoodLocationService(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val allRunners: StateFlow<List<DostRunner>> = repository.allRunners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTasks: StateFlow<List<TaskOrder>> = repository.activeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskOrder>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val meetups: StateFlow<List<CommunityMeetup>> = repository.allMeetups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyDrops: StateFlow<List<DailyDropSubscription>> = repository.allDailyDrops
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rentABros: StateFlow<List<RentABroSubscription>> = repository.allRentABros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val explainerScenes: List<ExplainerScene> = repository.getExplainerScenes()

    fun calculateTransparentPrice(mrp: Int, distanceKm: Double = 1.2): PriceBreakdown {
        return repository.calculateTransparentPrice(mrp, distanceKm)
    }

    fun toggleDailyDrop(id: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleDailyDropActive(id, isActive)
            _uiState.update {
                it.copy(userNotification = if (isActive) "Daily Drop activated for 7:00 AM delivery!" else "Daily Drop paused.")
            }
        }
    }

    fun addDailyDrop(
        title: String,
        items: String,
        time: String = "7:00 AM",
        floorNote: String = "4th Floor Door Hook",
        monthlyEstimate: Int = 1500
    ) {
        viewModelScope.launch {
            val newSub = DailyDropSubscription(
                id = "daily_" + System.currentTimeMillis().toString().takeLast(6),
                title = title,
                items = items,
                deliveryTime = time,
                deliveryFloorNote = floorNote,
                monthlyEstimate = monthlyEstimate,
                isActive = true,
                assignedRunnerName = "Aman Sharma"
            )
            repository.addDailyDropSubscription(newSub)
            _uiState.update {
                it.copy(userNotification = "Daily Drop set! $title scheduled daily at $time to $floorNote.")
            }
        }
    }

    fun updateRentABroSubscription(
        dailyWage: Int = 300,
        petrolAllowance: Int = 100,
        margin: Int = 500
    ) {
        viewModelScope.launch {
            val total = (dailyWage + petrolAllowance) * 30 + margin
            val sub = RentABroSubscription(
                id = "rent_1",
                runnerId = "dost_1",
                runnerName = "Aman Sharma",
                dailyWage = dailyWage,
                petrolAllowancePerDay = petrolAllowance,
                platformMarginMonthly = margin,
                totalMonthlyFee = total,
                daysRemaining = 30,
                isActive = true
            )
            repository.saveRentABroSubscription(sub)
            _uiState.update {
                it.copy(userNotification = "Rent-a-Bro 30-Day Retainer renewed at ₹$total/mo!")
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
            // Set a sample shelf image for initial Dost-Cam verify experience
            val sampleBitmap = createSampleShelfBitmap("Amul Taaza Milk & GoodDay Biscuits")
            _uiState.update {
                it.copy(
                    activeVerificationImageBase64 = GeminiApiClient.bitmapToBase64(sampleBitmap),
                    shelfVerificationResult = ShelfVerificationResult(
                        detectedBrand = "Amul & Britannia",
                        productName = "Amul Gold 500ml + GoodDay Butter 200g",
                        estimatedPrice = 142,
                        expiryOrBatch = "MFG: Today 06:00 AM • Fresh Batch",
                        trustScorePercent = 99,
                        notes = "Dost-Cam Live Verify: Shelf item in runner's hand is sealed and matches customer request. Expiry is verified.",
                        recommendedAction = "Approved for Instant UPI Pay"
                    )
                )
            }
        }
    }

    fun setNeighborhood(name: String) {
        val (lat, lng) = NeighborhoodLocationService.getCoordinatesForNeighborhood(name)
        _uiState.update {
            it.copy(
                currentNeighborhood = name,
                currentCoordinates = NeighborhoodCoordinates(
                    latitude = lat,
                    longitude = lng,
                    neighborhoodName = name,
                    city = name.substringAfterLast(", ").trim(),
                    accuracyMeters = null,
                    isGpsLive = false
                ),
                userNotification = "Switched neighborhood to $name"
            )
        }
    }

    fun fetchCurrentNeighborhoodLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingLocation = true) }
            val result = locationService.fetchCurrentLocation()
            val coords = result.getOrNull()
            if (coords != null) {
                _uiState.update {
                    it.copy(
                        isFetchingLocation = false,
                        currentCoordinates = coords,
                        currentNeighborhood = coords.neighborhoodName,
                        userNotification = if (coords.isGpsLive) {
                            "GPS matched neighborhood: ${coords.neighborhoodName} (${"%.4f".format(coords.latitude)}, ${"%.4f".format(coords.longitude)})"
                        } else {
                            "Location set to ${coords.neighborhoodName}"
                        }
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isFetchingLocation = false,
                        userNotification = "Could not fetch GPS coordinates. Using ${it.currentNeighborhood}"
                    )
                }
            }
        }
    }

    fun filterDostCategory(category: DostCategory?) {
        _uiState.update { it.copy(selectedDostCategory = category) }
    }

    fun setModel(model: String) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun setRole(role: String) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun openCreateTaskDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateTaskDialog = show) }
    }

    fun openHireDostDialog(runner: DostRunner?) {
        _uiState.update { it.copy(showHireDostDialog = runner) }
    }

    fun openUpiSheet(task: TaskOrder?) {
        _uiState.update { it.copy(showUpiSheetForTask = task) }
    }

    fun clearNotification() {
        _uiState.update { it.copy(userNotification = null) }
    }

    fun sendChatMessage(userText: String, attachedBitmap: Bitmap? = null) {
        if (userText.isBlank() && attachedBitmap == null) return

        val imageBase64 = attachedBitmap?.let { GeminiApiClient.bitmapToBase64(it) }
        val model = _uiState.value.selectedModel
        val role = _uiState.value.selectedRole

        val systemInstruction = when (role) {
            "Neighborhood Concierge" ->
                "You are Door Dost's friendly neighborhood concierge in India. Speak warmly, respectfully, and helpfully. Help calculate grocery costs in ₹, organize fast shop & drop tasks, and recommend trusted local runners."
            "Grocery Price Expert" ->
                "You are a savvy local grocery expert. Help find the freshest brands, compare local store prices, recommend healthy pantry staples, and ensure accurate quantities."
            "Emergency Chore Lead" ->
                "You are the Neighbor Pass Emergency Coordinator. Prioritize patient safety, urgent prescription medicine checks, elder parent check-ins, OTP protocols, and swift dispatch of police-verified runners."
            else -> "You are Door Dost AI assistant."
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isChatLoading = true) }
            repository.sendChatMessage(
                userText = userText,
                model = model,
                systemInstruction = systemInstruction,
                imageBase64 = imageBase64
            )
            _uiState.update { it.copy(isChatLoading = false) }
        }
    }

    fun analyzeShelfPhoto(bitmap: Bitmap, customPrompt: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingImage = true) }
            val base64 = GeminiApiClient.bitmapToBase64(bitmap)
            val prompt = customPrompt ?: "Analyze this grocery shelf/product photo from Door Dost runner. Verify brand name, exact item, price in ₹, freshness or batch code, and provide a clear recommendation."
            val result = repository.analyzeShelfPhoto(base64, prompt)
            _uiState.update {
                it.copy(
                    isAnalyzingImage = false,
                    activeVerificationImageBase64 = base64,
                    shelfVerificationResult = result,
                    userNotification = "Dost-Cam verified: ${result.detectedBrand} (${result.trustScorePercent}% match)"
                )
            }
        }
    }

    fun selectPresetShelfSample(sampleType: String) {
        val (label, result) = when (sampleType) {
            "dairy" -> Pair(
                "Amul Taaza Milk 1L & Curd",
                ShelfVerificationResult(
                    detectedBrand = "Amul Taaza",
                    productName = "Pasteurised Toned Milk 1000ml Pouch",
                    estimatedPrice = 54,
                    expiryOrBatch = "USE BY: Tomorrow morning • Fresh cold storage",
                    trustScorePercent = 100,
                    notes = "Live shelf fridge inspection verified. Pouch is intact, properly refrigerated at 4°C.",
                    recommendedAction = "Approved for UPI Pay"
                )
            )
            "meds" -> Pair(
                "MedPlus Prescription: Pantocid 40mg",
                ShelfVerificationResult(
                    detectedBrand = "Sun Pharma / Pantocid 40",
                    productName = "Pantocid 40mg Gastro-Resistant Tablets (Strip of 15)",
                    estimatedPrice = 168,
                    expiryOrBatch = "EXP: Dec 2027 • Batch #SN982",
                    trustScorePercent = 99,
                    notes = "Prescription verified with Chemist. Batch is sealed with tamper-proof silver foil.",
                    recommendedAction = "Approved for Neighbor Pass Emergency Delivery"
                )
            )
            "snacks" -> Pair(
                "Haldiram's Bhujia & Lays Wafers",
                ShelfVerificationResult(
                    detectedBrand = "Haldiram's Nagpur",
                    productName = "Aloo Bhujia 400g Family Pack",
                    estimatedPrice = 110,
                    expiryOrBatch = "MFG: 12 days ago • Crisp air-sealed pack",
                    trustScorePercent = 98,
                    notes = "Matched with customer snack request. Kirana has the fresh 400g pack.",
                    recommendedAction = "Approved for UPI Pay"
                )
            )
            else -> Pair(
                "Aashirvaad Shudh Chakki Atta 5kg",
                ShelfVerificationResult(
                    detectedBrand = "ITC Aashirvaad",
                    productName = "Shudh Chakki Whole Wheat Atta 5kg",
                    estimatedPrice = 245,
                    expiryOrBatch = "MFG: This Week • Fresh grain batch",
                    trustScorePercent = 100,
                    notes = "Heavy bag checked by Dost. Packaging is clean without moisture.",
                    recommendedAction = "Approved for UPI Pay"
                )
            )
        }

        val bitmap = createSampleShelfBitmap(label)
        val base64 = GeminiApiClient.bitmapToBase64(bitmap)
        _uiState.update {
            it.copy(
                activeVerificationImageBase64 = base64,
                shelfVerificationResult = result,
                userNotification = "Loaded Dost-Cam shelf sample: $label"
            )
        }
    }

    fun completeUpiPayment(taskId: String) {
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, TaskStatus.IN_TRANSIT)
            _uiState.update {
                it.copy(
                    showUpiSheetForTask = null,
                    userNotification = "UPI Payment Successful! Runner is heading to your doorstep."
                )
            }
        }
    }

    fun createEmergencyPassChore(
        title: String,
        choreType: TaskType,
        itemsDescription: String,
        address: String,
        urgency: String = "Emergency Care",
        cost: Int = 200,
        selectedRunner: DostRunner? = null
    ) {
        viewModelScope.launch {
            val task = repository.createTask(
                title = title,
                taskType = choreType,
                itemsDescription = itemsDescription,
                estimatedCost = cost,
                deliveryFee = 35,
                destinationAddress = address,
                urgency = urgency,
                runner = selectedRunner
            )
            _uiState.update {
                it.copy(
                    showCreateTaskDialog = false,
                    showHireDostDialog = null,
                    userNotification = "Neighbor Pass dispatched to ${task.runnerName}! Safe OTP: ${task.otpCode}"
                )
            }
        }
    }

    fun toggleMeetupRsvp(meetupId: String, currentRsvp: Boolean) {
        viewModelScope.launch {
            repository.toggleMeetupRsvp(meetupId, currentRsvp)
            _uiState.update {
                it.copy(
                    userNotification = if (!currentRsvp) "RSVP Confirmed for The Kirana Connector!" else "RSVP Cancelled"
                )
            }
        }
    }

    fun claimReferralReward() {
        _uiState.update {
            it.copy(
                referralCredits = it.referralCredits + 100,
                referredFriendsCount = it.referredFriendsCount + 1,
                userNotification = "₹100 Door Dost credits added to your wallet!"
            )
        }
    }

    private fun createSampleShelfBitmap(label: String): Bitmap {
        val bitmap = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background shelf wall
        val bgPaint = Paint().apply { color = AndroidColor.rgb(240, 243, 246) }
        canvas.drawRect(0f, 0f, 600f, 400f, bgPaint)
        
        // Wooden grocery shelf plank
        val shelfPaint = Paint().apply { color = AndroidColor.rgb(180, 110, 50) }
        canvas.drawRect(40f, 260f, 560f, 290f, shelfPaint)
        
        // Product Package
        val prodPaint = Paint().apply { color = AndroidColor.rgb(234, 88, 12) }
        canvas.drawRoundRect(140f, 100f, 320f, 260f, 16f, 16f, prodPaint)
        
        val prodPaint2 = Paint().apply { color = AndroidColor.rgb(13, 148, 136) }
        canvas.drawRoundRect(340f, 130f, 460f, 260f, 16f, 16f, prodPaint2)

        // Text banner
        val textPaint = Paint().apply {
            color = AndroidColor.WHITE
            textSize = 22f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("DOST-CAM", 230f, 150f, textPaint)
        canvas.drawText("VERIFIED", 230f, 185f, textPaint)
        canvas.drawText("LIVE", 400f, 180f, textPaint)

        // Overlay caption
        val captionPaint = Paint().apply {
            color = AndroidColor.rgb(15, 23, 42)
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("📍 $label", 300f, 340f, captionPaint)

        // Timestamp & GPS watermark
        val metaPaint = Paint().apply {
            color = AndroidColor.rgb(100, 116, 139)
            textSize = 16f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Verified by Dost Runner • 14:38 IST • 100% Genuine", 300f, 375f, metaPaint)

        return bitmap
    }
}
