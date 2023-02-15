package com.lotte.mart.daemonlib.module.sftp

import android.content.Context
import android.os.Environment
import com.lotte.mart.commonlib.exception.ExceptionHandler
import com.lotte.mart.daemonlib.module.sftp.filesystem.SftpFilesystemProvider
import org.apache.sshd.common.file.FileSystemFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory
import java.io.File

class SftpServerModule(context : Context, port:Int, id:String, pwd:String){
    private lateinit var _server: SshServer
    private var _context: Context = context
    private var _fsProvider: SftpFilesystemProvider

    init {
        _fsProvider = SftpFilesystemProvider(_context)
        initSFTPServer(port, id, pwd)
    }

    private fun initSFTPServer(port:Int, id:String, pwd:String) = ExceptionHandler.tryOrDefault(){
        val appdir = _context.filesDir   //hostkey 생성 경로(앱 경로 /data/user/0/packagename)
        System.setProperty("user.home", appdir.absolutePath)

        _server = SshServer.setUpDefaultServer()
        _server.port = port //prefs.getString("server_port", getString(R.string.prefs_port_default))!!.toInt()
        _server.keyPairProvider = SimpleGeneratorHostKeyProvider(File(appdir, "hostkey"))

        // Password authentication
        _server.passwordAuthenticator = PasswordAuthenticator { username, password, _ ->
//            val path = PathsManager.get(_context).getPathByUsername(username)
//            if (path != null)
//                password == path.password
//            else
//                false
            username.equals(id) && password.equals(pwd)
        }

        // Set filesystem for each user
        _server.fileSystemFactory = FileSystemFactory { session ->
//            val path = PathsManager.get(_context).getPathByUsername(session.username)
//            if (path != null && path.enabled)
                _fsProvider.newFileSystem(Environment.getExternalStorageDirectory().absolutePath)
//            else
//                FileSystems.getDefault()
        }

        val sftpFactory = SftpSubsystemFactory.Builder()
        sftpFactory.withShutdownOnExit(true)
        _server.subsystemFactories = listOf(sftpFactory.build())
    }

    fun startServer() = ExceptionHandler.tryOrDefault(){
        if(!_server.isStarted)
            _server.start()
    }

    fun stopServer() = ExceptionHandler.tryOrDefault(){
        if (_server != null && _server.isStarted)
            _server.stop()
    }

    fun isStarted():Boolean = ExceptionHandler.tryOrDefault(false){
        _server.isStarted
    }
}