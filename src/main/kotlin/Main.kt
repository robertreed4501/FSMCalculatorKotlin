fun main() {
    State.operand = OperandState()
    State.operation = OperationState()
    State.calculate = CalculateState()
    State.current = State.operand

    val statement = "39 + 44 / 2.48 - -12 + 4 * 10 = "
    var index = 0
    while (State.current !== State.calculate) {
        index = State.current!!.read(statement, index)
        State.current!!.updateState(statement, index)
    }
    val answer = ElementStack.instance!!.calculate()
    println("$statement $answer")

}


internal abstract class CalcElement
internal class ElementStack private constructor() {
    private val stack: MutableList<CalcElement>
    fun add(element: CalcElement) {
        stack.add(element)
    }

    fun calculate(): String {
        var maxPrecedence: Int
        var nextIndex: Int
        while (stack.size > 1) {
            maxPrecedence = maxPrec
            nextIndex = nextIndex(maxPrec)
            operate(nextIndex)
        }
        var answer = (stack[0] as Operand).number.toString()
        if (answer.endsWith(".0")) answer = answer.substring(0, answer.length - 2)
        stack.removeAt(0)
        return answer
    }

    private fun nextIndex(maxPrec: Int): Int {
        var i = 1
        while (i < stack.size) {
            if ((stack[i] as Operator).precedence == maxPrec) {
                return i
            }
            i += 2
        }
        return -1
    }

    private fun operate(index: Int) {
        when ((stack[index] as Operator).opType) {
            OperatorType.PLUS -> plus((stack[index - 1] as Operand).number, (stack[index + 1] as Operand).number, index)
            OperatorType.MINUS -> minus(
                (stack[index - 1] as Operand).number,
                (stack[index + 1] as Operand).number,
                index
            )
            OperatorType.TIMES -> times(
                (stack[index - 1] as Operand).number,
                (stack[index + 1] as Operand).number,
                index
            )
            OperatorType.DIVIDED_BY -> dividedBy(
                (stack[index - 1] as Operand).number,
                (stack[index + 1] as Operand).number,
                index
            )
        }
    }

    private fun dividedBy(num1: Double, num2: Double, index: Int) {
        val result = num1 / num2
        stack[index] = Operand(result)
        stack.removeAt(index - 1)
        stack.removeAt(index)
    }

    private fun times(num1: Double, num2: Double, index: Int) {
        val result = num1 * num2
        stack[index] = Operand(result)
        stack.removeAt(index - 1)
        stack.removeAt(index)
    }

    private fun minus(num1: Double, num2: Double, index: Int) {
        val result = num1 - num2
        stack[index] = Operand(result)
        stack.removeAt(index - 1)
        stack.removeAt(index)
    }

    private fun plus(num1: Double, num2: Double, index: Int) {
        val result = num1 + num2
        stack[index] = Operand(result)
        stack.removeAt(index - 1)
        stack.removeAt(index)
    }

    private val maxPrec: Int
        get() {
            var maxPrec = 0
            for (i in stack.indices) {
                if (stack[i].javaClass == Operator::class.java) {
                    val currPrec = (stack[i] as Operator).precedence
                    if (maxPrec < currPrec) {
                        maxPrec = currPrec
                    }
                }
            }
            return maxPrec
        }


    companion object {
        private var stackInstance: ElementStack? = null
        val instance: ElementStack?
            get() {
                if (stackInstance == null) stackInstance = ElementStack()
                return stackInstance
            }
    }

    init {
        stack = ArrayList()
    }
}

internal class Operand(val number: Double) : CalcElement()

internal class Operator(var opType: OperatorType) : CalcElement() {
    var precedence = 0

    init {
        precedence = when (opType) {
            OperatorType.PLUS, OperatorType.MINUS -> 1
            OperatorType.TIMES, OperatorType.DIVIDED_BY -> 2
            OperatorType.NOT_OP -> -1
        }
    }
}

internal enum class OperatorType {
    PLUS, MINUS, TIMES, DIVIDED_BY, NOT_OP;

    companion object {
        fun parseOperator(str: String): OperatorType {
            return when (str.trim { it <= ' ' }) {
                "+" -> PLUS
                "-" -> MINUS
                "*" -> TIMES
                "/" -> DIVIDED_BY
                else -> NOT_OP
            }
        }
    }
}

internal class OperandState : State() {
    override fun read(statement: String, index: Int): Int {
        val nextSpace = findNextWhitespace(statement, index)
        val number = statement.substring(index, nextSpace).trim { it <= ' ' }
        ElementStack.instance!!.add(Operand(number.toDouble()))
        return nextSpace + 1
    }
}

internal class OperationState : State() {
    override fun read(statement: String, index: Int): Int {
        val nextSpace = findNextWhitespace(statement, index)
        val nextSegment = statement.substring(index, nextSpace)
        ElementStack.instance!!.add(Operator(OperatorType.parseOperator(nextSegment)))
        return nextSpace + 1
    }
}

internal class CalculateState : State()
internal abstract class State {
    open fun read(statement: String, index: Int): Int {
        return index
    }

    fun updateState(statement: String, index: Int) {
        if (statement.substring(index).trim { it <= ' ' } == "=") {
            current = calculate
            return
        }
        val nextSpace = findNextWhitespace(statement, index)
        if (nextSpace == -1) {
            current = calculate
            return
        }

        val charAtIndex = statement[index]
        if (charAtIndex == '-') {
            current = if (statement[index + 1] == ' ') {
                operation
            } else operand
            return
        }
        current = if (charAtIndex == '+' || charAtIndex == '*' || charAtIndex == '/') operation
        else if (statement[index] == '=') calculate else operand
        return
    }

    fun findNextWhitespace(statement: String, index: Int): Int {
        for (i in index until statement.length) {
            if (statement[i] == ' ') return i
        }
        return -1
    }

    companion object {
        var operand: State? = null
        var operation: State? = null
        var calculate: State? = null
        var current: State? = null
    }
}