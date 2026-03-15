package com.example.weatherapp.ui.alerts

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.data.source.local.entity.WeatherAlertEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(onBack: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    // In a real app, this would come from a ViewModel
    val alerts = remember { mutableStateListOf<WeatherAlertEntity>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2E335A), Color(0xFF1C1B33))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "Weather Alerts",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alerts) { alert ->
                    AlertItem(alert, onDelete = { alerts.remove(alert) })
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF48319D),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Alert")
        }

        if (showDialog) {
            AddAlertDialog(
                onDismiss = { showDialog = false },
                onConfirm = { cityName, type, date, time ->
                    alerts.add(
                        WeatherAlertEntity(
                            cityName = cityName,
                            alertType = type,
                            alertDate = date,
                            alertTime = time
                        )
                    )
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun AlertItem(alert: WeatherAlertEntity, onDelete: () -> Unit) {
    val dateFormatter = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(alert.cityName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${dateFormatter.format(alert.alertDate)} at ${alert.alertTime}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Text(
                    "Type: ${alert.alertType}",
                    color = Color(0xFF4FC3F7),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun AddAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Date, String) -> Unit
) {
    val context = LocalContext.current
    var cityName by remember { mutableStateOf("") }
    var alertType by remember { mutableStateOf("Notification") }
    val calendar = remember { Calendar.getInstance() }
    
    var selectedDate by remember { mutableStateOf(calendar.time) }
    var selectedTime by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Weather Alert") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = cityName,
                    onValueChange = { cityName = it },
                    label = { Text("City Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Alert Type Dropdown (Simplified)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = alertType == "Notification", onClick = { alertType = "Notification" })
                    Text("Notification")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = alertType == "Alarm", onClick = { alertType = "Alarm" })
                    Text("Alarm")
                }

                // Date Picker Trigger
                Button(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                selectedDate = calendar.time
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(selectedDate))
                }

                // Time Picker Trigger
                Button(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(selectedTime)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                if (cityName.isNotBlank()) onConfirm(cityName, alertType, selectedDate, selectedTime) 
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
