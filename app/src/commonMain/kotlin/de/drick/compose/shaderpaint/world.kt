package de.drick.compose.shaderpaint

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.math.roundToInt

val persistentWorld = World()

fun Offset.toVector() = "vec2(${x.roundTo(3)}, ${y.roundTo(3)})"

fun Float.roundTo(numFractionDigits: Int) =
    toDouble().roundTo(numFractionDigits)

fun Double.roundTo(numFractionDigits: Int): String {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    val numberTimes = (this * factor).roundToInt()
    val number = (numberTimes / factor).toInt()
    val fraction = (numberTimes - (number * factor)).toInt()
    return "$number.$fraction"
}

fun Float.toShader() = roundTo(3)
fun Double.toShader() = roundTo(3)

fun Color.toVec3() = "vec3(${red.toShader()}, ${green.toShader()}, ${blue.toShader()})"


interface Shape {
    val id: String
    var position: Offset
    var radius: Float
    var smoothness: Float
    var color: Color
    fun instance(posVar: String): String
}

data class Circle(
    override val id: String,
    override var position: Offset,
    override var radius: Float,
    override var smoothness: Float = 10f,
    override var color: Color = Color.White,
): Shape {
    companion object {
        fun shaderDefinition() = """
            float dCircle(in vec2 p, in float r, in float sharpness) {
                float d = length(p) - r + sharpness;
                return smoothstep(sharpness, 0.0, d);
            }
        """.replaceIndent()
    }
    override fun instance(posVar: String): String = """
        dCircle($posVar - ${position.toVector()}, ${radius.toShader()}, ${smoothness.toShader()}) * ${color.toVec3()}
    """.replaceIndent()
}

data class Triangle(
    override val id: String,
    override var position: Offset,
    override var radius: Float,
    override var smoothness: Float = 10f,
    override var color: Color = Color.White,
): Shape {
    companion object {
        fun shaderDefinition() = """
            float dTriangle(in vec2 p, in float r, in float sharpness) {
                const float k = sqrt(3.0);
                p.x = abs(p.x) - r;
                p.y = p.y + r/k;
                if(p.x+k*p.y>0.0) p = vec2(p.x-k*p.y,-k*p.x-p.y)/2.0;
                p.x -= clamp(p.x, -2.0*r, 0.0);
                float d = -length(p)*sign(p.y);
                return smoothstep(sharpness, 0.0, d);
            }
        """.replaceIndent()
    }
    override fun instance(posVar: String): String = """
        dTriangle($posVar - ${position.toVector()}, ${radius.toShader()}, ${smoothness.toShader()}) * ${color.toVec3()}
    """.replaceIndent()
}



class World {
    val shapeList = SnapshotStateList<Shape>()
        .apply {
            add(Circle("Circle 1", Offset(100f, 100f), 100f))
        }

    var frameCounter by mutableStateOf(0)
        private set

    fun invalidate() {
        frameCounter++
    }

    fun addShape(shape: Shape) {
        shapeList.add(shape)
        invalidate()
    }

    fun mainCode(shapeInstances: List<Shape>): String {
        val instances = buildString {
            shapeInstances.forEach { shape ->
                append("    ")
                append("c += ")
                append(shape.instance("p"))
                appendLine(";")
            }
        }
        return """
vec4 main(vec2 fragCoord) {
    vec2 p = fragCoord;
    vec3 c = vec3(0.0);
$instances
    return vec4(c, 1);
}
        """.trim()
    }

    private fun mainShader(
        shapeInstances: List<Shape>
    ): String {
        val shapeClasses = shapeInstances.groupBy { it::class }.map { it.key }.toSet()
        val definitions = buildString {
            if (shapeClasses.contains(Circle::class)) {
                append(Circle.shaderDefinition())
                appendLine()
            }
            if (shapeClasses.contains(Triangle::class)) {
                append(Triangle.shaderDefinition())
            }
        }
        val mainCode = mainCode(shapeInstances)
        return """
$definitions
$mainCode
        """.trim()
    }
    fun searchShape(offset: Offset): Shape? {
        return shapeList
            .map { Pair(it, (it.position - offset).getDistance()) }
            .filter { it.second < it.first.radius + 20f}
            .minByOrNull { it.second }
            ?.first
    }

    fun generateShader() = mainShader(shapeList)
}
