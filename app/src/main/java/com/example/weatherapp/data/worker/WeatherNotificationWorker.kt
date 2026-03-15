package com.example.weatherapp.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherapp.MainActivity
import com.example.weatherapp.data.repository.WeatherRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WeatherNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val repository: WeatherRepository by inject()

    override suspend fun doWork(): Result {
        val cityName = inputData.getString("city_name") ?: "Unknown City"
        val alertType = inputData.getString("alert_type") ?: "Notification"
        val lat = inputData.getDouble("lat", 0.0)
        val lon = inputData.getDouble("lon", 0.0)

        Log.d("WeatherWorker", "Worker started for $cityName ($lat, $lon)")

        return try {
            val response = if (lat != 0.0 && lon != 0.0) {
                repository.getCurrentWeatherByCoords(lat, lon)
            } else {
                repository.getWeatherByCity(cityName)
            }

            if (response.isSuccessful) {
                val weather = response.body()
                if (weather != null) {
                    val msg = "Current temperature in ${weather.name} is ${weather.main.temp.toInt()}°C. ${weather.weather.firstOrNull()?.description ?: ""}"
                    showNotification(weather.name, msg, alertType == "Alarm")
                    Result.success()
                } else {
                    Result.failure()
                }
            } else {
                showNotification(cityName, "Weather alert triggered, but couldn't fetch latest data.", alertType == "Alarm")
                Result.success()
            }
        } catch (e: Exception) {
            Log.e("WeatherWorker", "Error in worker", e)
            showNotification(cityName, "Weather alert triggered. Please check your connection.", alertType == "Alarm")
            Result.success()
        }
    }

    private fun showNotification(title: String, message: String, isAlarm: Boolean) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = if (isAlarm) "weather_alarm_channel_v5" else "weather_notification_channel"
        val channelName = if (isAlarm) "Weather Alarms" else "Weather Notifications"
        val importance = if (isAlarm) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = if (isAlarm) "Critical weather alarms" else "Daily weather notifications"
                if (isAlarm) {
                    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    setSound(soundUri, audioAttributes)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Deep Link Intent
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(if (isAlarm) "weazy://alarm/$title/$message" else "weazy://alerts"),
            applicationContext,
            MainActivity::class.java
        )
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(if (isAlarm) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(if (isAlarm) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (isAlarm) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            builder.setSound(soundUri)
            builder.setVibrate(longArrayOf(0, 1000, 500, 1000))
            builder.setFullScreenIntent(pendingIntent, true)
        }

        notificationManager.notify(if (isAlarm) 1001 else System.currentTimeMillis().toInt(), builder.build())
    }
}
