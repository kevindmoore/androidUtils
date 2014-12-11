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
package entagged.audioformats;

/**
 * This class represents a structure for storing and retrieving information
 * about the codec respectively the encoding parameters.<br>
 * Most of the parameters are available for nearly each audio format. Some
 * others would result in standard values.<br>
 * <b>Consider:</b> None of the setter methods will actually affect the audio
 * file. This is just a structure for retrieving information, not manipulating
 * the audio file.<br>
 * 
 * @author Raphael Slinckx
 */
public class EncodingInfo {

	private int bitRate = -1;
	private int channel = -1;
	private String type = "";
	private String infos = "";
	private int sampleRate = -1;
	private float length = -1;
	private boolean isVbr = false;
	private boolean isEncrypted = false;
	private boolean isLossless = false;

	public EncodingInfo() {
	}

	/**
	 * This method returns the bitrate of the represented audio clip in
	 * &quot;Kbps&quot;.<br>
	 * 
	 * @return The bitrate in Kbps.
	 */
	public int getBitrate() {
		return bitRate;
	}

	/**
	 * This method returns the number of audio channels the clip contains.<br>
	 * (The stereo, mono thing).
	 * 
	 * @return The number of channels. (2 for stereo, 1 for mono)
	 */
	public int getChannelNumber() {
		return channel;
	}

	/**
	 * Returns the encoding type.
	 * 
	 * @return The encoding type
	 */
	public String getEncodingType() {
		return type;
	}

	/**
	 * This method returns some extra information about the encoding.<br>
	 * This may not contain anything for some audio formats.<br>
	 * 
	 * @return Some extra information.
	 */
	public String getExtraEncodingInfos() {
		return infos;
	}

	/**
	 * This method returns the duration of the represented audio clip in
	 * seconds.<br>
	 * 
	 * @see #getPreciseLength()
	 * @return The duration in seconds.
	 */
	public int getLength() {
		return (int) length;
	}

	/**
	 * This method returns the duration of the represented audio clip in seconds
	 * (single-precision).<br>
	 * 
	 * @see #getLength()
	 * @return The duration in seconds.
	 */
	public float getPreciseLength() {
		return length;
	}

	/**
	 * This method returns the sample rate, the audio clip was encoded with.<br>
	 * 
	 * @return Sample rate of the audio clip in &quot;Hz&quot;.
	 */
	public int getSamplingRate() {
		return sampleRate;
	}

	/**
	 * This method returns <code>true</code>, if the audio file is encoded
	 * with &quot;Variable Bitrate&quot;.<br>
	 * 
	 * @return <code>true</code> if audio clip is encoded with VBR.
	 */
	public boolean isVbr() {
		return isVbr;
	}

	/**
	 * This method returns <code>true</code>, if the audio file is encoded
	 * with &quot;Lossless&quot;.<br>
	 * 
	 * @return <code>true</code> if audio clip is encoded with VBR.
	 */
	public boolean isLossless() {
		return isLossless;
	}

	/**
	 * This method returns <code>true</code>, if the audio file is encrypted
	 * with &quot;Variable Bitrate&quot;.<br>
	 * 
	 * @return <code>true</code> if audio clip is encrypted.
	 */
	public boolean isEncrypted() {
		return isEncrypted;
	}

	/**
	 * This Method sets the bitrate in &quot;Kbps&quot;.<br>
	 * 
	 * @param bitrate
	 *            bitrate in kbps.
	 */
	public void setBitrate(int v) {
		bitRate = v;
	}

	/**
	 * Sets the number of channels.
	 * 
	 * @param chanNb
	 *            number of channels (2 for stereo, 1 for mono).
	 */
	public void setChannelNumber(int v) {
		channel = v;
	}

	/**
	 * Sets the type of the encoding.<br>
	 * This is a bit format specific.<br>
	 * eg:Layer I/II/III
	 * 
	 * @param Encoding
	 *            type.
	 */
	public void setEncodingType(String v) {
		type = v;
	}

	/**
	 * A string contianing anything else that might be interesting
	 * 
	 * @param infos
	 *            Extra information.
	 */
	public void setExtraEncodingInfos(String v) {
		infos = v;
	}

	/**
	 * This method sets the audio duration of the represented clip.<br>
	 * 
	 * @param length
	 *            The duration of the audio clip in seconds.
	 */
	public void setLength(int v) {
		length = v;
	}

	/**
	 * This method sets the audio duration of the represented clip.<br>
	 * 
	 * @param seconds
	 *            The duration of the audio clip in seconds (single-precision).
	 */
	public void setPreciseLength(float v) {
		length = v;
	}

	/**
	 * Sets the Sampling rate in &quot;Hz&quot;<br>
	 * 
	 * @param samplingRate
	 *            Sample rate.
	 */
	public void setSamplingRate(int v) {
		sampleRate = v;
	}

	/**
	 * Sets the VBR flag for the represented audio clip.<br>
	 * 
	 * @param b
	 *            <code>true</code> if VBR.
	 */
	public void setVbr(boolean b) {
		isVbr = b;
	}

	/**
	 * Sets the ENCRYPTED flag for the represented audio clip.<br>
	 * 
	 * @param b
	 *            <code>true</code> if ENCRYPTED.
	 */
	public void setEncrypted(boolean b) {
		isEncrypted = b;
	}

	/**
	 * Sets the Lossless flag for the represented audio clip.<br>
	 * 
	 * @param b
	 *            <code>true</code> if Lossless.
	 */
	public void setLossless(boolean b) {
		isLossless = b;
	}

	/**
	 * Pretty prints this encoding info
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder out = new StringBuilder(50);
		out.append("Encoding infos content:\n");
		out.append("\t").append("bitrate : ").append(getBitrate()).append("\n");
		out.append("\t").append("channel : ").append(getChannelNumber())
				.append("\n");
		out.append("\t").append("length  : ").append(getLength()).append("\n");
		out.append("\t").append("precLen : ").append(getPreciseLength())
				.append("\n");
		out.append("\t").append("smplRt  : ").append(getSamplingRate()).append(
				"\n");
		out.append("\t").append("isEncr  : ").append(isEncrypted())
				.append("\n");
		out.append("\t").append("isLossl : ").append(isLossless()).append("\n");
		out.append("\t").append("isVbr   : ").append(isVbr()).append("\n");
		return out.toString().substring(0, out.length() - 1);
	}
}
