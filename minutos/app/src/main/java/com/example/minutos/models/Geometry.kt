package com.example.minutos.models

import androidx.room.TypeConverters
import com.example.minutos.data.database.converter.ConverterLocation
import java.io.Serializable

@TypeConverters(ConverterLocation::class)
class Geometry(

    var location: Location?

): Serializable