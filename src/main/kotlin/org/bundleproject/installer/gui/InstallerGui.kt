package org.bundleproject.installer.gui

import com.formdev.flatlaf.*
import com.jthemedetecor.OsThemeDetector
import org.bundleproject.installer.installMultiMC
import org.bundleproject.installer.installOfficial
import org.bundleproject.installer.utils.*
import java.awt.*
import java.io.File
import java.lang.Exception
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.math.max
import kotlin.system.exitProcess

class InstallerGui : JFrame("Bundle Installer") {

    private var versionField: JComboBox<String>

    /**
     * Sets up all components of the gui
     *
     * @since 0.0.1
     */
    init {
        OsThemeDetector.getDetector().registerListener { dark ->
            SwingUtilities.invokeLater {
                if (dark) FlatDarkLaf.setup() else FlatLightLaf.setup()
                SwingUtilities.updateComponentTreeUI(this)
            }
        }
        iconImage = getResourceImage("/bundle.png")
        this.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setSize(404, 258)
        isResizable = false

        val center = JPanel()
        center.layout = null

        val titleText = JLabel("The Bundle Project")
        titleText.setBounds(2, 5, 385, 42)
        titleText.font = Font("Dialog", Font.BOLD, 18)
        titleText.horizontalAlignment = SwingConstants.CENTER
        titleText.preferredSize = Dimension(385, 25)
        titleText.name = "TitleText"
        center.add(titleText)

        val installerText = JLabel("Installer")
        installerText.name = "InstallerText"
        installerText.setBounds(2, 38, 385, 25)
        installerText.font = Font("Dialog", Font.BOLD, 14)
        installerText.horizontalAlignment = SwingConstants.CENTER
        installerText.preferredSize = Dimension(385, 25)
        center.add(installerText)

        val descriptionText = JTextArea()
        descriptionText.name = "Description"
        descriptionText.setBounds(15, 66, 365, 44)
        descriptionText.isEditable = false
        descriptionText.isEnabled = true
        descriptionText.font = Font("Dialog", Font.PLAIN, 12)
        descriptionText.lineWrap = true
        descriptionText.isOpaque = false
        descriptionText.preferredSize = Dimension(365, 44)
        descriptionText.wrapStyleWord = true
        descriptionText.highlighter = null
        descriptionText.text =
            """
            This installer will install Bundle into your desired version. It will NOT create a new profile - just launch how you would normally.
            """.trimIndent()
        center.add(descriptionText)


        val pathText = JLabel("Path")
        pathText.setBounds(15, 116, 47, 16)
        pathText.preferredSize = Dimension(47, 16)
        center.add(pathText)

        val pathField = JTextField(getDefaultMinecraftDir()?.path ?: "Please select .minecraft")
        pathField.setBounds(62, 114, 287, 20)
        pathField.preferredSize = Dimension(287, 20)
        pathField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = updateVersions(File(pathField.text))
            override fun removeUpdate(e: DocumentEvent) = updateVersions(File(pathField.text))
            override fun changedUpdate(e: DocumentEvent) = updateVersions(File(pathField.text))
        })
        center.add(pathField)

        val pathButton = JButton("...")
        pathButton.setBounds(350, 114, 25, 20)
        pathButton.margin = Insets(2, 2, 2, 2)
        pathButton.preferredSize = Dimension(25, 20)
        pathButton.addActionListener {
            val mcDir = File(pathField.text)
            val fileChooser = JFileChooser(mcDir)
            fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)  {
                pathField.text = fileChooser.selectedFile.path
            }
        }
        center.add(pathButton)

        val versionText = JLabel("Version")
        versionText.setBounds(15, 136, 47, 16)
        versionText.preferredSize = Dimension(47, 16)
        center.add(versionText)

        versionField = JComboBox()
        updateVersions(File(pathField.text))
        versionField.setBounds(62, 138, 312, 20)
        versionField.preferredSize = Dimension(312, 20)
        center.add(versionField)


        val bottom = JPanel()
        bottom.layout = FlowLayout(FlowLayout.CENTER, 15, 10)
        bottom.preferredSize = Dimension(390, 55)

        val closeButton = JButton("Close")
        closeButton.preferredSize = Dimension(100, 26)
        closeButton.addActionListener { exitProcess(0) }
        bottom.add(closeButton)

        val installButton = JButton("Install")
        installButton.preferredSize = Dimension(100, 26)
        installButton.addActionListener {
            val dir = File(pathField.text)
            val multimc = getMultiMCInstanceFolder(dir)

            val version = versionField.selectedItem
            if (version == null) {
                err("You must select a version or a valid path!")
                return@addActionListener
            }

            try {
                if (multimc != null) installMultiMC(multimc, versionField.selectedItem as String)
                else installOfficial(dir, versionField.selectedItem as String)
                success("Bundle has been successfully installed.")
            } catch (e: Exception) {
                e.printStackTrace()
                err("Failed to install Bundle!")
            }

        }
        installButton.requestFocus()
        bottom.add(installButton)


        val panel = JPanel()
        panel.layout = BorderLayout(5, 5)
        panel.preferredSize = Dimension(394, 203)
        panel.add(center, BorderLayout.CENTER)
        panel.add(bottom, BorderLayout.SOUTH)

        this.add(panel)

        centerFrame()
    }

    /**
     * Update the version combo box with the current
     * file path's version list
     *
     * @since 0.0.1
     */
    private fun updateVersions(dir: File) {
        versionField.removeAllItems()
        getVersionsForFolder(dir)?.forEach { versionField.addItem(it) }
    }

    /**
     * Centers the frame in the main screen
     *
     * @since 0.0.1
     */
    private fun centerFrame() {
        val bounds = this.bounds

        val dim = Toolkit.getDefaultToolkit().screenSize.let { Rectangle(0, 0, it.width, it.height) }

        val newX = max(dim.x + (dim.width - bounds.width) / 2, 0)
        val newY = max(dim.y + (dim.height - bounds.height) / 2, 0)

        this.setBounds(newX, newY, bounds.width, bounds.height)
    }

    /**
     * Displays a message dialogue as a plain message
     *
     * @since 0.0.1
     */
    private fun success(message: String) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.PLAIN_MESSAGE)
    }

    /**
     * Displays a message dialogue as an error message
     *
     * @since 0.0.1
     */
    private fun err(message: String) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE)
    }

    companion object {
        fun setupInitialTheme() {
            if (OsThemeDetector.getDetector().isDark) FlatDarkLaf.setup()
            else FlatLightLaf.setup()
        }
    }

}