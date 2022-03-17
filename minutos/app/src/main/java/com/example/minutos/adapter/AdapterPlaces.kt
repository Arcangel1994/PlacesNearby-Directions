package com.example.minutos.adapter

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.minutos.MyApplication
import com.example.minutos.R
import com.example.minutos.models.Places
import com.example.minutos.util.Features
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

class AdapterPlaces(private var places: MutableList<Places>): RecyclerView.Adapter<AdapterPlaces.ViewHolder>() {

    //Listener
    lateinit var placesClicklistener: PlacesClicklistener

    //Location
    private var location: Location? = null

    //Features
    private val features by lazy { Features() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_places_nearby, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return places.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.mItem = places[position]

        Glide.with(MyApplication.context!!)
            .load(holder.mItem?.icon)
            .error(R.mipmap.ic_launcher)
            .centerInside()
            .placeholder(R.mipmap.ic_launcher)
            .into(holder.shapeImageIcon!!)

        holder.materialTextName!!.text = holder.mItem?.name

        if(location != null){
            holder.materialTextKm!!.text = "%.2f".format(features.distFrom(location!!.latitude.toFloat(), location!!.longitude.toFloat(),
                holder.mItem!!.geometry!!.location!!.lat!!.toFloat(), holder.mItem!!.geometry!!.location!!.lng!!.toFloat()))+"m"
        }else{
            holder.materialTextKm!!.text = "unk"
        }

        if(holder.mItem!!.rating != null){
            holder.ratingBarRating!!.rating = holder.mItem!!.rating!!
        }

        holder.itemView.setOnClickListener{
            placesClicklistener.onPlacesClick(holder.mItem!!, position, holder.itemView)
        }

    }

    fun setData(setPlaces: MutableList<Places>, setLocation: Location? = null){
        location = setLocation
        places = setPlaces
        notifyDataSetChanged()
    }

    inner class ViewHolder(mView: View): RecyclerView.ViewHolder(mView){

        var shapeImageIcon: ShapeableImageView? = null
        var materialTextName: MaterialTextView? = null
        var materialTextKm: MaterialTextView? = null
        var ratingBarRating: AppCompatRatingBar? = null

        var mItem: Places? = null

        init {
            shapeImageIcon = mView.findViewById(R.id.shapeImageIcon)
            materialTextName = mView.findViewById(R.id.materialTextName)
            materialTextKm = mView.findViewById(R.id.materialTextKm)
            ratingBarRating = mView.findViewById(R.id.ratingBarRating)
        }

    }

    interface PlacesClicklistener{
        fun onPlacesClick(place: Places, position: Int, view: View)
    }

}