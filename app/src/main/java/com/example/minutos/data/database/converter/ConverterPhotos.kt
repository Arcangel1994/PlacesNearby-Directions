package com.example.minutos.data.database.converter

import androidx.room.TypeConverter
import com.example.minutos.models.Photos
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class ConverterPhotos {

    private val gson = Gson()

    @TypeConverter
    fun stringToListPhotos(data: String?): List<Photos> {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType = object : TypeToken<List<Photos>>() {}.type

        return gson.fromJson<List<Photos>>(data, listType)
    }

    @TypeConverter
    fun listPhotosToString(someObjects: List<Photos>): String {
        return gson.toJson(someObjects)
    }

}