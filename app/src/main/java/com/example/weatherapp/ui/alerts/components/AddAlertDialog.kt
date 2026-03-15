package com.example.weatherapp.ui.alerts.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = alertType == "Notification", onClick = { alertType = "Notification" })
                    Text("Notification")
                    Spacer(modifier = Modifier.width(8.dp))
                    RadioButton(selected = alertType == "Alarm", onClick = { alertType = "Alarm" })
                    Text("Alarm")
                }

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

                Button(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
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
