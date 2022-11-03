package com.example.callrecognizer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.Duration
import kotlin.math.abs

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED
}

@Entity(tableName = "registered_calls")
@TypeConverters(Converters.CallTypeConverter::class, Converters.DurationConverter::class)
data class RegisteredCall(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "source") val source: String,
    @ColumnInfo(name = "call_type") val callType: CallType,
    @ColumnInfo(name = "duration") val duration: Duration,
)

fun callFromId(id: Int): RegisteredCall {
    return RegisteredCall(
        id,
        "+55 31 99373-4365",
        CallType.values()[abs(id) % CallType.values().size],
        Duration.ofMinutes(5).plus(Duration.ofSeconds(23))
    )
}

fun dummyCalls(size: Int): List<RegisteredCall> {
    return (0 until size).map { callFromId(it) }
}