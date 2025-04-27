package whatis.love.agedate.report

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import whatis.love.agedate.R
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.qrcode.generateProfileQRCode
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Singleton
class ReportPrintService
    @Inject
    constructor(
        @ApplicationContext private val appContext: Context,
    ) {
        suspend fun printHtmlDocument(
            activity: Activity,
            html: String,
            documentName: String,
        ) = withContext(Dispatchers.Main) {
            val webView = WebView(activity)
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)

            suspendCancellableCoroutine { continuation ->
                webView.webViewClient =
                    object : WebViewClient() {
                        override fun onPageFinished(
                            view: WebView,
                            url: String,
                        ) {
                            val printManager =
                                activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
                            val printAdapter = webView.createPrintDocumentAdapter(documentName)

                            val attributes =
                                PrintAttributes
                                    .Builder()
                                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                    .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                    .build()

                            printManager.print(documentName, printAdapter, attributes)
                            continuation.resume(Unit) { cause, _, _ ->
                            }
                        }
                    }
            }
        }

        suspend fun generateAndPrintReport(
            activity: Activity,
            reportDetails: ReportDetails,
        ) {
            try {
                val html = generateReportHTML(appContext, reportDetails)
                val documentName = "Report_${reportDetails.reported.id}_${System.currentTimeMillis()}"
                printHtmlDocument(activity, html, documentName)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }

        fun generateReportHTML(
            context: Context,
            reportDetails: ReportDetails,
        ): String {
            val qrCode = htmlBase64QRCode(context, reportDetails.reported)
            val template = loadHtmlTemplate(context)

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru", "RU"))
            val currentDate = dateFormat.format(System.currentTimeMillis())
            val russianMonthNames =
                listOf(
                    "января",
                    "февраля",
                    "марта",
                    "апреля",
                    "мая",
                    "июня",
                    "июля",
                    "августа",
                    "сентября",
                    "октября",
                    "ноября",
                    "декабря",
                )
            val birthMonth =
                russianMonthNames.getOrNull(reportDetails.reporter.birthMonth - 1) ?: "нулября"

            val substitutions =
                mapOf<String, String>(
                    "qr_image" to qrCode,
                    "first_name" to escapeHtml(reportDetails.reporter.firstName),
                    "last_name" to escapeHtml(reportDetails.reporter.lastName),
                    "birth_day" to reportDetails.reporter.birthDay.toString(),
                    "birth_month" to birthMonth,
                    "birth_year" to reportDetails.reporter.birthYear.toString(),
                    "my_profile_id" to escapeHtml(reportDetails.reporter.id),
                    "reported_id" to escapeHtml(reportDetails.reported.id),
                    "reported_first_name" to escapeHtml(reportDetails.reported.firstName),
                    "reported_last_name" to escapeHtml(reportDetails.reported.lastName),
                    "reason_1" to reasonCheckBoxClass(reportDetails, "reason-1"),
                    "reason_2" to reasonCheckBoxClass(reportDetails, "reason-2"),
                    "reason_3" to reasonCheckBoxClass(reportDetails, "reason-3"),
                    "reason_4" to reasonCheckBoxClass(reportDetails, "reason-4"),
                    "reason_5" to reasonCheckBoxClass(reportDetails, "reason-5"),
                    "reason_6" to reasonCheckBoxClass(reportDetails, "reason-6"),
                    "reason_7" to reasonCheckBoxClass(reportDetails, "reason-7"),
                    "reason_8" to reasonCheckBoxClass(reportDetails, "reason-8"),
                    "reason_9" to reasonCheckBoxClass(reportDetails, "reason-9"),
                    "date" to currentDate,
                    "first_name_short" to
                        escapeHtml(
                            reportDetails.reporter.firstName
                                .firstOrNull()
                                ?.toString()
                                ?: "",
                        ).uppercase(),
                )

            var finalHtml = template
            substitutions.forEach { (key, value) ->
                finalHtml = finalHtml.replace("{{$key}}", value)
            }
            return finalHtml
        }

        private fun escapeHtml(input: String): String =
            input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")

        private fun loadHtmlTemplate(context: Context): String {
            val inputStream = context.resources.openRawResource(R.raw.report_form)
            return inputStream.bufferedReader().use { it.readText() }
        }

        private fun reasonCheckBoxClass(
            reportDetails: ReportDetails,
            reason: String,
        ): String {
            if (reason == reportDetails.reason) {
                return " checked"
            }
            return ""
        }

        @OptIn(ExperimentalEncodingApi::class)
        private fun htmlBase64QRCode(
            context: Context,
            profile: Profile,
        ): String {
            val bitmap = generateProfileQRCode(context, profile, applyGradient = false)!!

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val encodedImage = Base64.encode(byteArrayOutputStream.toByteArray())
            return "data:image/png;base64,$encodedImage"
        }
    }
