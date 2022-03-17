package com.example.minutos.data.database.converter

import androidx.room.TypeConverter
import com.example.minutos.models.Location
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConverterLocation {

    private val gson = Gson()

    @TypeConverter
    fun stringToObject(data: String?): Location? {
        if (data == null) {
            return null
        }

        val listType = object : TypeToken<Location>() {}.type

        return gson.fromJson<Location>(data, listType)
    }

    @TypeConverter
    fun objectToString(someObjects: Location): String {
        return gson.toJson(someObjects)
    }

}