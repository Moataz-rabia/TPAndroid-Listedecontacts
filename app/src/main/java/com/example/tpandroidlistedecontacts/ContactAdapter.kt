package com.example.tpandroidlistedecontacts

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

/**
 * Adapter personnalisé pour la ListView : chaque ligne affiche la photo,
 * le nom complet, le téléphone et l'e-mail du contact.
 */
class ContactAdapter(
    context: Context,
    private val contacts: List<Contact>
) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)
    private val photoCache = LruCache<String, Bitmap>(16)

    override fun getCount(): Int = contacts.size

    override fun getItem(position: Int): Contact = contacts[position]

    override fun getItemId(position: Int): Long = contacts[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.item_contact, parent, false)
        val holder = view.tag as? ViewHolder ?: ViewHolder(view).also { view.tag = it }
        val contact = getItem(position)

        holder.name.text = contact.nomComplet
        holder.phone.text = contact.telephone
        holder.email.text = contact.email.ifBlank { "—" }
        holder.photo.setImageResource(R.drawable.avatar_placeholder)

        val photoPath = contact.photoPath
        if (!photoPath.isNullOrBlank()) {
            val cached = photoCache.get(photoPath)
            if (cached != null) {
                holder.photo.setImageBitmap(cached)
            } else {
                val bitmap = ImageUtils.loadBitmap(photoPath, 120)
                if (bitmap != null) {
                    photoCache.put(photoPath, bitmap)
                    holder.photo.setImageBitmap(bitmap)
                }
            }
        }
        return view
    }

    private class ViewHolder(view: View) {
        val photo: ImageView = view.findViewById(R.id.imageContactPhoto)
        val name: TextView = view.findViewById(R.id.textContactName)
        val phone: TextView = view.findViewById(R.id.textContactPhone)
        val email: TextView = view.findViewById(R.id.textContactEmail)
    }
}
