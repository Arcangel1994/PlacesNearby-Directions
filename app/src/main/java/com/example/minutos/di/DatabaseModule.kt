package com.example.minutos.di

import android.content.Context
import androidx.room.Room
import com.example.minutos.data.database.PlacesNearbyDatabase
import com.example.minutos.util.Constants.Companion.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(context, PlacesNearbyDatabase::class.java, DATABASE_NAME).fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun favoritePlacesNearbyDao(database: PlacesNearbyDatabase) = database.favoritePlacesNearbyDao()

}