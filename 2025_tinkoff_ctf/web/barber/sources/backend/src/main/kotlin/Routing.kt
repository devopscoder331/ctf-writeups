package com.epriori

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.io.path.createTempFile
import kotlin.io.path.outputStream
import kotlin.io.path.pathString

fun Application.configureRouting() {
    routing {

        post("/upload") {
            val multipart = call.receiveMultipart()
            val uuid = UUID.randomUUID()
            val outputDir = File("public/files/$uuid")
            outputDir.mkdirs()

            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val tempfile = createTempFile()
                    part.streamProvider().use { input ->
                        tempfile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    net.lingala.zip4j.ZipFile(tempfile.pathString).extractAll(outputDir.path)
                }
                part.dispose()
            }

            call.respond(HttpStatusCode.OK, "api/images/$uuid/${outputDir.listFiles().random().name}")
        }
        staticFiles("/images", File("public/files")) {  }
    }
}

fun unzipFile(zipFilePath: String, destinationDirectory: String) {
    val destDir = File(destinationDirectory)
    if (!destDir.exists()) {
        destDir.mkdirs()
    }

    ZipInputStream(FileInputStream(zipFilePath)).use { zipIn ->
        var entry = zipIn.nextEntry
        while (entry != null) {
            val filePath = destinationDirectory + File.separator + entry.name
            val outputFile = File(filePath)

            if (!entry.isDirectory) {
                outputFile.parentFile?.mkdirs()

                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(4096)
                    var count = zipIn.read(buffer)
                    while (count != -1) {
                        output.write(buffer, 0, count)
                        count = zipIn.read(buffer)
                    }
                }
            } else {
                outputFile.mkdirs()
            }

            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
    }
}
