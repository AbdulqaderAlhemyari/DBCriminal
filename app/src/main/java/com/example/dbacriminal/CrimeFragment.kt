package com.example.dbacriminal

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.util.*
import androidx.lifecycle.Observer
import java.io.File
import java.text.SimpleDateFormat

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_TIME = 1
private const val REQUEST_CONTACT = 1
private const val DATE_FORMAT = "EEE, MMM, dd"
private const val REQUEST_NUMBER = 1
private const val REQUEST_PHOTO = 2
private const val PHOTO_TAG = 3

class CrimeFragment: Fragment()  ,DatePickerFragment.Callbacks , TimePickerFragment.Callbacks {

    override fun onTimeSelected(time: Date) {
        crime.date = time
        updateUI()
    }

    override fun onDateSelected(date: Date)
    {
        crime.date = date
        updateUI()
    }


    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var confrontSuspectButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()

        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        timeButton = view.findViewById(R.id.crime_Time) as Button
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        confrontSuspectButton = view.findViewById(R.id.call_suspect) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView
//        dateButton.apply {
//            text = crime.date.toString()
//            isEnabled = false
//        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner, Observer { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        photoFile)
                    updateUI()
                }
            })
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {

            }
        }
        titleField.addTextChangedListener(titleWatcher)
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }

        }
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }
        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date ).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
            }
        }
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }

        confrontSuspectButton.setOnClickListener {
            val telNum = Uri.parse("tel: ${crime.suspectNumber}")
            val intent = Intent(Intent.ACTION_DIAL, telNum)
            startActivity(intent)

        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY)
                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

        photoView.setOnClickListener{
            val fm = requireFragmentManager()
            val dialog = ImageDialog().newInstance(photoFile.path)
            dialog.setTargetFragment(this, PHOTO_TAG)
            dialog.show(fm, "file_path")



        }


    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL).format(this.crime.date).toString()
        timeButton.text = SimpleDateFormat("HH:mm").format(crime.date).toString()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }

        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }

        updatePhotoView()

    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {

            val pictureUtils = PictureUtils()
            val bitmap =  pictureUtils.getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
// Specify which fields you want your query to return values for
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME , ContactsContract.Contacts._ID)
// Perform your query - the contactUri is like a "where" clause here
                val cursor = contactUri?.let {
                    requireActivity().contentResolver
                        .query(it, queryFields, null, null, null)
                }
                cursor?.use {
                    // Verify cursor contains at least one result
                    if (it.count == 0) {
                        return
                    }
// Pull out the first column of the first row of data -
// that is your suspect's name
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect

                    val id = it.getString(1)
                    Log.d("ID", "ID = $id")



                    val hasReadContactsPermission = ContextCompat.checkSelfPermission(
                        activity!!,
                        Manifest.permission.READ_CONTACTS
                    )
                    if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_CONTACTS),
                            REQUEST_NUMBER
                        )
                    } else {
                        val phoneCursor = contactUri?.let{
                            requireActivity().contentResolver
                                .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = $id",
                                    null, null)
                        }
                        phoneCursor?.moveToFirst()
                        val number = phoneCursor?.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        Log.d("number", "Number = $number")
                        crime.suspectNumber = number ?: ""
                        crimeDetailViewModel.saveCrime(crime)

                    }
                }
            }

            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report,
            crime.title, dateString, solvedString, suspect)
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }

    }
}