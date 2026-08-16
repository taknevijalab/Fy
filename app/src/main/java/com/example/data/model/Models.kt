package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class DostCategory {
    COLLEGE_STUDENT,
    LOCAL_NEIGHBOR,
    PRO_RUNNER,
    EMERGENCY_SPECIALIST
}

enum class TaskType {
    KIRANA_SHOP_DROP,
    NEIGHBOR_PASS_EMERGENCY,
    RENT_A_BRO_CHORE,
    MEDICINE_RUN,
    ELDERLY_CARE_CHECKIN,
    HEAVY_LIFT
}

enum class TaskStatus {
    PENDING,
    RUNNER_ASSIGNED,
    SHELF_VERIFYING,
    VERIFIED_AWAITING_PAY,
    IN_TRANSIT,
    COMPLETED
}

enum class MessageSender {
    USER,
    GEMINI,
    DOST_RUNNER,
    SYSTEM
}

@Entity(tableName = "dost_runners")
data class DostRunner(
    @PrimaryKey val id: String,
    val name: String,
    val age: Int,
    val category: DostCategory,
    val collegeOrArea: String,
    val rating: Double,
    val completedDoorsteps: Int,
    val isPoliceVerified: Boolean = true,
    val isCommunityCertified: Boolean = true,
    val distanceKm: Double,
    val badges: List<String>,
    val phoneNumber: String,
    val hourlyRate: Int,
    val status: String, // "Active Nearby", "Available"
    val about: String,
    val avatarColorHex: Long
)

@Entity(tableName = "task_orders")
data class TaskOrder(
    @PrimaryKey val id: String,
    val title: String,
    val taskType: TaskType,
    val status: TaskStatus,
    val assignedRunnerId: String? = null,
    val runnerName: String? = null,
    val storeName: String? = null,
    val itemsDescription: String,
    val estimatedCost: Int,
    val deliveryFee: Int = 30,
    val livePhotoSnippet: String? = null,
    val isDostCamVerified: Boolean = false,
    val otpCode: String,
    val urgency: String, // "Emergency", "Standard", "Scheduled"
    val createdAt: Long = System.currentTimeMillis(),
    val destinationAddress: String,
    val specialInstructions: String = ""
)

@Entity(tableName = "community_meetups")
data class CommunityMeetup(
    @PrimaryKey val id: String,
    val title: String,
    val dateString: String,
    val location: String,
    val attendeesCount: Int,
    val isUserRsvpd: Boolean = false,
    val description: String,
    val tag: String
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val imageBase64: String? = null,
    val isShelfVerified: Boolean = false,
    val detectedBrand: String? = null,
    val detectedPrice: Int? = null,
    val confidenceNote: String? = null
)

data class KiranaStore(
    val id: String,
    val name: String,
    val ownerName: String,
    val neighborhood: String,
    val distanceMeters: Int,
    val rating: Double,
    val category: String,
    val isPartner: Boolean,
    val popularItems: List<String>
)

data class ShelfVerificationResult(
    val detectedBrand: String,
    val productName: String,
    val estimatedPrice: Int,
    val expiryOrBatch: String,
    val trustScorePercent: Int,
    val notes: String,
    val recommendedAction: String
)

data class PriceBreakdown(
    val itemMrp: Int,
    val distanceKm: Double,
    val distanceCharge: Int,
    val flatPlatformFee: Int = 5,
    val totalAmount: Int
)

@Entity(tableName = "daily_drop_subscriptions")
data class DailyDropSubscription(
    @PrimaryKey val id: String,
    val title: String,
    val items: String,
    val deliveryTime: String = "7:00 AM",
    val deliveryFloorNote: String = "4th Floor Door Hook",
    val monthlyEstimate: Int,
    val isActive: Boolean = true,
    val daysOfWeek: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    val assignedRunnerName: String = "Rahul Sharma"
)

@Entity(tableName = "rent_a_bro_subscriptions")
data class RentABroSubscription(
    @PrimaryKey val id: String,
    val runnerId: String,
    val runnerName: String,
    val monthlyPackageTitle: String = "30-Day Personal Runner Retainer",
    val dailyWage: Int = 300,
    val petrolAllowancePerDay: Int = 100,
    val platformMarginMonthly: Int = 500,
    val totalMonthlyFee: Int = 12500,
    val daysRemaining: Int = 28,
    val activeTasks: List<String> = listOf(
        "7:00 AM - Morning Milk & Bread Drop",
        "1:00 PM - Office Lunchbox Delivery",
        "6:30 PM - Fresh Veggies & Prescription Check"
    ),
    val isActive: Boolean = true
)

data class ExplainerScene(
    val id: Int,
    val title: String,
    val timeCode: String,
    val visualDesc: String,
    val voiceover: String,
    val iconEmoji: String,
    val highlightTag: String
)

// --- Gemini REST API Models ---

@JsonClass(generateAdapter = true)
data class GeminiApiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    val inline_data: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    val mime_type: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiApiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiApiError? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiApiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)
