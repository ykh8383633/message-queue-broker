import org.junit.jupiter.api.Test

class test {

    open class parent{
        open fun a() {
            println("parent A")
            this.b();
        }

        open fun b() {
            println("parent B")
        }
    }

    class child: parent() {
        override fun a() {
            println("child")
            super.a()
        }

        override fun b() {
            println("child B")
        }
    }

    @Test
    fun test() {
        val child = child()

        child.a()
    }
}