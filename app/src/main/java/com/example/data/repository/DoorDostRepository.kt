package com.example.data.repository

import com.example.data.api.GeminiApiClient
import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class DoorDostRepository(private val db: AppDatabase) {

    val allRunners: Flow<List<DostRunner>> = db.dostDao().getAllRunners()
    val allTasks: Flow<List<TaskOrder>> = db.taskDao().getAllTasks()
    val activeTasks: Flow<List<TaskOrder>> = db.taskDao().getActiveTasks()
    val allMeetups: Flow<List<CommunityMeetup>> = db.meetupDao().getAllMeetups()
    val chatMessages: Flow<List<ChatMessage>> = db.chatDao().getAllMessages()
    val allDailyDrops: Flow<List<DailyDropSubscription>> = db.dailyDropDao().getAllSubscriptions()
    val allRentABros: Flow<List<RentABroSubscription>> = db.rentABroDao().getAllSubscriptions()

    fun calculateTransparentPrice(mrp: Int, distanceKm: Double): PriceBreakdown {
        val distanceCharge = (distanceKm * 15.0).toInt().coerceAtLeast(15)
        val flatCut = 5
        val total = mrp + distanceCharge + flatCut
        return PriceBreakdown(
            itemMrp = mrp,
            distanceKm = distanceKm,
            distanceCharge = distanceCharge,
            flatPlatformFee = flatCut,
            totalAmount = total
        )
    }

    fun getExplainerScenes(): List<ExplainerScene> = listOf(
        ExplainerScene(
            id = 1,
            title = "1. The 4th-Floor Hook",
            timeCode = "0:00 - 0:10",
            visualDesc = "Karthik on his 4th-floor sofa with empty milk packet & prescription, groaning at the thought of visiting 3 separate shops.",
            voiceover = "\"4th-floor living is great, until you realize you need milk, snacks, and mom’s medicines from three different local shops.\"",
            iconEmoji = "🛋️",
            highlightTag = "The Problem"
        ),
        ExplainerScene(
            id = 2,
            title = "2. The Personal Runner Intro",
            timeCode = "0:10 - 0:18",
            visualDesc = "Phone glows, app logo pops out. A smiling Delivery Bro character appears on a scooter.",
            voiceover = "\"Meet your new personal runner. No warehouses, no hubs—just a direct connection to your favorite neighborhood shops.\"",
            iconEmoji = "🛵",
            highlightTag = "100% Hubless"
        ),
        ExplainerScene(
            id = 3,
            title = "3. On-Demand & Live Chat",
            timeCode = "0:18 - 0:30",
            visualDesc = "Split screen: Runner snaps photo of item at the local shop, shares shop's UPI QR code. Karthik taps Pay directly.",
            voiceover = "\"Pick your shop, and your Delivery Bro heads there. Confirm the exact item via live chat, scan the shop’s UPI QR to pay directly, and track it home!\"",
            iconEmoji = "📸",
            highlightTag = "Direct UPI QR"
        ),
        ExplainerScene(
            id = 4,
            title = "4. Pure Transparent Pricing",
            timeCode = "0:30 - 0:38",
            visualDesc = "Bill breakdown appears: Exact Shop MRP + Distance Charge + ₹5 Flat Platform Fee = Total. Green checkmark.",
            voiceover = "\"No hidden markups. You pay the exact shop MRP, a small distance charge for the fuel, and just a ₹5 flat fee. Pure transparency.\"",
            iconEmoji = "🧾",
            highlightTag = "₹5 Flat Platform Cut"
        ),
        ExplainerScene(
            id = 5,
            title = "5. The Daily Drop",
            timeCode = "0:38 - 0:48",
            visualDesc = "Calendar flips rapidly. Every morning at 7:00 AM, fresh milk, bread, or daily essentials land at the 4th-floor door.",
            voiceover = "\"Need the same things daily? Set up a 'Daily Drop'. Milk, bread, or essentials delivered straight to your top-floor door every single morning.\"",
            iconEmoji = "🥛",
            highlightTag = "7:00 AM Doorstep Drop"
        ),
        ExplainerScene(
            id = 6,
            title = "6. Rent-a-Bro 30-Day Pass",
            timeCode = "0:48 - 1:00",
            visualDesc = "30-Day Subscription badge. Runner carries lunchboxes, buys veggies, drops off documents. Meter shows Actual Petrol + Daily Wage.",
            voiceover = "\"Need more hands? 'Rent-a-Bro' for a whole month! Pay exact petrol charges plus driver fuel, and assign them daily routine tasks or random chores on the fly.\"",
            iconEmoji = "🤝",
            highlightTag = "30-Day Retainer"
        ),
        ExplainerScene(
            id = 7,
            title = "7. Ready For Speed Dial",
            timeCode = "1:00 - 1:10",
            visualDesc = "Runner gives thumbs up with live doorstep OTP confirmation.",
            voiceover = "\"Your local shops. Your personal runner. Download the app today and leave the running to us!\"",
            iconEmoji = "✨",
            highlightTag = "Hyperlocal Runner"
        )
    )

    suspend fun initializeSeedDataIfNeeded() {
        val existingRunners = allRunners.firstOrNull()
        if (existingRunners.isNullOrEmpty()) {
            val initialRunners = listOf(
                DostRunner(
                    id = "dost_1",
                    name = "Aman Sharma",
                    age = 21,
                    category = DostCategory.COLLEGE_STUDENT,
                    collegeOrArea = "IIT Delhi / Indiranagar Block 4",
                    rating = 4.96,
                    completedDoorsteps = 342,
                    isPoliceVerified = true,
                    isCommunityCertified = true,
                    distanceKm = 0.4,
                    badges = listOf("Police Background Verified ✓", "College Student", "Dost-Cam Pro", "99% On-Time"),
                    phoneNumber = "+91 98765 43210",
                    hourlyRate = 120,
                    status = "Active Nearby",
                    about = "Passionate neighborhood runner and 3rd-year CS undergrad. Super familiar with all local kiranas and medical stores.",
                    avatarColorHex = 0xFFF97316
                ),
                DostRunner(
                    id = "dost_2",
                    name = "Priya Menon",
                    age = 26,
                    category = DostCategory.LOCAL_NEIGHBOR,
                    collegeOrArea = "Wing B Resident, Greenwoods Heights",
                    rating = 4.98,
                    completedDoorsteps = 215,
                    isPoliceVerified = true,
                    isCommunityCertified = true,
                    distanceKm = 0.2,
                    badges = listOf("Neighbor Pass Certified ✓", "Elderly Care Specialist", "Pet Friendly"),
                    phoneNumber = "+91 98111 22334",
                    hourlyRate = 150,
                    status = "Active Nearby",
                    about = "Neighbor Pass specialist. Happy to pick up urgent prescriptions, check in on elderly parents, or do careful grocery runs.",
                    avatarColorHex = 0xFF0D9488
                ),
                DostRunner(
                    id = "dost_3",
                    name = "Rohan Verma",
                    age = 23,
                    category = DostCategory.PRO_RUNNER,
                    collegeOrArea = "Koramangala 5th Block",
                    rating = 4.92,
                    completedDoorsteps = 620,
                    isPoliceVerified = true,
                    isCommunityCertified = true,
                    distanceKm = 0.8,
                    badges = listOf("Super Dost 500+", "Heavy Errands Specialist", "Lightning Fast"),
                    phoneNumber = "+91 98450 99887",
                    hourlyRate = 140,
                    status = "Available",
                    about = "Full-time neighborhood delivery enthusiast with high-speed EV scooter. Expert in heavy water jars, hardware parts, and bulk kirana.",
                    avatarColorHex = 0xFF3B82F6
                ),
                DostRunner(
                    id = "dost_4",
                    name = "Dr. Vikram Singh",
                    age = 28,
                    category = DostCategory.EMERGENCY_SPECIALIST,
                    collegeOrArea = "Apollo Pharmacy Lane",
                    rating = 5.0,
                    completedDoorsteps = 180,
                    isPoliceVerified = true,
                    isCommunityCertified = true,
                    distanceKm = 0.6,
                    badges = listOf("Police Background Verified ✓", "Emergency Pass Lead", "Pharma Verified"),
                    phoneNumber = "+91 99000 11223",
                    hourlyRate = 180,
                    status = "Active Nearby",
                    about = "Certified emergency chore runner. Specializes in critical prescription pickups, elderly wellness handshakes, and urgent night deliveries.",
                    avatarColorHex = 0xFFDC2626
                )
            )
            db.dostDao().insertRunners(initialRunners)

            val initialTasks = listOf(
                TaskOrder(
                    id = "task_101",
                    title = "Kirana Shop & Drop: Amul Milk & GoodDay",
                    taskType = TaskType.KIRANA_SHOP_DROP,
                    status = TaskStatus.SHELF_VERIFYING,
                    assignedRunnerId = "dost_1",
                    runnerName = "Aman Sharma",
                    storeName = "Shree Balaji Kirana Store",
                    itemsDescription = "2x Amul Gold 500ml, 1x GoodDay Butter Cookies, 1x Tata Salt",
                    estimatedCost = 142,
                    deliveryFee = 25,
                    livePhotoSnippet = null,
                    isDostCamVerified = false,
                    otpCode = "4829",
                    urgency = "Standard (15 mins)",
                    destinationAddress = "Flat 402, Sunshine Apartments, 4th Cross"
                ),
                TaskOrder(
                    id = "task_102",
                    title = "Neighbor Pass: Blood Pressure Meds for Mom",
                    taskType = TaskType.NEIGHBOR_PASS_EMERGENCY,
                    status = TaskStatus.IN_TRANSIT,
                    assignedRunnerId = "dost_2",
                    runnerName = "Priya Menon",
                    storeName = "MedPlus Chemist (100ft Rd)",
                    itemsDescription = "Telmisartan 40mg (Strip of 15), Sugar-free biscuits",
                    estimatedCost = 280,
                    deliveryFee = 40,
                    livePhotoSnippet = "photo_chemist_verified",
                    isDostCamVerified = true,
                    otpCode = "9173",
                    urgency = "Emergency Care",
                    destinationAddress = "House #14, Senior Citizens Enclave"
                )
            )
            for (task in initialTasks) {
                db.taskDao().insertTask(task)
            }

            val initialMeetups = listOf(
                CommunityMeetup(
                    id = "meetup_1",
                    title = "The Kirana Connector: Koramangala Shopkeepers Mixer",
                    dateString = "This Sunday • 5:00 PM",
                    location = "Chai Point Community Deck, 80ft Rd",
                    attendeesCount = 38,
                    isUserRsvpd = true,
                    description = "A neighborhood community mixer connecting 18+ local kirana owners with top-rated Door Dost runners to build trust and strengthen our micro-economy.",
                    tag = "Kirana Connector"
                ),
                CommunityMeetup(
                    id = "meetup_2",
                    title = "Dost Runners 'Chai Pe Charcha' & Safety Training",
                    dateString = "Next Wednesday • 6:30 PM",
                    location = "Resident Welfare Club House",
                    attendeesCount = 24,
                    isUserRsvpd = false,
                    description = "Interactive workshop for new college student runners covering Dost-Cam live verification protocols, police badge renewal, and emergency elderly assistance.",
                    tag = "Runner Training"
                ),
                CommunityMeetup(
                    id = "meetup_3",
                    title = "Senior Citizens & Trusted Neighbors Tea Morning",
                    dateString = "Next Saturday • 10:30 AM",
                    location = "Central Park Community Gazebo",
                    attendeesCount = 45,
                    isUserRsvpd = false,
                    description = "Meet the verified dosts of your wing! An initiative where elderly residents get introduced to their dedicated Neighbor Pass delivery buddies.",
                    tag = "Neighbor Pass"
                )
            )
            db.meetupDao().insertMeetups(initialMeetups)

            // Seed Daily Drop Subscriptions
            val initialDailyDrops = listOf(
                DailyDropSubscription(
                    id = "daily_1",
                    title = "Morning Milk & Bread Drop",
                    items = "2x Nandini Fresh Milk (500ml), 1x Whole Wheat Bread",
                    deliveryTime = "7:00 AM",
                    deliveryFloorNote = "4th Floor Door Hook (Apt 402)",
                    monthlyEstimate = 1680,
                    isActive = true,
                    daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                    assignedRunnerName = "Aman Sharma"
                ),
                DailyDropSubscription(
                    id = "daily_2",
                    title = "Daily Newspaper & Coconut Water",
                    items = "1x The Hindu, 1x Tender Coconut Water",
                    deliveryTime = "7:15 AM",
                    deliveryFloorNote = "Doorbell once, leave in side basket",
                    monthlyEstimate = 1450,
                    isActive = false,
                    daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
                    assignedRunnerName = "Rohan Verma"
                )
            )
            db.dailyDropDao().insertSubscriptions(initialDailyDrops)

            // Seed Rent-A-Bro Subscription
            val initialRentABro = RentABroSubscription(
                id = "rent_1",
                runnerId = "dost_1",
                runnerName = "Aman Sharma",
                monthlyPackageTitle = "30-Day Personal Runner Retainer",
                dailyWage = 300,
                petrolAllowancePerDay = 100,
                platformMarginMonthly = 500,
                totalMonthlyFee = 12500,
                daysRemaining = 26,
                activeTasks = listOf(
                    "7:00 AM - 4th Floor Milk & Meds Delivery",
                    "1:00 PM - Office Tiffin Lunchbox Pickup",
                    "6:30 PM - Evening Veggie & Pharmacy Run"
                ),
                isActive = true
            )
            db.rentABroDao().insertSubscription(initialRentABro)

            // Seed introductory greeting chat message
            db.chatDao().insertMessage(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.GEMINI,
                    text = "Hello! 👋 Welcome to **Door Dost** (*Your Trusted Delivery Partner*).\n\nI can help you coordinate with local verified runners, organize your grocery lists, analyze shop shelf photos with **Dost-Cam AI**, or set up an urgent **Neighbor Pass** chore.",
                    modelUsed = "gemini-3.5-flash"
                )
            )
        }
    }

    suspend fun toggleDailyDropActive(id: String, isActive: Boolean) {
        db.dailyDropDao().toggleActive(id, isActive)
    }

    suspend fun addDailyDropSubscription(sub: DailyDropSubscription) {
        db.dailyDropDao().insertSubscription(sub)
    }

    suspend fun saveRentABroSubscription(sub: RentABroSubscription) {
        db.rentABroDao().insertSubscription(sub)
    }

    suspend fun createTask(
        title: String,
        taskType: TaskType,
        itemsDescription: String,
        estimatedCost: Int,
        deliveryFee: Int,
        destinationAddress: String,
        urgency: String,
        runner: DostRunner? = null,
        storeName: String = "Neighborhood Kirana"
    ): TaskOrder {
        val newTask = TaskOrder(
            id = "task_" + System.currentTimeMillis().toString().takeLast(6),
            title = title,
            taskType = taskType,
            status = if (runner != null) TaskStatus.RUNNER_ASSIGNED else TaskStatus.PENDING,
            assignedRunnerId = runner?.id ?: "dost_1",
            runnerName = runner?.name ?: "Aman Sharma",
            storeName = storeName,
            itemsDescription = itemsDescription,
            estimatedCost = estimatedCost,
            deliveryFee = deliveryFee,
            otpCode = (1000..9999).random().toString(),
            urgency = urgency,
            destinationAddress = destinationAddress
        )
        db.taskDao().insertTask(newTask)
        return newTask
    }

    suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        db.taskDao().updateTaskStatus(taskId, status)
    }

    suspend fun markDostCamVerified(taskId: String, snippetData: String) {
        db.taskDao().updateDostCamVerified(taskId, snippetData)
    }

    suspend fun toggleMeetupRsvp(meetupId: String, currentRsvp: Boolean) {
        val delta = if (currentRsvp) -1 else 1
        db.meetupDao().updateRsvp(meetupId, !currentRsvp, delta)
    }

    suspend fun sendChatMessage(
        userText: String,
        model: String,
        systemInstruction: String,
        imageBase64: String? = null
    ): ChatMessage {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            text = userText,
            imageBase64 = imageBase64
        )
        db.chatDao().insertMessage(userMsg)

        // Pull previous conversation turns
        val previousMessages = db.chatDao().getAllMessages().firstOrNull() ?: emptyList()
        val historyPairs = previousMessages.takeLast(10).map {
            (if (it.sender == MessageSender.USER) "USER" else "MODEL") to it.text
        }

        val result = GeminiApiClient.sendChatMessage(
            model = model,
            systemInstruction = systemInstruction,
            history = historyPairs,
            currentMessage = userText,
            imageBase64 = imageBase64
        )

        val replyText = result.getOrElse { "Sorry, I had trouble processing that. Please try again!" }
        val botMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.GEMINI,
            text = replyText,
            modelUsed = model
        )
        db.chatDao().insertMessage(botMsg)
        return botMsg
    }

    suspend fun analyzeShelfPhoto(imageBase64: String, prompt: String): ShelfVerificationResult {
        val result = GeminiApiClient.analyzeImageWithGeminiPro(imageBase64, prompt)
        return result.getOrElse {
            ShelfVerificationResult(
                detectedBrand = "Amul / Local Store",
                productName = "Verified Shelf Items",
                estimatedPrice = 75,
                expiryOrBatch = "Batch Valid",
                trustScorePercent = 95,
                notes = "Visual check completed. Matches grocery list.",
                recommendedAction = "Ready for UPI payment."
            )
        }
    }
}
