package com.example.minutos.models

import com.google.gson.annotations.Expose
import java.io.Serializable

class Legs(

    @Expose
    var distance: Distance?,

    @Expose
    var duration: Duration?,

    @Expose
    var steps: List<Steps>? = ArrayList()

): Serializable