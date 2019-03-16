package xyz.kukiteam.cubic

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.GLFW_FALSE
import org.lwjgl.glfw.GLFW.GLFW_TRUE
import org.lwjgl.glfw.GLFW.glfwWindowShouldClose
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import java.util.concurrent.TimeUnit


class Main {

    companion object {
        private var window: Long = -1L
        private var red: Float = 0.0f
        private var green: Float = 0.0f
        private var blue: Float = 0.0f
        private var alpha: Float = 0.0f
        private var change: Float = 0.1f
        private var hits: Int = 0
        private var add: Boolean = true
    }

    fun run() {
        println("Hello LWJGL " + Version.getVersion() + "!")

        init()
        loop()

        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window)
        GLFW.glfwDestroyWindow(window)

        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null)!!.free()
    }

    private fun init() {
        GLFWErrorCallback.createPrint(System.err).set()

        if (!GLFW.glfwInit())
            throw IllegalStateException("Unable to initialize GLFW")

        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable

        // Create the window
        val monitor = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())!!
        window = GLFW.glfwCreateWindow((monitor.width() * 0.85).toInt(), (monitor.height() * 0.85).toInt(), "Hello World!", NULL, NULL)
        if (window == NULL)
            throw RuntimeException("Failed to create the GLFW window")


        GLFW.glfwSetKeyCallback(window) { window, key, scancode, action, mods ->

            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE && hits < 3) {
                ++hits
                println("increased hits")
                GlobalScope.async {
                    delay(TimeUnit.SECONDS.toMillis(3))
                    println("decreased hits")
                    --hits
                }
            }

            if (key == GLFW.GLFW_KEY_KP_ADD && action == GLFW.GLFW_RELEASE) {
                print("changing mode old: $add")
                add = true
                println(" new: $add")
            }

            if (key == GLFW.GLFW_KEY_KP_SUBTRACT && action == GLFW.GLFW_RELEASE) {
                print("changing mode old: $add")
                add = false
                println(" new: $add")
            }

            if (key == GLFW.GLFW_KEY_R && action == GLFW.GLFW_PRESS) {
                print("Changing red old: $red")
                red = if (add) red + change else red - change
                println(" new: $red")
            }

            if (key == GLFW.GLFW_KEY_G && action == GLFW.GLFW_PRESS) {
                print("Changing green old: $green")
                green = if (add) green + change else green - change
                println(" new: $green")
            }

            if (key == GLFW.GLFW_KEY_B && action == GLFW.GLFW_PRESS) {
                print("Changing blue old: $blue")
                blue = if (add) blue + change else blue - change
                println(" new: $blue")
            }

            if (key == GLFW.GLFW_KEY_A && action == GLFW.GLFW_PRESS) {
                print("Changing alpha old: $alpha")
                alpha = if (add) alpha + change else alpha - change
                println(" new: $alpha")
            }

            if (hits == 2)
                GLFW.glfwSetWindowShouldClose(window, true)
        }

        stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            GLFW.glfwGetWindowSize(window, pWidth, pHeight)

            val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())!!

            GLFW.glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            )
        }

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window)

        // Enable v-sync
        GLFW.glfwSwapInterval(1)

        GLFW.glfwShowWindow(window)
    }

    private fun loop() {
        GL.createCapabilities()

        // Set the clear color

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            GL11.glClearColor(red, green, blue, alpha)
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT) // clear the framebuffer

            GLFW.glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            GLFW.glfwPollEvents()
        }
    }

}

fun main() {
    Main().run()
}