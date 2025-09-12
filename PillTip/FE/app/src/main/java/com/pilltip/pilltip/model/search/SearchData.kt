package com.pilltip.pilltip.model.search

import retrofit2.http.GET

/**
 * 약품 자동검색 API
 * http://164.125.253.20:20022/api/autocomplete/drugs?input=타이레놀&page=0
 *
 * */
data class SearchResponse( // JSON 전체 응답을 나타냄
    val data: List<SearchData>
)

data class SearchData( // 리스트 내부의 약품 정보 단위
    val type: String,
    val imageUrl: String?,
    val id: Long,
    val value: String
)

/**
 * 일반 검색 결과 API
 * http://164.125.253.20:20022/api/search/drugs?input=타이레놀&page=0
 *
 * */
data class DrugSearchResponse(
    val status: String,
    val message: String?,
    val data: List<DrugSearchResult>
)

data class DrugSearchResult(
    val id: Long,
    val drugName: String,
    val ingredients: List<Ingredient>,
    val manufacturer: String,
    val imageUrl: String?,
    val durTags: List<DurTag>
)

data class Ingredient(
    val name: String,
    val dose: String,
    val isMain: Boolean
)

/**
 * 약품 상세 페이지
 * [GET] /api/detailPage?id=(약 ID)
 * */

data class DetailDrugResponse(
    val status: String,
    val message: String?,
    val data: DetailDrugData
)

data class DetailDrugData(
    val id: Long,
    val name: String,
    val manufacturer: String,
    val ingredients: List<Ingredient>,
    val form: String,
    val packaging: String,
    val atcCode: String,
    val tag: String,
    val approvalDate: String,
    val imageUrl: String?,
    val container: StorageDetail,
    val temperature: StorageDetail,
    val light: StorageDetail,
    val humid: StorageDetail,
    val effect: EffectDetail,
    val usage: EffectDetail,
    val caution: EffectDetail,
    val durTags: List<DurTag>,
    val count: Int,
    val isTaking: Boolean
)

data class StorageDetail(
    val category: String,
    val value: String,
    val active: Boolean
)

data class EffectDetail(
    val type: String,
    val effect: String
)

data class DurTag(
    val title: String,
    val durDtos: List<DurDto>,
    val isTrue: Boolean
)

data class DurDto(
    val name: String,
    val reason: String,
    val note: String
)

/**
 * 복약 등록
 */
data class RegisterDosageRequest(
    val medicationId: Long,
    val medicationName: String,
    val startDate: String, // yyyy-MM-dd
    val endDate: String,
    val alarmName: String,
    val dosageAmount: Double,
    val daysOfWeek: List<String>,
    val dosageSchedules: List<DosageSchedule>
)

data class DosageSchedule(
    val hour: Int,
    val minute: Int,
    val period: String, // AM or PM
    val alarmOnOff: Boolean,
    val dosageUnit: String
)

data class RegisterDosageResponse(
    val status: String,
    val message: String,
    val data: TakingPillDetailData
)

data class TakingPillDetailData(
    val medicationId: Long,
    val medicationName: String,
    val startDate: String,
    val endDate: String,
    val alarmName: String,
    val daysOfWeek: List<String>,
    val dosageAmount: Double,
    val dosageSchedules: List<DosageScheduleDetail>
)

data class DosageScheduleDetail(
    val hour: Int,
    val minute: Int,
    val period: String,
    val dosageUnit: String,
    val alarmOnOff: Boolean
)

/**
 * 복약 리스트 불러오기
 */

data class TakingPillSummaryResponse(
    val status: String,
    val message: String,
    val data: TakingPillSummaryData
)

data class TakingPillSummaryData(
    val takingPills: List<TakingPillSummary>
)

data class TakingPillSummary(
    val medicationId: Long,
    val medicationName: String,
    val alarmName: String,
    val startDate: String, // yyyy-MM-dd
    val endDate: String,
    val dosageAmount: Double
)

/**
 * 복약 세부 데이터 불러오기
 */

data class TakingPillDetailResponse(
    val status: String,
    val message: String,
    val data: TakingPillDetailData
)

/**
 * Pilltip AI 호출
 */
data class GptAdviceResponse(
    val status: String,
    val message: String?,
    val data: String
)

/**
 * 민감정보 입력
 */

data class SensitiveSubmitRequest(
    val realName: String,
    val address: String,
    val phoneNumber: String,
    val allergyInfo: List<String>,
    val chronicDiseaseInfo: List<String>,
    val surgeryHistoryInfo: List<String>
)

data class MedicationInfo(
    val medicationName: String,
    val submitted: Boolean,
    val medicationId: Long,
)

data class AllergyInfo(
    val allergyName: String,
    val submitted: Boolean
)

data class ChronicDiseaseInfo(
    val chronicDiseaseName: String,
    val submitted: Boolean
)

data class SurgeryHistoryInfo(
    val surgeryHistoryName: String,
    val submitted: Boolean
)

data class SensitiveResponse(
    val status: String,
    val message: String,
    val data: SensitiveResponseData
)

data class SensitiveInfo(
    val allergyInfo: List<String>,
    val chronicDiseaseInfo: List<String>,
    val surgeryHistoryInfo: List<String>
)

data class SensitiveResponseData(
    val realName: String,
    val address: String,
    val phoneNumber: String,
    val sensitiveInfo: SensitiveInfo
)

/**
 * 문진표 조회
 */
data class QuestionnaireResponse(
    val status: String,
    val message: String,
    val data: QuestionnaireData
)

data class QuestionnaireData(
    val questionnaireId: Long?,
    val questionnaireName: String?,
    val realName: String,
    val address: String,
    val phoneNumber: String,
    val gender: String?,
    val birthDate: String?,
    val height: String?,
    val weight: String?,
    val pregnant: String?,
    val issueDate: String,
    val lastModifiedDate: String,
    val notes: String?,
    val medicationInfo: List<MedicationInfo>,
    val allergyInfo: List<AllergyInfo>,
    val chronicDiseaseInfo: List<ChronicDiseaseInfo>,
    val surgeryHistoryInfo: List<SurgeryHistoryInfo>
)

/**
 * 문진표 수정
 */
data class QuestionnaireSubmitRequest(
    val realName: String,
    val address: String,
    val phoneNumber: String,
    val medicationInfo: List<MedicationInfo>,
    val allergyInfo: List<AllergyInfo>,
    val chronicDiseaseInfo: List<ChronicDiseaseInfo>,
    val surgeryHistoryInfo: List<SurgeryHistoryInfo>
)

data class QuestionnaireSubmitResponse(
    val status: String,
    val message: String,
    val data: QuestionnaireData
)

/**
 * 문진표 QR
 */
data class QrResponseWrapper(
    val status: String,
    val message: String,
    val data: QrData? = null
)

data class QrData(
    val questionnaireUrl: String,
    val patientName: String,
    val patientPhone: String,
    val hospitalCode: String,
    val hospitalName: String,
    val questionnaireId: Long,
    val accessToken: String,
    val expiresIn: Int
)


/**
 * DUR 기능
 */

data class DurGptResponse(
    val status: String,
    val message: String?,
    val data: DurGptData
)

data class DurGptData(
    val drugA: String,
    val drugB: String,
    val durA: String,
    val durB: String,
    val interact: String,
    val durTrueA: Boolean,
    val durTrueB: Boolean,
    val durTrueInter: Boolean
)

/**
 * FCM 토큰
 */
data class FcmTokenResponse(
    val status: String,
    val message: String?
)

/**
 * 민감정보 동의
 */
data class PermissionRequest(
    val locationPermission: Boolean? = null,
    val cameraPermission: Boolean? = null,
    val galleryPermission: Boolean? = null,
    val phonePermission: Boolean? = null,
    val smsPermission: Boolean? = null,
    val filePermission: Boolean? = null,
    val sensitiveInfoPermission: Boolean? = null,
    val medicalInfoPermission: Boolean? = null
)

data class PermissionResponse(
    val status: String,
    val message: String,
    val data: PermissionData
)

data class PermissionData(
    val locationPermission: Boolean,
    val cameraPermission: Boolean,
    val galleryPermission: Boolean,
    val phonePermission: Boolean,
    val smsPermission: Boolean,
    val filePermission: Boolean,
    val sensitiveInfoPermission: Boolean,
    val medicalInfoPermission: Boolean
)

data class BaseResponse(
    val status: String,
    val message: String,
    val data: String
)

/**
 * 건강정보 조회
 */
data class SensitiveInfoResponse(
    val status: String,
    val message: String?,
    val data: SensitiveInfoData
)

data class SensitiveInfoData(
    val medicationInfo: List<String>,
    val allergyInfo: List<String>,
    val chronicDiseaseInfo: List<String>,
    val surgeryHistoryInfo: List<String>
)

/**
 * 실명/주소 API
 */
data class PersonalInfoUpdateRequest(
    val realName: String,
    val address: String
)

data class PersonalInfoUpdateResponse(
    val status: String,
    val message: String?,
    val data: UserProfileData
)

data class UserProfileData(
    val id: Long,
    val nickname: String,
    val profilePhoto: String,
    val terms: Boolean,
    val age: Int,
    val gender: String,
    val birthDate: String,
    val phone: String,
    val pregnant: Boolean,
    val height: String,
    val weight: String,
    val realName: String,
    val address: String,
    val permissions: Boolean
)

/**
 * 복약 알람 API
 */
data class DailyDosageLogResponse(
    val status: String,
    val message: String?,
    val data: DailyDosageLogData
)

data class DailyDosageLogData(
    val percent: Int,
    val perDrugLogs: List<DosageLogPerDrug>
)

data class DosageLogPerDrug(
    val percent: Int,
    val medicationName: String,
    val dosageSchedule: List<DosageLogSchedule>
)

data class DosageLogSchedule(
    val logId: Long,
    val scheduledTime: String,
    val isTaken: Boolean,
    val takenAt: String?
)

data class ToggleDosageTakenResponse(
    val status: String,
    val message: String?,
    val data: String
)

/**
 * 회원 탈퇴 API
 */

data class DeleteAccountResponse(
    val status: String,
    val message: String?,
    val data: String? = null
)

/**
 * 리뷰 통계 API
 * */
data class ReviewStatsResponse(
    val status: String,
    val message: String?,
    val data: ReviewStatsData
)

data class ReviewStatsData(
    val total: Int,
    val like: Int,
    val ratingStatsResponse: RatingStats,
    val tagStatsByType: Map<String, TagStats>
)

data class RatingStats(
    val average: Double,
    val ratingCounts: Map<String, Int>
)

data class TagStats(
    val mostUsedTagName: String,
    val mostUsedTagCount: Int,
    val totalTagCount: Int
)

/**
 * 리뷰 데이터 API
 */

data class ReviewListResponse(
    val status: String,
    val message: String?,
    val data: ReviewListData
)

data class ReviewListData(
    val content: List<ReviewItem>,
    val pageable: PageableData,
    val last: Boolean,
    val totalElements: Int,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val sort: SortInfo,
    val first: Boolean,
    val numberOfElements: Int,
    val empty: Boolean
)

data class ReviewItem(
    val id: Long,
    val userNickname: String,
    val gender: String,
    val isMine: Boolean,
    val isLiked: Boolean,
    val rating: Float,
    val likeCount: Int,
    val content: String,
    val imageUrls: List<String>,
    val efficacyTags: List<String>,
    val sideEffectTags: List<String>,
    val otherTags: List<String>,
    val createdAt: String,
    val updatedAt: String
)

data class PageableData(
    val pageNumber: Int,
    val pageSize: Int,
    val sort: SortInfo,
    val offset: Int,
    val paged: Boolean,
    val unpaged: Boolean
)

data class SortInfo(
    val empty: Boolean,
    val sorted: Boolean,
    val unsorted: Boolean
)

/**
 * 리뷰 작성
 */
data class ReviewCreateRequest(
    val drugId: Long,
    val rating: Float,
    val content: String,
    val tags: ReviewTagRequest
)

data class ReviewTagRequest(
    val efficacy: List<String>,
    val side_Effect: List<String>,
    val other: List<String>
)

data class ReviewCreateResponse(
    val status: String,
    val message: String?,
    val data: Long
)

/**
 * 리뷰 삭제
 * */
data class ReviewDeleteResponse(
    val status: String,
    val message: String?,
    val data: String? = null
)


/**
 * 친구추가 링크 보내기
 */
data class InviteUrlResponse(
    val inviteUrl: String
)

/**
 * 친구 추가 수락하기
 */
data class FriendAcceptRequest(
    val inviteToken: String
)

data class FriendAcceptResponse(
    val status: String,
    val message: String?,
    val data: String // ex: "친구 추가가 완료되었습니다."
)

/**
 * 친구 리스트 불러오기
 */
data class FriendListResponse(
    val status: String,
    val message: String?,
    val data: List<FriendListDto>
)

data class FriendListDto(
    val friendId: Long,
    val nickName: String
)


data class ReviewResponse<T>(
    val status: String,
    val message: String?,
    val data: T
)

/**
 * 임신 여부 업데이트
 */
// PregnantUpdateRequest.kt
data class PregnantUpdateRequest(
    val pregnant: Boolean
)

// PregnantUpdateResponse.kt
data class PregnantUpdateResponse(
    val status: String,
    val message: String?,
    val data: PregnantUserData?
)

data class PregnantUserData(
    val age: Int,
    val gender: String,
    val pregnant: Boolean
)

/**
 * 자녀 프로필
 */

data class CreateProfileRequest(
    val nickname: String,
    val gender: String,
    val birthDate: String,
    val age: Int,
    val height: Int,
    val weight: Int
)

// CreateProfileResponse.kt
data class CreateProfileResponse(
    val status: String,
    val message: String?,
    val data: ProfileData?
)

data class ProfileData(
    val id: Long,
    val nickname: String,
    val profilePhoto: String?,
    val terms: Boolean,
    val age: Int,
    val gender: String,
    val birthDate: String,
    val pregnant: Boolean,
    val height: String,
    val weight: String,
    val realName: String?,
    val address: String?,
    val permissions: Boolean
)

