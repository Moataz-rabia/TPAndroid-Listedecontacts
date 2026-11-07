package com.example.tpandroidlistedecontacts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    private val contacts = mutableListOf<Contact>()
    private lateinit var adapter: ContactAdapter
    private lateinit var toolbar: MaterialToolbar

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

        val data = result.data ?: return@registerForActivityResult
        @Suppress("DEPRECATION")
        val updated = data.getSerializableExtra(DetailActivity.EXTRA_UPDATED_CONTACT) as? Contact
            ?: return@registerForActivityResult

        val returnedPosition = data.getIntExtra(DetailActivity.EXTRA_POSITION, -1)
        val index = if (returnedPosition in contacts.indices &&
            contacts[returnedPosition].id == updated.id
        ) {
            returnedPosition
        } else {
            contacts.indexOfFirst { it.id == updated.id }
        }

        if (index >= 0) {
            contacts[index] = updated
            adapter.notifyDataSetChanged()
            updateToolbarSubtitle()
            Toast.makeText(this, R.string.contact_updated, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        applySystemBarInsets(findViewById(R.id.root))
        toolbar = findViewById(R.id.toolbar)

        if (savedInstanceState == null) {
            contacts.addAll(sampleContacts())
        } else {
            @Suppress("DEPRECATION", "UNCHECKED_CAST")
            val restored = savedInstanceState.getSerializable(KEY_CONTACTS) as? ArrayList<Contact>
            contacts.addAll(restored ?: sampleContacts())
        }

        adapter = ContactAdapter(this, contacts)
        val listView = findViewById<ListView>(R.id.listContacts)
        listView.emptyView = findViewById<TextView>(R.id.textEmpty)
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ -> openDetail(position) }

        updateToolbarSubtitle()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(KEY_CONTACTS, ArrayList(contacts))
    }

    private fun openDetail(position: Int) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_CONTACT, contacts[position])
            putExtra(DetailActivity.EXTRA_POSITION, position)
        }
        detailLauncher.launch(intent)
    }

    private fun updateToolbarSubtitle() {
        toolbar.subtitle = resources.getQuantityString(
            R.plurals.contact_count,
            contacts.size,
            contacts.size
        )
    }

    private fun applySystemBarInsets(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun sampleContacts(): List<Contact> = listOf(
        Contact(
            id = 1,
            nom = "Martin",
            prenom = "Alice",
            telephone = "06 12 34 56 78",
            email = "alice.martin@example.com"
        ),
        Contact(
            id = 2,
            nom = "Bernard",
            prenom = "Lucas",
            telephone = "06 98 76 54 32",
            email = "lucas.bernard@example.com"
        ),
        Contact(
            id = 3,
            nom = "Dubois",
            prenom = "Chloé",
            telephone = "07 11 22 33 44",
            email = "chloe.dubois@example.com"
        ),
        Contact(
            id = 4,
            nom = "Petit",
            prenom = "Nicolas",
            telephone = "06 55 44 33 22",
            email = "nicolas.petit@example.com"
        ),
        Contact(
            id = 5,
            nom = "Robert",
            prenom = "Emma",
            telephone = "07 66 77 88 99",
            email = "emma.robert@example.com"
        )
    )

    companion object {
        private const val KEY_CONTACTS = "key_contacts"
    }
}
