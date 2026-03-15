package com.example.weatherapp.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun MapSelectionDialog(
    initialLocation: GeoPoint, // Using osmdroid's GeoPoint instead of LatLng
    onDismiss: () -> Unit,
    onLocationConfirmed: (GeoPoint) -> Unit
) {
    var selectedLocation by remember { mutableStateOf(initialLocation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Location on Map") },
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                AndroidView(
                    factory = { context ->
                        // Initialize osmdroid configuration
                        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
                        
                        MapView(context).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(10.0)
                            controller.setCenter(initialLocation)

                            val marker = Marker(this)
                            marker.position = initialLocation
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            overlays.add(marker)

                            val eventsReceiver = object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                    selectedLocation = p
                                    marker.position = p
                                    invalidate()
                                    return true
                                }

                                override fun longPressHelper(p: GeoPoint): Boolean {
                                    return false
                                }
                            }
                            overlays.add(MapEventsOverlay(eventsReceiver))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onLocationConfirmed(selectedLocation) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48319D))
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
