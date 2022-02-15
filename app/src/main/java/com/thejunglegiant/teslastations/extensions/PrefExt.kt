package com.thejunglegiant.teslastations.extensions

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private const val PREFERENCES = "prefs"
private const val MAP_MODE_DEFAULT = "MAP_MODE_DEFAULT"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES)

val mapModeDefaultKey: Preferences.Key<Boolean>
    get() = booleanPreferencesKey(MAP_MODE_DEFAULT)