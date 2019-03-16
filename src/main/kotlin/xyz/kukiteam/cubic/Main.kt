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
import xyz.kukiteam.cubic.managers.KeyManager
import xyz.kukiteam.cubic.objects.CallbackParameters
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Predicate


class Main {

    companion object {
        private var window: Long = -1L
        private var red: Float = 0.0f
        private var green: Float = 0.0f
        private var blue: Float = 0.0f

        private var change: Float = 0.1f

        private var hits: Int = 0
        private var add: Boolean = true

        private val releasePredicate: Predicate<CallbackParameters> = Predicate { p -> p.action == GLFW.GLFW_RELEASE }

        @JvmField
        val keyManager = KeyManager()
    }

    fun run() {
        println("Hello LWJGL " + Version.getVersion() + "!")

        initKeyManager()
        init()
        loop()

        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window)
        GLFW.glfwDestroyWindow(window)

        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null)!!.free()
    }

    private fun initKeyManager() {
        keyManager.add(GLFW.GLFW_KEY_ESCAPE, releasePredicate.and { hits < 3}, Consumer {
            ++hits
            println("increased hits")
            GlobalScope.async {
                delay(TimeUnit.SECONDS.toMillis(3))
                println("decreased hits")
                --hits
            }
        }, CallbackParameters(GLFW.GLFW_KEY_ESCAPE, -1, GLFW.GLFW_RELEASE, -1, listOf<Any>(2)))

        keyManager.add(GLFW.GLFW_KEY_TAB, releasePredicate, Consumer {
            add = !add
        }, CallbackParameters(GLFW.GLFW_KEY_TAB, -1, GLFW.GLFW_RELEASE, -1))

        keyManager.add(GLFW.GLFW_KEY_R, releasePredicate, Consumer {
            red = if (add) red + change else red - change
        }, CallbackParameters(GLFW.GLFW_KEY_R, -1, GLFW.GLFW_RELEASE, -1))
        keyManager.add(GLFW.GLFW_KEY_G, releasePredicate, Consumer {
            green = if (add) green + change else green - change
        }, CallbackParameters(GLFW.GLFW_KEY_G, -1, GLFW.GLFW_RELEASE, -1))
        keyManager.add(GLFW.GLFW_KEY_B, releasePredicate, Consumer {
            blue = if (add) blue + change else blue - change
        }, CallbackParameters(GLFW.GLFW_KEY_B, -1, GLFW.GLFW_RELEASE, -1))
        keyManager.add(GLFW.GLFW_KEY_C, releasePredicate, Consumer {
            change = if (add) change + 0.05f else change - 0.05f
        }, CallbackParameters(GLFW.GLFW_KEY_C, -1, GLFW.GLFW_RELEASE, -1))

        keyManager.add(GLFW.GLFW_KEY_UP, releasePredicate, Consumer {
            red   += change
            green += change
            blue  += change
        }, CallbackParameters(GLFW.GLFW_KEY_UP, -1, GLFW.GLFW_RELEASE, -1))
        keyManager.add(GLFW.GLFW_KEY_DOWN, releasePredicate, Consumer {
            red   -= change
            green -= change
            blue  -= change
        }, CallbackParameters(GLFW.GLFW_KEY_DOWN, -1, GLFW.GLFW_RELEASE, -1))

        keyManager.add(GLFW.GLFW_KEY_U, releasePredicate, Consumer {
            val manager = keyManager
            println(keyManager.getBoundEvents(null).size)
            for (boundEvent in keyManager.getBoundEvents(null)) {
                keyManager.remove(boundEvent.expectedCallbackParameters.key, boundEvent)
                println(keyManager.getBoundEvents(null).size)
            }
            println("Unbound all keys")

            keyManager.add(GLFW.GLFW_KEY_R, releasePredicate, Consumer {
                GlobalScope.async {
                    delay(TimeUnit.SECONDS.toMillis(10))
                    for (boundEvent in keyManager.getBoundEvents(GLFW.GLFW_KEY_R)) {
                        keyManager.remove(GLFW.GLFW_KEY_R, boundEvent)
                    }
                    initKeyManager()
                }
            }, CallbackParameters(GLFW.GLFW_KEY_R, -1, GLFW.GLFW_RELEASE, -1))

        }, CallbackParameters(GLFW.GLFW_KEY_U, -1, GLFW.GLFW_RELEASE, -1))

        println("Bound all keys")

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

            val param = CallbackParameters(key, window, action, mods)

            if (keyManager.hasEvents(param))
                keyManager.runEvents(param)

            if (hits >= 2)
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
            GL11.glClearColor(red, green, blue, 0.0f)
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