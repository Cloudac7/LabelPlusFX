package info.meodinger.lpfx.options

import info.meodinger.lpfx.util.dialog.showException

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.options
 */
object Options {

    private const val LPFX = ".lpfx"
    private const val FileName_Config = "config"
    private const val FileName_Settings = "settings"
    private const val FileName_RecentFiles = "recent_files"

    val lpfx: Path = Paths.get(System.getProperty("user.home")).resolve(LPFX)
    val config: Path = lpfx.resolve(FileName_Config)
    val settings: Path = lpfx.resolve(FileName_Settings)
    val recentFiles: Path = lpfx.resolve(FileName_RecentFiles)

    init {
        try {
            // project data folder
            if (Files.notExists(lpfx)) Files.createDirectories(lpfx)

            // config
            initConfig()
            // settings
            initSettings()
            // recent_files
            initRecentFiles()
        } catch (e: IOException) {
            showException(e)
        }
    }

    @Throws(IOException::class)
    private fun initConfig() {
        if (Files.notExists(config)) {
            Files.createFile(config)
            Config.save()
        }
        Config.load()
        Runtime.getRuntime().addShutdownHook(Thread { Config.save() })
    }

    @Throws(IOException::class)
    private fun initSettings() {
        if (Files.notExists(settings)) {
            Files.createFile(settings)
            Settings.save()
        }
        Settings.load()
        Runtime.getRuntime().addShutdownHook(Thread { Settings.save() })
    }

    @Throws(IOException::class)
    private fun initRecentFiles() {
        if (Files.notExists(recentFiles)) {
            Files.createFile(recentFiles)
        }
        RecentFiles.load()
        Runtime.getRuntime().addShutdownHook(Thread { RecentFiles.save() })
    }

}