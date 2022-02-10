package com.thejunglegiant.teslastations.presentation.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.thejunglegiant.teslastations.R

abstract class BaseBindingBottomDialog<T : ViewBinding>(
    private val bindingInflater: (inflater: LayoutInflater) -> T
) : BottomSheetDialogFragment() {

    private var _binding: T? = null
    val binding: T
        get() = _binding as T

    /**
     * @property bottomSheetBehavior using for setting dialog programmatically state: hidden,
     * collapsed, expanded etc.
     */
    protected val bottomSheetBehavior by lazy {
        val bottomSheet = (dialog as BottomSheetDialog)
            .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout

        BottomSheetBehavior.from(bottomSheet)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflater.invoke(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}