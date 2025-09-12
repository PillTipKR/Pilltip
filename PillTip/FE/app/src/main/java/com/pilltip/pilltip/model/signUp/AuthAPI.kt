package com.pilltip.pilltip.model.signUp

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

interface ServerAuthAPI {
    @POST("api/auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/social-login")
    suspend fun socialLogin(
        @Body request: SocialLoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/terms")
    suspend fun submitTerms(
        @Header("Authorization") token: String,
    ): Response<AuthMeResponse>

    @GET("api/auth/me")
    suspend fun getMyInfo(
        @Header("Authorization") token: String,
        @Header("X-Profile-Id") profileId: Long? = null
    ): Response<AuthMeResponse>


    @POST("api/auth/check-duplicate")
    suspend fun checkDuplicate(
        @Body request: DuplicateCheckRequest
    ): Response<DuplicateCheckResponse>
}

interface ProfileApi {
    @DELETE("/api/user-profile")
    suspend fun deleteProfile(
        @Header("Authorization") token: String,
        @Header("X-Profile-Id") profileId: Long
    ): Response<Unit>
}

interface ProfileRepository {
    suspend fun deleteProfile(token: String, profileId: Long)
}

class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi
) : ProfileRepository {

    override suspend fun deleteProfile(token: String, profileId: Long) {
        val bearerToken = "Bearer $token"
        val response = api.deleteProfile(bearerToken, profileId)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            val message = JSONObject(errorBody ?: "{}")
                .optString("message", "프로필 삭제에 실패했어요.")
            throw Exception(message)
        }
    }
}


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    @Provides
    @Singleton
    @Named("AuthRetrofit")
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val gson = GsonBuilder().setLenient().serializeNulls().create()
        return Retrofit.Builder()
            .baseUrl("https://pilltip.com:20022/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    fun provideServerAuthAPI(@Named("AuthRetrofit") retrofit: Retrofit): ServerAuthAPI {
        return retrofit.create(ServerAuthAPI::class.java)
    }

    @Provides
    fun provideProfileApi(@Named("AuthRetrofit") retrofit: Retrofit): ProfileApi {
        return retrofit.create(ProfileApi::class.java)
    }

    @Provides
    fun provideProfileRepository(api: ProfileApi): ProfileRepository {
        return ProfileRepositoryImpl(api)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}