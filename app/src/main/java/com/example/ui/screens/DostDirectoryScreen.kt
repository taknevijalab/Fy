package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.DostCategory
import com.example.data.model.DostRunner
import com.example.ui.components.DostRunnerCard
import com.example.ui.theme.*

@Composable
fun DostDirectoryScreen(
    runners: List<DostRunner>,
    selectedCategory: DostCategory?,
    onSelectCategory: (DostCategory?) -> Unit,
    onHireDost: (DostRunner) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredRunners = if (selectedCategory == null) {
        runners
    } else {
        runners.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dost_directory_screen"),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Screen Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.People,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Verified Dost Directory",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Your Neighborhood Runners & College Buddies",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Every Dost carries a verified police background clearance, community rating score, and live Dost-Cam certification. Hire them for rent-a-bro chores, heavy deliveries, or grocery runs.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onSelectCategory(null) },
                        label = { Text("All Dosts (${runners.size})") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == DostCategory.COLLEGE_STUDENT,
                        onClick = { onSelectCategory(DostCategory.COLLEGE_STUDENT) },
                        label = { Text("🎓 College Students") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == DostCategory.LOCAL_NEIGHBOR,
                        onClick = { onSelectCategory(DostCategory.LOCAL_NEIGHBOR) },
                        label = { Text("🏡 Local Neighbors") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == DostCategory.PRO_RUNNER,
                        onClick = { onSelectCategory(DostCategory.PRO_RUNNER) },
                        label = { Text("⚡ Pro Runners") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == DostCategory.EMERGENCY_SPECIALIST,
                        onClick = { onSelectCategory(DostCategory.EMERGENCY_SPECIALIST) },
                        label = { Text("🚨 Emergency Certified") }
                    )
                }
            }
        }

        // Runners List
        items(filteredRunners) { runner ->
            DostRunnerCard(
                runner = runner,
                onHireClick = onHireDost
            )
        }
    }
}
