package info.meodinger.lpfx.component

import javafx.beans.property.*
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.layout.HBox
import javafx.scene.text.TextAlignment
import tornadofx.getValue
import tornadofx.setValue


/**
 * Author: Meodinger
 * Date: 2021/7/29
 * Location: info.meodinger.lpfx.component
 */

/**
 * A ComboBox with back/next Button (in HBox)
 */
class CComboBox<T> : HBox() {

    private val comboBox = ComboBox<T>()
    private val back = Button("<")
    private val next = Button(">")

    val valueProperty: ObjectProperty<T> = comboBox.valueProperty()
    val indexProperty: ReadOnlyIntegerProperty = comboBox.selectionModel.selectedIndexProperty()
    val isWrappedProperty = SimpleBooleanProperty(false)

    val value: T by valueProperty
    val index: Int by indexProperty
    var isWrapped: Boolean by isWrappedProperty

    init {
        back.setOnMouseClicked { back() }
        next.setOnMouseClicked { next() }

        comboBox.prefWidth = 150.0
        back.textAlignment = TextAlignment.CENTER
        next.textAlignment = TextAlignment.CENTER

        children.addAll(comboBox, back, next)
    }

    fun reset() {
        comboBox.items.clear()
        comboBox.selectionModel.clearSelection()
    }

    fun setList(list: List<T>) {
        reset()
        comboBox.items.addAll(list)

        if (list.isNotEmpty()) comboBox.selectionModel.select(0)
    }

    fun back() {
        val size = comboBox.items.size
        var newIndex = index - 1

        if (isWrapped) {
            if (newIndex < 0) newIndex += size
        }
        if (newIndex >= 0) {
            comboBox.value = comboBox.items[newIndex]
        }
    }

    fun next() {
        val size = comboBox.items.size
        var newIndex = index + 1

        if (isWrapped) {
            if (newIndex > size - 1) newIndex -= size
        }
        if (newIndex <= size - 1) {
            comboBox.value = comboBox.items[newIndex]
        }
    }

    fun moveTo(index: Int) {
        if (index in 0 until comboBox.items.size) comboBox.selectionModel.select(index)
        // else throw IllegalArgumentException("index invalid")
    }

    fun moveTo(item: T) {
        if (comboBox.items.contains(item)) comboBox.selectionModel.select(item)
        // else throw IllegalArgumentException("no such item")
    }

}