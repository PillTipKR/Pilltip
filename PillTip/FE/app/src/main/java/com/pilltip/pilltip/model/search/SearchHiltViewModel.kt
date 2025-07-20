package com.pilltip.pilltip.model.search

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.pilltip.pilltip.model.AuthInterceptor
import com.pilltip.pilltip.model.ProfileIdInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@HiltViewModel
class SearchHiltViewModel @Inject constructor(
    private val repository: AutoCompleteRepository,
    private val drugSearchRepo: DrugSearchRepository,
    private val drugDetailRepo: DrugDetailRepository,
    private val gptAdviceRepo: GptAdviceRepository,
    private val dosageRegisterRepo: DosageRegisterRepository,
    private val dosageSummaryRepo: DosageSummaryRepository,
    private val dosageDetailRepo: DosageDetailRepository,
    private val dosageDeleteRepo: DosageDeleteRepository,
    private val dosageModifyRepo: DosageModifyRepository,
    private val fcmRepo: FcmTokenRepository,
    private val durGptRepo: DurGptRepository,
    private val sensitiveInfoRepo: SensitiveInfoRepository,
    private val dosageLogRepo: DosageLogRepository,
    private val deleteRepo: DeleteRepository,
    private val personalInfoRepo: PersonalInfoRepository,
    private val reviewStatsRepo: ReviewStatsRepository,
    private val questionnaireRepo: QuestionnaireRepository,
    private val friendRepo: FriendRepository
) : ViewModel() {

    /* 약품명 자동 완성 API*/

    private val _autoComplete = MutableStateFlow<List<SearchData>>(emptyList())
    val autoCompleted: StateFlow<List<SearchData>> = _autoComplete.asStateFlow()

    private val _isAutoCompleteLoading = MutableStateFlow(false)
    val isAutoCompleteLoading: StateFlow<Boolean> = _isAutoCompleteLoading.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentPage = 0
    private var currentQuery = ""

    fun fetchAutoComplete(query: String, reset: Boolean = false) {
        if (_isAutoCompleteLoading.value) return
        viewModelScope.launch {
            _isAutoCompleteLoading.value = true
            try {
                if (reset || query != currentQuery) {
                    currentPage = 0
                    currentQuery = query
                    _autoComplete.value = emptyList()
                }
                val newResults = try {
                    repository.getAutoComplete(currentQuery, currentPage)
                } catch (e: Exception) {
                    Log.e("AutoComplete", "자동완성 API 호출 실패: ${e.message}")
                    emptyList()
                }

                if (newResults.isNotEmpty()) {
                    _autoComplete.value += newResults
                    currentPage++
                }
            } finally {
                _isAutoCompleteLoading.value = false
            }
        }
    }

    /* 약품명 일반 검색 API*/

    private val _drugSearchResults = MutableStateFlow<List<DrugSearchResult>>(emptyList())
    val drugSearchResults: StateFlow<List<DrugSearchResult>> = _drugSearchResults.asStateFlow()

    fun fetchDrugSearch(query: String, reset: Boolean = true) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = drugSearchRepo.search(query, page = 0)
                _drugSearchResults.value = results
            } catch (e: Exception) {
                Log.e("DrugSearch", "일반 검색 API 호출 실패: ${e.message}")
                _drugSearchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* 약품 상세 페이지 API */
    private val _drugDetail = MutableStateFlow<DetailDrugData?>(null)
    val drugDetail: StateFlow<DetailDrugData?> = _drugDetail.asStateFlow()

    fun fetchDrugDetail(id: Long) {
        Log.d("DrugDetail", "Fetching detail for drug ID: $id")
        viewModelScope.launch {
            try {
                _isLoading.value = true
//                val detail = drugDetailRepo.getDetail(id)
//                Log.d("DrugDetail", "Fetched drug detail: $detail")
                _drugDetail.value = drugDetailRepo.getDetail(id)
            } catch (e: Exception) {
                Log.e("DrugDetail", "상세정보 API 실패: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* pilltip AI */
    private val _gptAdvice = MutableStateFlow<String?>(null)
    val gptAdvice: StateFlow<String?> = _gptAdvice.asStateFlow()

    fun fetchGptAdvice(detail: DetailDrugData) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val advice = gptAdviceRepo.getGptAdvice(detail)
                _gptAdvice.value = advice
                Log.d("GptAdvice", "GPT 복약 설명: $advice")
            } catch (e: Exception) {
                Log.e("GptAdvice", "GPT 설명 요청 실패: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }


    /* 복약 등록 API */
    private val _registerResult = MutableStateFlow<RegisterDosageResponse?>(null)
    val registerResult: StateFlow<RegisterDosageResponse?> = _registerResult.asStateFlow()

    // 단일 복약 상세 객체 저장
    private val _pillDetail = MutableStateFlow<TakingPillDetailData?>(null)
    val pillDetail: StateFlow<TakingPillDetailData?> = _pillDetail.asStateFlow()

    fun registerDosage(request: RegisterDosageRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = dosageRegisterRepo.registerDosage(request)
                _registerResult.value = response
                Log.d("DosageRegister", "등록 완료된 복약 정보: ${response.data}")
            } catch (e: Exception) {
                Log.e("DosageRegister", "복약 등록 실패: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* 복약 리스트 불러오기 */
    private val _pillSummaryList = MutableStateFlow<List<TakingPillSummary>>(emptyList())
    val pillSummaryList: StateFlow<List<TakingPillSummary>> = _pillSummaryList.asStateFlow()

    fun fetchDosageSummary() {
        viewModelScope.launch {
            try {
                val list = dosageSummaryRepo.getDosageSummary()
                _pillSummaryList.value = list
                Log.d("API Response", _pillSummaryList.value.toString())
            } catch (e: Exception) {
                Log.e("DosageSummary", "불러오기 실패: ${e.message}")
            }
        }
    }

    /* 복약 등록 삭제 */
    fun deletePill(medicationId: Long) {
        viewModelScope.launch {
            try {
                _pillSummaryList.value = dosageDeleteRepo.deleteTakingPill(medicationId)
            } catch (e: Exception) {
                Log.e("DosageDelete", "삭제 실패: ${e.message}")
            }
        }
    }

    /* 복약 세부 데이터 */
    fun fetchTakingPillDetail(
        medicationId: Long,
        onSuccess: ((TakingPillDetailData) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val result = dosageDetailRepo.getDosageDetail(medicationId)
                _pillDetail.value = result
                onSuccess?.invoke(result)
            } catch (e: Exception) {
                _pillDetail.value = null
                Log.e("DosageDetail", "상세 조회 실패: ${e.message}")
            }
        }
    }

    /* 복약 데이터 수정 */
    fun modifyDosage(medicationId: Long, request: RegisterDosageRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedList = dosageModifyRepo.updateDosage(medicationId, request)
                _pillSummaryList.value = updatedList
                Log.d("DosageModify", "수정 완료. 복약 목록 업데이트됨.")
            } catch (e: Exception) {
                Log.e("DosageModify", "수정 실패: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearPillDetail() {
        _pillDetail.value = null
    }

    var pendingDosageRequest by mutableStateOf<RegisterDosageRequest?>(null)

    fun setPendingRequest(request: RegisterDosageRequest) {
        pendingDosageRequest = request
    }

    fun clearPendingRequest() {
        pendingDosageRequest = null
    }

    /* DUR 기능 */
    private val _durGptResult = MutableStateFlow<DurGptData?>(null)
    val durGptResult: StateFlow<DurGptData?> = _durGptResult.asStateFlow()

    private val _isDurGptLoading = MutableStateFlow(false)
    val isDurGptLoading: StateFlow<Boolean> = _isDurGptLoading.asStateFlow()

    fun fetchDurAi(drugId1: Long, drugId2: Long) {
        viewModelScope.launch {
            _isDurGptLoading.value = true
            try {
                val result = durGptRepo.getDurResult(drugId1, drugId2)
                _durGptResult.value = result
                Log.d("DurGpt", "결과: $result")
            } catch (e: Exception) {
                Log.e("DurGpt", "에러: ${e.message}")
                _durGptResult.value = null
            } finally {
                _isDurGptLoading.value = false
            }
        }
    }

    /* FCM 토큰 */
    fun sendFcmToken(token: String) {
        viewModelScope.launch {
            try {
                fcmRepo.sendToken(token)
            } catch (e: Exception) {
                Log.e("FCM", "토큰 전송 실패: ${e.message}")
            }
        }
    }

    /* 건강정보 조회 */
    private val _sensitiveInfo = MutableStateFlow<SensitiveInfoData?>(null)
    val sensitiveInfo: StateFlow<SensitiveInfoData?> = _sensitiveInfo.asStateFlow()

    fun fetchSensitiveInfo() {
        viewModelScope.launch {
            try {
                _sensitiveInfo.value = sensitiveInfoRepo.fetchSensitiveInfo()
            } catch (e: Exception) {
                Log.e("SensitiveInfo", "조회 실패: ${e.message}")
            }
        }
    }

    /* 복약 알림 */
    private val _dailyDosageLog = MutableStateFlow<DailyDosageLogData?>(null)
    val dailyDosageLog: StateFlow<DailyDosageLogData?> = _dailyDosageLog.asStateFlow()
    var selectedDrugLog by mutableStateOf<DosageLogPerDrug?>(null)
    var isFriendView by mutableStateOf(false)
    var targetFriendId: Long? = null
    fun fetchDailyDosageLog(date: LocalDate) {
        selectedDrugLog = null
        viewModelScope.launch {
            try {
                val data = if (isFriendView && targetFriendId != null) {
                    dosageLogRepo.getFriendDosageLog(targetFriendId!!, date.toString())
                } else {
                    dosageLogRepo.getDailyDosageLog(date.toString()).data
                }
                _dailyDosageLog.value = data
            } catch (e: Exception) {
                Log.e("DosageLog", "복약 기록 조회 실패: ${e.message}")
            }
        }
    }


    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        fetchDailyDosageLog(date)
    }

    fun toggleDosageTaken(
        logId: Long,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = dosageLogRepo.toggleDosageTaken(logId)
                if (response.status == "success") {
                    onSuccess(response.data)

                    val latest =
                        dosageLogRepo.getDailyDosageLog(_selectedDate.value.toString()).data
                    _dailyDosageLog.value = latest

                    selectedDrugLog?.let { selected ->
                        val updated =
                            latest.perDrugLogs.find { it.medicationName == selected.medicationName }
                        selectedDrugLog = updated
                    }
                } else {
                    onError(response.message ?: "실패했습니다.")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "에러가 발생했습니다.")
            }
        }
    }

    fun updateSelectedDrugLog(updatedDrug: DosageLogPerDrug?) {
        selectedDrugLog = updatedDrug
    }

    fun fetchDosageLogMessage(
        logId: Long,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = dosageLogRepo.getDosageLogMessage(logId)
                if (response.status == "success") {
                    onSuccess(response.data)
                } else {
                    onError(response.message ?: "서버 응답이 실패했어요.")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "에러가 발생했어요.")
            }
        }
    }

    private val _deleteAccountResult = MutableStateFlow<String?>(null)
    val deleteAccountResult: StateFlow<String?> = _deleteAccountResult

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = deleteRepo.deleteAccount()
                if (response.status == "success") {
                    _deleteAccountResult.value = response.message
                    onSuccess()
                } else {
                    onError(response.message ?: "계정 삭제 실패")
                }
            } catch (e: Exception) {
                onError("에러: ${e.localizedMessage}")
            }
        }
    }

    private val _updatedProfile = MutableStateFlow<UserProfileData?>(null)
    val updatedProfile: StateFlow<UserProfileData?> = _updatedProfile.asStateFlow()

    fun updatePersonalInfo(
        request: PersonalInfoUpdateRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = personalInfoRepo.updatePersonalInfo(request)
                _updatedProfile.value = result
                onSuccess()
            } catch (e: Exception) {
                onError("수정 실패: ${e.localizedMessage}")
            }
        }
    }

    /* 리뷰 통계 API */
    private val _reviewStats = MutableStateFlow<ReviewStatsData?>(null)
    val reviewStats: StateFlow<ReviewStatsData?> = _reviewStats.asStateFlow()

    fun fetchReviewStats(drugId: Long) {
        viewModelScope.launch {
            try {
                _reviewStats.value = reviewStatsRepo.getReviewStats(drugId)
            } catch (e: Exception) {
                Log.e("ReviewStats", "조회 실패: ${e.message}")
                _reviewStats.value = null
            }
        }
    }

    /* 문진표 조회 */
    private val _questionnaireState = mutableStateOf<QuestionnaireData?>(null)
    val questionnaireState: State<QuestionnaireData?> = _questionnaireState

    fun loadQuestionnaire() {
        viewModelScope.launch {
            try {
                val result = questionnaireRepo.getQuestionnaire()
                _questionnaireState.value = result
                _editableQuestionnaire.value = result.copy()
            } catch (e: Exception) {
                Log.e("Questionnaire", "문진표 불러오기 실패", e)
            }
        }
    }

    /* 문진표 수정 */
    private val _editableQuestionnaire = mutableStateOf<QuestionnaireData?>(null)
    val editableQuestionnaire: State<QuestionnaireData?> = _editableQuestionnaire

    fun toggleMedication(index: Int) {
        _editableQuestionnaire.value = _editableQuestionnaire.value?.copy(
            medicationInfo = _editableQuestionnaire.value?.medicationInfo?.mapIndexed { i, item ->
                if (i == index) item.copy(submitted = !item.submitted) else item
            } ?: emptyList()
        )
    }

    fun toggleAllergy(index: Int) {
        _editableQuestionnaire.value = _editableQuestionnaire.value?.copy(
            allergyInfo = _editableQuestionnaire.value?.allergyInfo?.mapIndexed { i, item ->
                if (i == index) item.copy(submitted = !item.submitted) else item
            } ?: emptyList()
        )
    }

    fun toggleChronicDisease(index: Int) {
        _editableQuestionnaire.value = _editableQuestionnaire.value?.copy(
            chronicDiseaseInfo = _editableQuestionnaire.value?.chronicDiseaseInfo?.mapIndexed { i, item ->
                if (i == index) item.copy(submitted = !item.submitted) else item
            } ?: emptyList()
        )
    }

    fun toggleSurgeryHistory(index: Int) {
        _editableQuestionnaire.value = _editableQuestionnaire.value?.copy(
            surgeryHistoryInfo = _editableQuestionnaire.value?.surgeryHistoryInfo?.mapIndexed { i, item ->
                if (i == index) item.copy(submitted = !item.submitted) else item
            } ?: emptyList()
        )
    }

    fun submitEditedQuestionnaire() {
        val edited = _editableQuestionnaire.value ?: return

        val request = QuestionnaireSubmitRequest(
            realName = edited.realName,
            address = edited.address,
            phoneNumber = edited.phoneNumber,
            allergyInfo = edited.allergyInfo,
            medicationInfo = edited.medicationInfo,
            chronicDiseaseInfo = edited.chronicDiseaseInfo,
            surgeryHistoryInfo = edited.surgeryHistoryInfo
        )

        viewModelScope.launch {
            try {
                val response = questionnaireRepo.updateQuestionnaire(request)
                _questionnaireState.value = response
                _editableQuestionnaire.value = response.copy()
                Log.d("문진표 수정", "성공")
            } catch (e: Exception) {
                Log.e("문진표 수정 실패", e.toString())
            }
        }
    }

    /* 친구 추가 링크 생성*/
    private val _inviteUrl = MutableStateFlow<String?>(null)
    val inviteUrl: StateFlow<String?> = _inviteUrl.asStateFlow()

    fun fetchInviteUrl() {
        viewModelScope.launch {
            try {
                val url = friendRepo.fetchInviteUrl()
                _inviteUrl.value = url
            } catch (e: Exception) {
                Log.e("InviteURL", "초대 링크 요청 실패: ${e.message}")
            }
        }
    }

    /* 친구 추가 수락 */
    private val _friendAcceptResult = MutableStateFlow<String?>(null)
    val friendAcceptResult: StateFlow<String?> = _friendAcceptResult.asStateFlow()

    fun acceptFriendInvite(token: String) {
        viewModelScope.launch {
            try {
                val result = friendRepo.acceptFriendInvite(token)
                _friendAcceptResult.value = result
                Log.d("FriendAccept", "친구 수락 완료: $result")
            } catch (e: Exception) {
                Log.e("FriendAccept", "친구 수락 실패: ${e.message}")
            }
        }
    }

    /* 친구 리스트 */
    private val _friendList = MutableStateFlow<List<FriendListDto>>(emptyList())
    val friendList: StateFlow<List<FriendListDto>> = _friendList.asStateFlow()

    fun fetchFriendList() {
        viewModelScope.launch {
            try {
                val list = friendRepo.getFriendList()
                _friendList.value = list
            } catch (e: Exception) {
                Log.e("FriendList", "친구 목록 불러오기 실패: ${e.message}")
            }
        }
    }
}

@HiltViewModel
class SensitiveViewModel @Inject constructor(
    private val permissionRepository: PermissionRepository,
    private val sensitiveInfoRepository: SensitiveInfoRepository,
    private val qrRepository: QrRepository,
    private val pregnantRepository: UserProfileRepository
) : ViewModel() {

    var realName by mutableStateOf("")
    var address by mutableStateOf("")
    var phoneNumber by mutableStateOf("")

    var allergyInfo by mutableStateOf<List<AllergyInfo>>(emptyList())
    var chronicDiseaseInfo by mutableStateOf<List<ChronicDiseaseInfo>>(emptyList())
    var surgeryHistoryInfo by mutableStateOf<List<SurgeryHistoryInfo>>(emptyList())

    var sensitivePermission by mutableStateOf(false)
    var medicalPermission by mutableStateOf(false)

    var permissionState by mutableStateOf<PermissionData?>(null)
    var isPermissionLoading by mutableStateOf(false)

    private val _permissionUpdateResult = MutableStateFlow<PermissionData?>(null)
    val permissionUpdateResult: StateFlow<PermissionData?> = _permissionUpdateResult.asStateFlow()

    fun updateSensitivePermissions() {
        viewModelScope.launch {
            isPermissionLoading = true
            try {
                val request = PermissionRequest(
                    sensitiveInfoPermission = sensitivePermission,
                    medicalInfoPermission = sensitivePermission
                )
                val response = permissionRepository.updatePermissions(request)
                permissionState = response.data
                Log.d("PermissionUpdate", "민감정보 동의 성공: ${response.message}")
            } catch (e: Exception) {
                Log.e("PermissionUpdate", "민감정보 동의 실패: ${e.message}")
            } finally {
                isPermissionLoading = false
            }
        }
    }

    fun updateSinglePermission(permissionType: String, granted: Boolean) {
        viewModelScope.launch {
            try {
                val response = permissionRepository.updateSinglePermission(permissionType, granted)
                _permissionUpdateResult.value = response.data
                permissionState = response.data
                Log.d("Permission", "업데이트 완료: $permissionType = $granted")
            } catch (e: Exception) {
                Log.e("Permission", "업데이트 실패: ${e.message}")
            }
        }
    }

    fun loadPermissions() {
        viewModelScope.launch {
            isPermissionLoading = true
            try {
                val response = permissionRepository.getPermissions()
                permissionState = response.data
                Log.d("PermissionLoad", "현재 권한 상태: ${response.data}")
            } catch (e: Exception) {
                Log.e("PermissionLoad", "권한 불러오기 실패: ${e.message}")
            } finally {
                isPermissionLoading = false
            }
        }
    }

    fun resetAll() {
        realName = ""
        address = ""
        phoneNumber = ""
        allergyInfo = emptyList()
        chronicDiseaseInfo = emptyList()
        surgeryHistoryInfo = emptyList()
    }

    fun resetAllergyInfo() {
        allergyInfo = emptyList()
    }

    fun resetChronicDiseaseInfo() {
        chronicDiseaseInfo = emptyList()
    }

    fun resetSurgeryHistoryInfo() {
        surgeryHistoryInfo = emptyList()
    }

    fun toRequest(): SensitiveSubmitRequest {
        return SensitiveSubmitRequest(
            realName = realName,
            address = address,
            phoneNumber = phoneNumber,
            allergyInfo = allergyInfo.map { it.allergyName },
            chronicDiseaseInfo = chronicDiseaseInfo.map { it.chronicDiseaseName },
            surgeryHistoryInfo = surgeryHistoryInfo.map { it.surgeryHistoryName }
        )
    }

    fun submitSensitiveProfile(onSuccess: () -> Unit = {}, onFailure: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = sensitiveInfoRepository.updateSensitiveProfile(toRequest())
                realName = response.realName
                address = response.address
                phoneNumber = response.phoneNumber
                allergyInfo = response.sensitiveInfo.allergyInfo.map { AllergyInfo(it, true) }
                chronicDiseaseInfo =
                    response.sensitiveInfo.chronicDiseaseInfo.map { ChronicDiseaseInfo(it, true) }
                surgeryHistoryInfo =
                    response.sensitiveInfo.surgeryHistoryInfo.map { SurgeryHistoryInfo(it, true) }

                Log.d("SensitiveSubmit", "업데이트 성공")
                onSuccess()
            } catch (e: Exception) {
                Log.e("SensitiveSubmit", "업데이트 실패: ${e.message}")
                onFailure(e)
            }
        }
    }

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun qrSubmit(path: String, onSuccess: (QrData) -> Unit) {
        viewModelScope.launch {
            try {
                val result = qrRepository.submitQrRequest(path)
                onSuccess(result)
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    fun deleteAllSensitiveInfo(
        onSuccess: (String) -> Unit = {},
        onFailure: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val resultMessage = sensitiveInfoRepository.deleteAllSensitiveInfo()
                Log.d("SensitiveDelete", "삭제 성공: $resultMessage")
                resetAll()
                onSuccess(resultMessage)
            } catch (e: Exception) {
                Log.e("SensitiveDelete", "삭제 실패: ${e.message}")
                onFailure(e)
            }
        }
    }

    private val _pregnant = MutableStateFlow(false)
    val pregnant: StateFlow<Boolean> = _pregnant
    fun initPregnant(pregnantValue: Boolean) {
        _pregnant.value = pregnantValue
    }

    private val _pregnantResult = MutableStateFlow<Result<PregnantUpdateResponse>?>(null)
    val pregnantResult: StateFlow<Result<PregnantUpdateResponse>?> = _pregnantResult

    fun updatePregnantStatus(
        newValue: Boolean,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = pregnantRepository.updatePregnantStatus(newValue)
                if (response.status == "success") {
                    _pregnant.value = response.data?.pregnant ?: false
                    onSuccess()
                } else {
                    onError(Exception(response.message ?: "임신 여부 변경 실패"))
                }
            } catch (e: Exception) {
                Log.e("SensitiveViewModel", "임신 여부 업데이트 실패", e)
                onError(e)
            }
        }
    }
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _reviewListData = MutableStateFlow<ReviewListData?>(null)
    val reviewListData: StateFlow<ReviewListData?> = _reviewListData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentPage = 0
    private val pageSize = 10
    private var currentSortKey = "createdAt"
    private var currentDirection = "DESC"
    private var currentDrugId: Long = -1L

    fun loadReviews(
        drugId: Long,
        reset: Boolean = false,
        sortKey: String = "createdAt",
        direction: String = "DESC"
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                if (reset || drugId != currentDrugId) {
                    currentPage = 0
                    _reviewListData.value = null
                }

                currentDrugId = drugId
                currentSortKey = sortKey
                currentDirection = direction

                val response = reviewRepository.getDrugReviews(
                    drugId = drugId,
                    page = currentPage,
                    size = pageSize,
                    sortKey = sortKey,
                    direction = direction
                )

                val currentData = _reviewListData.value

                val updatedContent = if (currentData == null || reset) {
                    response.content
                } else {
                    currentData.content + response.content
                }

                _reviewListData.value = response.copy(content = updatedContent)
                currentPage++
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshReviews() {
        if (currentDrugId != -1L) {
            loadReviews(
                drugId = currentDrugId,
                reset = true,
                sortKey = currentSortKey,
                direction = currentDirection
            )
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private val _createResult = MutableStateFlow<Long?>(null)
    val createResult: StateFlow<Long?> = _createResult.asStateFlow()

    private val _deleteResult = MutableStateFlow<String?>(null)
    val deleteResult: StateFlow<String?> = _deleteResult.asStateFlow()

    fun uriToFile(uri: Uri, context: Context): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return tempFile
    }


    fun createReview(
        drugId: Long,
        rating: Float,
        content: String,
        tags: ReviewTagRequest,
        imageUris: List<Uri>,
        context: Context,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val json = Gson().toJson(
                    ReviewCreateRequest(drugId, rating, content, tags)
                )
                val reviewBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

                val imageParts = imageUris.mapNotNull { uri ->
                    val file = uriToFile(uri, context)
                    file?.let {
                        val requestFile = it.asRequestBody("image/*".toMediaType())
                        MultipartBody.Part.createFormData("images", it.name, requestFile)
                    }
                }

                val reviewId = reviewRepository.createReviewMultipart(reviewBody, imageParts)
                _createResult.value = reviewId
                refreshReviews()
                onSuccess()
            } catch (e: Exception) {
                Log.e("ReviewCreate", "리뷰 등록 실패: ${e.message}")
                onError(e.localizedMessage ?: "리뷰 등록 실패")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteReview(
        reviewId: Long,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val message = reviewRepository.deleteReview(reviewId)
                _deleteResult.value = message
                refreshReviews()
                onSuccess()
            } catch (e: Exception) {
                Log.e("ReviewDelete", "리뷰 삭제 실패: ${e.message}")
                onError(e.localizedMessage ?: "리뷰 삭제 실패")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun likeReview(reviewId: Long) {
        viewModelScope.launch {
            _reviewListData.update { current ->
                current?.copy(
                    content = current.content.map { review ->
                        if (review.id == reviewId) {
                            val liked = !review.isLiked
                            review.copy(
                                isLiked = liked,
                                likeCount = if (liked) review.likeCount + 1 else review.likeCount - 1
                            )
                        } else review
                    }
                )
            }

            try {
                reviewRepository.likeReview(reviewId)
            } catch (e: Exception) {
                Log.e("LikeReview", "서버 통신 실패: ${e.message}")
            }
        }
    }
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val repository: UserProfileRepository
) : ViewModel() {

    private val _createProfileResult = MutableStateFlow<Result<ProfileData>?>(null)
    val createProfileResult: StateFlow<Result<ProfileData>?> = _createProfileResult


    fun createProfile(
        request: CreateProfileRequest,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.createProfile(request)
                if (response.status == "success" && response.data != null) {
                    _createProfileResult.value = Result.success(response.data)
                    onSuccess()
                } else {
                    onError(Exception(response.message ?: "프로필 생성 실패"))
                }
            } catch (e: Exception) {
                _createProfileResult.value = Result.failure(e)
                onError(e)
            }
        }
    }
}


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(ProfileIdInterceptor(context))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    @Named("SearchRetrofit")
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://pilltip.com:20022")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    fun provideAutoCompleteApi(@Named("SearchRetrofit") retrofit: Retrofit): AutoCompleteApi {
        return retrofit.create(AutoCompleteApi::class.java)
    }

    @Provides
    fun provideAutoCompleteRepository(api: AutoCompleteApi): AutoCompleteRepository {
        return AutoCompleteRepositoryImpl(api)
    }

    @Provides
    fun provideDrugSearchApi(@Named("SearchRetrofit") retrofit: Retrofit): DrugSearchApi {
        return retrofit.create(DrugSearchApi::class.java)
    }

    @Provides
    fun provideDrugSearchRepository(api: DrugSearchApi): DrugSearchRepository {
        return DrugSearchRepositoryImpl(api)
    }

    @Provides
    fun provideDrugDetailApi(@Named("SearchRetrofit") retrofit: Retrofit): DrugDetailApi {
        return retrofit.create(DrugDetailApi::class.java)
    }

    @Provides
    fun provideDrugDetailRepository(api: DrugDetailApi): DrugDetailRepository {
        return DrugDetailRepositoryImpl(api)
    }

    @Provides
    fun provideGptAdviceApi(@Named("SearchRetrofit") retrofit: Retrofit): GptAdviceApi {
        return retrofit.create(GptAdviceApi::class.java)
    }

    @Provides
    fun provideGptAdviceRepository(api: GptAdviceApi): GptAdviceRepository {
        return GptAdviceRepositoryImpl(api)
    }

    @Provides
    fun provideDosageRegisterApi(@Named("SearchRetrofit") retrofit: Retrofit): DosageRegisterApi {
        return retrofit.create(DosageRegisterApi::class.java)
    }

    @Provides
    fun provideDosageRegisterRepository(api: DosageRegisterApi): DosageRegisterRepository {
        return DosageRegisterRepositoryImpl(api)
    }

    @Provides
    fun provideDosageSummaryApi(@Named("SearchRetrofit") retrofit: Retrofit): DosageSummaryApi {
        return retrofit.create(DosageSummaryApi::class.java)
    }

    @Provides
    fun provideDosageSummaryRepository(api: DosageSummaryApi): DosageSummaryRepository {
        return DosageSummaryRepositoryImpl(api)
    }

    @Provides
    fun provideDosageDeleteApi(@Named("SearchRetrofit") retrofit: Retrofit): DosageDeleteApi {
        return retrofit.create(DosageDeleteApi::class.java)
    }

    @Provides
    fun provideDosageDeleteRepository(api: DosageDeleteApi): DosageDeleteRepository {
        return DosageDeleteRepositoryImpl(api)
    }

    @Provides
    fun provideDosageDetailApi(@Named("SearchRetrofit") retrofit: Retrofit): DosageDetailApi =
        retrofit.create(DosageDetailApi::class.java)

    @Provides
    fun provideDosageDetailRepository(api: DosageDetailApi): DosageDetailRepository =
        DosageDetailRepositoryImpl(api)

    @Provides
    fun provideDosageModifyApi(@Named("SearchRetrofit") retrofit: Retrofit): DosageModifyApi {
        return retrofit.create(DosageModifyApi::class.java)
    }

    @Provides
    fun provideDosageModifyRepository(api: DosageModifyApi): DosageModifyRepository {
        return DosageModifyRepositoryImpl(api)
    }

    @Provides
    fun provideFcmApi(@Named("SearchRetrofit") retrofit: Retrofit): FcmApi {
        return retrofit.create(FcmApi::class.java)
    }

    @Provides
    fun provideFcmTokenRepository(api: FcmApi): FcmTokenRepository {
        return FcmTokenRepositoryImpl(api)
    }

    @Provides
    fun providePermissionApi(@Named("SearchRetrofit") retrofit: Retrofit): PermissionApi {
        return retrofit.create(PermissionApi::class.java)
    }

    @Provides
    fun providePermissionRepository(api: PermissionApi): PermissionRepository {
        return PermissionRepositoryImpl(api)
    }

    @Provides
    fun provideDurGptApi(@Named("SearchRetrofit") retrofit: Retrofit): DurGptApi {
        return retrofit.create(DurGptApi::class.java)
    }

    @Provides
    fun provideDurGptRepository(api: DurGptApi): DurGptRepository {
        return DurGptRepositoryImpl(api)
    }

    @Provides
    fun provideSensitiveInfoApi(@Named("SearchRetrofit") retrofit: Retrofit): SensitiveInfoApi {
        return retrofit.create(SensitiveInfoApi::class.java)
    }

    @Provides
    fun provideSensitiveInfoRepository(api: SensitiveInfoApi): SensitiveInfoRepository {
        return SensitiveInfoRepositoryImpl(api)
    }

    @Provides
    fun provideDosageLogApi(@Named("SearchRetrofit") retrofit: Retrofit): DosageLogApi {
        return retrofit.create(DosageLogApi::class.java)
    }

    @Provides
    fun provideDosageLogRepository(api: DosageLogApi): DosageLogRepository {
        return DosageLogRepositoryImpl(api)
    }

    @Provides
    fun providePersonalInfoApi(@Named("SearchRetrofit") retrofit: Retrofit): PersonalInfoApi {
        return retrofit.create(PersonalInfoApi::class.java)
    }

    @Provides
    fun providePersonalInfoRepository(api: PersonalInfoApi): PersonalInfoRepository {
        return PersonalInfoRepositoryImpl(api)
    }

    @Provides
    fun provideDeleteAccountApi(@Named("SearchRetrofit") retrofit: Retrofit): DeleteAccountAPI {
        return retrofit.create(DeleteAccountAPI::class.java)
    }

    @Provides
    fun provideDeleteRepository(api: DeleteAccountAPI): DeleteRepository {
        return DeleteRepositoryImpl(api)
    }

    @Provides
    fun provideReviewStatsApi(@Named("SearchRetrofit") retrofit: Retrofit): ReviewStatsApi {
        return retrofit.create(ReviewStatsApi::class.java)
    }

    @Provides
    fun provideReviewStatsRepository(api: ReviewStatsApi): ReviewStatsRepository {
        return ReviewStatsRepositoryImpl(api)
    }

    @Provides
    fun provideReviewApi(@Named("SearchRetrofit") retrofit: Retrofit): ReviewApi {
        return retrofit.create(ReviewApi::class.java)
    }

    @Provides
    fun provideReviewRepository(api: ReviewApi): ReviewRepository {
        return ReviewRepositoryImpl(api)
    }

    @Provides
    fun provideQuestionnaireApi(@Named("SearchRetrofit") retrofit: Retrofit): QuestionnaireApi {
        return retrofit.create(QuestionnaireApi::class.java)
    }

    @Provides
    fun provideQuestionnaireRepository(api: QuestionnaireApi): QuestionnaireRepository {
        return QuestionnaireRepositoryImpl(api)
    }

    /* 문진표 QR */
    @Provides
    fun provideQrApi(@Named("SearchRetrofit") retrofit: Retrofit): QrApi {
        return retrofit.create(QrApi::class.java)
    }

    @Provides
    fun provideQrRepository(api: QrApi): QrRepository {
        return QrRepositoryImpl(api)
    }

    /* 친구 추가 */
    @Provides
    fun provideFriendApi(@Named("SearchRetrofit") retrofit: Retrofit): FriendApi {
        return retrofit.create(FriendApi::class.java)
    }

    @Provides
    fun provideFriendRepository(api: FriendApi): FriendRepository {
        return FriendRepositoryImpl(api)
    }

    /* 자녀 계정 및 임신 여부 */
    @Provides
    fun provideUserProfileApi(@Named("SearchRetrofit") retrofit: Retrofit): UserProfileApi {
        return retrofit.create(UserProfileApi::class.java)
    }

    @Provides
    fun provideUserProfileRepository(
        api: UserProfileApi
    ): UserProfileRepository {
        return UserProfileRepositoryImpl(api)
    }

}