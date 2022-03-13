package com.thejunglegiant.teslastations.extensions

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

fun <T : View> BottomSheetBehavior<T>.setBottomSheetState(state: Int) {
    this.state = state
}

fun <T : View> BottomSheetBehavior<T>.hide() {
    this.state = BottomSheetBehavior.STATE_HIDDEN
}

fun <T : View> BottomSheetBehavior<T>.halfExpand() {
    this.state = BottomSheetBehavior.STATE_HALF_EXPANDED
}

fun <T : View> BottomSheetBehavior<T>.expand() {
    this.state = BottomSheetBehavior.STATE_EXPANDED
}

fun <T : View> BottomSheetBehavior<T>.collapse() {
    this.state = BottomSheetBehavior.STATE_COLLAPSED
}

val <T : View> BottomSheetBehavior<T>.isCollapsed: Boolean
    get() = this.state == BottomSheetBehavior.STATE_COLLAPSED

val <T : View> BottomSheetBehavior<T>.isHidden: Boolean
    get() = this.state == BottomSheetBehavior.STATE_HIDDEN

val <T : View> BottomSheetBehavior<T>.isExpanded: Boolean
    get() = this.state == BottomSheetBehavior.STATE_EXPANDED
