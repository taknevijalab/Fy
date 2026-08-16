package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeighborPassScreen(
    runners: List<DostRunner>,
    activeTasks: List<TaskOrder>,
    onDispatchChore: (String, TaskType, String, String, String, Int, DostRunner?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(TaskType.MEDICINE_RUN) }
    var choreTitle by remember { mutableStateOf("Urgent Prescription Medicine Pickup") }
    var choreItems by remember { mutableStateOf("Blood pressure meds (Amlodipine 5mg), Dolo 650, ORS sachets") }
    var destinationAddress by remember { mutableStateOf("Flat 302, Senior Living Enclave, 5th Main") }
    var urgencyLevel by remember { mutableStateOf("Emergency Care (Under 20 Mins)") }
    var selectedRunner by remember { mutableStateOf(runners.firstOrNull { it.isPoliceVerified }) }
    var estimatedBudget by remember { mutableStateOf("250") }

    val emergencyTasks = activeTasks.filter {
        it.taskType == TaskType.NEIGHBOR_PASS_EMERGENCY ||
        it.taskType == TaskType.MEDICINE_RUN ||
        it.taskType == TaskType.ELDERLY_CARE_CHECKIN
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("neighbor_pass_screen"),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmergencyRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.HealthAndSafety,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Neighbor Pass (Emergency Chores)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = EmergencyRed
                                )
                            )
                            Text(
                                text = "Rent-a-Bro Trusted Sub-Mode for Elderly Parents & Urgencies",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Assign highly trusted, police-verified runners to pick up urgent prescribed medicines or check in on elderly parents with photo verification and 4-digit OTP handover.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // Active Emergency Pass Runs
        if (emergencyTasks.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Active Emergency Errands (${emergencyTasks.size})",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    emergencyTasks.forEach { task ->
                        TaskProgressCard(
                            task = task,
                            onVerifyDostCam = {},
                            onPayUpi = {},
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Dispatch New Emergency Chore Form
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emergency_chore_form_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dispatch an Emergency Chore",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Chore Category Chips
                    Text(
                        text = "Select Urgent Task Type:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EmergencyTypeChip(
                                label = "💊 Prescribed Medicines",
                                isSelected = selectedCategory == TaskType.MEDICINE_RUN,
                                onClick = {
                                    selectedCategory = TaskType.MEDICINE_RUN
                                    choreTitle = "Urgent Prescription Medicine Pickup"
                                    choreItems = "Prescription Strip: Blood pressure & Cardiac meds"
                                },
                                modifier = Modifier.weight(1f)
                            )
                            EmergencyTypeChip(
                                label = "👵 Elderly Parent Care",
                                isSelected = selectedCategory == TaskType.ELDERLY_CARE_CHECKIN,
                                onClick = {
                                    selectedCategory = TaskType.ELDERLY_CARE_CHECKIN
                                    choreTitle = "Elderly Parents Wellness Check-in & Essentials"
                                    choreItems = "Fresh coconut water, fruit basket, wellness greeting"
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EmergencyTypeChip(
                                label = "🔑 Spare Key Drop",
                                isSelected = selectedCategory == TaskType.RENT_A_BRO_CHORE,
                                onClick = {
                                    selectedCategory = TaskType.RENT_A_BRO_CHORE
                                    choreTitle = "Emergency Spare Key Handover"
                                    choreItems = "Apartment duplicate keys in sealed envelope"
                                },
                                modifier = Modifier.weight(1f)
                            )
                            EmergencyTypeChip(
                                label = "📦 Heavy LPG / Water",
                                isSelected = selectedCategory == TaskType.HEAVY_LIFT,
                                onClick = {
                                    selectedCategory = TaskType.HEAVY_LIFT
                                    choreTitle = "Heavy LPG Cylinder / 20L Water Can"
                                    choreItems = "20L Bisleri Water Jar doorstep doorstep lift"
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title Field
                    OutlinedTextField(
                        value = choreTitle,
                        onValueChange = { choreTitle = it },
                        label = { Text("Chore Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emergency_title_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Description / Prescriptions
                    OutlinedTextField(
                        value = choreItems,
                        onValueChange = { choreItems = it },
                        label = { Text("Details & Special Instructions") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emergency_items_input"),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Destination Address
                    OutlinedTextField(
                        value = destinationAddress,
                        onValueChange = { destinationAddress = it },
                        label = { Text("Destination Address / Wing / Flat") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Home, contentDescription = null, tint = TrustTealPrimary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emergency_address_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Select Verified Runner
                    Text(
                        text = "Assign Police-Verified Runner:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val verifiedRunners = runners.filter { it.isPoliceVerified }
                    verifiedRunners.forEach { runner ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedRunner?.id == runner.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedRunner = runner }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedRunner?.id == runner.id,
                                onClick = { selectedRunner = runner }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = runner.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Filled.Verified, contentDescription = null, tint = TrustTealPrimary, modifier = Modifier.size(14.dp))
                                }
                                Text(
                                    text = "${runner.collegeOrArea} • ${runner.distanceKm} km away",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Text(
                                text = "₹${runner.hourlyRate}/hr",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SaffronPrimary)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dispatch Button
                    Button(
                        onClick = {
                            val cost = estimatedBudget.toIntOrNull() ?: 200
                            onDispatchChore(
                                choreTitle,
                                selectedCategory,
                                choreItems,
                                destinationAddress,
                                urgencyLevel,
                                cost,
                                selectedRunner
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("dispatch_emergency_pass_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dispatch Neighbor Pass (4-Digit OTP Enabled)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Trust Protocol Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🛡️ Door Dost Safety Guarantee",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SafetyPoint(text = "✓ 100% Police Background Checked & Aadhaar Verified Dosts")
                    SafetyPoint(text = "✓ Real-time Dost-Cam live snapshot of medicine strips & chemist bill")
                    SafetyPoint(text = "✓ 4-digit Handshake OTP verification before doorstep package handover")
                    SafetyPoint(text = "✓ Direct phone call & SOS support with neighborhood community lead")
                }
            }
        }
    }
}

@Composable
private fun EmergencyTypeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isSelected) EmergencyRed else Color.LightGray.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) EmergencyRed else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun SafetyPoint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        ),
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
