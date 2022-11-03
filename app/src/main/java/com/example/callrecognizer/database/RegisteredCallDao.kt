package com.example.callrecognizer.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RegisteredCallDao {
    @Query("SELECT * FROM registered_calls")
    fun list(): Flow<List<RegisteredCall>>

    @Query("SELECT * FROM registered_calls WHERE id == :id")
    suspend fun get(id: Int): List<RegisteredCall>

    @Insert
    suspend fun insert(registeredCall: RegisteredCall)

    @Delete
    suspend fun delete(registeredCall: RegisteredCall)

    @Query("DELETE FROM registered_calls")
    suspend fun deleteAll()
}