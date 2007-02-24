/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Font;

public class GomeOptions {

	private static final byte ENGLISH = 0;
	private static final byte FRENCH = 1;
	private static final byte JAPANESE = 2;

	private static final byte SMALL = 0;
	public static final byte MEDIUM = 1;
	private static final byte LIGHT = 0;

	private static final byte DARK = 2;
	private static final byte LARGE = 2;
	private static final byte EXTRA_LARGE = 3;

	public static final byte MANUAL = 3;
	public static final byte SLOW = 0;

	public static final byte FAST = 2;

	public static final int COLOR_LIGHT = 0x00FFDD00;
	public static final int COLOR_MEDIUM = 0x00F0CC00;
	public static final int COLOR_DARK = 0x00E0BB00;

	public static final String EN_US = "en_US";
	public static final String FR_FR = "fr_FR";
	public static final String JP_JP = "jp_JP";

	public String locale = EN_US;

	public int gobanColor = COLOR_MEDIUM;

	public int fontSize = Font.SIZE_MEDIUM;

	public byte scrollerSpeed = MEDIUM;

	public byte scrollerSize = SMALL;

	public String igsLogin = "";
	public String igsPassword = "";

	public byte igsSize = 19;

	public int igsMinutes = 25;

	public int igsByoyomi = 10;

	public String email = "";

	public String user = "";

	public String key = "";

	public long expiration = 0;

	public Font getScrollerFont() {
		return Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, fontSize);
	}

	public GomeOptions() {
		// default options
		String l = System.getProperty("microedition.locale"); //$NON-NLS-1$
		if (l != null) {
			locale = l; // some systems don't have a
			// locale//$NON-NLS-1$
		}
	}

	public GomeOptions(DataInputStream is) {
		try {
			setLocaleFromByte(is.readByte());
			setGobanColorFromByte(is.readByte());
			setScrollerFontFromByte(is.readByte());
			scrollerSpeed = is.readByte();
			scrollerSize = is.readByte();
			igsLogin = is.readUTF();
			igsPassword = is.readUTF();
			igsSize = is.readByte();
			igsMinutes = is.readInt();
			igsByoyomi = is.readInt();
			email = is.readUTF();
			user = is.readUTF();
			key = is.readUTF();
			expiration = is.readLong();

		} catch (IOException ioe) {
			// probably ended before the end, leav the remaining defaults for
			// compatiibility
		}
	}

	/**
	 * @param fontByte
	 */
	public void setScrollerFontFromByte(byte fontByte) {
		// Scroller Font
		switch (fontByte) {
		case SMALL:
			fontSize = Font.SIZE_SMALL;
			break;
		case MEDIUM:
			fontSize = Font.SIZE_MEDIUM;
			break;
		case LARGE:
			fontSize = Font.SIZE_LARGE;
			break;
		}
	}

	/**
	 * @param colorByte
	 */
	public void setGobanColorFromByte(byte colorByte) {
		// goban Color
		switch (colorByte) {
		case LIGHT:
			gobanColor = COLOR_LIGHT;
			break;
		case MEDIUM:
			gobanColor = COLOR_MEDIUM;
			break;
		case DARK:
			gobanColor = COLOR_DARK;
			break;
		}
	}

	public void setIGSGobanSizeFromByte(byte sizeByte) {
		switch (sizeByte) {
		case SMALL:
			igsSize = 9;
			break;
		case MEDIUM:
			igsSize = 13;
			break;
		case LARGE:
			igsSize = 19;
			break;
		}
	}

	/**
	 * @param localeByte
	 */
	public void setLocaleFromByte(byte localeByte) {
		// Locale
		switch (localeByte) {
		case FRENCH:
			locale = FR_FR; //$NON-NLS-1$
			break;
		case ENGLISH:
			locale = EN_US; //$NON-NLS-1$
			break;
		case JAPANESE:
			locale = JP_JP; //$NON-NLS-1$
			break;
		}
	}

	public byte getLocaleByte() {
		if (locale == EN_US)
			return ENGLISH;
		else if (locale == FR_FR)
			return FRENCH;
		else if (locale == JP_JP)
			return JAPANESE;
		return ENGLISH;
	}

	public byte getGobanColorByte() {
		switch (gobanColor) {
		case COLOR_LIGHT:
			return LIGHT;

		case COLOR_MEDIUM:
			return MEDIUM;

		case COLOR_DARK:
			return DARK;
		}
		return -1;

	}

	public byte getIGSGobanSizeByte() {
		switch (igsSize) {
		case 9:
			return SMALL;
		case 13:
			return MEDIUM;
		case 19:
			return LARGE;
		}
		return -1;
	}

	public byte getScrollerFontByte() {
		switch (fontSize) {
		case Font.SIZE_SMALL:
			return SMALL;
		case Font.SIZE_MEDIUM:
			return MEDIUM;
		case Font.SIZE_LARGE:
			return LARGE;
		}
		return -1;
	}

	public void marshalOut(DataOutputStream out) throws IOException {
		out.writeByte(getLocaleByte());
		out.writeByte(getGobanColorByte());
		out.writeByte(getScrollerFontByte());
		out.writeByte(scrollerSpeed);
		out.writeByte(scrollerSize);
		out.writeUTF(igsLogin);
		out.writeUTF(igsPassword);
		out.writeByte(igsSize);
		out.writeInt(igsMinutes);
		out.writeInt(igsByoyomi);
		out.writeUTF(email);
		out.writeUTF(user);
		out.writeUTF(key);
		out.writeLong(expiration);
	}

	public int getScrollerSpeed() {
		switch (scrollerSpeed) {
		case SLOW:
			return 300;
		default:
		case MEDIUM:
			return 200;
		case FAST:
			return 100;
		case MANUAL:
			return -1;
		}

	}

	public int getScrollerSize() {
		int fontSize = getScrollerFont().getHeight();
		switch (scrollerSize) {
		default:
		case SMALL:
			return fontSize;
		case MEDIUM:
			return (fontSize * 3) / 2;
		case LARGE:
			return fontSize * 2;
		case EXTRA_LARGE:
			return (fontSize * 5) / 2;
		}
	}

}
