package com.ndhunju.relay.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ndhunju.relay.data.ChildSmsInfo
import com.ndhunju.relay.data.SmsInfo

/**
 * Room generates an instance of [MainDatabase] at compile time
 * @Note: Room keeps this Flow updated for you, which means you only need to explicitly get
 * the data once.
 */
@Database(
    entities = [SmsInfo::class, ChildSmsInfo::class],
    version = 2, // When You change schema of the database table, you've to increase the version
    exportSchema = true // Set to true to keep schema version history backups.
)
abstract class MainDatabase: RoomDatabase() {

    abstract fun smsInfoDao(): SmsInfoDao

    abstract fun childSmsInfoDao(): ChildSmsInfoDao

    companion object {

        /**
         * @Note: The Instance variable keeps a reference to the database, when one has been
         * created. This helps maintain a single instance of the database opened at a given
         * time, which is an expensive resource to create and maintain.
         *
         * The value of a volatile variable is never cached, and all reads and writes are to
         * and from the main memory. These features help ensure the value of Instance is always
         * up to date and is the same for all execution threads. It means that changes made by
         * one thread to Instance are immediately visible to all other threads.
         */
        @Volatile
        private var Instance: MainDatabase? = null

        fun getDatabase(context: Context): MainDatabase {
            /**
             * Multiple threads can potentially ask for a database instance at the same time,
             * which results in two databases instead of one. This issue is known as a race
             * condition. Wrapping the code to get the database inside a synchronized block
             * means that only one thread of execution at a time can enter this block of code,
             * which makes sure the database only gets initialized once.
             */
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    MainDatabase::class.java,
                    "sms_info_database"
                )
                    // For now, during development phase, destroy and rebuild the database if we change
                    // something in the entity class. But once in production, we should pass migration object
                    // See https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929
                    .fallbackToDestructiveMigration()
                    .build()
                    .also {
                        Instance = it
                    }
            }
        }
    }
}