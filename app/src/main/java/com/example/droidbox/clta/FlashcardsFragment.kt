package com.example.droidbox.clta

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.google.firebase.Timestamp

import java.util.*

class FlashcardsFragment : Fragment() {

    // UI Elements
    private lateinit var sectionContainer: LinearLayout
    private lateinit var addSectionButton: Button
    private lateinit var deleteSectionButton: Button
    private lateinit var shareSectionButton: Button
    private lateinit var inputDataButton: Button
    private lateinit var historyRecyclerView: RecyclerView

    // Firebase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userUID: String

    // History and Sections Data
    private val historyList = mutableListOf<FlashcardHistory>()
    private lateinit var historyAdapter: FlashcardHistoryAdapter
    private val sectionData = mutableMapOf<String, MutableList<Flashcard>>()
    private var selectedSectionName: String? = null

    // Constants
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

        // Initialize UI
        sectionContainer = view.findViewById(R.id.sectionContainer)
        addSectionButton = view.findViewById(R.id.addSectionButton)
        deleteSectionButton = view.findViewById(R.id.deleteSectionButton)
        shareSectionButton = view.findViewById(R.id.shareSectionButton)
        inputDataButton = view.findViewById(R.id.inputDataButton)
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView)

        // Initialize Adapter
        historyAdapter = FlashcardHistoryAdapter(historyList)
        historyRecyclerView.adapter = historyAdapter
        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Firebase Setup
        firestore = FirebaseFirestore.getInstance()
        userUID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Set Button Listeners
        addSectionButton.setOnClickListener { handleAddSection() }
        inputDataButton.setOnClickListener { handleInputData() }
        deleteSectionButton.setOnClickListener { handleDeleteSection() }
        shareSectionButton.setOnClickListener { handleShareSection() }

        listenForSections()
        fetchHistoryFromFirestore() // Fetch history data on initialization
        setupRecyclerViewLongPress()


        return view
    }

    private fun clearHistoryInFirestore(onComplete: (Boolean) -> Unit) {
        firestore.collection("users")
            .document(userUID)
            .collection("history")
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit()
                    .addOnSuccessListener {
                        historyList.clear()
                        historyAdapter.notifyDataSetChanged()
                        Log.d("Firestore", "History cleared successfully.")
                        onComplete(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Failed to clear history: ${e.message}")
                        onComplete(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch history for clearing: ${e.message}")
                onComplete(false)
            }
    }


    private fun fetchHistoryFromFirestore() {
        firestore.collection("users")
            .document(userUID)
            .collection("history")
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("Firestore", "Fetched history snapshot: ${snapshot.documents}")
                if (!snapshot.isEmpty) {
                    val retrievedHistory = snapshot.documents.mapNotNull { document ->
                        val action = document.getString("action") ?: return@mapNotNull null
                        val sectionName = document.getString("sectionName") ?: return@mapNotNull null
                        val previousName = document.getString("previousName")
                        val dateTime = document.getTimestamp("dateTime") ?: return@mapNotNull null
                        val detailsMap = document.get("details") as? Map<*, *>
                        val shared = detailsMap?.get("shared") as? Boolean ?: false
                        val downloaded = detailsMap?.get("downloaded") as? Boolean ?: false

                        FlashcardHistory(
                            action = action,
                            sectionName = sectionName,
                            previousName = previousName,
                            dateTime = dateTime,
                            details = Details(shared = shared, downloaded = downloaded)
                        )
                    }

                    historyList.clear()
                    historyList.addAll(retrievedHistory)
                    historyAdapter.notifyDataSetChanged()

                    Log.d("Firestore", "History list after fetch: $historyList")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to fetch history: ${e.message}")
                Toast.makeText(requireContext(), "Failed to fetch history data.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setupRecyclerViewLongPress() {
        val gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                val child = historyRecyclerView.findChildViewUnder(e.x, e.y)
                if (child != null) {
                    showClearHistoryDialog()
                }
            }
        })

        historyRecyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(e)
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    private fun showClearHistoryDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.clear_flashcard_history, null)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelDeleteHistoryButton)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmDeleteHistoryButton)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener { dialog.dismiss() }

        confirmButton.setOnClickListener {
            clearHistoryInFirestore { success ->
                if (success) {
                    Toast.makeText(requireContext(), "History cleared successfully.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to clear history.", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }

        dialog.show()
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
                        addDynamicSection(sectionName) // Redraw the section dynamically
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

                    // Add to history in Firestore
                    val historyRef = firestore.collection("users")
                        .document(userUID)
                        .collection("history")
                        .document()

                    val historyItem = FlashcardHistory(
                        action = "Added",
                        sectionName = sectionName,
                        dateTime = getCurrentDateTime()
                    )

                    historyRef.set(historyItem)
                        .addOnSuccessListener {
                            // Add to local historyList
                            historyList.add(historyItem)
                            historyAdapter.notifyItemInserted(historyList.size - 1)
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to add history to Firestore.", Toast.LENGTH_SHORT).show()
                        }
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

            val sectionRef = firestore.collection("users")
                .document(userUID)
                .collection("sections")
                .document(sectionName)

            sectionRef.delete().addOnSuccessListener {
                // Remove section from local data
                sectionData.remove(sectionName)

                // Refresh UI to sync with updated data
                refreshSections()

                // Add deletion to Firestore history
                val historyRef = firestore.collection("users")
                    .document(userUID)
                    .collection("history")
                    .document()

                val historyItem = FlashcardHistory(
                    action = "Deleted",
                    sectionName = sectionName,
                    dateTime = getCurrentDateTime()
                )

                historyRef.set(historyItem)
                    .addOnSuccessListener {
                        // Add to local historyList
                        historyList.add(historyItem)
                        historyAdapter.notifyItemInserted(historyList.size - 1)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to add deletion to history.", Toast.LENGTH_SHORT).show()
                    }

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

                // Show dialog to enter description
                showDescriptionDialog(sectionName)
            }
            .create()
            .show()
    }

    private fun showDescriptionDialog(sectionName: String) {
        // Inflate the custom layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_share_section, null)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.descriptionInput)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelShareButton)
        val shareButton = dialogView.findViewById<Button>(R.id.confirmShareButton)

        // Create the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Set up the Cancel button
        cancelButton.setOnClickListener {
            dialog.dismiss() // Close the dialog
        }

        // Set up the Share button
        shareButton.setOnClickListener {
            val description = descriptionInput.text.toString().trim()

            if (description.isEmpty()) {
                Toast.makeText(requireContext(), "Description cannot be empty!", Toast.LENGTH_SHORT).show()
            } else {
                // Proceed with sharing
                dialog.dismiss() // Close the dialog before sharing
                shareSection(sectionName, description)
            }
        }

        // Show the dialog
        dialog.show()
    }

    private fun shareSection(sectionName: String, description: String) {
        val sharedContentRef = firestore.collection("shared_content").document()

        val sharedContent = mapOf(
            "title" to sectionName,
            "description" to description,
            "dateTime" to getCurrentDateTime(),
            "sharedBy" to userUID,
            "type" to "Flashcard" // Add type field
        )

        sharedContentRef.set(sharedContent)
            .addOnSuccessListener {
                // Add notification
                addNotificationForSharedSection(userUID, sectionName, description, "Flashcard")

                Toast.makeText(requireContext(), "Section shared successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to share section: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addNotificationForSharedSection(userId: String, sectionName: String, message: String, type: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val ownerName = currentUser?.displayName ?: "Unknown"

        val notificationData = mapOf(
            "title" to sectionName,
            "description" to message,
            "ownerName" to ownerName,
            "sharedContentId" to UUID.randomUUID().toString(),
            "timestamp" to Timestamp.now(),
            "isRead" to false,
            "type" to type,
            "userId" to userId
        )

        firestore.collection("notifications")
            .add(notificationData)
            .addOnSuccessListener {
                Log.d("Notification", "Notification added successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("Notification", "Failed to add notification: ${e.message}")
            }
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

        // Rename section logic
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
                        renameSection(sectionName, newName) { success ->
                            if (success) {
                                refreshSections() // Refresh UI immediately
                                dialog.dismiss()
                            } else {
                                Toast.makeText(requireContext(), "Failed to rename section!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Invalid section name!", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()
        }

        // Start Button Logic
        startButton.setOnClickListener {
            firestore.collection("users")
                .document(userUID)
                .collection("sections")
                .document(sectionName)
                .get()
                .addOnSuccessListener { document ->
                    val flashcards = (document.get("flashcards") as? Map<*, *> ?: emptyMap<Any, Any>())
                        .mapNotNull { (_, value) ->
                            val cardMap = value as? Map<*, *>
                            val title = cardMap?.get("title") as? String
                            val description = cardMap?.get("description") as? String
                            if (title != null && description != null) {
                                Flashcard(title, description)
                            } else null
                        }

                    if (flashcards.isEmpty()) {
                        Toast.makeText(requireContext(), "No flashcards to display!", Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(requireContext(), FlashcardViewer::class.java)
                        intent.putParcelableArrayListExtra("flashcards", ArrayList(flashcards))
                        intent.putExtra("sectionName", sectionName) // Pass the actual section name
                        startActivity(intent)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load flashcards!", Toast.LENGTH_SHORT).show()
                }
        }

        sectionContainer.addView(sectionView)
    }

    private fun renameSection(oldId: String, newId: String, onComplete: (Boolean) -> Unit) {
        val oldRef = firestore.collection("users").document(userUID).collection("sections").document(oldId)
        val newRef = firestore.collection("users").document(userUID).collection("sections").document(newId)

        oldRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val data = document.data?.toMutableMap() ?: mutableMapOf()

                // Update 'name' field
                data["name"] = newId

                // Set the new document with the updated data
                newRef.set(data)
                    .addOnSuccessListener {
                        // Delete the old document
                        oldRef.delete()
                            .addOnSuccessListener {
                                // Update sectionData map
                                val flashcards = sectionData.remove(oldId)
                                sectionData[newId] = flashcards ?: mutableListOf()

                                // Add to history in Firestore
                                val historyRef = firestore.collection("users")
                                    .document(userUID)
                                    .collection("history")
                                    .document()

                                val historyItem = FlashcardHistory(
                                    action = "Renamed",
                                    sectionName = newId,
                                    previousName = oldId,
                                    dateTime = getCurrentDateTime()
                                )

                                historyRef.set(historyItem)
                                    .addOnSuccessListener {
                                        // Avoid duplicate entries by ensuring no redundant additions
                                        if (!historyList.any { it.dateTime == historyItem.dateTime && it.action == "Renamed" }) {
                                            historyList.add(historyItem)
                                            historyAdapter.notifyItemInserted(historyList.size - 1)
                                        }
                                    }
                                    .addOnFailureListener {
                                        Log.e("Firestore", "Failed to add renamed action to Firestore: ${it.message}")
                                    }

                                // Refresh the UI
                                refreshSections()
                                onComplete(true) // Signal success
                            }
                            .addOnFailureListener {
                                onComplete(false) // Signal failure
                                Log.e("Firestore", "Failed to delete old section: $oldId")
                            }
                    }
                    .addOnFailureListener {
                        onComplete(false) // Signal failure
                        Log.e("Firestore", "Failed to create new section: $newId")
                    }
            } else {
                onComplete(false) // Signal failure
                Log.e("Firestore", "Old section not found: $oldId")
            }
        }.addOnFailureListener {
            onComplete(false) // Signal failure
            Log.e("Firestore", "Failed to fetch old section: $oldId")
        }
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

    private fun getCurrentDateTime(): Timestamp {
        return Timestamp.now() // Firestore's server timestamp
    }

}
