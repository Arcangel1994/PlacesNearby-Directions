package com.example.minutos.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.minutos.data.database.converter.ConverterPhotos
import com.google.gson.annotations.Expose
import java.io.Serializable

@Entity(tableName = "Places")
@TypeConverters(ConverterPhotos::class)
class Places(

    @Expose
    var place_id: String?,

    @Expose
    var name: String?,

    @Expose
    var rating: Float?,

    @Expose
    var user_ratings_total: Int?,

    @Expose
    var icon: String?,

    @Expose
    var vicinity: String?,

    @Expose
    var types: List<String>? = ArrayList(),

    @Expose
    var photos: List<Photos>? = ArrayList(),

    @Expose
    @Embedded
    var geometry: Geometry?

): Serializable {

    @PrimaryKey(autoGenerate = true)
    var favorite: Int = 0

}