package com.example.minutos.models

import com.google.gson.annotations.Expose
import java.io.Serializable

class Steps(

    @Expose
    var start_location: Location?,

    @Expose
    var end_location: Location?,

    @Expose
    var polyline: Polyline?,

    @Expose
    var travel_mode: String?

): Serializable