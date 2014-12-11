/*
 * Entagged Audio Tag library
 * Copyright (c) 2003-2005 Raphal Slinckx <raphael@slinckx.net>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package entagged.audioformats.mp3.util.id3frames;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import entagged.audioformats.generic.TagField;
import entagged.audioformats.mp3.Id3v2Tag;
import entagged.audioformats.mp3.util.Id3v2TagCreator;

/**
 * This class represents an ID3-Frame and provides basic information.<br>
 * 
 * @author Raphal Slinckx
 * @author Christian Laireiter
 */
public abstract class Id3Frame implements TagField {

	/**
	 * The frame header flags are stored here.<br>
	 * The flags have always been 2 bytes long. (Id3 version 2.x).
	 */
	protected byte[] flags;

	/**
	 * ID3 version identifier.<br>
	 * 
	 * @see Id3v2Tag#ID3V22
	 * @see Id3v2Tag#ID3V23
	 * @see Id3v2Tag#ID3V24
	 */
	protected byte version;

	/**
	 * Creates a new frame instance.<br>
	 * Id3 version assigned is {@linkplain Id3v2Tag#ID3V24 2.4}.<br>
	 */
	public Id3Frame() {
		this.version = Id3v2Tag.ID3V24;
		createDefaultFlags();
	}

	/**
	 * Creates a frame instance which adjusts to the given <code>raw</code>-data.<br>
	 * 
	 * @param raw
	 *            The data of the frame.
	 * @param id3Version
	 *            The ID3-Tag version.
	 * @throws UnsupportedEncodingException
	 *             If a textframe's content cannot be interpreted.
	 */
	public Id3Frame(byte[] raw, byte id3Version)
			throws UnsupportedEncodingException {
		byte[] rawNew;
		if (id3Version == Id3v2Tag.ID3V23 || id3Version == Id3v2Tag.ID3V24) {
			byte size = 2;

			if ((raw[1] & 0x80) == 0x80) {
				// Compression zlib, 4 bytes uncompressed size.
				size += 4;
			}

			if ((raw[1] & 0x80) == 0x40) {
				// Encryption method byte
				size += 1;
			}

			if ((raw[1] & 0x80) == 0x20) {
				// Group identity byte
				size += 1;
			}

			this.flags = new byte[size];
			for (int i = 0; i < size; i++)
				this.flags[i] = raw[i];
			rawNew = raw;
		} else {
			createDefaultFlags();
			rawNew = new byte[this.flags.length + raw.length];
			copy(this.flags, rawNew, 0);
			copy(raw, rawNew, this.flags.length);
		}

		this.version = id3Version;

		populate(rawNew);
	}

	/**
	 * This method creates a binary representation of the current
	 * {@link Id3Frame} data.<br>
	 * This data can directly be written to a file or stream.<br>
	 * s
	 * 
	 * @return Binary representation of current frame based on the
	 *         implementation (what does it stand for) and data (its contents).<br>
	 * @throws UnsupportedEncodingException
	 *             If a text-Frame is constructed there can occur an error
	 *             during text conversions.<br>
	 */
	protected abstract byte[] build() throws UnsupportedEncodingException;

	/**
	 * (overridden)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	protected void copy(byte[] src, byte[] dst, int dstOffset) {
		for (int i = 0; i < src.length; i++)
			dst[i + dstOffset] = src[i];
	}

	/**
	 * Fills {@link #flags} with two zero bytes.
	 */
	private void createDefaultFlags() {
		this.flags = new byte[2];
		this.flags[0] = 0;
		this.flags[1] = 0;
	}

	/**
	 * (overridden) <br>
	 * For Id3Frame objects the comparison can be easily done by comparing their
	 * binary representation which can be retrieved by invoking
	 * {@link Id3Frame#build()}.<br>
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Id3Frame) {
			Id3Frame other = (Id3Frame) obj;
			try {
				return Arrays.equals(this.build(), other.build());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Convenience method converting a string to a binary representation
	 * according to the given encoding.<br>
	 * 
	 * @param s
	 *            The string to get the binary representation from.
	 * @param encoding
	 *            The encoding which should be used.
	 * @return Binary representation of the given string in the given encoding
	 *         with BOM for UTF and the terminating character(s).
	 * @throws UnsupportedEncodingException
	 *             If the string needs to be converted and the encoding is not
	 *             present.
	 */
	protected byte[] getBytes(String s, String encoding)
			throws UnsupportedEncodingException {
		byte[] result = null;
		if ("UTF-16".equalsIgnoreCase(encoding)) {
			result = s.getBytes("UTF-16LE");
			// 2 for BOM and 2 for terminal character
			byte[] tmp = new byte[result.length + 4];
			System.arraycopy(result, 0, tmp, 2, result.length);
			// Create the BOM
			tmp[0] = (byte) 0xFF;
			tmp[1] = (byte) 0xFE;
			result = tmp;
		} else {
			// this is encoding ISO-8859-1, for the time of this change.
			result = s.getBytes(encoding);
			int zeroTerm = 1;
			if ("UTF-16BE".equals(encoding)) {
				zeroTerm = 2;
			}
			byte[] tmp = new byte[result.length + zeroTerm];
			System.arraycopy(result, 0, tmp, 0, result.length);
			result = tmp;
		}
		return result;
	}

	/**
	 * Returns the flags of the frame.<br>
	 * 
	 * @return The flag bytes.
	 */
	public byte[] getFlags() {
		/*
		 * Create a copy in order to prevent developers from manipulating the
		 * frames data using this methods result.
		 */
		byte[] result = new byte[this.flags.length];
		System.arraycopy(this.flags, 0, result, 0, result.length);
		return result;
	}

	/**
	 * (overridden)<br>
	 * For Id3-Frames its something like
	 * &quot;TCOM&quot;,&quot;TENC&quot;,&quot;TALB&quot; and so on.
	 * 
	 * @see entagged.audioformats.generic.TagField#getId()
	 */
	public abstract String getId();

	/**
	 * Convenience method for getting the binary representation of
	 * {@link #getId()}.<br>
	 * 
	 * @see String#getBytes()
	 * 
	 * @return The bytes of {@link #getId()}.
	 */
	protected byte[] getIdBytes() {
		return getId().getBytes();
	}

	/**
	 * (overridden) <br>
	 * simply calls {@link #build()} directly.
	 * 
	 * @see entagged.audioformats.generic.TagField#getRawContent()
	 */
	public byte[] getRawContent() throws UnsupportedEncodingException {
		return build();
	}

	/**
	 * Converts the given value of <code>size</code> into a representation
	 * which can be used to write a frames size into a file (or stream).<br>
	 * For now there are two different kinds of binary representation of a size
	 * value in ID3 frames.<br>
	 * First the prior 2.4 variation which is a simple &quot;litte endian&quot;.<br>
	 * Second the 2.4 variation which utilizes
	 * {@link Id3v2TagCreator#getSyncSafe(int)}.<br>
	 * 
	 * @param size
	 *            The integer value to convert.
	 * @return The binary representation according to the ID3 version.<br>
	 */
	protected byte[] getSize(int size) {
		byte[] b = null;
		if (this.version == Id3v2Tag.ID3V24) {
			b = Id3v2TagCreator.getSyncSafe(size);
		} else {
			b = new byte[4];
			b[0] = (byte) ((size >> 24) & 0xFF);
			b[1] = (byte) ((size >> 16) & 0xFF);
			b[2] = (byte) ((size >> 8) & 0xFF);
			b[3] = (byte) (size & 0xFF);
		}
		return b;
	}

	/**
	 * Another convenience Method which parses a byte array and returns a
	 * {@link String} instance.<br>
	 * 
	 * @param b
	 *            The array where the string resides.
	 * @param offset
	 *            The offset in <code>b</code> where the string begins.
	 * @param length
	 *            The length in bytes which makes up the string.
	 * @param encoding
	 *            The encoding of the string in <code>b</code>.
	 * @return A String representation of the specified data.
	 * @throws UnsupportedEncodingException
	 *             If an conversion error occurs or the encoding is not
	 *             available on the running system.
	 */
	protected String getString(byte[] b, int offset, int length, String encoding)
			throws UnsupportedEncodingException {
		String result = null;
		if ("UTF-16".equalsIgnoreCase(encoding)) {
			int zerochars = 0;
			// do we have zero terminating chars (old entagged did not)
			if (b[offset + length - 2] == 0x00
					&& b[offset + length - 1] == 0x00) {
				zerochars = 2;
			}
			if (b[offset] == (byte) 0xFE && b[offset + 1] == (byte) 0xFF) {
				result = new String(b, offset + 2, length - 2 - zerochars,
						"UTF-16BE");
			} else if (b[offset] == (byte) 0xFF && b[offset + 1] == (byte) 0xFE) {
				result = new String(b, offset + 2, length - 2 - zerochars,
						"UTF-16LE");
			} else {
				/*
				 * Now we have a little problem. The tag is not id3-spec
				 * conform. And since I don't have a way to see if its little or
				 * big endian, i decide for the windows default little endian.
				 */
				result = new String(b, offset, length - zerochars, "UTF-16LE");
			}
		} else {
			int zerochars = 0;
			if ("UTF-16BE".equals(encoding)) {
				if (b[offset + length - 2] == 0x00
						&& b[offset + length - 1] == 0x00) {
					zerochars = 2;
				}
			} else if (b[offset + length - 1] == 0x00) {
				zerochars = 1;
			}
			if (length == 0 || offset + length > b.length) {
				result = "";
			} else {
				result = new String(b, offset, length - zerochars, encoding);
			}
		}
		return result;
	}

	/**
	 * Convenience method which searches for the next zero byte in an array.
	 * 
	 * @param b
	 *            The array to search in.
	 * @param offset
	 *            The offset of <b>b</b> from where to look for the next zero
	 *            byte.
	 * @return The index of the zero byte in <code>b</code> if found. If none
	 *         was found, &quot;<code>-1</code>&quot; is returned.
	 */
	protected int indexOfFirstNull(byte[] b, int offset) {
		for (int i = offset; i < b.length; i++)
			if (b[i] == 0)
				return i;
		return -1;
	}

	/**
	 * (overridden)
	 * 
	 * @see entagged.audioformats.generic.TagField#isBinary()
	 */
	public abstract boolean isBinary();

	/**
	 * (overridden)
	 * 
	 * @see entagged.audioformats.generic.TagField#isBinary(boolean)
	 */
	public void isBinary(boolean b) {
		// Unsused
	}

	/**
	 * (overridden)
	 * 
	 * @see entagged.audioformats.generic.TagField#isCommon()
	 */
	public abstract boolean isCommon();

	/**
	 * This method reads the given data of an ID3-Frame and interprets it
	 * implementation specific.<br>
	 * The values of the Id3Frame instance are adjusted.
	 * 
	 * @param raw
	 *            The frame data.
	 * @throws UnsupportedEncodingException
	 *             On text frames there can be such errors.<br>
	 */
	protected abstract void populate(byte[] raw)
			throws UnsupportedEncodingException;
}
