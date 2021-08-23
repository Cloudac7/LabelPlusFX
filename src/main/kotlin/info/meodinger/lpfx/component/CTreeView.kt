package info.meodinger.lpfx.component

import info.meodinger.lpfx.GRAPHICS_CIRCLE_RADIUS
import info.meodinger.lpfx.State
import info.meodinger.lpfx.ViewMode
import info.meodinger.lpfx.options.Settings
import info.meodinger.lpfx.type.TransGroup
import info.meodinger.lpfx.util.color.toHex
import info.meodinger.lpfx.util.dialog.showChoice
import info.meodinger.lpfx.util.dialog.showConfirm
import info.meodinger.lpfx.util.dialog.showError
import info.meodinger.lpfx.util.dialog.showInput
import info.meodinger.lpfx.util.resource.I18N
import info.meodinger.lpfx.util.resource.get
import info.meodinger.lpfx.util.tree.*
import javafx.collections.ObservableList

import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.ContextMenuEvent
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.util.Callback

/**
 * Author: Meodinger
 * Date: 2021/8/16
 * Location: info.meodinger.lpfx.component
 */
class CTreeView: TreeView<String>() {

    private abstract class TreeMenu : ContextMenu() {
        abstract fun update(selectedItems: ObservableList<TreeItem<String>>)
    }
    private val treeMenu = object : TreeMenu() {
        private val groupNameFormatter = TextFormatter<String> { change ->
            change.text = change.text
                .trim()
                .replace(" ", "_")
                .replace("|", "_")
            change
        }

        private val r_addGroupField = TextField()
        private val r_addGroupPicker = CColorPicker()
        private val r_addGroupDialog = Dialog<TransGroup>()
        private val r_addGroupAction = { rootItem: TreeItem<String> ->
            val newGroupId = State.transFile.groupList.size
            val newColor = if (newGroupId >= 9) Color.WHITE
            else Color.web(Settings[Settings.DefaultColorList].asList()[newGroupId])

            r_addGroupField.text = String.format(I18N["context.add_group.dialog.format"], newGroupId + 1)
            r_addGroupPicker.value = newColor
            r_addGroupDialog.result = null
            r_addGroupDialog.showAndWait().ifPresent { newGroup ->
                // Edit data
                State.transFile.groupList.add(newGroup)
                // Update view
                State.controller.addLabelLayer()
                rootItem.children.add(TreeItem(newGroup.name, Circle(8.0, Color.web(newGroup.color))))
                // Mark change
                State.isChanged = true
            }
        }
        private val r_addGroupItem = MenuItem(I18N["context.add_group"])

        private val g_renameAction = { groupItem: TreeItem<String> ->
            showInput(
                State.stage,
                I18N["context.rename_group.dialog.title"],
                I18N["context.rename_group.dialog.header"],
                groupItem.value,
                groupNameFormatter
            ).ifPresent { newName ->
                if (newName.isBlank()) return@ifPresent
                for (group in State.transFile.groupList) {
                    if (group.name == newName) showError(I18N["context.rename_group.error.same_name"])
                    return@ifPresent
                }

                val groupId = State.getGroupIdByName(groupItem.value)

                // Edit data
                State.transFile.getTransGroupAt(groupId).name = newName
                // Update view
                groupItem.value = newName
                // Mark change
                State.isChanged = true
            }
        }
        private val g_renameItem = MenuItem(I18N["context.rename_group"])
        private val g_changeColorPicker = CColorPicker()
        private val g_changeColorAction = { groupItem: TreeItem<String> ->
            val newColor = g_changeColorPicker.value
            val groupId = State.getGroupIdByName(groupItem.value)

            // Edit data
            State.transFile.getTransGroupAt(groupId).color = newColor.toHex()
            // Update view
            (groupItem.graphic as Circle).fill = newColor
            // Mark change
            State.isChanged = true
        }
        private val g_changeColorItem = MenuItem()
        private val g_deleteAction = { groupItem: TreeItem<String> ->
            val groupId = State.getGroupIdByName(groupItem.value)

            // Edit data
            for (labels in State.transFile.transMap.values) for (label in labels) {
                if (label.groupId >= groupId) {
                    label.groupId = label.groupId - 1
                }
            }
            State.transFile.groupList.removeIf { it.name == groupItem.value }
            // Update view
            groupItem.parent.children.remove(groupItem)
            State.controller.delLabelLayer(groupId)
            // Mark change
            State.isChanged = true
        }
        private val g_deleteItem = MenuItem(I18N["context.delete_group"])

        private val l_moveToAction = { items: ObservableList<TreeItem<String>> ->
            val groupNameList = ArrayList<String>()
            for (group in State.transFile.groupList) groupNameList.add(group.name)

            showChoice(
                State.stage,
                I18N["context.move_to.title"],
                I18N["context.move_to.header"],
                groupNameList
            ).ifPresent { newGroupName ->
                val newGroupId = State.getGroupIdByName(newGroupName)

                // Edit data
                for (item in items) (item as CTreeItem).groupId = newGroupId
                // Update view
                State.controller.updateTreeView()
                // Mark change
                State.isChanged = true
            }
        }
        private val l_moveToItem = MenuItem(I18N["context.move_to"])
        private val l_deleteAction = { items: ObservableList<TreeItem<String>> ->
            val result = showConfirm(
                I18N["context.delete_label.dialog.title"],
                null,
                I18N["context.delete_label.dialog.content.pl"],
            )

            if (result.isPresent && result.get() == ButtonType.YES) {
                // Edit data
                for (item in items) {
                    val label = (item as CTreeItem).meta
                    for (l in State.transFile.getTransLabelListOf(State.currentPicName)) {
                        if (l.index > label.index) {
                            l.index = l.index - 1
                        }
                    }
                    State.transFile.getTransLabelListOf(State.currentPicName).remove(label)
                }
                // Update view
                State.controller.updateTreeView()
                // Mark change
                State.isChanged = true
            }
        }
        private val l_deleteItem = MenuItem(I18N["context.delete_label"])

        init {
            r_addGroupField.textFormatter = groupNameFormatter
            r_addGroupPicker.hide()
            r_addGroupDialog.title = I18N["context.add_group.dialog.title"]
            r_addGroupDialog.headerText = I18N["context.add_group.dialog.header"]
            r_addGroupDialog.dialogPane.content = HBox(r_addGroupField, r_addGroupPicker).also { box -> box.alignment = Pos.CENTER }
            r_addGroupDialog.dialogPane.buttonTypes.addAll(ButtonType.APPLY, ButtonType.CANCEL)
            r_addGroupDialog.setResultConverter {
                if (it == ButtonType.APPLY)
                    TransGroup(r_addGroupField.text, r_addGroupPicker.value.toHex())
                else
                    null
            }

            g_changeColorPicker.valueProperty().addListener { _, _, newValue -> g_changeColorItem.text = newValue.toHex() }
            g_changeColorPicker.setPrefSize(40.0, 20.0)
            g_changeColorItem.graphic = g_changeColorPicker
        }

        override fun update(selectedItems: ObservableList<TreeItem<String>>) {
            items.clear()

            r_addGroupItem.onAction = null
            g_renameItem.onAction = null
            g_changeColorPicker.onAction = null
            g_deleteItem.onAction = null
            l_moveToItem.onAction = null
            l_deleteItem.onAction = null

            var rootCount = 0
            var groupCount = 0
            var labelCount = 0

            if (selectedItems.size == 0) return
            for (item in selectedItems) {
                if (item.parent == null) rootCount += 1
                else if (item is CTreeItem) labelCount += 1
                else groupCount += 1
            }

            if (rootCount == 1 && groupCount == 0 && labelCount == 0) {
                // root
                val rootItem = selectedItems[0]

                r_addGroupItem.setOnAction { r_addGroupAction(rootItem) }

                items.add(r_addGroupItem)
            } else if (rootCount == 0 && groupCount == 1 && labelCount == 0) {
                // group
                val groupItem = selectedItems[0]

                g_renameItem.setOnAction { g_renameAction(groupItem) }
                g_changeColorItem.text = g_changeColorPicker.value.toHex()
                g_changeColorPicker.value = (groupItem.graphic as Circle).fill as Color
                g_changeColorPicker.setOnAction { g_changeColorAction(groupItem) }
                g_deleteItem.isDisable = run {
                    val thisGroupId = State.getGroupIdByName(groupItem.value)

                    for (labels in State.transFile.transMap.values) for (label in labels)
                        if (label.groupId == thisGroupId) return@run true
                    false
                }
                g_deleteItem.setOnAction { g_deleteAction(groupItem) }

                items.add(g_renameItem)
                items.add(g_changeColorItem)
                items.add(SeparatorMenuItem())
                items.add(g_deleteItem)
            } else if (rootCount == 0 && groupCount == 0 && labelCount > 0) {
                // label(s)

                l_moveToItem.setOnAction { l_moveToAction(selectedItems) }
                l_deleteItem.setOnAction { l_deleteAction(selectedItems) }

                items.add(l_moveToItem)
                items.add(SeparatorMenuItem())
                items.add(l_deleteItem)
            } else {
                // other
            }
        }

    }

    init {
        // Init
        this.cellFactory = Callback {
            object : TreeCell<String>() {
                init {
                    alignment = Pos.CENTER_LEFT
                    setOnMouseClicked {
                        if (treeItem == treeItem.root && it.clickCount > 1)
                            treeItem.expandAll()
                    }
                }
            }
        }
        this.selectionModel.selectionMode = SelectionMode.MULTIPLE
        this.contextMenu = treeMenu

        // Update tree menu when requested
        addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED) {
            treeMenu.update(this.selectionModel.selectedItems)
        }
    }

}