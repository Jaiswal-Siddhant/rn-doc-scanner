// DocScanner.kt

package com.margelo.nitro.docscanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
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
  }

  // Store the continuation to resume it later
  private var pendingContinuation: ((Result<Array<String>>) -> Unit)? = null

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

            // Get image URIs
            result?.pages?.forEach { page ->
              val imageUri = page.imageUri
              val imagePath = NitroModules.applicationContext?.let {
                copyUriToFile(imageUri, it)
              }
              res.add(imagePath.toString())
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

  override fun scanDocument(options: ScanOptions): Promise<Array<String>> {
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

          val isGalleryImportAllowed = options.galleryImport ?: false;
          val pageLimit = (options.pages ?: 1.0).toInt();

          val gmsBuilder = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(isGalleryImportAllowed)
            .setPageLimit(pageLimit)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
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
