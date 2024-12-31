package com.example.droidbox.clta

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class FlashcardsFragment : Fragment() {

    private lateinit var sectionContainer: LinearLayout
    private lateinit var addSectionButton: Button
    private lateinit var deleteSectionButton: Button
    private lateinit var shareSectionButton: Button
    private lateinit var inputDataButton: Button
    private lateinit var historyRecyclerView: RecyclerView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userUID: String

    private val historyList = mutableListOf<FlashcardHistory>()
    private lateinit var historyAdapter: FlashcardHistoryAdapter

    private val sectionData = mutableMapOf<String, MutableList<Flashcard>>()
    private var selectedSectionName: String? = null
    private val MAX_SECTIONS_LIMITED_USER = 5
    private var isPremiumUser = false

    private val inputDataLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val flashcards =
                    result.data?.getParcelableArrayListExtra<Flashcard>("flashcards")
                if (selectedSectionName != null && flashcards != null) {
                    val sectionRef = firestore.collection("users")
                        .document(userUID)
                        .collection("sections")
                        .document(selectedSectionName!!)

                    sectionRef.update(
                        "flashcards", flashcards.associate { it.title to mapOf("title" to it.title, "description" to it.description) }
                    ).addOnSuccessListener {
                        Toast.makeText(requireContext(), "Flashcards added to $selectedSectionName!", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to save flashcards!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_flashcards, container, false)

        sectionContainer = view.findViewById(R.id.sectionContainer)
        addSectionButton = view.findViewById(R.id.addSectionButton)
        deleteSectionButton = view.findViewById(R.id.deleteSectionButton)
        shareSectionButton = view.findViewById(R.id.shareSectionButton)
        inputDataButton = view.findViewById(R.id.inputDataButton)
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView)

        historyAdapter = FlashcardHistoryAdapter(historyList)
        historyRecyclerView.adapter = historyAdapter
        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        firestore = FirebaseFirestore.getInstance()
        userUID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        addSectionButton.setOnClickListener { handleAddSection() }
        inputDataButton.setOnClickListener { handleInputData() }
        deleteSectionButton.setOnClickListener { handleDeleteSection() }
        shareSectionButton.setOnClickListener { handleShareSection() }

        listenForSections()

        return view
    }

    private fun listenForSections() {
        firestore.collection("users").document(userUID).collection("sections")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Error listening to sections: $error")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    sectionData.clear()
                    sectionContainer.removeAllViews()

                    for (document in snapshot.documents) {
                        val sectionName = document.getString("name") ?: "Unnamed Section"
                        val flashcards = (document.get("flashcards") as? Map<*, *> ?: emptyMap<Any, Any>())
                            .mapNotNull { (_, value) ->
                                val cardMap = value as? Map<*, *>
                                val title = cardMap?.get("title") as? String
                                val description = cardMap?.get("description") as? String
                                if (title != null && description != null) {
                                    Flashcard(title, description)
                                } else null
                            }

                        sectionData[sectionName] = flashcards.toMutableList()
                        addDynamicSection(sectionName)
                    }
                }
            }
    }

    private fun handleAddSection() {
        if (!isPremiumUser && sectionContainer.childCount >= MAX_SECTIONS_LIMITED_USER) {
            Toast.makeText(
                requireContext(),
                "You can only add up to $MAX_SECTIONS_LIMITED_USER sections. Upgrade to premium for unlimited sections!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.flashcard_dialog_add_section, null)
        val sectionNameInput = dialogView.findViewById<EditText>(R.id.sectionNameInput)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.addSectionButton).setOnClickListener {
            val sectionName = sectionNameInput.text.toString().trim()
            if (sectionName.isNotEmpty()) {
                if (sectionData.containsKey(sectionName)) {
                    Toast.makeText(requireContext(), "Section with this name already exists!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val sectionRef = firestore.collection("users")
                    .document(userUID)
                    .collection("sections")
                    .document(sectionName)

                sectionRef.set(
                    mapOf(
                        "name" to sectionName,
                        "flashcards" to emptyMap<String, Map<String, String>>() // Default flashcards map
                    )
                ).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Section added!", Toast.LENGTH_SHORT).show()

                    // Add to history
                    historyList.add(
                        FlashcardHistory(
                            action = "Added",
                            sectionName = sectionName,
                            dateTime = getCurrentDateTime()
                        )
                    )
                    historyAdapter.notifyItemInserted(historyList.size - 1)

                    dialog.dismiss()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to add section!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Section name cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun handleDeleteSection() {
        if (sectionContainer.childCount == 0) {
            Toast.makeText(requireContext(), "No sections to delete.", Toast.LENGTH_SHORT).show()
            return
        }
        val sectionNames = sectionData.keys.toList()

        AlertDialog.Builder(requireContext())
            .setTitle("Select Section to Delete")
            .setItems(sectionNames.toTypedArray()) { _, which ->
                val sectionName = sectionNames[which]

                showDeleteConfirmationDialog(sectionName, which)
            }
            .create()
            .show()
    }

    private fun showDeleteConfirmationDialog(sectionName: String, sectionIndex: Int) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.flashcard_delete_confirmation, null)
        val sectionSelected = dialogView.findViewById<TextView>(R.id.sectionSelected)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelDeleteAccountButton)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmDeleteAccountButton)

        sectionSelected.text = sectionName

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        confirmButton.setOnClickListener {
            if (sectionIndex >= sectionContainer.childCount) {
                Toast.makeText(requireContext(), "Invalid section index.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sectionRef = firestore.collection("users").document(userUID).collection("sections").document(sectionName)

            sectionRef.delete().addOnSuccessListener {
                // Remove section from local data
                sectionData.remove(sectionName)

                // Refresh UI to sync with updated data
                refreshSections()

                // Add deletion to history
                historyList.add(
                    FlashcardHistory(
                        action = "Deleted",
                        sectionName = sectionName,
                        dateTime = getCurrentDateTime()
                    )
                )
                historyAdapter.notifyItemInserted(historyList.size - 1)

                Toast.makeText(requireContext(), "Section \"$sectionName\" deleted.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete section!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun refreshSections() {
        // Clear the UI
        sectionContainer.removeAllViews()

        // Re-render all sections
        sectionData.keys.forEach { addDynamicSection(it) }
    }


    private fun handleShareSection() {
        if (sectionData.isEmpty()) {
            Toast.makeText(requireContext(), "No sections to share!", Toast.LENGTH_SHORT).show()
            return
        }

        val sectionNames = sectionData.keys.toList()
        AlertDialog.Builder(requireContext())
            .setTitle("Share Section")
            .setItems(sectionNames.toTypedArray()) { _, which ->
                val sectionName = sectionNames[which]
                historyList.add(
                    FlashcardHistory(
                        action = "Shared",
                        sectionName = sectionName,
                        dateTime = getCurrentDateTime()
                    )
                )
                historyAdapter.notifyItemInserted(historyList.size - 1)
                Toast.makeText(requireContext(), "Section \"$sectionName\" shared to public.", Toast.LENGTH_SHORT).show()
            }
            .create()
            .show()
    }

    private fun handleInputData() {
        if (sectionData.isEmpty()) {
            Toast.makeText(requireContext(), "Please add a section first!", Toast.LENGTH_SHORT).show()
        } else {
            showSectionSelectionDialog()
        }
    }

    private fun addDynamicSection(sectionName: String) {
        val sectionView = LayoutInflater.from(requireContext())
            .inflate(R.layout.flashcard_dynamic_layout, sectionContainer, false)
        val sectionTitle = sectionView.findViewById<TextView>(R.id.sectionTitle)
        val startButton = sectionView.findViewById<Button>(R.id.startButton)

        sectionTitle.text = sectionName

        sectionTitle.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.flashcard_edit_section_name, null)
            val editSectionName = dialogView.findViewById<EditText>(R.id.editSectionName)
            val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

            editSectionName.setText(sectionTitle.text)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            saveButton.setOnClickListener {
                val newName = editSectionName.text.toString().trim()
                if (newName.isNotEmpty() && newName != sectionName) {
                    if (sectionData.containsKey(newName)) {
                        Toast.makeText(requireContext(), "Section with this name already exists!", Toast.LENGTH_SHORT).show()
                    } else {
                        val flashcards = sectionData.remove(sectionName)
                        sectionData[newName] = flashcards ?: mutableListOf()
                        refreshSections() // Refresh UI immediately
                        val sectionRef = firestore.collection("users").document(userUID).collection("sections").document(sectionName)
                        sectionRef.update("name", newName)
                            .addOnSuccessListener {
                                historyList.add(
                                    FlashcardHistory(
                                        action = "Renamed",
                                        sectionName = "$sectionName → $newName",
                                        dateTime = getCurrentDateTime()
                                    )
                                )
                                historyAdapter.notifyItemInserted(historyList.size - 1)
                            }.addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to rename section!", Toast.LENGTH_SHORT).show()
                            }
                        dialog.dismiss()
                    }
                } else {
                    Toast.makeText(requireContext(), "Invalid section name!", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()
        }

        startButton.setOnClickListener {
            val flashcards = sectionData[sectionName] ?: mutableListOf()
            if (flashcards.isEmpty()) {
                Toast.makeText(requireContext(), "No flashcards to display!", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(requireContext(), FlashcardViewer::class.java)
                intent.putParcelableArrayListExtra("flashcards", ArrayList(flashcards))
                startActivity(intent)
            }
        }

        sectionContainer.addView(sectionView)
    }

    private fun showSectionSelectionDialog() {
        val sectionNames = sectionData.keys.toList()
        AlertDialog.Builder(requireContext())
            .setTitle("Select Section")
            .setItems(sectionNames.toTypedArray()) { _, which ->
                selectedSectionName = sectionNames[which]
                val intent = Intent(requireContext(), FlashcardInput::class.java)
                intent.putExtra("sectionName", selectedSectionName)
                inputDataLauncher.launch(intent)
            }
            .create()
            .show()
    }

    private fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
}
