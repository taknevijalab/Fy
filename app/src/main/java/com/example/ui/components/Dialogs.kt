package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DostRunner
import com.example.data.model.TaskType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onCreateTask: (String, TaskType, String, String, String, Int, DostRunner?) -> Unit,
    runners: List<DostRunner>
) {
    var title by remember { mutableStateOf("Shop & Drop: Daily Essentials") }
    var items by remember { mutableStateOf("Amul Toned Milk 1L, Britannia Brown Bread, 6 Eggs") }
    var address by remember { mutableStateOf("Flat 204, Sunshine Residency, 2nd Main") }
    var budget by remember { mutableStateOf("160") }
    var selectedType by remember { mutableStateOf(TaskType.KIRANA_SHOP_DROP) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.AddShoppingCart, contentDescription = null, tint = SaffronPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Delivery Request", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth().testTag("new_task_title_input")
                )

                OutlinedTextField(
                    value = items,
                    onValueChange = { items = it },
                    label = { Text("Items to Purchase / Errand Details") },
                    modifier = Modifier.fillMaxWidth().testTag("new_task_items_input"),
                    minLines = 2
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Doorstep Delivery Address") },
                    modifier = Modifier.fillMaxWidth().testTag("new_task_address_input")
                )

                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Estimated Item Cost (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = budget.toIntOrNull() ?: 150
                    onCreateTask(
                        title,
                        selectedType,
                        items,
                        address,
                        "Standard (15 Mins)",
                        cost,
                        runners.firstOrNull()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                modifier = Modifier.testTag("confirm_create_task_btn")
            ) {
                Text("Dispatch Dost", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun HireDostDialog(
    runner: DostRunner,
    onDismiss: () -> Unit,
    onConfirmHire: (String, String, Int) -> Unit
) {
    var taskDesc by remember { mutableStateOf("Pick up groceries from local store with Dost-Cam verify") }
    var address by remember { mutableStateOf("Flat 402, Greenwoods Heights") }
    var hours by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.DirectionsRun, contentDescription = null, tint = TrustTealPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hire ${runner.name}", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Police Background Verified • ₹${runner.hourlyRate}/hr • ${runner.distanceKm} km away",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        )
                    }
                }

                OutlinedTextField(
                    value = taskDesc,
                    onValueChange = { taskDesc = it },
                    label = { Text("What should ${runner.name} do?") },
                    modifier = Modifier.fillMaxWidth().testTag("hire_dost_task_desc_input"),
                    minLines = 2
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Doorstep Address") },
                    modifier = Modifier.fillMaxWidth().testTag("hire_dost_address_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = runner.hourlyRate * (hours.toIntOrNull() ?: 1)
                    onConfirmHire(taskDesc, address, cost)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                modifier = Modifier.testTag("confirm_hire_dost_btn")
            ) {
                Text("Confirm & Book (${runner.hourlyRate} ₹/hr)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddDailyDropDialog(
    onDismiss: () -> Unit,
    onConfirmAdd: (String, String, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf("Morning Milk & Meds Drop") }
    var items by remember { mutableStateOf("2x Fresh Cow Milk (500ml), 1x BP Tablets") }
    var time by remember { mutableStateOf("7:00 AM") }
    var floorNote by remember { mutableStateOf("4th Floor Door Hook (Flat 402)") }
    var monthlyBudget by remember { mutableStateOf("1600") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🥛", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Daily Drop Subscription", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Recurring daily morning deliveries straight to your top-floor door at 7:00 AM.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Drop Title") },
                    modifier = Modifier.fillMaxWidth().testTag("daily_drop_title_input")
                )

                OutlinedTextField(
                    value = items,
                    onValueChange = { items = it },
                    label = { Text("Daily Items (Milk, Bread, Meds)") },
                    modifier = Modifier.fillMaxWidth().testTag("daily_drop_items_input"),
                    minLines = 2
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = monthlyBudget,
                        onValueChange = { monthlyBudget = it },
                        label = { Text("Est. ₹/mo") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = floorNote,
                    onValueChange = { floorNote = it },
                    label = { Text("Apartment Doorstep Instructions") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = monthlyBudget.toIntOrNull() ?: 1500
                    onConfirmAdd(title, items, time, floorNote, budget)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                modifier = Modifier.testTag("confirm_add_daily_drop_btn")
            ) {
                Text("Activate Daily Drop", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ManageRentABroDialog(
    currentWage: Int = 300,
    currentPetrol: Int = 100,
    currentMargin: Int = 500,
    onDismiss: () -> Unit,
    onSaveRetainer: (Int, Int, Int) -> Unit
) {
    var wage by remember { mutableStateOf(currentWage.toString()) }
    var petrol by remember { mutableStateOf(currentPetrol.toString()) }
    var margin by remember { mutableStateOf(currentMargin.toString()) }

    val w = wage.toIntOrNull() ?: 300
    val p = petrol.toIntOrNull() ?: 100
    val m = margin.toIntOrNull() ?: 500
    val monthlyTotal = (w + p) * 30 + m

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🤝", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rent-a-Bro 30-Day Retainer", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Hire a runner for a 30-day block. Pay exact petrol + daily wage (\"driver fuel\").",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                OutlinedTextField(
                    value = wage,
                    onValueChange = { wage = it },
                    label = { Text("Daily Wage (₹ / day)") },
                    modifier = Modifier.fillMaxWidth().testTag("rent_wage_input")
                )

                OutlinedTextField(
                    value = petrol,
                    onValueChange = { petrol = it },
                    label = { Text("Petrol Allowance (₹ / day)") },
                    modifier = Modifier.fillMaxWidth().testTag("rent_petrol_input")
                )

                OutlinedTextField(
                    value = margin,
                    onValueChange = { margin = it },
                    label = { Text("Platform Margin (₹ / month)") },
                    modifier = Modifier.fillMaxWidth().testTag("rent_margin_input")
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "30-Day Total: ₹$monthlyTotal / month",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Text(
                            text = "Assign routine or random tasks via live chat all month long.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 11.sp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveRetainer(w, p, m) },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                modifier = Modifier.testTag("save_rent_a_bro_btn")
            ) {
                Text("Save Retainer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NeighborhoodPickerDialog(
    currentNeighborhood: String,
    isDetectingLocation: Boolean = false,
    onDetectLocation: () -> Unit = {},
    onDismiss: () -> Unit,
    onSelectNeighborhood: (String) -> Unit
) {
    val neighborhoods = listOf(
        "Indiranagar 4th Block, Bengaluru",
        "Koramangala 5th Block, Bengaluru",
        "Bandra West (14th Road), Mumbai",
        "Powai Hiranandani Gardens, Mumbai",
        "Lajpat Nagar IV, New Delhi",
        "Hauz Khas Enclave, New Delhi",
        "Jubilee Hills Road #36, Hyderabad",
        "Gachibowli Financial District, Hyderabad"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.LocationCity, contentDescription = null, tint = TrustTealPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Neighborhood", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // GPS Auto-Detect Button
                Button(
                    onClick = onDetectLocation,
                    enabled = !isDetectingLocation,
                    colors = ButtonDefaults.buttonColors(containerColor = TrustTealPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("detect_gps_location_btn")
                ) {
                    if (isDetectingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detecting GPS Coordinates...", fontSize = 12.sp)
                    } else {
                        Icon(imageVector = Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Auto-Detect GPS Neighborhood", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "Or choose a neighborhood hub:",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    neighborhoods.forEach { area ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectNeighborhood(area) }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (area == currentNeighborhood) Icons.Filled.CheckCircle else Icons.Filled.Place,
                                contentDescription = null,
                                tint = if (area == currentNeighborhood) SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = area,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (area == currentNeighborhood) FontWeight.Bold else FontWeight.Normal,
                                    color = if (area == currentNeighborhood) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
