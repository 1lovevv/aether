package com.aether.mailbox;

import com.aether.message.Message;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MailboxTest {

    @Test
    void testEnqueueAndDequeue() throws InterruptedException {
        Mailbox mailbox = new Mailbox();
        Message msg = new Message() {};

        mailbox.enqueue(msg);
        assertEquals(1, mailbox.size());
        assertFalse(mailbox.isEmpty());

        Message dequeued = mailbox.dequeue();
        assertSame(msg, dequeued);
        assertEquals(0, mailbox.size());
        assertTrue(mailbox.isEmpty());
    }

    @Test
    void testUnboundedMailbox() throws InterruptedException {
        Mailbox mailbox = new Mailbox();
        assertEquals(Integer.MAX_VALUE, mailbox.getCapacity());

        for (int i = 0; i < 1000; i++) {
            mailbox.enqueue(new Message() {});
        }
        assertEquals(1000, mailbox.size());
    }

    @Test
    void testBoundedMailboxCapacity() {
        Mailbox mailbox = new Mailbox(5);
        assertEquals(5, mailbox.getCapacity());
    }

    @Test
    void testBoundedMailboxBlocking() throws InterruptedException {
        Mailbox mailbox = new Mailbox(2);
        Message msg1 = new Message() {};
        Message msg2 = new Message() {};
        Message msg3 = new Message() {};

        mailbox.enqueue(msg1);
        mailbox.enqueue(msg2);
        assertEquals(2, mailbox.size());

        CountDownLatch producerStarted = new CountDownLatch(1);
        CountDownLatch producerBlocked = new CountDownLatch(1);
        AtomicReference<Exception> exceptionRef = new AtomicReference<>();

        Thread producer = new Thread(() -> {
            try {
                producerStarted.countDown();
                mailbox.enqueue(msg3);
                producerBlocked.countDown();
            } catch (InterruptedException e) {
                exceptionRef.set(e);
            }
        });

        producer.start();
        assertTrue(producerStarted.await(1, TimeUnit.SECONDS));

        Thread.sleep(100);
        assertEquals(2, mailbox.size());

        Message dequeued = mailbox.dequeue();
        assertSame(msg1, dequeued);

        assertTrue(producerBlocked.await(1, TimeUnit.SECONDS));
        assertEquals(2, mailbox.size());

        producer.join(1000);
        assertFalse(producer.isAlive());
    }

    @Test
    void testFifoOrder() throws InterruptedException {
        Mailbox mailbox = new Mailbox(10);
        Message msg1 = new Message() {};
        Message msg2 = new Message() {};
        Message msg3 = new Message() {};

        mailbox.enqueue(msg1);
        mailbox.enqueue(msg2);
        mailbox.enqueue(msg3);

        assertSame(msg1, mailbox.dequeue());
        assertSame(msg2, mailbox.dequeue());
        assertSame(msg3, mailbox.dequeue());
    }

    @Test
    void testInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new Mailbox(0));
        assertThrows(IllegalArgumentException.class, () -> new Mailbox(-1));
    }
}
