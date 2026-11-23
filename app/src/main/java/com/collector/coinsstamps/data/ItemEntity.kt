package com.collector.coinsstamps.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val category: String, // "Coin" or "Stamp"
    val country: String,
    val year: String,
    val denomination: String,
    val condition: String,
    val estimatedValue: Double,
    val notes: String,
    val photoUri: String?
)
