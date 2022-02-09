package com.thejunglegiant.teslastations.extensions

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

inline fun <reified T> Fragment.getArgsLiveData(key: String) =
    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key)

inline fun <reified T> Fragment.removeArgsLiveData(key: String) =
    findNavController().currentBackStackEntry?.savedStateHandle?.remove<T>(key)

fun Fragment.setArgsLiveData(key: String, result: Any) =
    findNavController().previousBackStackEntry?.savedStateHandle?.set(key, result)

fun Fragment.resetArgsLiveData(key: String) {
    findNavController().currentBackStackEntry?.savedStateHandle?.set(key, null)
    findNavController().previousBackStackEntry?.savedStateHandle?.set(key, null)
}