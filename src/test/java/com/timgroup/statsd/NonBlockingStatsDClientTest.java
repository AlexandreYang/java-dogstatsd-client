package com.timgroup.statsd;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.contrib.java.lang.system.EnvironmentVariables;


public class NonBlockingStatsDClientTest {

    private static final int STATSD_SERVER_PORT = 17254;
    private static final NonBlockingStatsDClient client = new NonBlockingStatsDClient("my.prefix", "localhost", STATSD_SERVER_PORT);
    private static DummyStatsDServer server;

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @BeforeClass
    public static void start() throws IOException {
        server = new DummyStatsDServer(STATSD_SERVER_PORT);
    }

    @AfterClass
    public static void stop() throws Exception {
        try {
            client.stop();
            server.close();
        } catch (java.io.IOException e) {
            return;
        }
    }

    @After
    public void clear() {
        server.clear();
    }

    @Test(timeout = 5000L)
    public void sends_counter_value_to_statsd() throws Exception {


        client.count("mycount", 24);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mycount:24|c"));
    }

    @Test(timeout = 5000L)
    public void sends_counter_value_with_sample_rate_to_statsd() throws Exception {

        client.count("mycount", 24, 1);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mycount:24|c|@1.000000"));
    }

    @Test(timeout = 5000L)
    public void sends_counter_value_to_statsd_with_null_tags() throws Exception {


        client.count("mycount", 24, (java.lang.String[]) null);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mycount:24|c"));
    }

    @Test(timeout = 5000L)
    public void sends_counter_value_to_statsd_with_empty_tags() throws Exception {


        client.count("mycount", 24);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mycount:24|c"));
    }

    @Test(timeout = 5000L)
    public void sends_counter_value_to_statsd_with_tags() throws Exception {


        client.count("mycount", 24, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mycount:24|c|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_counter_value_with_sample_rate_to_statsd_with_tags() throws Exception {


        client.count("mycount", 24, 1, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mycount:24|c|@1.000000|#baz,foo:bar"));
    }


    @Test(timeout = 5000L)
    public void sends_counter_increment_to_statsd() throws Exception {


        client.incrementCounter("myinc");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.myinc:1|c"));
    }

    @Test(timeout = 5000L)
    public void sends_counter_increment_to_statsd_with_tags() throws Exception {


        client.incrementCounter("myinc", "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.myinc:1|c|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_counter_increment_with_sample_rate_to_statsd_with_tags() throws Exception {


        client.incrementCounter("myinc", 1, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.myinc:1|c|@1.000000|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_counter_decrement_to_statsd() throws Exception {


        client.decrementCounter("mydec");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mydec:-1|c"));
    }

    @Test(timeout = 5000L)
    public void sends_counter_decrement_to_statsd_with_tags() throws Exception {


        client.decrementCounter("mydec", "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mydec:-1|c|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_counter_decrement_with_sample_rate_to_statsd_with_tags() throws Exception {


        client.decrementCounter("mydec", 1, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mydec:-1|c|@1.000000|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_gauge_to_statsd() throws Exception {


        client.recordGaugeValue("mygauge", 423);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mygauge:423|g"));
    }

    @Test(timeout = 5000L)
    public void sends_gauge_with_sample_rate_to_statsd() throws Exception {


        client.recordGaugeValue("mygauge", 423, 1);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mygauge:423|g|@1.000000"));
    }

    @Test(timeout = 5000L)
    public void sends_large_double_gauge_to_statsd() throws Exception {


        client.recordGaugeValue("mygauge", 123456789012345.67890);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mygauge:123456789012345.67|g"));
    }

    @Test(timeout = 5000L)
    public void sends_exact_double_gauge_to_statsd() throws Exception {


        client.recordGaugeValue("mygauge", 123.45678901234567890);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mygauge:123.456789|g"));
    }

    @Test(timeout = 5000L)
    public void sends_double_gauge_to_statsd() throws Exception {


        client.recordGaugeValue("mygauge", 0.423);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mygauge:0.423|g"));
    }

    @Test(timeout = 5000L)
    public void sends_gauge_to_statsd_with_tags() throws Exception {


        client.recordGaugeValue("mygauge", 423, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mygauge:423|g|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_gauge_with_sample_rate_to_statsd_with_tags() throws Exception {


        client.recordGaugeValue("mygauge", 423, 1, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mygauge:423|g|@1.000000|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_double_gauge_to_statsd_with_tags() throws Exception {


        client.recordGaugeValue("mygauge", 0.423, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mygauge:0.423|g|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_histogram_to_statsd() throws Exception {

        client.recordHistogramValue("myhistogram", 423);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.myhistogram:423|h"));
    }

    @Test(timeout = 5000L)
    public void sends_double_histogram_to_statsd() throws Exception {


        client.recordHistogramValue("myhistogram", 0.423);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.myhistogram:0.423|h"));
    }

    @Test(timeout = 5000L)
    public void sends_histogram_to_statsd_with_tags() throws Exception {


        client.recordHistogramValue("myhistogram", 423, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.myhistogram:423|h|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_histogram_with_sample_rate_to_statsd_with_tags() throws Exception {


        client.recordHistogramValue("myhistogram", 423, 1, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.myhistogram:423|h|@1.000000|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_double_histogram_to_statsd_with_tags() throws Exception {


        client.recordHistogramValue("myhistogram", 0.423, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.myhistogram:0.423|h|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_double_histogram_with_sample_rate_to_statsd_with_tags() throws Exception {


        client.recordHistogramValue("myhistogram", 0.423, 1, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.myhistogram:0.423|h|@1.000000|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_distribtuion_to_statsd() throws Exception {

        client.recordDistributionValue("mydistribution", 423);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mydistribution:423|d"));
    }

    @Test(timeout = 5000L)
    public void sends_double_distribution_to_statsd() throws Exception {


        client.recordDistributionValue("mydistribution", 0.423);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mydistribution:0.423|d"));
    }

    @Test(timeout = 5000L)
    public void sends_distribution_to_statsd_with_tags() throws Exception {


        client.recordDistributionValue("mydistribution", 423, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mydistribution:423|d|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_distribution_with_sample_rate_to_statsd_with_tags() throws Exception {


        client.recordDistributionValue("mydistribution", 423, 1, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mydistribution:423|d|@1.000000|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_double_distribution_to_statsd_with_tags() throws Exception {


        client.recordDistributionValue("mydistribution", 0.423, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mydistribution:0.423|d|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_double_distribution_with_sample_rate_to_statsd_with_tags() throws Exception {


        client.recordDistributionValue("mydistribution", 0.423, 1, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mydistribution:0.423|d|@1.000000|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_timer_to_statsd() throws Exception {


        client.recordExecutionTime("mytime", 123);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mytime:123|ms"));
    }

    /**
     * A regression test for <a href="https://github.com/indeedeng/java-dogstatsd-client/issues/3">this i18n number formatting bug</a>
     *
     * @throws Exception
     */
    @Test
    public void
    sends_timer_to_statsd_from_locale_with_unamerican_number_formatting() throws Exception {

        Locale originalDefaultLocale = Locale.getDefault();

        // change the default Locale to one that uses something other than a '.' as the decimal separator (Germany uses a comma)
        Locale.setDefault(Locale.GERMANY);

        try {


            client.recordExecutionTime("mytime", 123, "foo:bar", "baz");
            server.waitForMessage();

            assertThat(server.messagesReceived(), contains("my.prefix.mytime:123|ms|#baz,foo:bar"));
        } finally {
            // reset the default Locale in case changing it has side-effects
            Locale.setDefault(originalDefaultLocale);
        }
    }


    @Test(timeout = 5000L)
    public void sends_timer_to_statsd_with_tags() throws Exception {


        client.recordExecutionTime("mytime", 123, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mytime:123|ms|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_timer_with_sample_rate_to_statsd_with_tags() throws Exception {


        client.recordExecutionTime("mytime", 123, 1, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mytime:123|ms|@1.000000|#baz,foo:bar"));
    }


    @Test(timeout = 5000L)
    public void sends_gauge_mixed_tags() throws Exception {

        final NonBlockingStatsDClient empty_prefix_client = new NonBlockingStatsDClient("my.prefix", "localhost", STATSD_SERVER_PORT, Integer.MAX_VALUE, "instance:foo", "app:bar");
        empty_prefix_client.gauge("value", 423, "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.value:423|g|#app:bar,instance:foo,baz"));
    }

    @Test(timeout = 5000L)
    public void sends_gauge_mixed_tags_with_sample_rate() throws Exception {

        final NonBlockingStatsDClient empty_prefix_client = new NonBlockingStatsDClient("my.prefix", "localhost", STATSD_SERVER_PORT, Integer.MAX_VALUE, "instance:foo", "app:bar");
        empty_prefix_client.gauge("value", 423, 1, "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.value:423|g|@1.000000|#app:bar,instance:foo,baz"));
    }

    @Test(timeout = 5000L)
    public void sends_gauge_constant_tags_only() throws Exception {

        final NonBlockingStatsDClient empty_prefix_client = new NonBlockingStatsDClient("my.prefix", "localhost", STATSD_SERVER_PORT, Integer.MAX_VALUE, "instance:foo", "app:bar");
        empty_prefix_client.gauge("value", 423);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.value:423|g|#app:bar,instance:foo"));
    }

    @Test(timeout = 5000L)
    public void sends_gauge_entityID_from_env() throws Exception {
        final String entity_value =  "foo-entity";
        environmentVariables.set(NonBlockingStatsDClient.DD_ENTITY_ID_ENV_VAR, entity_value);
        final NonBlockingStatsDClient client = new NonBlockingStatsDClient("my.prefix", "localhost", STATSD_SERVER_PORT, Integer.MAX_VALUE);
        client.gauge("value", 423);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.value:423|g|#dd.internal.entity_id:foo-entity"));
    }

    @Test(timeout = 5000L)
    public void sends_gauge_entityID_from_args() throws Exception {
        final String entity_value =  "foo-entity";
        environmentVariables.set(NonBlockingStatsDClient.DD_ENTITY_ID_ENV_VAR, entity_value);
        final NonBlockingStatsDClient client = new NonBlockingStatsDClient("my.prefix", "localhost", STATSD_SERVER_PORT, Integer.MAX_VALUE, null, null, entity_value+"-arg");
        client.gauge("value", 423);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.value:423|g|#dd.internal.entity_id:foo-entity-arg"));
    }


    @Test(timeout = 5000L)
    public void init_client_from_env_vars() throws Exception {
        final String entity_value =  "foo-entity";
        environmentVariables.set(NonBlockingStatsDClient.DD_DOGSTATSD_PORT_ENV_VAR, "17254");
        environmentVariables.set(NonBlockingStatsDClient.DD_AGENT_HOST_ENV_VAR, "localhost");
        final NonBlockingStatsDClient client = new NonBlockingStatsDClient("my.prefix");
        client.gauge("value", 423);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.value:423|g"));
    }

    @Test(timeout = 5000L)
    public void sends_gauge_empty_prefix() throws Exception {

        final NonBlockingStatsDClient empty_prefix_client = new NonBlockingStatsDClient("", "localhost", STATSD_SERVER_PORT);
        empty_prefix_client.gauge("top.level.value", 423);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("top.level.value:423|g"));
    }

    @Test(timeout = 5000L)
    public void sends_gauge_null_prefix() throws Exception {

        final NonBlockingStatsDClient null_prefix_client = new NonBlockingStatsDClient(null, "localhost", STATSD_SERVER_PORT);
        null_prefix_client.gauge("top.level.value", 423);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("top.level.value:423|g"));
    }

    @Test(timeout = 5000L)
    public void sends_event() throws Exception {

        final Event event = Event.builder()
                .withTitle("title1")
                .withText("text1\nline2")
                .withDate(1234567000)
                .withHostname("host1")
                .withPriority(Event.Priority.LOW)
                .withAggregationKey("key1")
                .withAlertType(Event.AlertType.ERROR)
                .build();
        client.recordEvent(event);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("_e{16,12}:my.prefix.title1|text1\\nline2|d:1234567|h:host1|k:key1|p:low|t:error"));
    }

    @Test(timeout = 5000L)
    public void sends_partial_event() throws Exception {

        final Event event = Event.builder()
                .withTitle("title1")
                .withText("text1")
                .withDate(1234567000)
                .build();
        client.recordEvent(event);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("_e{16,5}:my.prefix.title1|text1|d:1234567"));
    }

    @Test(timeout = 5000L)
    public void sends_event_with_tags() throws Exception {

        final Event event = Event.builder()
                .withTitle("title1")
                .withText("text1")
                .withDate(1234567000)
                .withHostname("host1")
                .withPriority(Event.Priority.LOW)
                .withAggregationKey("key1")
                .withAlertType(Event.AlertType.ERROR)
                .build();
        client.recordEvent(event, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("_e{16,5}:my.prefix.title1|text1|d:1234567|h:host1|k:key1|p:low|t:error|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_partial_event_with_tags() throws Exception {

        final Event event = Event.builder()
                .withTitle("title1")
                .withText("text1")
                .withDate(1234567000)
                .build();
        client.recordEvent(event, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("_e{16,5}:my.prefix.title1|text1|d:1234567|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_event_empty_prefix() throws Exception {

        final NonBlockingStatsDClient empty_prefix_client = new NonBlockingStatsDClient("", "localhost", STATSD_SERVER_PORT);
        final Event event = Event.builder()
                .withTitle("title1")
                .withText("text1")
                .withDate(1234567000)
                .withHostname("host1")
                .withPriority(Event.Priority.LOW)
                .withAggregationKey("key1")
                .withAlertType(Event.AlertType.ERROR)
                .build();
        empty_prefix_client.recordEvent(event, "foo:bar", "baz");
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("_e{6,5}:title1|text1|d:1234567|h:host1|k:key1|p:low|t:error|#baz,foo:bar"));
    }

    @Test(timeout = 5000L)
    public void sends_service_check() throws Exception {
        final String inputMessage = "\u266c \u2020\u00f8U \n\u2020\u00f8U \u00a5\u00bau|m: T0\u00b5 \u266a"; // "♬ †øU \n†øU ¥ºu|m: T0µ ♪"
        final String outputMessage = "\u266c \u2020\u00f8U \\n\u2020\u00f8U \u00a5\u00bau|m\\: T0\u00b5 \u266a"; // note the escaped colon
        final String[] tags = {"key1:val1", "key2:val2"};
        final ServiceCheck sc = ServiceCheck.builder()
                .withName("my_check.name")
                .withStatus(ServiceCheck.Status.WARNING)
                .withMessage(inputMessage)
                .withHostname("i-abcd1234")
                .withTags(tags)
                .withTimestamp(1420740000)
                .build();

        assertEquals(outputMessage, sc.getEscapedMessage());

        client.serviceCheck(sc);
        server.waitForMessage();

        assertThat(server.messagesReceived(), contains(String.format("_sc|my_check.name|1|d:1420740000|h:i-abcd1234|#key2:val2,key1:val1|m:%s",
                outputMessage)));
    }

    @Test(timeout = 5000L)
    public void sends_nan_gauge_to_statsd() throws Exception {
        client.recordGaugeValue("mygauge", Double.NaN);

        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.mygauge:NaN|g"));
    }

    @Test(timeout = 5000L)
    public void sends_set_to_statsd() throws Exception {
        client.recordSetValue("myset", "myuserid");

        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.myset:myuserid|s"));

    }

    @Test(timeout = 5000L)
    public void sends_set_to_statsd_with_tags() throws Exception {
        client.recordSetValue("myset", "myuserid", "foo:bar", "baz");

        server.waitForMessage();

        assertThat(server.messagesReceived(), contains("my.prefix.myset:myuserid|s|#baz,foo:bar"));

    }

    @Test(timeout=5000L)
    public void sends_too_large_message() throws Exception {
        final RecordingErrorHandler errorHandler = new RecordingErrorHandler();

        try (final NonBlockingStatsDClient testClient = new NonBlockingStatsDClient("my.prefix", "localhost", STATSD_SERVER_PORT,  null, errorHandler)) {

            final byte[] messageBytes = new byte[1600];
            final ServiceCheck tooLongServiceCheck = ServiceCheck.builder()
                    .withName("toolong")
                    .withMessage(new String(messageBytes))
                    .withStatus(ServiceCheck.Status.OK)
                    .build();
            testClient.serviceCheck(tooLongServiceCheck);

            final ServiceCheck withinLimitServiceCheck = ServiceCheck.builder()
                    .withName("fine")
                    .withStatus(ServiceCheck.Status.OK)
                    .build();
            testClient.serviceCheck(withinLimitServiceCheck);

            server.waitForMessage();

            final List<Exception> exceptions = errorHandler.getExceptions();
            assertEquals(1, exceptions.size());
            final Exception exception = exceptions.get(0);
            assertEquals(InvalidMessageException.class, exception.getClass());
            assertTrue(((InvalidMessageException)exception).getInvalidMessage().startsWith("_sc|toolong|"));

            final List<String> messages = server.messagesReceived();
            assertEquals(1, messages.size());
            assertEquals("_sc|fine|0", messages.get(0));
        }
    }

    @Test(timeout = 5000L)
    public void shutdown_test() throws Exception {
        final int port = 17256;
        final DummyStatsDServer server = new DummyStatsDServer(port);
        final CountDownLatch lock = new CountDownLatch(1);
        final NonBlockingStatsDClient client = new NonBlockingStatsDClient("my.prefix.shutdownTest", "localhost", port) {
            @Override
            protected StatsDSender createSender(final Callable<SocketAddress> addressLookup, final int queueSize,
                                                final StatsDClientErrorHandler handler, final DatagramChannel clientChannel, final int maxPacketSizeBytes) {
                return new SlowStatsDSender(addressLookup, new SlowBlockingQueue(lock), handler, clientChannel, lock);
            }
        };
        try {
            client.count("mycounter", 5);
            assertEquals(0, server.messagesReceived().size());
            client.stop();
            server.waitForMessage();
            assertEquals(1, server.messagesReceived().size());
        } finally {
            client.stop();
            server.close();
        }
    }


    private static class SlowStatsDSender extends StatsDSender {
        private final CountDownLatch lock;

        SlowStatsDSender(Callable<SocketAddress> addressLookup, BlockingQueue queue,
                         StatsDClientErrorHandler handler, DatagramChannel clientChannel,
                         CountDownLatch lock) {
            super(addressLookup, queue, handler, clientChannel, 1400);
            this.lock = lock;
        }

        @Override
        void shutdown() {
            super.shutdown();
            lock.countDown();
        }
    }

    private static class SlowBlockingQueue extends LinkedBlockingQueue<String> {
        private final CountDownLatch countDownLatch;
        private boolean lock = false;


        private SlowBlockingQueue(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public boolean isEmpty() {
            return !lock && super.isEmpty();
        }

        @Override
        public String poll(long timeout, TimeUnit unit) throws InterruptedException {
            lock = true;
            countDownLatch.await(1, TimeUnit.MINUTES);
            lock = false;
            return super.poll(timeout, unit);
        }
    }
}
