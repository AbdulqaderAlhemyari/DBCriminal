package com.example.dbacriminal

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.DialogFragment


private const val FILE_PATH = "file_path"

class ImageDialog : DialogFragment() {

    private lateinit var imageView: ImageView

    fun newInstance(filePath: String): ImageDialog{
        val args = Bundle()
        args.putString(FILE_PATH, filePath)

        val imageDialog = ImageDialog()
        imageDialog.arguments = args

        return imageDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            val view = inflater.inflate(R.layout.image_dialog, null)
            val imageView = view.findViewById<ImageView>(R.id.image_zoom_dialog)


            val filePath = arguments?.getString(FILE_PATH)
            if(filePath != null) {
                val pictureUtils = PictureUtils()
                val bm: Bitmap = pictureUtils.getScaledBitmap(filePath, activity!!)
                imageView.setImageBitmap(bm)
            }



            builder.setView(view)


            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")


    }

}