// DocScanner.kt

package com.margelo.nitro.docscanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.app.ComponentActivity
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.margelo.nitro.NitroModules
import java.io.File

@DoNotStrip
class DocScanner : HybridDocScannerSpec() {
  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  companion object {
    private const val MY_REQUEST_CODE = 1001
  }

  private fun copyUriToFile(uri: Uri, context: Context): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val publicDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Scans")
    publicDir.mkdirs()
    val destFile = File(publicDir, "scanned_${System.currentTimeMillis()}.jpg")

    inputStream?.use { input ->
      destFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }

    return destFile.absolutePath
  }

  private val eventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(
      activity: Activity,
      requestCode: Int,
      resultCode: Int,
      data: Intent?
    ) {
      val res: ArrayList<String> = ArrayList();

      if (requestCode == MY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
        try {
          val result = GmsDocumentScanningResult.fromActivityResultIntent(data)


          val pageCount = result?.pages?.size ?: 0
          if (pageCount == 0) {
            throw Exception("No files scanned!")
          }

          // Get image URIs
          result?.pages?.forEach { page ->
            val imageUri = page.imageUri // Uri of scanned image
            val imagePath = NitroModules.applicationContext?.let {
              copyUriToFile(
                imageUri,
                context = it
              )
            }
            res.add(imagePath.toString());
          }

          Log.d("CODE_SUCCESS", res.toString())
        } catch (e: Exception) {
          Log.d("CODE_EXCEPTION", e.toString())
          throw Exception(e)
        }
      }
    }
  }

  override fun scanDocument() {
    val context = NitroModules.applicationContext
    val options = GmsDocumentScannerOptions.Builder()
      .setGalleryImportAllowed(false)
      .setPageLimit(2)
      .setResultFormats(
        GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
      )
      .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
      .build()

    val scanner = GmsDocumentScanning.getClient(options)

    Log.d("SCANNER_CONTENT_HERE", scanner.toString())
    val activity = NitroModules.applicationContext?.currentActivity ?: return
    context?.addActivityEventListener(eventListener)

    GmsDocumentScanning.getClient(options)
      .getStartScanIntent(activity)
      .addOnSuccessListener { intentSender ->
        try {
          activity.startIntentSenderForResult(
            intentSender,
            1001,
            null,
            0,
            0,
            0
          )

        } catch (e: Exception) {
          Log.e("DocScanner", "Failed to start intent", e)
        }
      }

      .addOnFailureListener { error ->
        Log.e("DocScanner", "Failed to launch scanner", error)
      }
  }
}
