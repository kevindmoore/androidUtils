package com.mastertechsoftware.io;

import java.io.IOException;
import java.io.Reader;

/**
 * Taken from java.io.BufferedReader.
 * Want to avoid creating strings for each line, so return buffer instead
 * Date: Mar 4, 2010
 */
/*
 * @(#)BufferedReader.java	1.37 06/03/15
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */



/**
 * Reads text from a character-input stream, buffering characters so as to
 * provide for the efficient reading of characters, arrays, and lines.
 * <p/>
 * <p> The buffer size may be specified, or the default size may be used.  The
 * default is large enough for most purposes.
 * <p/>
 * <p> In general, each read request made of a Reader causes a corresponding
 * read request to be made of the underlying character or byte stream.  It is
 * therefore advisable to wrap a BufferedReader around any Reader whose read()
 * operations may be costly, such as FileReaders and InputStreamReaders.  For
 * example,
 * <p/>
 * <pre>
 * BufferedReader in
 *   = new BufferedReader(new FileReader("foo.in"));
 * </pre>
 * <p/>
 * will buffer the input from the specified file.  Without buffering, each
 * invocation of read() or readLine() could cause bytes to be read from the
 * file, converted into characters, and then returned, which can be very
 * inefficient.
 * <p/>
 * <p> Programs that use DataInputStreams for textual input can be localized by
 * replacing each DataInputStream with an appropriate BufferedReader.
 *
 * @version 1.37, 06/03/15
 * @author Mark Reinhold
 * @since JDK1.1
 * @see java.io.FileReader
 * @see java.io.InputStreamReader
 */

public class BufferedReader extends Reader {

    protected Reader in;

    protected char cb[];
    protected StringBuilder buffer = new StringBuilder();
    protected int nChars, nextChar;

    protected static final int INVALIDATED = -2;
    protected static final int UNMARKED = -1;
    protected int markedChar = UNMARKED;
    protected int readAheadLimit = 0; /* Valid only when markedChar > 0 */

    /**
     * If the next character is a line feed, skip it
     */
    protected boolean skipLF = false;

    /**
     * The skipLF flag when the mark was set
     */
    protected boolean markedSkipLF = false;

    // Set when -1 returned from reading
    protected boolean finishedReading = false;

    protected static int defaultCharBufferSize = 8192;
    protected static int defaultExpectedLineLength = 80;
    protected StringData stringData = new StringData();

    /**
     * Creates a buffering character-input stream that uses an input buffer of
     * the specified size.
     *
     * @param in A Reader
     * @param sz Input-buffer size
     * @throws IllegalArgumentException If sz is <= 0
     */
    public BufferedReader(Reader in, int sz) {
        super(in);
        if (sz <= 0)
            throw new IllegalArgumentException("Buffer size <= 0");
        this.in = in;
        cb = new char[sz];
        nextChar = nChars = 0;
        stringData.setBuffer(buffer);
    }

    /**
     * Creates a buffering character-input stream that uses a default-sized
     * input buffer.
     *
     * @param in A Reader
     */
    public BufferedReader(Reader in) {
        this(in, defaultCharBufferSize);
    }

    /**
     * Checks to make sure that the stream has not been closed
     */
    protected void ensureOpen() throws IOException {
        if (in == null)
            throw new IOException("Stream closed");
    }

    /**
     * Fills the input buffer, taking the mark into account if it is valid.
     */
    protected void fill() throws IOException {
        int dst;
        if (markedChar <= UNMARKED) {
            /* No mark */
            dst = 0;
        } else {
            /* Marked */
            int delta = nextChar - markedChar;
            if (delta >= readAheadLimit) {
                /* Gone past read-ahead limit: Invalidate mark */
                markedChar = INVALIDATED;
                readAheadLimit = 0;
                dst = 0;
            } else {
                if (readAheadLimit <= cb.length) {
                    /* Shuffle in the current buffer */
                    System.arraycopy(cb, markedChar, cb, 0, delta);
                    markedChar = 0;
                    dst = delta;
                } else {
                    /* Reallocate buffer to accommodate read-ahead limit */
                    char ncb[] = new char[readAheadLimit];
                    System.arraycopy(cb, markedChar, ncb, 0, delta);
                    cb = ncb;
                    markedChar = 0;
                    dst = delta;
                }
                nextChar = nChars = delta;
            }
        }

        int n;
        do {
            n = in.read(cb, dst, cb.length - dst);
        } while (n == 0);
        if (n > 0) {
            nChars = dst + n;
            nextChar = dst;
        }
        if (n == -1)  {
            finishedReading = true;
        }
    }

    /**
     * Reads a single character.
     *
     * @return The character read, as an integer in the range
     *         0 to 65535 (<tt>0x00-0xffff</tt>), or -1 if the
     *         end of the stream has been reached
     * @throws IOException If an I/O error occurs
     */
    public int read() throws IOException {
        synchronized (lock) {
            ensureOpen();
            for (; ;) {
                if (nextChar >= nChars) {
                    fill();
                    if (nextChar >= nChars)
                        return -1;
                }
                if (skipLF) {
                    skipLF = false;
                    if (cb[nextChar] == '\n') {
                        nextChar++;
                        continue;
                    }
                }
                return cb[nextChar++];
            }
        }
    }

    /**
     * Reads characters into a portion of an array, reading from the underlying
     * stream if necessary.
     */
    protected int read1(char[] cbuf, int off, int len) throws IOException {
        if (nextChar >= nChars) {
            /* If the requested length is at least as large as the buffer, and
              if there is no mark/reset activity, and if line feeds are not
              being skipped, do not bother to copy the characters into the
              local buffer.  In this way buffered streams will cascade
              harmlessly. */
            if (len >= cb.length && markedChar <= UNMARKED && !skipLF) {
                int readAmt = in.read(cbuf, off, len);
                if (readAmt == -1) {
                    finishedReading = true;
                }
                return readAmt;
            }
            fill();
        }
        if (nextChar >= nChars) return -1;
        if (skipLF) {
            skipLF = false;
            if (cb[nextChar] == '\n') {
                nextChar++;
                if (nextChar >= nChars)
                    fill();
                if (nextChar >= nChars)
                    return -1;
            }
        }
        int n = Math.min(len, nChars - nextChar);
        System.arraycopy(cb, nextChar, cbuf, off, n);
        nextChar += n;
        return n;
    }

    /**
     * Reads characters into a portion of an array.
     * <p/>
     * <p> This method implements the general contract of the corresponding
     * <code>{@link Reader#read(char[], int, int) read}</code> method of the
     * <code>{@link Reader}</code> class.  As an additional convenience, it
     * attempts to read as many characters as possible by repeatedly invoking
     * the <code>read</code> method of the underlying stream.  This iterated
     * <code>read</code> continues until one of the following conditions becomes
     * true: <ul>
     * <p/>
     * <li> The specified number of characters have been read,
     * <p/>
     * <li> The <code>read</code> method of the underlying stream returns
     * <code>-1</code>, indicating end-of-file, or
     * <p/>
     * <li> The <code>ready</code> method of the underlying stream
     * returns <code>false</code>, indicating that further input requests
     * would block.
     * <p/>
     * </ul> If the first <code>read</code> on the underlying stream returns
     * <code>-1</code> to indicate end-of-file then this method returns
     * <code>-1</code>.  Otherwise this method returns the number of characters
     * actually read.
     * <p/>
     * <p> Subclasses of this class are encouraged, but not required, to
     * attempt to read as many characters as possible in the same fashion.
     * <p/>
     * <p> Ordinarily this method takes characters from this stream's character
     * buffer, filling it from the underlying stream as necessary.  If,
     * however, the buffer is empty, the mark is not valid, and the requested
     * length is at least as large as the buffer, then this method will read
     * characters directly from the underlying stream into the given array.
     * Thus redundant <code>BufferedReader</code>s will not copy data
     * unnecessarily.
     *
     * @param cbuf Destination buffer
     * @param off  Offset at which to start storing characters
     * @param len  Maximum number of characters to read
     * @return The number of characters read, or -1 if the end of the
     *         stream has been reached
     * @throws IOException If an I/O error occurs
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                    ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }

            int n = read1(cbuf, off, len);
            if (n <= 0) return n;
            while ((n < len) && in.ready()) {
                int n1 = read1(cbuf, off + n, len - n);
                if (n1 <= 0) break;
                n += n1;
            }
            return n;
        }
    }

    /**
     * Reads a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a linefeed.
     *
     * @param ignoreLF If true, the next '\n' will be skipped
     * @return A String containing the contents of the line, not including
     *         any line-termination characters, or null if the end of the
     *         stream has been reached
     * @throws IOException If an I/O error occurs
     * @see java.io.LineNumberReader#readLine()
     */
    StringData readLine(boolean ignoreLF) throws IOException {
        int startChar;

        synchronized (lock) {
            ensureOpen();
            boolean omitLF = ignoreLF || skipLF;

            bufferLoop:
            for (; ;) {

                if (nextChar >= nChars)
                    fill();
                if (nextChar >= nChars) { /* EOF */
                    if (stringData.getLength() > 0) {
                        return stringData;
                    } else {
                        stringData.setSuccess(false);
                        return stringData;
                    }
                }
                boolean eol = false;
                char c = 0;
                int i;

                /* Skip a leftover '\n', if necessary */
                if (omitLF && (cb[nextChar] == '\n'))
                    nextChar++;
                skipLF = false;
                omitLF = false;

                charLoop:
                for (i = nextChar; i < nChars; i++) {
                    c = cb[i];
                    if ((c == '\n') || (c == '\r')) {
                        eol = true;
                        break charLoop;
                    }
                }

                startChar = nextChar;
                nextChar = i;

                if (eol) {
                    stringData.setStartPos(startChar);
                    stringData.addLength(i - startChar);
                    buffer.append(cb, startChar, i - startChar);
                    nextChar++;
                    if (c == '\r') {
                        skipLF = true;
                    }
                    return stringData;
                }
                buffer.append(cb, startChar, i - startChar);
            }
        }
    }

    /**
     * Reads a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a linefeed.
     *
     * @return A String containing the contents of the line, not including
     *         any line-termination characters, or null if the end of the
     *         stream has been reached
     * @throws IOException If an I/O error occurs
     */
    public StringData readLine() throws IOException {
        return readLine(false);
    }

    /**
     * Read the whole File, returning everything in the StringBuffer
     * @return StringBuilder
     * @throws IOException any IO exception
     */
    public StringBuilder readAll() throws IOException {
        stringData.resetBuffer();
        while (!finishedReading) {
            readLine(false);
        }
        return stringData.getBuffer();
    }

    /**
     * Skips characters.
     *
     * @param n The number of characters to skip
     * @return The number of characters actually skipped
     * @throws IllegalArgumentException If <code>n</code> is negative.
     * @throws IOException              If an I/O error occurs
     */
    public long skip(long n) throws IOException {
        if (n < 0L) {
            throw new IllegalArgumentException("skip value is negative");
        }
        synchronized (lock) {
            ensureOpen();
            long r = n;
            while (r > 0) {
                if (nextChar >= nChars)
                    fill();
                if (nextChar >= nChars)    /* EOF */
                    break;
                if (skipLF) {
                    skipLF = false;
                    if (cb[nextChar] == '\n') {
                        nextChar++;
                    }
                }
                long d = nChars - nextChar;
                if (r <= d) {
                    nextChar += r;
                    r = 0;
                    break;
                } else {
                    r -= d;
                    nextChar = nChars;
                }
            }
            return n - r;
        }
    }

    /**
     * Tells whether this stream is ready to be read.  A buffered character
     * stream is ready if the buffer is not empty, or if the underlying
     * character stream is ready.
     *
     * @throws IOException If an I/O error occurs
     */
    public boolean ready() throws IOException {
        synchronized (lock) {
            ensureOpen();

            /*
            * If newline needs to be skipped and the next char to be read
            * is a newline character, then just skip it right away.
            */
            if (skipLF) {
                /* Note that in.ready() will return true if and only if the next
             * read on the stream will not block.
             */
                if (nextChar >= nChars && in.ready()) {
                    fill();
                }
                if (nextChar < nChars) {
                    if (cb[nextChar] == '\n')
                        nextChar++;
                    skipLF = false;
                }
            }
            return (nextChar < nChars) || in.ready();
        }
    }

    /**
     * Tells whether this stream supports the mark() operation, which it does.
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * Marks the present position in the stream.  Subsequent calls to reset()
     * will attempt to reposition the stream to this point.
     *
     * @param readAheadLimit Limit on the number of characters that may be
     *                       read while still preserving the mark. An attempt
     *                       to reset the stream after reading characters
     *                       up to this limit or beyond may fail.
     *                       A limit value larger than the size of the input
     *                       buffer will cause a new buffer to be allocated
     *                       whose size is no smaller than limit.
     *                       Therefore large values should be used with care.
     * @throws IllegalArgumentException If readAheadLimit is < 0
     * @throws IOException              If an I/O error occurs
     */
    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        synchronized (lock) {
            ensureOpen();
            this.readAheadLimit = readAheadLimit;
            markedChar = nextChar;
            markedSkipLF = skipLF;
        }
    }

    /**
     * Resets the stream to the most recent mark.
     *
     * @throws IOException If the stream has never been marked,
     *                     or if the mark has been invalidated
     */
    public void reset() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (markedChar < 0)
                throw new IOException((markedChar == INVALIDATED)
                        ? "Mark invalid"
                        : "Stream not marked");
            nextChar = markedChar;
            skipLF = markedSkipLF;
            buffer.setLength(nextChar);
        }
    }

    public void close() throws IOException {
        synchronized (lock) {
            if (in == null)
                return;
            in.close();
            in = null;
            cb = null;
            finishedReading = true;
        }
    }

    class StringData {
        protected boolean success = true;
        protected int startPos;
        protected int length;
        protected StringBuilder buffer;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public int getStartPos() {
            return startPos;
        }

        public void setStartPos(int startPos) {
            this.startPos = startPos;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public void addLength(int length) {
            this.length += length;
        }

        public StringBuilder getBuffer() {
            return buffer;
        }

        public void setBuffer(StringBuilder buffer) {
            this.buffer = buffer;
        }

        public void resetBuffer() {
            buffer.setLength(0);
        }
    }
}
