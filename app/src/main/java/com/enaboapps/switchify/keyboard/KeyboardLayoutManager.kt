package com.enaboapps.switchify.keyboard

sealed class KeyType {
    data class Character(val char: String) : KeyType() {
        override fun toString() = char
    }

    data class Prediction(val prediction: String) : KeyType() {
        override fun toString() = prediction
    }

    object Backspace : KeyType() {
        override fun toString() = "⌫"
    }

    object DeleteWord : KeyType() {
        override fun toString() = "⌦"
    }

    object Clear : KeyType() {
        override fun toString() = "C"
    }

    object Space : KeyType() {
        override fun toString() = " "
    }

    object ImeAction : KeyType() {
        override fun toString() = "Action"
    }

    object Return : KeyType() {
        override fun toString() = "⏎"
    }

    data object Tab : KeyType()

    object ShiftCaps : KeyType() {
        override fun toString() = "⇧"
    }

    object LeftArrow : KeyType() {
        override fun toString() = "←"
    }

    object RightArrow : KeyType() {
        override fun toString() = "→"
    }

    object UpArrow : KeyType() {
        override fun toString() = "↑"
    }

    object DownArrow : KeyType() {
        override fun toString() = "↓"
    }

    data object Cut : KeyType()

    data object Copy : KeyType()

    data object Paste : KeyType()

    object SelectAll : KeyType() {
        override fun toString() = "Select All"
    }

    object HideKeyboard : KeyType() {
        override fun toString() = "⌨"
    }

    object SwitchToNextInput : KeyType() {
        override fun toString() = "⇥"
    }

    data class Special(val symbol: String) : KeyType() {
        override fun toString() = symbol
    }

    object SwitchToSymbols : KeyType() {
        override fun toString() = "?123"
    }

    object SwitchToSymbolsOne : KeyType() {
        override fun toString() = "123"
    }

    object SwitchToSymbolsTwo : KeyType() {
        override fun toString() = "#+="
    }

    object SwitchToAlphabetic : KeyType() {
        override fun toString() = "ABC"
    }

    object SwitchToMenu : KeyType() {
        override fun toString() = "⋮"
    }

    object SwitchToEdit : KeyType() {
        override fun toString() = "Edit"
    }

    object CloseMenu : KeyType() {
        override fun toString() = "⨉"
    }
}

enum class KeyboardLayoutType {
    AlphabeticLower, AlphabeticUpper, SymbolsPageOne, SymbolsPageTwo, NumPad, Menu, Edit
}

enum class KeyboardLayoutState {
    Lower, Shift, Caps
}

interface KeyboardLayoutListener {
    fun onLayoutChanged(layoutType: KeyboardLayoutType)
}

object KeyboardLayoutManager {
    var listener: KeyboardLayoutListener? = null

    private var currentLayoutType: KeyboardLayoutType = KeyboardLayoutType.AlphabeticLower
    private var previousLayoutType: KeyboardLayoutType = KeyboardLayoutType.AlphabeticLower

    var currentLayoutState: KeyboardLayoutState = KeyboardLayoutState.Lower

    private val alphabeticLowerLayout = listOf(
        listOf(
            KeyType.Tab,
            KeyType.Character("q"),
            KeyType.Character("w"),
            KeyType.Character("e"),
            KeyType.Character("r"),
            KeyType.Character("t"),
            KeyType.Character("y"),
            KeyType.Character("u"),
            KeyType.Character("i"),
            KeyType.Character("o"),
            KeyType.Character("p")
        ),
        listOf(
            KeyType.Character("a"),
            KeyType.Character("s"),
            KeyType.Character("d"),
            KeyType.Character("f"),
            KeyType.Character("g"),
            KeyType.Character("h"),
            KeyType.Character("j"),
            KeyType.Character("k"),
            KeyType.Character("l"),
            KeyType.Return
        ),
        listOf(
            KeyType.ShiftCaps,
            KeyType.Character("z"),
            KeyType.Character("x"),
            KeyType.Character("c"),
            KeyType.Character("v"),
            KeyType.Character("b"),
            KeyType.Character("n"),
            KeyType.Character("m"),
            KeyType.DeleteWord,
            KeyType.Backspace
        ),
        listOf(
            KeyType.SwitchToSymbols,
            KeyType.Space,
            KeyType.Special("."),
            KeyType.Clear,
            KeyType.SwitchToMenu,
            KeyType.HideKeyboard
        )
    )

    private val alphabeticUpperLayout = alphabeticLowerLayout.map { row ->
        row.map { key ->
            when (key) {
                is KeyType.Character -> KeyType.Character(key.char.uppercase())
                else -> key
            }
        }
    }

    private val symbolsOneLayout = listOf(
        listOf(
            KeyType.Special("1"),
            KeyType.Special("2"),
            KeyType.Special("3"),
            KeyType.Special("4"),
            KeyType.Special("5"),
            KeyType.Special("6"),
            KeyType.Special("7"),
            KeyType.Special("8"),
            KeyType.Special("9"),
            KeyType.Special("0")
        ),
        listOf(
            KeyType.Special("-"),
            KeyType.Special("/"),
            KeyType.Special(":"),
            KeyType.Special(";"),
            KeyType.Special("("),
            KeyType.Special(")"),
            KeyType.Special("$"),
            KeyType.Special("&"),
            KeyType.Special("@"),
            KeyType.Special("\"")
        ),
        listOf(
            KeyType.SwitchToSymbolsTwo,
            KeyType.Special("."),
            KeyType.Special(","),
            KeyType.Special("?"),
            KeyType.Special("!"),
            KeyType.Special("'"),
            KeyType.DeleteWord,
            KeyType.Backspace
        ),
        listOf(
            KeyType.SwitchToAlphabetic,
            KeyType.Space,
            KeyType.ImeAction,
            KeyType.SwitchToMenu,
            KeyType.HideKeyboard
        )
    )

    private val symbolsTwoLayout = listOf(
        listOf(
            KeyType.Special("["),
            KeyType.Special("]"),
            KeyType.Special("{"),
            KeyType.Special("}"),
            KeyType.Special("#"),
            KeyType.Special("%"),
            KeyType.Special("^"),
            KeyType.Special("*"),
            KeyType.Special("+"),
            KeyType.Special("=")
        ),
        listOf(
            KeyType.Special("_"),
            KeyType.Special("\\"),
            KeyType.Special("|"),
            KeyType.Special("~"),
            KeyType.Special("<"),
            KeyType.Special(">"),
            KeyType.Special("€"),
            KeyType.Special("£"),
            KeyType.Special("¥"),
            KeyType.Special("•")
        ),
        listOf(
            KeyType.SwitchToSymbolsOne,
            KeyType.Special("."),
            KeyType.Special(","),
            KeyType.Special("?"),
            KeyType.Special("!"),
            KeyType.Special("'"),
            KeyType.DeleteWord,
            KeyType.Backspace
        ),
        listOf(
            KeyType.SwitchToAlphabetic,
            KeyType.Space,
            KeyType.ImeAction,
            KeyType.SwitchToMenu,
            KeyType.HideKeyboard
        )
    )

    private val numPad = listOf(
        listOf(
            KeyType.Character("1"),
            KeyType.Character("2"),
            KeyType.Character("3")
        ),
        listOf(
            KeyType.Character("4"),
            KeyType.Character("5"),
            KeyType.Character("6")
        ),
        listOf(
            KeyType.Character("7"),
            KeyType.Character("8"),
            KeyType.Character("9")
        ),
        listOf(
            KeyType.Character("."),
            KeyType.Character("0"),
            KeyType.Backspace,
            KeyType.ImeAction,
            KeyType.HideKeyboard
        )
    )

    private val menuLayout = listOf(
        listOf(
            KeyType.CloseMenu,
            KeyType.SwitchToNextInput,
            KeyType.SwitchToEdit
        ),
        listOf(
            KeyType.SwitchToAlphabetic,
            KeyType.SwitchToSymbols,
            KeyType.SwitchToSymbolsOne,
            KeyType.SwitchToSymbolsTwo
        ),
        listOf(
            KeyType.ImeAction,
            KeyType.HideKeyboard
        )
    )

    private val editLayout = listOf(
        listOf(
            KeyType.LeftArrow,
            KeyType.RightArrow,
            KeyType.UpArrow,
            KeyType.DownArrow
        ),
        listOf(
            KeyType.Cut,
            KeyType.Copy,
            KeyType.Paste,
            KeyType.SelectAll
        ),
        listOf(
            KeyType.SwitchToAlphabetic,
            KeyType.SwitchToSymbols,
            KeyType.SwitchToSymbolsOne,
            KeyType.SwitchToSymbolsTwo
        ),
        listOf(
            KeyType.ImeAction,
            KeyType.HideKeyboard
        )
    )

    private val layouts = mapOf(
        KeyboardLayoutType.AlphabeticLower to alphabeticLowerLayout,
        KeyboardLayoutType.AlphabeticUpper to alphabeticUpperLayout,
        KeyboardLayoutType.SymbolsPageOne to symbolsOneLayout,
        KeyboardLayoutType.SymbolsPageTwo to symbolsTwoLayout,
        KeyboardLayoutType.NumPad to numPad,
        KeyboardLayoutType.Menu to menuLayout,
        KeyboardLayoutType.Edit to editLayout
    )

    val currentLayout: List<List<KeyType>>
        get() = layouts[currentLayoutType] ?: listOf()

    fun switchLayout(layoutType: KeyboardLayoutType) {
        previousLayoutType = currentLayoutType
        currentLayoutType = layoutType
        listener?.onLayoutChanged(layoutType)
    }

    fun switchToPreviousLayout() {
        switchLayout(previousLayoutType)
    }

    fun toggleState() {
        currentLayoutState = when (currentLayoutState) {
            KeyboardLayoutState.Lower -> KeyboardLayoutState.Shift
            KeyboardLayoutState.Shift -> KeyboardLayoutState.Caps
            KeyboardLayoutState.Caps -> KeyboardLayoutState.Lower
        }
        switchLayout(if (currentLayoutState == KeyboardLayoutState.Lower) KeyboardLayoutType.AlphabeticLower else KeyboardLayoutType.AlphabeticUpper)
        listener?.onLayoutChanged(currentLayoutType)
    }

    fun setLayoutState(state: KeyboardLayoutState) {
        currentLayoutState = state
        if (currentLayoutType == KeyboardLayoutType.AlphabeticLower || currentLayoutType == KeyboardLayoutType.AlphabeticUpper) {
            switchLayout(if (currentLayoutState == KeyboardLayoutState.Lower) KeyboardLayoutType.AlphabeticLower else KeyboardLayoutType.AlphabeticUpper)
        }
        listener?.onLayoutChanged(currentLayoutType)
    }

    fun isAlphabeticLayout(): Boolean {
        return currentLayoutType == KeyboardLayoutType.AlphabeticLower || currentLayoutType == KeyboardLayoutType.AlphabeticUpper
    }
}