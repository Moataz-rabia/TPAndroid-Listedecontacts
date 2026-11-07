package com.example.tpandroidlistedecontacts

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Gère les photos des contacts dans le stockage interne de l'application.
 * Copier la photo évite de dépendre d'une autorisation temporaire sur l'URI
 * renvoyée par la galerie.
 */
object PhotoStorage {

    private const val CONTACTS_DIR = "contacts"
    private const val CAMERA_DIR = "camera"

    fun importFromUri(context: Context, uri: Uri, contactId: Long): String? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            input.use { source ->
                val target = newPhotoFile(context, contactId)
                target.outputStream().use { destination -> source.copyTo(destination) }
                target.absolutePath
            }
        } catch (e: Exception) {
            null
        }
    }

    fun importFromFile(context: Context, source: File, contactId: Long): String? {
        return try {
            if (!source.exists() || source.length() == 0L) return null
            val target = newPhotoFile(context, contactId)
            source.inputStream().use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            target.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun createCameraTempFile(context: Context): File {
        val directory = File(context.cacheDir, CAMERA_DIR)
        if (!directory.exists()) directory.mkdirs()
        return File(directory, "camera_${System.currentTimeMillis()}.jpg").apply { createNewFile() }
    }

    /** Supprime une ancienne photo uniquement si elle appartient à l'application. */
    fun deleteManagedFile(context: Context, path: String?) {
        if (path.isNullOrBlank()) return
        try {
            val file = File(path)
            val contactsDirectory = File(context.filesDir, CONTACTS_DIR).canonicalPath + File.separator
            if (file.canonicalPath.startsWith(contactsDirectory)) file.delete()
        } catch (e: Exception) {
            // Rien à faire : le fichier sera simplement ignoré.
        }
    }

    private fun newPhotoFile(context: Context, contactId: Long): File {
        val directory = File(context.filesDir, CONTACTS_DIR)
        if (!directory.exists()) directory.mkdirs()
        return File(directory, "contact_${contactId}_${System.currentTimeMillis()}.jpg")
    }
}
