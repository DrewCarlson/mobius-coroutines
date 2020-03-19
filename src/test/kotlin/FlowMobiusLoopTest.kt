package drewcarlson.mobius.flow

import com.spotify.mobius.Connection
import com.spotify.mobius.Mobius
import com.spotify.mobius.Next.next
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.lang.Exception

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class FlowMobiusLoopTest {

    private lateinit var loop: FlowMobiusLoop<String, Int>

    @Before
    fun setUp() {
        val factory = Mobius.loop<String, Int, Boolean>(
            { model, event -> next(model + event.toString()) },
            {
                object : Connection<Boolean> {
                    override fun accept(value: Boolean) = Unit
                    override fun dispose() = Unit
                }
            }
        )

        loop = FlowMobiusLoop(factory, "")
    }

    @Test
    fun shouldPropagateIncomingErrorsAsUnrecoverable() = runBlockingTest {
        val input = Channel<Int>()

        val result = async {
            loop(input.consumeAsFlow()).toList()
        }

        input.close(RuntimeException("expected"))

        try {
            result.await()
            fail()
        } catch (e: Exception) {
            assertTrue(e is UnrecoverableIncomingException)
            assertEquals("expected", e.cause?.message)
        }
    }
}
