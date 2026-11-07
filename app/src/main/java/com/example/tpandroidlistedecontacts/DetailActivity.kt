package com.example.tpandroidlistedecontacts

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File

class DetailActivity : AppCompatActivity() {

    private lateinit var contact: Contact
    private var position: Int = -1

    private lateinit var photoView: ShapeableImageView
    private lateinit var inputNom: TextInputEditText
    private lateinit var inputPrenom: TextInputEditText
    private lateinit var inputTelephone: TextInputEditText
    private lateinit var inputEmail: TextInputEditText
    private lateinit var layoutNom: TextInputLayout
    private lateinit var layoutPrenom: TextInputLayout
    private lateinit var layoutTelephone: TextInputLayout
    private lateinit var layoutEmail: TextInputLayout

    private var initialPhotoPath: String? = null
    private var photoPath: String? = null
    private var cameraTempFile: File? = null

    /** Galerie : utilise le sélecteur de photos moderne avec repli automatique. */
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) {
            toast(R.string.photo_selection_cancelled)
            return@registerForActivityResult
        }

        val newPath = PhotoStorage.importFromUri(this, uri, contact.id)
        if (newPath == null) {
            toast(R.string.error_load_photo)
            return@registerForActivityResult
        }
        applyNewPhoto(newPath)
    }

    /** Appareil photo : écrit la photo dans un fichier temporaire puis l'importe. */
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val tempFile = cameraTempFile
        cameraTempFile = null

        if (!success || tempFile == null) {
            tempFile?.delete()
            toast(R.string.photo_capture_cancelled)
            return@registerForActivityResult
        }

        val newPath = PhotoStorage.importFromFile(this, tempFile, contact.id)
        tempFile.delete()

        if (newPath == null) {
            toast(R.string.error_load_photo)
            return@registerForActivityResult
        }
        applyNewPhoto(newPath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)

        applySystemBarInsets(findViewById(R.id.root))

        @Suppress("DEPRECATION")
        contact = intent.getSerializableExtra(EXTRA_CONTACT) as? Contact ?: run {
            finish()
            return
        }
        position = intent.getIntExtra(EXTRA_POSITION, -1)

        findViewById<MaterialToolbar>(R.id.toolbarDetail).setNavigationOnClickListener {
            cancelChanges()
        }

        photoView = findViewById(R.id.imageDetailPhoto)
        inputNom = findViewById(R.id.inputNom)
        inputPrenom = findViewById(R.id.inputPrenom)
        inputTelephone = findViewById(R.id.inputTelephone)
        inputEmail = findViewById(R.id.inputEmail)
        layoutNom = findViewById(R.id.layoutNom)
        layoutPrenom = findViewById(R.id.layoutPrenom)
        layoutTelephone = findViewById(R.id.layoutTelephone)
        layoutEmail = findViewById(R.id.layoutEmail)

        initialPhotoPath = contact.photoPath
        photoPath = savedInstanceState?.getString(STATE_PHOTO_PATH) ?: contact.photoPath
        savedInstanceState?.getString(STATE_CAMERA_FILE)?.let { cameraTempFile = File(it) }

        inputNom.setText(contact.nom)
        inputPrenom.setText(contact.prenom)
        inputTelephone.setText(contact.telephone)
        inputEmail.setText(contact.email)

        clearErrorWhileTyping()

        findViewById<MaterialButton>(R.id.buttonGallery).setOnClickListener { chooseFromGallery() }
        findViewById<MaterialButton>(R.id.buttonCamera).setOnClickListener { takePhoto() }
        findViewById<MaterialButton>(R.id.buttonCall).setOnClickListener { callContact() }
        findViewById<MaterialButton>(R.id.buttonEmail).setOnClickListener { sendEmail() }
        findViewById<MaterialButton>(R.id.buttonSave).setOnClickListener { saveContact() }
        findViewById<MaterialButton>(R.id.buttonCancel).setOnClickListener { cancelChanges() }

        // Le bouton Retour du système se comporte comme « Annuler ».
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancelChanges()
            }
        })

        showPhoto()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_PHOTO_PATH, photoPath)
        outState.putString(STATE_CAMERA_FILE, cameraTempFile?.absolutePath)
    }

    private fun chooseFromGallery() {
        try {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        } catch (e: ActivityNotFoundException) {
            toast(R.string.error_no_gallery)
        }
    }

    private fun takePhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) == null) {
            toast(R.string.error_no_camera)
            return
        }

        val tempFile = try {
            PhotoStorage.createCameraTempFile(this)
        } catch (e: Exception) {
            toast(R.string.error_camera_unavailable)
            return
        }

        val uri = try {
            FileProvider.getUriForFile(this, "$packageName.fileprovider", tempFile)
        } catch (e: Exception) {
            tempFile.delete()
            toast(R.string.error_camera_unavailable)
            return
        }

        cameraTempFile = tempFile
        try {
            takePictureLauncher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            cameraTempFile = null
            tempFile.delete()
            toast(R.string.error_no_camera)
        } catch (e: Exception) {
            cameraTempFile = null
            tempFile.delete()
            toast(R.string.error_camera_unavailable)
        }
    }

    private fun applyNewPhoto(newPath: String) {
        val previousPhoto = photoPath
        // On supprime seulement un brouillon pris pendant cette session.
        if (!previousPhoto.isNullOrBlank() && previousPhoto != initialPhotoPath) {
            PhotoStorage.deleteManagedFile(this, previousPhoto)
        }
        photoPath = newPath
        showPhoto()
    }

    private fun showPhoto() {
        val bitmap = ImageUtils.loadBitmap(photoPath, 512)
        if (bitmap != null) {
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageResource(R.drawable.avatar_placeholder)
        }
    }

    private fun callContact() {
        val phone = inputTelephone.text?.toString()?.trim().orEmpty()
        if (phone.isEmpty()) {
            layoutTelephone.error = getString(R.string.error_phone_required)
            inputTelephone.requestFocus()
            toast(R.string.error_phone_required)
            return
        }

        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
        if (intent.resolveActivity(packageManager) == null) {
            toast(R.string.error_no_dialer)
            return
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(R.string.error_no_dialer)
        }
    }

    private fun sendEmail() {
        val email = inputEmail.text?.toString()?.trim().orEmpty()
        if (email.isEmpty()) {
            layoutEmail.error = getString(R.string.error_email_required)
            inputEmail.requestFocus()
            toast(R.string.error_email_required)
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.error = getString(R.string.error_email_invalid)
            inputEmail.requestFocus()
            toast(R.string.error_email_invalid)
            return
        }

        val intent = Intent(Intent.ACTION_SENDTO, "mailto:$email".toUri()).apply {
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject, contact.nomComplet))
        }
        if (intent.resolveActivity(packageManager) == null) {
            toast(R.string.error_no_email_app)
            return
        }
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email_app)))
        } catch (e: ActivityNotFoundException) {
            toast(R.string.error_no_email_app)
        }
    }

    private fun saveContact() {
        val nom = inputNom.text?.toString()?.trim().orEmpty()
        val prenom = inputPrenom.text?.toString()?.trim().orEmpty()
        val telephone = inputTelephone.text?.toString()?.trim().orEmpty()
        val email = inputEmail.text?.toString()?.trim().orEmpty()

        layoutNom.error = null
        layoutPrenom.error = null
        layoutTelephone.error = null
        layoutEmail.error = null

        var isValid = true
        if (nom.isEmpty()) {
            layoutNom.error = getString(R.string.error_required)
            isValid = false
        }
        if (prenom.isEmpty()) {
            layoutPrenom.error = getString(R.string.error_required)
            isValid = false
        }
        if (telephone.isEmpty()) {
            layoutTelephone.error = getString(R.string.error_phone_required)
            isValid = false
        }
        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            layoutEmail.error = getString(R.string.error_email_invalid)
            isValid = false
        }

        if (!isValid) {
            toast(R.string.error_fix_fields)
            return
        }

        if (photoPath != initialPhotoPath) {
            PhotoStorage.deleteManagedFile(this, initialPhotoPath)
        }

        val updatedContact = contact.copy(
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = email,
            photoPath = photoPath
        )

        val result = Intent().apply {
            putExtra(EXTRA_UPDATED_CONTACT, updatedContact)
            putExtra(EXTRA_POSITION, position)
        }
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    private fun cancelChanges() {
        // Une photo choisie mais non enregistrée ne doit pas remplacer l'ancienne.
        if (photoPath != initialPhotoPath) {
            PhotoStorage.deleteManagedFile(this, photoPath)
        }
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun clearErrorWhileTyping() {
        inputNom.doAfterTextChanged { layoutNom.error = null }
        inputPrenom.doAfterTextChanged { layoutPrenom.error = null }
        inputTelephone.doAfterTextChanged { layoutTelephone.error = null }
        inputEmail.doAfterTextChanged { layoutEmail.error = null }
    }

    private fun applySystemBarInsets(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun toast(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_CONTACT = "extra_contact"
        const val EXTRA_POSITION = "extra_position"
        const val EXTRA_UPDATED_CONTACT = "extra_updated_contact"

        private const val STATE_PHOTO_PATH = "state_photo_path"
        private const val STATE_CAMERA_FILE = "state_camera_file"
    }
}
