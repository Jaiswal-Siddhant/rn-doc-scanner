// DocScanner.kt

package com.margelo.nitro.docscanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.bridge.BaseActivityEventListener
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.margelo.nitro.NitroModules
import com.margelo.nitro.core.Promise
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@DoNotStrip
class DocScanner : HybridDocScannerSpec() {
  companion object {
    private const val MY_REQUEST_CODE = 1001
    private var outputMode = GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
  }

  // Store the continuation to resume it later
  private var pendingContinuation: ((Result<Array<String>>) -> Unit)? = null

  private fun copyUriToFile(uri: Uri, context: Context): String {
    Log.d("URI_STRING", uri.toString())
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(uri)

    var ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString())

    // If empty, try from MIME type (example: image/jpeg → jpg)
    if (ext.isNullOrEmpty()) {
      val mime = contentResolver.getType(uri)
      ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
    }

    // Fallback if still null
    if (ext.isNullOrEmpty()) ext = "jpg"

    // Ensure directory exists
    val publicDir = File(
      context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
      "Scans"
    )
    publicDir.mkdirs()

    val destFile = File(
      publicDir,
      "scanned_${System.currentTimeMillis()}.$ext"
    )

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
      if (requestCode == MY_REQUEST_CODE) {
        if (resultCode == Activity.RESULT_OK) {
          try {
            val result = GmsDocumentScanningResult.fromActivityResultIntent(data)
            val res: ArrayList<String> = ArrayList()

            val pageCount = result?.pages?.size ?: 0
            if (pageCount == 0) {
              pendingContinuation?.invoke(Result.failure(Exception("No files scanned!")))
              pendingContinuation = null
              return
            }

            if (outputMode == GmsDocumentScannerOptions.RESULT_FORMAT_PDF) {
              Log.d("SCANNER_MODE_PDF", result?.pdf?.uri.toString())
              val imagePath = NitroModules.applicationContext?.let {
                if(result?.pdf?.uri != null) {
                  val pdfPath = NitroModules.applicationContext?.let {
                    copyUriToFile(result.pdf!!.uri, it)
                  }
                  res.add(pdfPath.toString())
                }
              }
              res.add(imagePath.toString())
            } else {
              // Get image URIs
              result?.pages?.forEach { page ->
                val imageUri = page.imageUri
                val imagePath = NitroModules.applicationContext?.let {
                  copyUriToFile(imageUri, it)
                }
                res.add(imagePath.toString())
              }
            }

            Log.d("CODE_SUCCESS", res.toString())

            // Resume the coroutine with success
            pendingContinuation?.invoke(Result.success(res.toTypedArray()))

          } catch (e: Exception) {
            Log.d("CODE_EXCEPTION", e.toString())
            pendingContinuation?.invoke(Result.failure(Exception(e)))
          }
        } else {
          // User cancelled
          pendingContinuation?.invoke(Result.failure(Exception("Scan cancelled by user")))
        }

        // Clean up
        pendingContinuation = null
      }
    }
  }

  override fun scanDocument(options: ScanOptions?): Promise<Array<String>> {
    return Promise.async {
      suspendCoroutine { continuation ->
        try {
          // Store the continuation
          pendingContinuation = { result ->
            result.fold(
              onSuccess = { continuation.resume(it) },
              onFailure = { continuation.resumeWithException(it) }
            )
          }

          val isGalleryImportAllowed = options?.galleryImport ?: false
          val pageLimit = (options?.pages ?: 1.0).toInt()
          val resultFormat = GmsDocumentScannerOptions.RESULT_FORMAT_PDF
          val scannerMode =
            options?.scannerMode?.value ?: GmsDocumentScannerOptions.SCANNER_MODE_BASE

          if (resultFormat == ResultFormat.JPEG.value) {
            outputMode = GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
          } else {
            outputMode = GmsDocumentScannerOptions.RESULT_FORMAT_PDF
          }

          val gmsBuilder = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(isGalleryImportAllowed)
            .setPageLimit(pageLimit)
            .setResultFormats(resultFormat)
            .setScannerMode(scannerMode)
            .build()

          val scanner = GmsDocumentScanning.getClient(gmsBuilder)
          Log.d("SCANNER_CONTENT_HERE", scanner.toString())

          val context = NitroModules.applicationContext
          val activity = context?.currentActivity ?: throw Exception("No Context found!")

          // Add the event listener
          context.addActivityEventListener(eventListener)

          scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
              try {
                activity.startIntentSenderForResult(
                  intentSender,
                  MY_REQUEST_CODE,
                  null,
                  0,
                  0,
                  0
                )
              } catch (e: Exception) {
                Log.e("DocScanner", "Failed to start intent", e)
                pendingContinuation?.invoke(Result.failure(Exception(e)))
                pendingContinuation = null
              }
            }
            .addOnFailureListener { error ->
              Log.e("DocScanner", "Failed to launch scanner", error)
              pendingContinuation?.invoke(Result.failure(Exception(error)))
              pendingContinuation = null
            }

        } catch (e: Exception) {
          pendingContinuation?.invoke(Result.failure(e))
          pendingContinuation = null
        }
      }
    }
  }
}
