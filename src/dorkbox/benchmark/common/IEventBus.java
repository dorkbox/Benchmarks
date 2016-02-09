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
 *
 *
 * Copyright 2016 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dorkbox.benchmark.common;

import com.adamtaft.eb.EventBusService;
import com.mycila.event.Dispatcher;
import com.mycila.event.Dispatchers;
import com.mycila.event.Topic;
import com.mycila.event.Topics;
import dorkbox.messagebus.MessageBus;
import net.engio.mbassy.bus.MBassador;
import org.bushe.swing.event.annotation.AnnotationProcessor;

/**
 * Adapter interface to plug in different event bus systems
 */
public interface IEventBus {

    void shutdown();
    void publish(Object event);
    void publishAsync(Object event);
    void subscribe(Object listener);
    boolean unsubscribe(Object listener);
    boolean hasPending();
    String getName();

    class GuavaBusAdapter implements IEventBus {
        private com.google.common.eventbus.EventBus delegate = new com.google.common.eventbus.EventBus();

        @Override
        public void subscribe(Object listener) {
            delegate.register(listener);
        }

        @Override
        public void publish(Object event) {
           delegate.post(event);
        }

        @Override
        public void publishAsync(Object event) {
            throw new RuntimeException("no such method 'publishAsync()'");
        }

        @Override
        public boolean unsubscribe(Object listener) {
            try{
                delegate.unregister(listener);
                return true;
            }catch (Exception e){
                return false;
            }
        }

        @Override
        public boolean hasPending() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getName() {
            return "Guava Event Bus";
        }

        @Override
        public void shutdown() {
        }
    }

    class MbassadorAdapter implements IEventBus {
        private MBassador delegate = new MBassador();

        @Override
        public void publish(Object event) {
            delegate.publish(event);
        }

        @Override
        public void publishAsync(Object event) {
            delegate.publishAsync(event);
        }

        @Override
        public void subscribe(Object listener) {
            delegate.subscribe(listener);
        }

        @Override
        public boolean unsubscribe(Object listener) {
           return delegate.unsubscribe(listener);
        }

        @Override
        public boolean hasPending() {
            return delegate.hasPendingMessages();
        }

        @Override
        public String getName() {
            return "Mbassador";
        }

        @Override
        public void shutdown() {
        }
    }

    class MessageBusAdapter implements IEventBus {
        public MessageBus delegate = new MessageBus();

        @Override
        public void publish(Object event) {
            delegate.publish(event);
        }

        @Override
        public void publishAsync(Object event) {
            delegate.publishAsync(event);
        }

        @Override
        public void subscribe(Object listener) {
            delegate.subscribe(listener);
        }

        @Override
        public boolean unsubscribe(Object listener) {
            delegate.unsubscribe(listener);
            return true;
        }

        @Override
        public boolean hasPending() {
            return delegate.hasPendingMessages();
        }

        @Override
        public String getName() {
            return "MessageBus";
        }

        @Override
        public void shutdown() {
            delegate.shutdown();
        }
    }


    class SimpleBusAdapter implements IEventBus {
        @Override
        public void publish(Object event) {
            EventBusService.publish(event);
        }

        @Override
        public void publishAsync(Object event) {
            throw new RuntimeException("no such method 'publishAsync()'");
        }

        @Override
        public void subscribe(Object listener) {
            EventBusService.subscribe(listener);
        }

        @Override
        public boolean unsubscribe(Object listener) {
            EventBusService.unsubscribe(listener);
            return true; // TODO
        }

        @Override
        public boolean hasPending() {
            return EventBusService.hasPendingEvents();
        }

        @Override
        public String getName() {
            return "Simplebus";
        }

        @Override
        public void shutdown() {
        }
    }


    class EventBusAdapter implements IEventBus {
        @Override
        public void publish(Object event) {
            org.bushe.swing.event.EventBus.publish(event);
        }

        @Override
        public void publishAsync(Object event) {
            throw new RuntimeException("no such method 'publishAsync()'");
        }

        @Override
        public void subscribe(Object listener) {
            AnnotationProcessor.process(listener);
        }

        @Override
        public boolean unsubscribe(Object listener) {
            AnnotationProcessor.unprocess(listener);
            return true; //TODO
        }

        @Override
        public boolean hasPending() {
           return false;
        }

        @Override
        public String getName() {
            return "Eventbus";
        }

        @Override
        public void shutdown() {
        }
    }

    class MycilaAdapter implements IEventBus {
        private Dispatcher bus = Dispatchers.synchronousSafe();

        @Override
        public void publish(Object event) {
            bus.publish(Topic.topic("any"), event);
        }

        @Override
        public void publishAsync(Object event) {
            throw new RuntimeException("no such method 'publishAsync()'");
        }

        @Override
        public void subscribe(Object listener){
            bus.subscribe(Topics.any(), Object.class, StrongListener.Mycila.testEventSubscriber());
        }

        @Override
        public boolean unsubscribe(Object listener) {
            return false;
        }

        @Override
        public boolean hasPending() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getName() {
            return "Mycila";
        }

        @Override
        public void shutdown() {
        }
    }

    class MycilaAdapterAsync implements IEventBus {
        private Dispatcher bus = Dispatchers.asynchronousSafe();

        @Override
        public void publish(Object event) {
            bus.publish(Topic.topic("any"), event);
        }

        @Override
        public void publishAsync(Object event) {
            throw new RuntimeException("no such method 'publishAsync()'");
        }

        @Override
        public void subscribe(Object listener){
            bus.subscribe(Topics.any(), Object.class, StrongListener.Mycila.testEventSubscriber());
        }

        @Override
        public boolean unsubscribe(Object listener) {
            return false;
        }

        @Override
        public boolean hasPending() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getName() {
            return "Mycila";
        }

        @Override
        public void shutdown() {
        }
    }
}
