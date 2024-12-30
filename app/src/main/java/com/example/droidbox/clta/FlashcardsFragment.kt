
package com.example.droidbox.clta

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FlashcardsFragment : Fragment() {

    private lateinit var sectionContainer: LinearLayout
    private lateinit var addSectionButton: Button
    private lateinit var deleteSectionButton: Button
    private lateinit var shareSectionButton: Button
    private lateinit var inputDataButton: Button
    private lateinit var historyRecyclerView: RecyclerView

    private val historyList = mutableListOf<FlashcardHistory>()
    private lateinit var historyAdapter: FlashcardHistoryAdapter

    private val sectionData = mutableMapOf<String, MutableList<Flashcard>>()
    private var selectedSectionName: String? = null
    private val MAX_SECTIONS_LIMITED_USER = 5
    private var isPremiumUser = false

    private val inputDataLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val flashcards =
                    result.data?.getParcelableArrayListExtra<Flashcard>("flashcards")
                if (selectedSectionName != null && flashcards != null) {
                    sectionData[selectedSectionName]?.addAll(flashcards)
                    Toast.makeText(
                        requireContext(),
                        "Flashcards added to $selectedSectionName!",
                        Toast.LENGTH_SHORT
                    ).show()
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

        addSectionButton.setOnClickListener { handleAddSection() }
        inputDataButton.setOnClickListener { handleInputData() }
        deleteSectionButton.setOnClickListener { handleDeleteSection() }
        shareSectionButton.setOnClickListener { Toast.makeText(requireContext(), "Feature not implemented.", Toast.LENGTH_SHORT).show() }

        return view
    }

    private fun handleAddSection() {
        if (!isPremiumUser && sectionContainer.childCount >= MAX_SECTIONS_LIMITED_USER) {
            Toast.makeText(
                requireContext(),
                "You can only add up to $MAX_SECTIONS_LIMITED_USER sections. Upgrade to premium for unlimited sections!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            showAddSectionDialog()
        }
    }

    private fun handleInputData() {
        if (sectionData.isEmpty()) {
            Toast.makeText(requireContext(), "Please add a section first!", Toast.LENGTH_SHORT).show()
        } else {
            showSectionSelectionDialog()
        }
    }

    private fun handleDeleteSection() {
        if (sectionContainer.childCount == 0) {
            Toast.makeText(requireContext(), "No sections to delete.", Toast.LENGTH_SHORT).show()
            return
        }
        val sectionNames = sectionData.keys.toList()
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Section")
            .setItems(sectionNames.toTypedArray()) { _, which ->
                val sectionName = sectionNames[which]
                sectionContainer.removeViewAt(which)
                sectionData.remove(sectionName)
                historyList.add(FlashcardHistory("Deleted Section: $sectionName"))
                historyAdapter.notifyItemRemoved(which)
            }
            .create()
            .show()
    }

    // UPDATED LOGIC: Modified showAddSectionDialog to use the existing addSectionButton
    private fun showAddSectionDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.flashcard_dialog_add_section, null)
        val sectionNameInput = dialogView.findViewById<EditText>(R.id.sectionNameInput)
        val addSectionButton = dialogView.findViewById<Button>(R.id.addSectionButton)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        addSectionButton.setOnClickListener {
            val sectionName = sectionNameInput.text.toString().trim()
            if (sectionName.isNotEmpty()) {
                if (!sectionData.containsKey(sectionName)) {
                    sectionData[sectionName] = mutableListOf()
                    addDynamicSection(sectionName)
                    historyList.add(FlashcardHistory("Added Section: $sectionName"))
                    historyAdapter.notifyItemInserted(historyList.size - 1)
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Section already exists!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Section name cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
// END OF UPDATED LOGIC

    private fun addDynamicSection(sectionName: String) {
        val sectionView = LayoutInflater.from(requireContext())
            .inflate(R.layout.flashcard_dynamic_layout, sectionContainer, false)
        val sectionTitle = sectionView.findViewById<TextView>(R.id.sectionTitle)
        val startButton = sectionView.findViewById<Button>(R.id.startButton)

        sectionTitle.text = sectionName

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
}

