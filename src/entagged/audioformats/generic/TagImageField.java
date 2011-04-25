package entagged.audioformats.generic;

public interface TagImageField extends TagField {

	/**
	 * Returns the content of the field.
	 * 
	 * @return Content
	 */
	public byte[] getContent();

	/**
	 * Sets the content of the field.
	 * 
	 * @param content
	 *            fields content.
	 */
	public void setContent(byte[] content);

}
