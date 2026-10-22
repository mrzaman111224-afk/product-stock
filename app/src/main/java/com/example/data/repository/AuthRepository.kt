package com.example.data.repository

import com.example.data.dao.StoreSettingDao
import com.example.data.dao.UserDao
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthRepository(
    private val userDao: UserDao,
    private val settingDao: StoreSettingDao
) {
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    fun observeAllUsers(): Flow<List<UserAccount>> = userDao.observeAllUsers()

    suspend fun login(email: String, passwordAttempt: String): Result<UserAccount> {
        val user = userDao.getUserByEmail(email.trim())
            ?: return Result.failure(Exception("এই ইমেইল দিয়ে কোনো অ্যাকাউন্ট পাওয়া যায়নি। (Account not found)"))

        if (user.passwordHash != passwordAttempt.trim()) {
            return Result.failure(Exception("ভুল পাসওয়ার্ড। দয়া করে সঠিক পাসওয়ার্ড দিন। (Invalid password)"))
        }

        if (user.status == UserStatus.PENDING) {
            return Result.failure(Exception("আপনার অ্যাকাউন্টটি অনুমোদনের অপেক্ষায় আছে। স্টোর অ্যাডমিনের অনুমতি ছাড়া লগইন করা সম্ভব নয়। (Account pending admin approval)"))
        }

        if (user.status == UserStatus.REJECTED || user.status == UserStatus.SUSPENDED) {
            return Result.failure(Exception("আপনার অ্যাকাউন্টটি অ্যাডমিন কর্তৃক নিষ্ক্রিয়/স্থগিত করা হয়েছে। (Account suspended/deactivated)"))
        }

        val updated = user.copy(lastLogin = System.currentTimeMillis())
        userDao.updateUser(updated)
        _currentUser.value = updated
        return Result.success(updated)
    }

    suspend fun quickLoginAs(email: String): Result<UserAccount> {
        val user = userDao.getUserByEmail(email)
            ?: return Result.failure(Exception("Account not found"))
        _currentUser.value = user
        return Result.success(user)
    }

    suspend fun updatePassword(userId: String, oldPasswordRaw: String, newPasswordRaw: String): Result<Unit> {
        val user = userDao.getUserById(userId)
            ?: return Result.failure(Exception("User not found"))

        if (user.passwordHash != oldPasswordRaw.trim()) {
            return Result.failure(Exception("বর্তমান পাসওয়ার্ডটি সঠিক নয়। (Current password incorrect)"))
        }

        if (newPasswordRaw.trim().length < 4) {
            return Result.failure(Exception("নতুন পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে। (Password too short)"))
        }

        userDao.updateUserPassword(userId, newPasswordRaw.trim())
        refreshIfCurrent(userId)
        return Result.success(Unit)
    }

    suspend fun requestStaffAccount(
        email: String,
        displayName: String,
        passwordRaw: String
    ): Result<UserAccount> {
        val allowRequests = settingDao.getSetting("allow_staff_requests") ?: "true"
        if (allowRequests == "false") {
            return Result.failure(Exception("নতুন কর্মীদের স্ব-নিবন্ধন বর্তমানে অ্যাডমিন কর্তৃক বন্ধ রাখা হয়েছে। স্টোর অ্যাডমিনের সাথে সরাসরি যোগাযোগ করুন। (Registration closed)"))
        }

        val existing = userDao.getUserByEmail(email.trim())
        if (existing != null) {
            return Result.failure(Exception("এই ইমেইল দিয়ে ইতোমধ্যে একটি অ্যাকাউন্ট রয়েছে।"))
        }

        if (passwordRaw.trim().length < 4) {
            return Result.failure(Exception("পাসওয়ার্ড কমপক্ষে ৪ অক্ষরের হতে হবে।"))
        }

        // Strictly creates a PENDING account with role STAFF - cannot access until admin explicitly approves
        val newAccount = UserAccount(
            id = UUID.randomUUID().toString(),
            email = email.trim(),
            displayName = displayName.trim(),
            role = UserRole.STAFF,
            status = UserStatus.PENDING,
            passwordHash = passwordRaw.trim()
        )
        userDao.insertUser(newAccount)
        return Result.success(newAccount)
    }

    suspend fun createStaffDirectly(
        email: String,
        displayName: String,
        passwordRaw: String,
        role: UserRole
    ): Result<UserAccount> {
        val existing = userDao.getUserByEmail(email.trim())
        if (existing != null) {
            return Result.failure(Exception("এই ইমেইল দিয়ে ইতোমধ্যে অ্যাকাউন্ট রয়েছে।"))
        }

        val newAccount = UserAccount(
            id = UUID.randomUUID().toString(),
            email = email.trim(),
            displayName = displayName.trim(),
            role = role,
            status = UserStatus.APPROVED, // Direct creation by Admin is immediately approved
            passwordHash = passwordRaw.trim()
        )
        userDao.insertUser(newAccount)
        return Result.success(newAccount)
    }

    suspend fun approveStaff(userId: String) {
        userDao.updateUserStatus(userId, UserStatus.APPROVED)
        refreshIfCurrent(userId)
    }

    suspend fun rejectStaff(userId: String) {
        userDao.updateUserStatus(userId, UserStatus.REJECTED)
        refreshIfCurrent(userId)
    }

    suspend fun suspendStaff(userId: String) {
        val user = userDao.getUserById(userId)
        if (user?.email == "admin@prantej.com" || user?.id == "admin-primary-001") {
            return // Master admin cannot be suspended
        }
        userDao.updateUserStatus(userId, UserStatus.SUSPENDED)
        refreshIfCurrent(userId)
    }

    suspend fun changeUserRole(userId: String, newRole: UserRole): Result<Unit> {
        val user = userDao.getUserById(userId)
            ?: return Result.failure(Exception("User not found"))

        if (user.email == "admin@prantej.com" || user.id == "admin-primary-001") {
            return Result.failure(Exception("মূল অ্যাডমিন (Primary Store Owner) একাউন্ট স্টাফে রূপান্তর করা যাবে না।"))
        }

        userDao.updateUserRole(userId, newRole)
        refreshIfCurrent(userId)
        return Result.success(Unit)
    }

    suspend fun deleteUser(userId: String): Result<Unit> {
        val user = userDao.getUserById(userId)
            ?: return Result.failure(Exception("User not found"))

        if (user.email == "admin@prantej.com" || user.id == "admin-primary-001") {
            return Result.failure(Exception("মূল অ্যাডমিন (Primary Store Owner) একাউন্ট ডিলিট করা সম্ভব নয়।"))
        }

        userDao.deleteUser(userId)
        return Result.success(Unit)
    }

    private suspend fun refreshIfCurrent(userId: String) {
        if (_currentUser.value?.id == userId) {
            _currentUser.value = userDao.getUserById(userId)
        }
    }

    fun logout() {
        _currentUser.value = null
    }
}
