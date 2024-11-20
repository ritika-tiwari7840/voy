package com.ritika.voy.home

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.ritika.voy.R
import com.ritika.voy.databinding.FragmentCreateAccountBinding
import com.ritika.voy.databinding.FragmentEditInfoBinding

class EditInfo : Fragment() {
    private var _binding: FragmentEditInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        binding.btnBack.setOnClickListener {
            navController.navigate(R.id.action_editInfo_to_profile)
        }
        binding.nameLayout.setOnClickListener {
            if (binding.editGenderPopup.visibility == View.VISIBLE) {
                binding.editGenderPopup.visibility = View.GONE
            }
            slideInPopup(binding.editNamePopup)
        }
        binding.genderLayout.setOnClickListener {
            if (binding.editNamePopup.visibility == View.VISIBLE) {
                binding.editNamePopup.visibility = View.GONE
            }
            slideInPopup(binding.editGenderPopup)
        }
    }

    fun slideInPopup(editPopupLayout: View) {
        if (editPopupLayout.visibility != View.VISIBLE) {
            editPopupLayout.visibility = View.VISIBLE
            val animator = ObjectAnimator.ofFloat(editPopupLayout, "translationY", 1000f, 0f)
            animator.duration = 300
            animator.start()
        }
        binding.root.setOnTouchListener { _, event ->
            outsideClickEvent(editPopupLayout, event)
        }
    }

    fun outsideClickEvent(editPopupLayout: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && editPopupLayout.visibility == View.VISIBLE) {
            val outRect = Rect()
            editPopupLayout.getGlobalVisibleRect(outRect)
            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                val animator =
                    ObjectAnimator.ofFloat(editPopupLayout, "translationY", 0f, 1000f)
                animator.duration = 300
                animator.start()
                animator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) {
                        editPopupLayout.visibility = View.GONE
                    }

                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                })
            }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}