package com.example.data.db

import android.content.Context
import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun fromDostCategory(value: DostCategory): String = value.name

    @TypeConverter
    fun toDostCategory(value: String): DostCategory = enumValueOf(value)

    @TypeConverter
    fun fromTaskType(value: TaskType): String = value.name

    @TypeConverter
    fun toTaskType(value: String): TaskType = enumValueOf(value)

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = enumValueOf(value)

    @TypeConverter
    fun fromMessageSender(value: MessageSender): String = value.name

    @TypeConverter
    fun toMessageSender(value: String): MessageSender = enumValueOf(value)

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(";;;")

    @TypeConverter
    fun toStringList(value: String): List<String> = if (value.isBlank()) emptyList() else value.split(";;;")
}

@Dao
interface DostDao {
    @Query("SELECT * FROM dost_runners ORDER BY rating DESC")
    fun getAllRunners(): Flow<List<DostRunner>>

    @Query("SELECT * FROM dost_runners WHERE id = :id")
    suspend fun getRunnerById(id: String): DostRunner?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRunners(runners: List<DostRunner>)

    @Query("UPDATE dost_runners SET status = :status WHERE id = :id")
    suspend fun updateRunnerStatus(id: String, status: String)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM task_orders ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskOrder>>

    @Query("SELECT * FROM task_orders WHERE status != 'COMPLETED' ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<TaskOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskOrder)

    @Update
    suspend fun updateTask(task: TaskOrder)

    @Query("UPDATE task_orders SET status = :status WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: TaskStatus)

    @Query("UPDATE task_orders SET isDostCamVerified = 1, livePhotoSnippet = :snippet, status = 'VERIFIED_AWAITING_PAY' WHERE id = :id")
    suspend fun updateDostCamVerified(id: String, snippet: String)
}

@Dao
interface MeetupDao {
    @Query("SELECT * FROM community_meetups")
    fun getAllMeetups(): Flow<List<CommunityMeetup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeetups(meetups: List<CommunityMeetup>)

    @Query("UPDATE community_meetups SET isUserRsvpd = :isRsvpd, attendeesCount = attendeesCount + :delta WHERE id = :id")
    suspend fun updateRsvp(id: String, isRsvpd: Boolean, delta: Int)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface DailyDropDao {
    @Query("SELECT * FROM daily_drop_subscriptions")
    fun getAllSubscriptions(): Flow<List<DailyDropSubscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptions(subs: List<DailyDropSubscription>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(sub: DailyDropSubscription)

    @Query("UPDATE daily_drop_subscriptions SET isActive = :isActive WHERE id = :id")
    suspend fun toggleActive(id: String, isActive: Boolean)
}

@Dao
interface RentABroDao {
    @Query("SELECT * FROM rent_a_bro_subscriptions")
    fun getAllSubscriptions(): Flow<List<RentABroSubscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(sub: RentABroSubscription)

    @Query("UPDATE rent_a_bro_subscriptions SET isActive = :isActive WHERE id = :id")
    suspend fun toggleActive(id: String, isActive: Boolean)
}

@Database(
    entities = [
        DostRunner::class,
        TaskOrder::class,
        CommunityMeetup::class,
        ChatMessage::class,
        DailyDropSubscription::class,
        RentABroSubscription::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dostDao(): DostDao
    abstract fun taskDao(): TaskDao
    abstract fun meetupDao(): MeetupDao
    abstract fun chatDao(): ChatDao
    abstract fun dailyDropDao(): DailyDropDao
    abstract fun rentABroDao(): RentABroDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "door_dost_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
