package fr.cpe.pokemon_geo.database.profile

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val PROFILE_TABLE_NAME = "Profile"

@Entity(tableName = PROFILE_TABLE_NAME)
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int? = null,
    @ColumnInfo(name = "pseudo") val pseudo: String,
    @ColumnInfo(name = "experience") val experience: Long = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)