package com.example.minutos.ui.favoritesPlaces

import android.app.Application
import androidx.lifecycle.*
import com.example.minutos.data.Repository
import com.example.minutos.models.Places
import com.example.minutos.util.Features
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FavoritesPlacesViewModel @Inject constructor(private val repository: Repository, application: Application): AndroidViewModel(application) ,
    LifecycleObserver {

    private var features: Features = Features()

    val favorites: LiveData<List<Places>?> = repository.local.getFavorites().asLiveData()

}