package org.camunda.tngp.log.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.camunda.tngp.log.BufferedLogReader;
import org.camunda.tngp.log.Log;
import org.camunda.tngp.log.LogEntryWriter;
import org.camunda.tngp.log.LogReader;
import org.camunda.tngp.log.Logs;
import org.camunda.tngp.log.ReadableLogEntry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LogRecoveryTest
{
    private static final int MSG_SIZE = 911;

    @Rule
    public TemporaryFolder temFolder = new TemporaryFolder();

    @Test
    public void shouldRecover() throws InterruptedException, ExecutionException
    {
        final String logPath = temFolder.getRoot().getAbsolutePath();

        final Log log1 = Logs.createFsLog("foo", 0)
                .logRootPath(logPath)
                .deleteOnClose(false)
                .logSegmentSize(1024 * 1024 * 8)
                .build()
                .get();

        final LogEntryWriter writer = new LogEntryWriter(log1);

        final UnsafeBuffer msg = new UnsafeBuffer(ByteBuffer.allocateDirect(MSG_SIZE));

        final int workCount = 200000;

        for (int i = 0; i < workCount; i++)
        {
            msg.putInt(0, i);

            writer
                .key(i)
                .value(msg);

            while (writer.tryWrite() < 0)
            {
                // spin
            }
        }

        // wait until fully written
        final BufferedLogReader log1Reader = new BufferedLogReader(log1);

        long entryKey = 0;
        while (entryKey < workCount - 1)
        {
            log1Reader.seekToLastEntry();

            if (log1Reader.hasNext())
            {
                final ReadableLogEntry nextEntry = log1Reader.next();
                entryKey = nextEntry.getLongKey();
            }
        }

        log1.close();

        final Log log2 = Logs.createFsLog("foo", 0)
                .logRootPath(logPath)
                .deleteOnClose(true)
                .logSegmentSize(1024 * 1024 * 8)
                .build()
                .get();

        final LogReader logReader = new BufferedLogReader(log2);
        logReader.seekToFirstEntry();

        int count = 0;
        long lastPosition = -1L;

        while (count < workCount)
        {
            if (count % 10 == 0)
            {
                // make sure index is used
                logReader.seek(lastPosition + 1);
            }

            if (logReader.hasNext())
            {
                final ReadableLogEntry entry = logReader.next();
                final long currentPosition = entry.getPosition();

                assertThat(currentPosition > lastPosition);

                final DirectBuffer valueBuffer = entry.getValueBuffer();
                final long value = valueBuffer.getInt(entry.getValueOffset());
                assertThat(value).isEqualTo(entry.getLongKey());
                assertThat(entry.getValueLength()).isEqualTo(MSG_SIZE);

                lastPosition = currentPosition;

                count++;
            }
        }

        assertThat(count).isEqualTo(workCount);
        assertThat(logReader.hasNext()).isFalse();

        log2.close();
    }

}
