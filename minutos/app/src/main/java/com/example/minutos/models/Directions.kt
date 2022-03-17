package com.example.minutos.models

import com.google.gson.annotations.Expose
import java.io.Serializable

class Directions(

    @Expose
    var routes: List<Routes>? = ArrayList()

): Serializable