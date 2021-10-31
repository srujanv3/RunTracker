package com.blogspot.svdevs.runtracker.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.blogspot.svdevs.runtracker.db.RunningDatabase
import com.blogspot.svdevs.runtracker.utils.Constants
import com.blogspot.svdevs.runtracker.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.blogspot.svdevs.runtracker.utils.Constants.KEY_NAME
import com.blogspot.svdevs.runtracker.utils.Constants.KEY_WEIGHT
import com.blogspot.svdevs.runtracker.utils.Constants.SHARED_PREFERENCES
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRunningDatabase(@ApplicationContext app:Context) =
        Room.databaseBuilder(
            app,
            RunningDatabase::class.java,
            Constants.RUNNING_DB_NAME
        ).build()

    @Provides
    @Singleton
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCES,MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideName(sharedPrefs: SharedPreferences) = sharedPrefs.getString(KEY_NAME,"")?: ""

    @Provides
    @Singleton
    fun provideWeight(sharedPrefs: SharedPreferences) = sharedPrefs.getFloat(KEY_WEIGHT,80f)

    @Provides
    @Singleton
    fun provideFirstTimeToggle(sharedPrefs: SharedPreferences) = sharedPrefs.getBoolean(
        KEY_FIRST_TIME_TOGGLE,true)

}