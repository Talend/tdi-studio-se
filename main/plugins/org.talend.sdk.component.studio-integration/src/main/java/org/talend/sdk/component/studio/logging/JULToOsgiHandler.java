/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.logging;

import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.log4j.Logger;

public class JULToOsgiHandler extends Handler {

    private final Logger logger = Logger.getLogger(JULToOsgiHandler.class.getName());

    public JULToOsgiHandler() {
        setFormatter(new DefaultFormatter());
    }

    @Override
    public void publish(final LogRecord record) {
        try {
            final org.apache.log4j.Level priority = toLog4jPriority(record.getLevel());
            final String message = getFormatter().format(record);
            if (record.getThrown() != null) {
                logger.log(priority, message, record.getThrown());
            } else {
                logger.log(priority, message);
            }
        } catch (final Exception e) {
            reportError(null, e, ErrorManager.FORMAT_FAILURE);
        }
    }

    private org.apache.log4j.Level toLog4jPriority(final Level level) {
        if (level.equals(Level.SEVERE)) {
            return org.apache.log4j.Level.ERROR;
        }
        if (level.equals(Level.INFO)) {
            return org.apache.log4j.Level.INFO;
        }
        if (level.equals(Level.CONFIG)) {
            return org.apache.log4j.Level.INFO;
        }
        if (level.equals(Level.WARNING)) {
            return org.apache.log4j.Level.WARN;
        }
        return org.apache.log4j.Level.DEBUG;
    }

    @Override
    public void flush() {
        // no-op
    }

    @Override
    public void close() throws SecurityException {
        // no-op
    }

    private static class DefaultFormatter extends Formatter {

        @Override // simplified cause embed in another logger already so no need of timestamp/level
        public String format(final LogRecord record) {
            final StringBuilder sb = new StringBuilder();
            sb.append('[');
            sb.append(record.getSourceClassName());
            sb.append('.');
            sb.append(record.getSourceMethodName());
            sb.append("] ");
            sb.append(formatMessage(record));
            return sb.toString();
        }
    }
}
