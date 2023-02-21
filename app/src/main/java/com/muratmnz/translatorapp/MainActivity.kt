package com.muratmnz.translatorapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.Menu
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

class MainActivity : AppCompatActivity() {

    //UI views
    private lateinit var sourceLanguageEt: EditText
    private lateinit var targetLanguageTv: TextView
    private lateinit var sourceLanguageSelectBtn: MaterialButton
    private lateinit var targetLanguageSelectBtn: MaterialButton
    private lateinit var translateBtn: MaterialButton

    companion object {

        //for printing logs
        private const val TAG = "MAIN_TAG"
    }

    //will contain list with language code and title
    private var languageArrayList: ArrayList<ModelLanguage>? = null

    private var sourceLanguageCode = "en"
    private var sourceLanguageTitle = "English"
    private var targetLanguageCode = "tr"
    private var targetLanguageTitle = "Turkish"


    //Translator options to set source and destination language it
    private lateinit var translatorOptions: TranslatorOptions

    // translator object, for configuring it with source and target language
    private lateinit var translator: Translator

    // progressivedialog, to show while translation process
    private lateinit var progressDialog: ProgressDialog

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //init UI views
        sourceLanguageEt = findViewById(R.id.sourceLanguageEt)
        targetLanguageTv = findViewById(R.id.targetLanguageTv)
        sourceLanguageSelectBtn = findViewById(R.id.sourceLanguageSelectionBtn)
        targetLanguageSelectBtn = findViewById(R.id.targetLanguageSelectionBtn)
        translateBtn = findViewById(R.id.translateBtn)


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)


        loadAvailableLanguages()

        //handle sourceLanguageSelectBtn click , choose language (from list) which you want to translate
        sourceLanguageSelectBtn.setOnClickListener {

            sourceLanguageChoose()

        }

        targetLanguageSelectBtn.setOnClickListener {

            targetLanguageChoose()
        }

        translateBtn.setOnClickListener {

            validateData()
        }

    }


    private var sourceLanguageText = ""
    private fun validateData() {

        sourceLanguageText = sourceLanguageEt.text.toString().trim()

        Log.d(TAG, "validateDate: sourceLanguageText: $sourceLanguageText")

        if (sourceLanguageText.isEmpty()) {
            showToast("Enter text to translate...")
        } else {
            startTranslation()
        }
    }

    private fun startTranslation() {
        //set progress message and show
        progressDialog.setMessage("Processing language model...")
        progressDialog.show()

        //init translatorOptions with source and target languages.
        translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguageCode)
            .setTargetLanguage(targetLanguageCode)
            .build()
        translator = Translation.getClient(translatorOptions)

        //init downloadConditions with option to requiredWifi
        val downloadConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        //start downloading translation model if required
        translator.downloadModelIfNeeded(downloadConditions)
            .addOnSuccessListener {
                // translation model ready to be translated
                Log.d(TAG, "startTranslation: model ready, start translation...")

                progressDialog.setMessage("Translating...")

                translator.translate(sourceLanguageText)
                    .addOnSuccessListener { translatedText ->
                        //successfully translated
                        Log.d(TAG, "startTranslation: translatedText: $translatedText")

                        progressDialog.dismiss()

                        targetLanguageTv.text = translatedText
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Log.d(TAG, "startTranslation: ", e)
                        showToast("Failed to translate due to ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.d(TAG, "startTranslation: ", e)
                showToast("Failed to translate due to ${e.message}")
                // failed to ready translation model, cant proceed to translatiom
            }
    }

    private fun loadAvailableLanguages() {
        //init language array before starting adding data into it
        languageArrayList = ArrayList()

        //get list all languages
        val languageCodeList = TranslateLanguage.getAllLanguages()

        for (languageCode in languageCodeList) {
            val languageTitle = Locale(languageCode).displayLanguage

            Log.d(TAG, "loadAvailableLanguages: languageCode: $languageCode")
            Log.d(TAG, "loadAvailableLanguages: languageTitle: $languageTitle")

            val moduleLanguage = ModelLanguage(languageCode, languageTitle)

            languageArrayList!!.add(moduleLanguage)
        }

    }

    private fun sourceLanguageChoose() {
        val popupMenu = PopupMenu(this, sourceLanguageSelectBtn)

        for (i in languageArrayList!!.indices) {

            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList!![i].languageTitle)
        }

        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val position = menuItem.itemId
            sourceLanguageCode = languageArrayList!![position].languageCode
            sourceLanguageTitle = languageArrayList!![position].languageTitle

            sourceLanguageSelectBtn.text = sourceLanguageTitle
            sourceLanguageEt.hint = "Enter $sourceLanguageTitle"

            Log.d(TAG, "loadAvailableLanguages: sourcelanguageCode: $sourceLanguageCode")
            Log.d(TAG, "loadAvailableLanguages: sourcelanguageTitle: $sourceLanguageTitle")

            false
        }
    }

    private fun targetLanguageChoose() {
        //init popupmenu param 1 is context, param 2 is the ui view around which we want to show popup menu, to choose source language from list
        val popupMenu = PopupMenu(this, targetLanguageSelectBtn)

        //from languageArrayList we will display language titles.
        for (i in languageArrayList!!.indices) {

            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList!![i].languageTitle)
        }

        //show popup menu
        popupMenu.show()

        //handle popup menu item click
        popupMenu.setOnMenuItemClickListener { menuItem ->
            //get clicked item id which is position from list
            val position = menuItem.itemId

            //get code and title of language selected.
            targetLanguageCode = languageArrayList!![position].languageCode
            targetLanguageTitle = languageArrayList!![position].languageTitle

            //set the selected language to sourcelanguageselectBtn as text
            targetLanguageSelectBtn.text = targetLanguageTitle

            //show in logs
            Log.d(TAG, "loadAvailableLanguages: targetLanguageCode: $targetLanguageCode")
            Log.d(TAG, "loadAvailableLanguages: targetLanguageTitle: $targetLanguageTitle")

            false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    }

}