package com.pilltip.pilltip.model

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pilltip.pilltip.MainActivity
import com.pilltip.pilltip.R
import com.pilltip.pilltip.model.signUp.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.Path

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "새로운 토큰 발급됨: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "알림 수신 성공")

        val title = remoteMessage.data["title"] ?: "PillTip"
        val body = remoteMessage.data["body"] ?: "복약 알림이에요!"
        val logId = remoteMessage.data["logId"]?.toLongOrNull() ?: -1L

        sendNotification(title, body, logId)
    }

    private fun sendNotification(title: String, messageBody: String, logId: Long) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_title", title)
            putExtra("notification_body", messageBody)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        // 5분 뒤 다시 알림 버튼
        val snoozeIntent = Intent(this, SnoozeReceiver::class.java).apply {
            putExtra("notification_id", 0)
            putExtra("title", title)
            putExtra("body", messageBody)
            putExtra("logId", logId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this, 1, snoozeIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 복약 완료 버튼
        val markAsTakenIntent = Intent(this, MarkAsTakenReceiver::class.java).apply {
            putExtra("notification_id", 0)
            putExtra("logId", logId)
        }
        val markAsTakenPendingIntent = PendingIntent.getBroadcast(
            this, 2, markAsTakenIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_pilltip)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .addAction(0, "5분 뒤 다시 알림", snoozePendingIntent)
            .addAction(0, "복약 완료", markAsTakenPendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "기본 알림 채널",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}

class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra("logId", -1L)

        if (logId == -1L) {
            Toast.makeText(context, "logId가 유효하지 않아요.", Toast.LENGTH_SHORT).show()
            return
        }

        val api = ApiClient.getInstance(context)

        api.markDosageAsPending(logId).enqueue(object : Callback<PendingResponse> {
            override fun onResponse(call: Call<PendingResponse>, response: Response<PendingResponse>) {
                if (response.isSuccessful) {
                    val message = "5분 뒤 알림을 다시 보내드릴게요!"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "예약 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PendingResponse>, t: Throwable) {
                Toast.makeText(context, "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

interface ApiService {
    @POST("api/alarm/{logId}/taken")
    fun markDosageAsTaken(
        @Path("logId") logId: Long
    ): Call<TakenResponse>

    @POST("api/alarm/{logId}/pending")
    fun markDosageAsPending(@Path("logId") logId: Long): Call<PendingResponse>
}

object ApiClient {
    private const val BASE_URL = "https://pilltip.com:20022/"

    fun getInstance(context: Context): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val accessToken = TokenManager.getAccessToken(context)

                val requestWithAuth = if (!accessToken.isNullOrBlank()) {
                    originalRequest.newBuilder()
                        .addHeader("Authorization", "Bearer $accessToken")
                        .build()
                } else {
                    originalRequest
                }

                chain.proceed(requestWithAuth)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

data class TakenResponse(
    val status: String,
    val message: String?,
    val data: String
)

data class PendingResponse(
    val status: String,
    val message: String?,
    val data: String
)

class MarkAsTakenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val logId = intent.getLongExtra("logId", -1L)
        val notificationId = intent.getIntExtra("notification_id", 0)

        if (logId == -1L) {
            Toast.makeText(context, "logId가 유효하지 않아요.", Toast.LENGTH_SHORT).show()
            return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)

        val api = ApiClient.getInstance(context)

        api.markDosageAsTaken(logId).enqueue(object : Callback<TakenResponse> {
            override fun onResponse(call: Call<TakenResponse>, response: Response<TakenResponse>) {
                if (response.isSuccessful) {
                    val message = response.body()?.data ?: "복약 완료!"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "응답 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TakenResponse>, t: Throwable) {
                Toast.makeText(context, "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}


