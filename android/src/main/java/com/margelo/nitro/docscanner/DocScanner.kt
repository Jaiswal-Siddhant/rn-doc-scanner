// DocScanner.kt

package com.margelo.nitro.docscanner

import android.util.Log
import androidx.core.app.ComponentActivity
import com.facebook.proguard.annotations.DoNotStrip
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.margelo.nitro.NitroModules

@DoNotStrip
class DocScanner : HybridDocScannerSpec() {
  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  override fun scanDocument() {
//    TODO("Not yet implemented")
    val options = GmsDocumentScannerOptions.Builder()
      .setGalleryImportAllowed(false)
      .setPageLimit(2)
      .setResultFormats(
        GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
        GmsDocumentScannerOptions.RESULT_FORMAT_PDF
      )
      .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
      .build()

    val scanner = GmsDocumentScanning.getClient(options)

    Log.d("SCANNER_CONTENT_HERE", scanner.toString())
    val activity = NitroModules.applicationContext?.currentActivity ?: return
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
