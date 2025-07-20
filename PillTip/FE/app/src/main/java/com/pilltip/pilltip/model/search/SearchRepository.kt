package com.pilltip.pilltip.model.search

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import javax.inject.Inject

/**
 * 약품명 자동완성 API
 * */
interface AutoCompleteApi {
    @GET("/api/autocomplete/drugs")
    suspend fun getAutoCompleteDatas(
        @Query("input") input: String,
        @Query("page") page: Int
    ): SearchResponse
}

interface AutoCompleteRepository {
    suspend fun getAutoComplete(query: String, page: Int = 0): List<SearchData>
}

class AutoCompleteRepositoryImpl(
    private val api: AutoCompleteApi
) : AutoCompleteRepository {
    override suspend fun getAutoComplete(query: String, page: Int): List<SearchData> {
        return api.getAutoCompleteDatas(query, page).data
    }
}

/**
 * 일반 검색 API
 * */
interface DrugSearchApi {
    @GET("/api/search/drugs")
    suspend fun searchDrugs(
        @Query("input") input: String,
        @Query("page") page: Int
    ): DrugSearchResponse
}

interface DrugSearchRepository {
    suspend fun search(query: String, page: Int = 0): List<DrugSearchResult>
}

class DrugSearchRepositoryImpl(
    private val api: DrugSearchApi
) : DrugSearchRepository {
    override suspend fun search(query: String, page: Int): List<DrugSearchResult> {
        return api.searchDrugs(query, page).data
    }
}

/**
 * 약품 상세 페이지 API
 * */
interface DrugDetailApi {
    @GET("/api/detailPage")
    suspend fun getDrugDetail(@Query("id") id: Long): DetailDrugResponse
}

interface DrugDetailRepository {
    suspend fun getDetail(id: Long): DetailDrugData
}

class DrugDetailRepositoryImpl(
    private val api: DrugDetailApi
) : DrugDetailRepository {
    override suspend fun getDetail(id: Long): DetailDrugData {
        return api.getDrugDetail(id).data
    }
}

/**
 * Pilltip AI API
 */

interface GptAdviceApi {
    @POST("/api/detailPage/gpt")
    suspend fun getGptAdvice(
        @Body detail: DetailDrugData
    ): GptAdviceResponse
}

interface GptAdviceRepository {
    suspend fun getGptAdvice(detail: DetailDrugData): String
}

class GptAdviceRepositoryImpl(
    private val api: GptAdviceApi
) : GptAdviceRepository {
    override suspend fun getGptAdvice(detail: DetailDrugData): String {
        return api.getGptAdvice(detail).data
    }
}

/**
 * 문진표 조회 및 수정
 */
interface QuestionnaireApi {
    @GET("/api/questionnaire")
    suspend fun getQuestionnaire(): QuestionnaireResponse

    @PUT("/api/questionnaire")
    suspend fun updateQuestionnaire(
        @Body request: QuestionnaireSubmitRequest
    ): QuestionnaireResponse
}

interface QuestionnaireRepository {
    suspend fun getQuestionnaire(): QuestionnaireData
    suspend fun updateQuestionnaire(
        request: QuestionnaireSubmitRequest
    ): QuestionnaireData

}

class QuestionnaireRepositoryImpl(
    private val api: QuestionnaireApi
) : QuestionnaireRepository {
    override suspend fun getQuestionnaire(): QuestionnaireData {
        return api.getQuestionnaire().data
    }

    override suspend fun updateQuestionnaire(
        request: QuestionnaireSubmitRequest
    ): QuestionnaireData {
        return api.updateQuestionnaire(request).data
    }
}

/**
 * 복약 등록 API
 *
 */
interface DosageRegisterApi {
    @POST("api/taking-pill")
    suspend fun registerDosage(
        @Body request: RegisterDosageRequest
    ): RegisterDosageResponse
}

interface DosageRegisterRepository {
    suspend fun registerDosage(request: RegisterDosageRequest): RegisterDosageResponse
}

class DosageRegisterRepositoryImpl(
    private val api: DosageRegisterApi
) : DosageRegisterRepository {
    override suspend fun registerDosage(request: RegisterDosageRequest): RegisterDosageResponse {
        return api.registerDosage(request)
    }
}

/**
 * 복약 리스트 불러오기 API
 */

interface DosageSummaryApi {
    @GET("/api/taking-pill")
    suspend fun getDosageSummary(): TakingPillSummaryResponse
}

interface DosageSummaryRepository {
    suspend fun getDosageSummary(): List<TakingPillSummary>
}

class DosageSummaryRepositoryImpl(
    private val api: DosageSummaryApi
) : DosageSummaryRepository {
    override suspend fun getDosageSummary(): List<TakingPillSummary> {
        return api.getDosageSummary().data.takingPills
    }
}

/**
 * 복약 데이터 삭제 API
 */

interface DosageDeleteApi {
    @DELETE("/api/taking-pill/{medicationId}")
    suspend fun deleteTakingPill(
        @Path("medicationId") medicationId: Long
    ): TakingPillSummaryResponse
}

interface DosageDeleteRepository {
    suspend fun deleteTakingPill(medicationId: Long): List<TakingPillSummary>
}

class DosageDeleteRepositoryImpl(
    private val api: DosageDeleteApi
) : DosageDeleteRepository {
    override suspend fun deleteTakingPill(medicationId: Long): List<TakingPillSummary> {
        return api.deleteTakingPill(medicationId).data.takingPills
    }
}

/**
 * 복약 세부 데이터 불러오기 API
 */

interface DosageDetailApi {
    @GET("/api/taking-pill/{medicationId}")
    suspend fun getDosageDetail(
        @Path("medicationId") medicationId: Long
    ): TakingPillDetailResponse
}

interface DosageDetailRepository {
    suspend fun getDosageDetail(medicationId: Long): TakingPillDetailData
}

class DosageDetailRepositoryImpl(
    private val api: DosageDetailApi
) : DosageDetailRepository {
    override suspend fun getDosageDetail(medicationId: Long): TakingPillDetailData {
        return api.getDosageDetail(medicationId).data
    }
}

/**
 * 복약 데이터 수정 API
 */
interface DosageModifyApi {
    @PUT("/api/taking-pill")
    suspend fun updateDosage(
        @Body request: RegisterDosageRequest
    ): TakingPillSummaryResponse
}

interface DosageModifyRepository {
    suspend fun updateDosage(
        medicationId: Long,
        request: RegisterDosageRequest
    ): List<TakingPillSummary>
}

class DosageModifyRepositoryImpl(
    private val api: DosageModifyApi
) : DosageModifyRepository {
    override suspend fun updateDosage(
        medicationId: Long,
        request: RegisterDosageRequest
    ): List<TakingPillSummary> {
        return api.updateDosage(request).data.takingPills
    }
}

/**
 * 민감정보 API
 */

interface SensitiveInfoApi {
    @PUT("/api/sensitive-info/profile")
    suspend fun updateSensitiveInfo(
        @Body request: SensitiveSubmitRequest
    ): SensitiveResponse

    @GET("/api/sensitive-info")
    suspend fun getSensitiveInfo(): SensitiveInfoResponse

    @DELETE("/api/sensitive-info/all")
    suspend fun deleteAllSensitiveInfo(): BaseResponse
}

interface SensitiveInfoRepository {
    suspend fun updateSensitiveProfile(request: SensitiveSubmitRequest): SensitiveResponseData
    suspend fun fetchSensitiveInfo(): SensitiveInfoData
    suspend fun deleteAllSensitiveInfo(): String

}

class SensitiveInfoRepositoryImpl(
    private val api: SensitiveInfoApi
) : SensitiveInfoRepository {
    override suspend fun updateSensitiveProfile(request: SensitiveSubmitRequest): SensitiveResponseData {
        return api.updateSensitiveInfo(request).data
    }

    override suspend fun fetchSensitiveInfo(): SensitiveInfoData {
        return api.getSensitiveInfo().data
    }

    override suspend fun deleteAllSensitiveInfo(): String {
        return api.deleteAllSensitiveInfo().data
    }
}

/**
 * 문진표 QR
 */
interface QrApi {
    @POST
    suspend fun postQrPath(@Url path: String): QrResponseWrapper
}

interface QrRepository {
    suspend fun submitQrRequest(path: String): QrData
}

class QrRepositoryImpl(
    private val api: QrApi
) : QrRepository {
    override suspend fun submitQrRequest(path: String): QrData {
        val response = api.postQrPath(path)
        if (response.status != "success" || response.data == null) {
            throw Exception(response.message)
        }
        return response.data
    }
}


/**
 * DUR 기능
 */

interface DurGptApi {
    @GET("/api/dur/gpt")
    suspend fun getDurGptResult(
        @Query("drugId1") drugId1: Long,
        @Query("drugId2") drugId2: Long
    ): DurGptResponse
}

interface DurGptRepository {
    suspend fun getDurResult(drugId1: Long, drugId2: Long): DurGptData
}

class DurGptRepositoryImpl(
    private val api: DurGptApi
) : DurGptRepository {
    override suspend fun getDurResult(drugId1: Long, drugId2: Long): DurGptData {
        return api.getDurGptResult(drugId1, drugId2).data
    }
}

/**
 * FCM 토큰
 */
//SearcgRepository.kt
interface FcmApi {
    @POST("/api/alarm/token")
    suspend fun sendFcmToken(
        @Query("token") token: String
    ): FcmTokenResponse
}

interface FcmTokenRepository {
    suspend fun sendToken(token: String): FcmTokenResponse
}

class FcmTokenRepositoryImpl(
    private val api: FcmApi
) : FcmTokenRepository {
    override suspend fun sendToken(token: String): FcmTokenResponse {
        return api.sendFcmToken(token)
    }
}

/**
 * 민감정보 동의
 */
interface PermissionApi {
    @PUT("/api/questionnaire/permissions/multi")
    suspend fun updatePermissions(
        @Body request: PermissionRequest
    ): PermissionResponse

    @GET("/api/questionnaire/permissions")
    suspend fun getPermissions(): PermissionResponse

    @PUT("/api/questionnaire/permissions/{permissionType}")
    suspend fun updateSinglePermission(
        @Path("permissionType") permissionType: String,
        @Query("granted") granted: Boolean
    ): PermissionResponse
}

interface PermissionRepository {
    suspend fun updatePermissions(request: PermissionRequest): PermissionResponse
    suspend fun getPermissions(): PermissionResponse
    suspend fun updateSinglePermission(permissionType: String, granted: Boolean): PermissionResponse
}

class PermissionRepositoryImpl(
    private val api: PermissionApi
) : PermissionRepository {
    override suspend fun updatePermissions(request: PermissionRequest): PermissionResponse {
        return api.updatePermissions(request)
    }

    override suspend fun getPermissions(): PermissionResponse {
        return api.getPermissions()
    }

    override suspend fun updateSinglePermission(
        permissionType: String,
        granted: Boolean
    ): PermissionResponse {
        return api.updateSinglePermission(permissionType, granted)
    }
}

/**
 * 복약 알림 API
 */

interface DosageLogApi {
    @GET("/api/dosageLog/date")
    suspend fun getDailyDosageLog(
        @Query("date") date: String
    ): DailyDosageLogResponse

    @GET("/api/dosageLog/{friendId}/date")
    suspend fun getFriendDosageLog(
        @Path("friendId") friendId: Long,
        @Query("date") date: String
    ): DailyDosageLogResponse

    @POST("/api/dosageLog/{logId}/taken")
    suspend fun toggleDosageTaken(
        @Path("logId") logId: Long
    ): ToggleDosageTakenResponse

    @POST("/api/alarm/{logId}/pending")
    suspend fun getDosageLogMessage(
        @Path("logId") logId: Long
    ): ToggleDosageTakenResponse
}

interface DosageLogRepository {
    suspend fun getDailyDosageLog(date: String): DailyDosageLogResponse
    suspend fun toggleDosageTaken(logId: Long): ToggleDosageTakenResponse
    suspend fun getDosageLogMessage(logId: Long): ToggleDosageTakenResponse
    suspend fun getFriendDosageLog(friendId: Long, date: String): DailyDosageLogData
}

class DosageLogRepositoryImpl(
    private val api: DosageLogApi
) : DosageLogRepository {
    override suspend fun getDailyDosageLog(date: String): DailyDosageLogResponse {
        return api.getDailyDosageLog(date)
    }

    override suspend fun toggleDosageTaken(logId: Long): ToggleDosageTakenResponse {
        return api.toggleDosageTaken(logId)
    }

    override suspend fun getDosageLogMessage(logId: Long): ToggleDosageTakenResponse {
        return api.getDosageLogMessage(logId)
    }

    override suspend fun getFriendDosageLog(friendId: Long, date: String): DailyDosageLogData {
        return api.getFriendDosageLog(friendId, date).data
    }

}

/**
 * 실명/주소 API
 */
interface PersonalInfoApi {
    @PUT("/api/auth/personal-info")
    suspend fun updatePersonalInfo(
        @Body request: PersonalInfoUpdateRequest
    ): PersonalInfoUpdateResponse
}

interface PersonalInfoRepository {
    suspend fun updatePersonalInfo(request: PersonalInfoUpdateRequest): UserProfileData
}

class PersonalInfoRepositoryImpl(
    private val api: PersonalInfoApi
) : PersonalInfoRepository {
    override suspend fun updatePersonalInfo(request: PersonalInfoUpdateRequest): UserProfileData {
        return api.updatePersonalInfo(request).data
    }
}

/**
 * 회원탈퇴 API
 */
interface DeleteAccountAPI {
    @DELETE("/api/auth/delete-account")
    suspend fun deleteAccount(): DeleteAccountResponse
}

interface DeleteRepository {
    suspend fun deleteAccount(): DeleteAccountResponse
}

class DeleteRepositoryImpl(
    private val api: DeleteAccountAPI
) : DeleteRepository {
    override suspend fun deleteAccount(): DeleteAccountResponse {
        return api.deleteAccount()
    }
}

/**
 * 리뷰 통계 API
 */
interface ReviewStatsApi {
    @GET("/api/review/drug/{drugId}/stats")
    suspend fun getReviewStats(
        @Path("drugId") drugId: Long
    ): ReviewStatsResponse
}

interface ReviewStatsRepository {
    suspend fun getReviewStats(drugId: Long): ReviewStatsData
}

class ReviewStatsRepositoryImpl(
    private val api: ReviewStatsApi
) : ReviewStatsRepository {
    override suspend fun getReviewStats(drugId: Long): ReviewStatsData {
        return api.getReviewStats(drugId).data
    }
}

/**
 * 리뷰 데이터 API
 */

interface ReviewApi {
    @GET("/api/review/drug")
    suspend fun getDrugReviews(
        @Query("drugId") drugId: Long,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("sortKey") sortKey: String,
        @Query("direction") direction: String
    ): ReviewListResponse

    @Multipart
    @POST("/api/review/create")
    suspend fun createReview(
        @Part("review") review: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): ReviewCreateResponse


    @DELETE("/api/review/delete/{reviewId}")
    suspend fun deleteReview(
        @Path("reviewId") reviewId: Long
    ): ReviewDeleteResponse

    @POST("/api/review/{reviewId}/like")
    suspend fun likeReview(
        @Path("reviewId") reviewId: Long
    ): ReviewResponse<String>
}

interface ReviewRepository {
    suspend fun getDrugReviews(
        drugId: Long,
        page: Int,
        size: Int,
        sortKey: String,
        direction: String
    ): ReviewListData

    suspend fun createReviewMultipart(
        review: RequestBody,
        images: List<MultipartBody.Part>
    ): Long

    suspend fun deleteReview(reviewId: Long): String
    suspend fun likeReview(reviewId: Long): String

}

class ReviewRepositoryImpl(
    private val api: ReviewApi
) : ReviewRepository {

    override suspend fun getDrugReviews(
        drugId: Long,
        page: Int,
        size: Int,
        sortKey: String,
        direction: String
    ): ReviewListData {
        return api.getDrugReviews(drugId, page, size, sortKey, direction).data
    }

    override suspend fun createReviewMultipart(
        review: RequestBody,
        images: List<MultipartBody.Part>
    ): Long {
        return api.createReview(review, images).data
    }

    override suspend fun deleteReview(reviewId: Long): String {
        return api.deleteReview(reviewId).message ?: "삭제 완료"
    }

    override suspend fun likeReview(reviewId: Long): String {
        return api.likeReview(reviewId).data
    }
}

/**
 * 친구 추가
 */
interface FriendApi {
    @POST("/api/friend/invite")
    suspend fun getInviteUrl(): InviteUrlResponse

    @POST("/api/friend/accept")
    suspend fun acceptInvite(
        @Body request: FriendAcceptRequest
    ): FriendAcceptResponse

    @GET("/api/friend/list")
    suspend fun getFriendList(): FriendListResponse
}

interface FriendRepository {
    suspend fun fetchInviteUrl(): String
    suspend fun acceptFriendInvite(inviteToken: String): String
    suspend fun getFriendList(): List<FriendListDto>
}

class FriendRepositoryImpl(
    private val api: FriendApi
) : FriendRepository {

    override suspend fun fetchInviteUrl(): String {
        return api.getInviteUrl().inviteUrl
    }

    override suspend fun acceptFriendInvite(inviteToken: String): String {
        val response = api.acceptInvite(FriendAcceptRequest(inviteToken))
        if (response.status != "success") {
            throw Exception(response.message ?: "친구 수락 실패")
        }
        return response.data
    }

    override suspend fun getFriendList(): List<FriendListDto> {
        return api.getFriendList().data
    }
}

interface UserProfileApi {
    @PUT("/api/user-profile/pregnant")
    suspend fun updatePregnantStatus(
        @Body request: PregnantUpdateRequest
    ): Response<PregnantUpdateResponse>

    @POST("/api/user-profile/create")
    suspend fun createProfile(
        @Body request: CreateProfileRequest
    ): CreateProfileResponse
}

interface UserProfileRepository {
    suspend fun updatePregnantStatus(pregnant: Boolean): PregnantUpdateResponse
    suspend fun createProfile(request: CreateProfileRequest): CreateProfileResponse
}

class UserProfileRepositoryImpl @Inject constructor(
    private val api: UserProfileApi
) : UserProfileRepository {
    override suspend fun updatePregnantStatus(pregnant: Boolean): PregnantUpdateResponse {
        val response = api.updatePregnantStatus(PregnantUpdateRequest(pregnant))
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("응답이 비어 있습니다")
        } else {
            val errorMsg = response.errorBody()?.string() ?: "알 수 없는 오류"
            throw Exception(errorMsg)
        }
    }

    override suspend fun createProfile(request: CreateProfileRequest): CreateProfileResponse {
        return api.createProfile(request)
    }
}

