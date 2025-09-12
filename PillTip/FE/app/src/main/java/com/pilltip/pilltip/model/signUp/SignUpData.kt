package com.pilltip.pilltip.model.signUp

data class SignUpData(
    val loginType: LoginType = LoginType.IDPW,
    val loginId: String = "",
    val password: String = "",
    val token: String = "",
    val provider: String = "",
    val nickname: String = "",
    val term: Boolean = false,
    val gender: String = "",
    val birthDate: String = "",
    val age: Int = 0,
    val height: Int = 0,
    val weight: Int = 0,
    val phone: String = "",
    val interest: String = ""
)

data class SignUpRequest(
    val loginType: String,
    val loginId: String?, // token일 시 null
    val password: String?,  // token일 시 null
    val token: String?,  // id/pw일 시 null
    val provider: String?, // id/pw일 시 null
    val nickname: String,
    val gender: String,
    val birthDate: String,
    val age: Int,
    val height: Int,
    val weight: Int,
    val phone: String,
    val interest: String,
)

data class SignUpResponse(
    val status: String,
    val message: String,
    val data: SignUpTokenData
)

data class SignUpTokenData(
    val accessToken: String,
    val refreshToken: String
)

/* ID 로그인 */
data class LoginRequest(
    val loginId: String,
    val password: String
)

/* Social 로그인 */
data class SocialLoginRequest(
    val token: String,
    val provider: String
)

data class LoginResponse(
    val status: String,
    val message: String,
    val data: SignUpTokenData
)

data class TermsRequest(
    val termsOfService: Boolean,
    val privacyPolicy: Boolean,
    val marketingConsent: Boolean
)

/* 로그인 유효성 검증 */
data class AuthMeResponse(
    val status: String,
    val message: String?,
    val data: UserData?
)

data class UserData(
    val id: Long,
    val nickname: String,
    val profilePhoto: String?,
    val terms: Boolean,
    val age: Int,
    val gender: String,
    val birthDate: String,
    val phone: String,
    val pregnant: Boolean,
    val height: String,
    val weight: String,
    val realName: String?,
    val address: String?,
    val permissions: Boolean,
    val userList: List<UserProfile>?
)

data class UserProfile(
    val userId: Long,
    val nickname: String,
    val age : Int,
    val birthDate : String,
    val gender : String,
    val isMain: Boolean,
    val isSelected: Boolean
)

/* 중복 체크 */
data class DuplicateCheckRequest(
    val value: String,
    val type: String
)

data class DuplicateCheckResponse(
    val status: String,
    val message: String,
    val data: Boolean
)