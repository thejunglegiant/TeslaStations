package com.thejunglegiant.teslastations.presentation.list.models

import androidx.annotation.StringRes
import com.thejunglegiant.teslastations.domain.entity.MapSettingsItem
import com.thejunglegiant.teslastations.domain.entity.StationEntity

sealed class ListViewState {
    object Loading : ListViewState()
    data class Error(val msg: String? = null, @StringRes val msgRes: Int? = null) : ListViewState()
    data class Display(
        val data: List<StationEntity>
    ) : ListViewState()
    data class DisplayMore(
        val data: List<StationEntity>
    ) : ListViewState()
}
