package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    UserEntity::class,
    RoomEntity::class,
    MembershipEntity::class,
    EntryEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class RoomSplitDatabase : RoomDatabase() {
  abstract fun roomSplitDao(): RoomSplitDao

  companion object {
    @Volatile
    private var INSTANCE: RoomSplitDatabase? = null

    fun getDatabase(context: Context): RoomSplitDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          RoomSplitDatabase::class.java,
          "roomsplit_db"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
