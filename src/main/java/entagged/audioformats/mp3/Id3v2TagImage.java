package entagged.audioformats.mp3;

import java.io.UnsupportedEncodingException;

import entagged.audioformats.generic.TagField;
import entagged.audioformats.generic.TagImageField;

public class Id3v2TagImage extends Id3v2Tag implements TagImageField {

	byte[] rawImageData;
	
	public Id3v2TagImage(String id, byte[] content) {
		rawImageData = content;
	}

	public void copyContent(TagField field) {
	}

	public String getId() {
		return "covr";
	}

	public byte[] getRawContent() throws UnsupportedEncodingException {
		return null;
	}

	public boolean isBinary() {
		return true;
	}

	public void isBinary(boolean b) {
	}

	public boolean isCommon() {
		return false;
	}

	public boolean isEmpty() {
		return rawImageData == null || rawImageData.length == 0;
	}

	public byte[] getContent() {
		return rawImageData;
	}

	public void setContent(byte[] content) {
		rawImageData = new byte[content.length];
		System.arraycopy(content, 0, rawImageData, 0, content.length);
	}

}
