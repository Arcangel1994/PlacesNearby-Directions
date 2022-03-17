package com.example.minutos.ui.listNearbyPlaces

import android.app.Application
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.minutos.MyApplication
import com.example.minutos.data.Repository
import com.example.minutos.models.Results
import com.example.minutos.util.Features
import com.example.minutos.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyPlacesViewModel @Inject constructor(private val repository: Repository, application: Application): AndroidViewModel(application) , LifecycleObserver {

    private var features: Features = Features()

    var places: MutableLiveData<NetworkResult<Results?>> = MutableLiveData()

    fun getPlaces(location: String = "", radius: String = "", type: String = "", key: String = "") = viewModelScope.launch {
        getPlacesSafeCall(location,radius,type, key)
    }

    private suspend fun getPlacesSafeCall(location: String = "", radius: String = "", type: String = "", key: String = "") {
        places.value = NetworkResult.Loading()
        if (features.isConnected(MyApplication.context!!)){
            try {

                val responsePlaces = repository.remote.getPlaceNearby(location,radius,type, key)

                if(responsePlaces.isSuccessful){

                    places.value = NetworkResult.Success(responsePlaces.body())

                }else{

                    places.value = NetworkResult.Error("Error en las credenciales.")

                }

            }catch (e: Exception){

                places.value = NetworkResult.Error("Error en la peticion.")
                Log.i("NearbyPlacesActivity", e.message.toString())
                Log.i("NearbyPlacesActivity", e.localizedMessage.toString())


            }
        }else{

            places.value = NetworkResult.Error("No tienes conexion a internet.")

        }
    }

}