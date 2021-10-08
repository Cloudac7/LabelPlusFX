package info.meodinger.lpfx.component.common

import info.meodinger.lpfx.util.property.getValue
import info.meodinger.lpfx.util.property.setValue

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.TextArea
import javafx.scene.control.TextFormatter


/**
 * Author: Meodinger
 * Date: 2021/8/16
 * Location: info.meodinger.lpfx.component
 */

/**
 * A TextArea with a symbol ContextMenu
 */
class CLigatureArea: TextArea() {

    companion object {
        private const val LIGATURE_MAX_LENGTH: Int = 10
        private const val LIGATURE_MARK: String = "\\"
        private val DEFAULT_RULES: List<Pair<String, String>> = listOf("cc" to "◎")
    }

    private var ligaturing: Boolean = false
    private var ligatureStart: Int = 0
    private var ligatureString: String = ""

    val ligatureMaxLengthProperty: IntegerProperty = SimpleIntegerProperty(LIGATURE_MAX_LENGTH)
    var ligatureMaxLength: Int by ligatureMaxLengthProperty

    val ligatureRulesProperty: ListProperty<Pair<String, String>> = SimpleListProperty(FXCollections.observableArrayList(DEFAULT_RULES))
    var ligatureRules: ObservableList<Pair<String, String>> by ligatureRulesProperty

    val ligatureMarkProperty: StringProperty = SimpleStringProperty(LIGATURE_MARK)
    var ligatureMark: String by ligatureMarkProperty

    private val boundTextPropertyProperty = SimpleObjectProperty<StringProperty>(null)
    private var boundTextProperty: StringProperty? by boundTextPropertyProperty
    val boundProperty: StringProperty? get() = boundTextProperty
    val isBound: Boolean get() = boundTextProperty != null

    init {
        this.textFormatter = TextFormatter<String> { change ->
            if (change.isAdded) {
                if (change.text == ligatureMark) {
                    ligatureStart(this.caretPosition)
                    return@TextFormatter change
                }

                ligatureString += change.text

                if (ligatureString.length <= ligatureMaxLength) {
                    if (ligaturing) for (rule in ligatureRules) if (rule.first == ligatureString) {
                        val ligatureEnd = this.caretPosition
                        val caretPosition = ligatureStart + rule.second.length

                        this.text = this.text.replaceRange(ligatureStart, ligatureEnd, rule.second)

                        change.text = ""
                        change.setRange(caretPosition, caretPosition)
                        change.caretPosition = caretPosition
                        change.anchor = caretPosition

                        ligatureEnd()
                    }
                } else {
                    ligatureEnd()
                }
            } else if (change.isDeleted) {
                if (ligaturing) {
                    val end = ligatureString.length - 1 - change.text.length

                    if (end >= 0) {
                        ligatureString = ligatureString.substring(0, end)
                    } else {
                        ligatureEnd()
                    }
                }
            } else {
                ligatureEnd()
            }

            change
        }
    }

    fun reset() {
        unbindBidirectional()
    }

    private fun ligatureStart(startCaret: Int) {
        ligaturing = true
        ligatureStart = startCaret
        ligatureString = ""
    }
    private fun ligatureEnd() {
        ligaturing = false
        ligatureStart = 0
        ligatureString = ""
    }

    fun bindBidirectional(property: StringProperty) {
        textProperty().bindBidirectional(property)
        boundTextProperty = property
    }
    fun unbindBidirectional() {
        if (boundTextProperty == null)  return

        textProperty().unbindBidirectional(boundTextProperty)
        boundTextProperty = null
        text = ""
    }

}