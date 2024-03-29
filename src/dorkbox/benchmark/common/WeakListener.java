/*
 * Copyright 2012 Benjamin Diedrichsen
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dorkbox.benchmark.common;

import com.adamtaft.eb.EventHandler;
import com.google.common.eventbus.Subscribe;
import com.mycila.event.Event;
import com.mycila.event.Subscriber;
import dorkbox.messagebus.annotations.Handler;
import dorkbox.messagebus.annotations.Listener;
import dorkbox.messagebus.annotations.References;
import org.bushe.swing.event.annotation.EventSubscriber;


@Listener(references = References.Weak)
public class WeakListener {
    // every event of type TestEvent or any subtype will be delivered to this listener
    @net.engio.mbassy.listener.Handler
    @Handler
    @Subscribe
    @EventHandler
    @EventSubscriber
    public void handleTestEvent(Object event) { }

    public static class Mycila {

        public static
        Subscriber<Object> testEventSubscriber(){
            return new Subscriber<Object>() {
                @Override
                public void onEvent(Event<Object> testEventEvent) throws Exception {
                }
            };
        }

        // analogous to

        @com.mycila.event.annotation.Subscribe(topics = "all", eventType = Object.class)
        public void handleTestEvent(Event<Object> event) {
        }
    }
}
