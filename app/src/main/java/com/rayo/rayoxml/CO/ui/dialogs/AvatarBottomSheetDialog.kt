package com.rayo.rayoxml.co.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.rayo.rayoxml.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class AvatarBottomSheetDialog (private val currentSelection: Int? = null, private val onImageSelected: (Int) -> Unit): BottomSheetDialogFragment() {
    private var currentSelectedAvatar: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_avatar_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias a los avatares en el BottomSheetDialog
        val avatar0: ImageView = view.findViewById(R.id.avatar0)
        val avatar1: ImageView = view.findViewById(R.id.avatar1)
        val avatar2: ImageView = view.findViewById(R.id.avatar2)
        val avatar3: ImageView = view.findViewById(R.id.avatar3)
        val avatar4: ImageView = view.findViewById(R.id.avatar4)
        val avatar5: ImageView = view.findViewById(R.id.avatar5)
        val avatar6: ImageView = view.findViewById(R.id.avatar6)
        val avatar7: ImageView = view.findViewById(R.id.avatar7)
        val avatar8: ImageView = view.findViewById(R.id.avatar8)
        val avatar9: ImageView = view.findViewById(R.id.avatar9)
        val avatar10: ImageView = view.findViewById(R.id.avatar10)
        val avatar11: ImageView = view.findViewById(R.id.avatar11)
        val avatar12: ImageView = view.findViewById(R.id.avatar12)
        val avatar13: ImageView = view.findViewById(R.id.avatar13)

        // Asigna eventos de clic para cambiar la imagen
        avatar0.setOnClickListener { onImageSelected(R.drawable.avatar_00) }
        avatar1.setOnClickListener { onImageSelected(R.drawable.avatar_01) }
        avatar2.setOnClickListener { onImageSelected(R.drawable.avatar_02) }
        avatar3.setOnClickListener { onImageSelected(R.drawable.avatar_03) }
        avatar4.setOnClickListener { onImageSelected(R.drawable.avatar_04) }
        avatar5.setOnClickListener { onImageSelected(R.drawable.avatar_05) }
        avatar6.setOnClickListener { onImageSelected(R.drawable.avatar_06) }
        avatar7.setOnClickListener { onImageSelected(R.drawable.avatar_07) }
        avatar8.setOnClickListener { onImageSelected(R.drawable.avatar_08) }
        avatar9.setOnClickListener { onImageSelected(R.drawable.avatar_09) }
        avatar10.setOnClickListener { onImageSelected(R.drawable.avatar_10) }
        avatar11.setOnClickListener { onImageSelected(R.drawable.avatar_11) }
        avatar12.setOnClickListener { onImageSelected(R.drawable.avatar_12) }
        avatar13.setOnClickListener { onImageSelected(R.drawable.avatar_13) }

        val avatarMap = mapOf(
            R.id.avatar0 to R.drawable.avatar_00,
            R.id.avatar1 to R.drawable.avatar_01,
            R.id.avatar2 to R.drawable.avatar_02,
            R.id.avatar3 to R.drawable.avatar_03,
            R.id.avatar4 to R.drawable.avatar_04,
            R.id.avatar5 to R.drawable.avatar_05,
            R.id.avatar6 to R.drawable.avatar_06,
            R.id.avatar7 to R.drawable.avatar_07,
            R.id.avatar8 to R.drawable.avatar_08,
            R.id.avatar9 to R.drawable.avatar_09,
            R.id.avatar10 to R.drawable.avatar_10,
            R.id.avatar11 to R.drawable.avatar_11,
            R.id.avatar12 to R.drawable.avatar_12,
            R.id.avatar13 to R.drawable.avatar_13
        )

        // Configura los listeners
        avatarMap.forEach { (viewId, drawableId) ->
            val avatar = view.findViewById<ImageView>(viewId)

            // Marcar la selección actual si coincide
            if (currentSelection == drawableId) {
                avatar.setBackgroundResource(R.drawable.avatar_selected_border)
                currentSelectedAvatar = avatar
            }

            avatar.setOnClickListener {
                currentSelectedAvatar?.background = null
                avatar.setBackgroundResource(R.drawable.avatar_selected_border)
                currentSelectedAvatar = avatar
                onImageSelected(drawableId)
            }
        }
    }
}