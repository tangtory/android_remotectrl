package com.lotte.mart.daemonlib.module.sftp.filesystem

import android.net.Uri
import com.lotte.mart.commonlib.exception.ExceptionHandler
import org.apache.sshd.common.file.util.BasePath
import java.io.File
import java.nio.file.LinkOption
import java.nio.file.Path

class SftpPath(private val fileSystem: SftpFilesystem, rootPath: String?, names: List<String>) : BasePath<SftpPath, SftpFilesystem>(fileSystem, rootPath, names) {
    override fun toRealPath(vararg options: LinkOption): Path = ExceptionHandler.tryOrDefault(null) {
        val absolute = toAbsolutePath()
        val provider = fileSystem.provider()
        provider.checkAccess(absolute)
        absolute
    }!!

    override fun toFile(): File = ExceptionHandler.tryOrDefault(null) {
        val absolute = toAbsolutePath()
        var path = fileSystem.getRoot()
        for (n in absolute.names) {
            path = path.resolve(n)
        }
        path.toFile()
    }!!

    fun getContentResolverUri(): Uri? = ExceptionHandler.tryOrDefault(null) {
        fileSystem.getContentResolverUri()
    }
}