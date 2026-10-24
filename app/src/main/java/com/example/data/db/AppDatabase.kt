package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.InventoryLogDao
import com.example.data.dao.ProductDao
import com.example.data.dao.StoreSettingDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserDao
import com.example.data.model.InventoryChangeType
import com.example.data.model.InventoryLog
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.ProductStatus
import com.example.data.model.StoreSetting
import com.example.data.model.TransactionRecord
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserStatus(value: String): UserStatus = UserStatus.valueOf(value)
    @TypeConverter
    fun fromUserStatus(status: UserStatus): String = status.name

    @TypeConverter
    fun toProductStatus(value: String): ProductStatus = ProductStatus.valueOf(value)
    @TypeConverter
    fun fromProductStatus(status: ProductStatus): String = status.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)
    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod): String = method.name

    @TypeConverter
    fun toInventoryChangeType(value: String): InventoryChangeType = InventoryChangeType.valueOf(value)
    @TypeConverter
    fun fromInventoryChangeType(type: InventoryChangeType): String = type.name
}

@Database(
    entities = [
        UserAccount::class,
        Product::class,
        TransactionRecord::class,
        InventoryLog::class,
        StoreSetting::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun inventoryLogDao(): InventoryLogDao
    abstract fun storeSettingDao(): StoreSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moner_upohar_boutique.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    initCleanDatabase(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    ensureCleanState(database)
                }
            }
        }

        private suspend fun initCleanDatabase(database: AppDatabase) {
            val userDao = database.userDao()
            val settingDao = database.storeSettingDao()

            // Master Owner Account for Moner Upohar
            val adminUser = UserAccount(
                id = "admin-primary-001",
                email = "admin@monerupohar.com",
                displayName = "মনের উপহার Admin",
                role = UserRole.ADMIN,
                status = UserStatus.APPROVED,
                passwordHash = "admin123"
            )
            userDao.insertUser(adminUser)

            // Clean Store Settings
            settingDao.setSetting(StoreSetting("store_name", "মনের উপহার (Moner Upohar)"))
            settingDao.setSetting(StoreSetting("store_currency", "BDT (৳)"))
            settingDao.setSetting(StoreSetting("currency_symbol", "৳"))
            settingDao.setSetting(StoreSetting("store_phone", "+880 1700-000000"))
            settingDao.setSetting(StoreSetting("store_address", "মনের উপহার জুয়েলারি ও গিফট শপ, বাংলাদেশ"))
            settingDao.setSetting(StoreSetting("quick_admin_login_enabled", "false"))
            settingDao.setSetting(StoreSetting("allow_staff_requests", "true"))
            settingDao.setSetting(StoreSetting("enforce_single_admin", "true"))
            settingDao.setSetting(StoreSetting("public_share_url", "https://ais-pre-a3omklob3sh7jfdsfqrb6o-760393657966.asia-northeast1.run.app"))
            // Zero products, zero transactions, zero dummy inventory logs
        }

        private suspend fun ensureCleanState(database: AppDatabase) {
            val userDao = database.userDao()
            val settingDao = database.storeSettingDao()

            // Make sure the master admin exists if table is empty
            if (userDao.getUserCount() == 0) {
                val adminUser = UserAccount(
                    id = "admin-primary-001",
                    email = "admin@monerupohar.com",
                    displayName = "মনের উপহার Admin",
                    role = UserRole.ADMIN,
                    status = UserStatus.APPROVED,
                    passwordHash = "admin123"
                )
                userDao.insertUser(adminUser)
            }

            if (settingDao.getSetting("quick_admin_login_enabled") == null) {
                settingDao.setSetting(StoreSetting("quick_admin_login_enabled", "false"))
            }
            if (settingDao.getSetting("allow_staff_requests") == null) {
                settingDao.setSetting(StoreSetting("allow_staff_requests", "true"))
            }
            if (settingDao.getSetting("enforce_single_admin") == null) {
                settingDao.setSetting(StoreSetting("enforce_single_admin", "true"))
            }
            if (settingDao.getSetting("public_share_url") == null) {
                settingDao.setSetting(StoreSetting("public_share_url", "https://ais-pre-a3omklob3sh7jfdsfqrb6o-760393657966.asia-northeast1.run.app"))
            }
        }
    }
}
