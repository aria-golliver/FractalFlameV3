/*
 * Part of the Processing project - http://processing.org Copyright (c) 2011-12 hansi raber,
 * released under LGPL under agreement This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, version 2.1. This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this library; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package japplemenubar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import processing.core.PApplet;

/**
 * Starting point for the application. General initialization should be done inside the
 * ApplicationController's init() method. If certain kinds of non-Swing initialization takes too
 * long, it should happen in a new Thread and off the Swing event dispatch thread (EDT).
 * 
 * @author hansi
 */
public class JAppleMenuBar {
	static JAppleMenuBar	instance;
	static final String	 FILENAME	= "libjAppleMenuBar.jnilib";

	static {
		try {
			final File temp = File.createTempFile("processing", "menubar");
			temp.delete(); // remove the file itself
			temp.mkdirs(); // create a directory out of it
			temp.deleteOnExit();

			final File jnilibFile = new File(temp, JAppleMenuBar.FILENAME);
			final InputStream input = JAppleMenuBar.class.getResourceAsStream(JAppleMenuBar.FILENAME);
			if (input != null) {
				if (PApplet.saveStream(jnilibFile, input)) {
					System.load(jnilibFile.getAbsolutePath());
					JAppleMenuBar.instance = new JAppleMenuBar();

				} else {
					JAppleMenuBar.sadness("Problem saving " + JAppleMenuBar.FILENAME + " for full screen use.");
				}
			} else {
				JAppleMenuBar.sadness("Could not load " + JAppleMenuBar.FILENAME + " from core.jar");
			}
		} catch (final IOException e) {
			JAppleMenuBar.sadness("Unknown error, here's the stack trace.");
			e.printStackTrace();
		}
	}

	static void sadness(final String msg) {
		System.err.println("Full screen mode disabled. " + msg);
	}

	// static public void show() {
	// instance.setVisible(true);
	// }

	static public void hide() {
		JAppleMenuBar.instance.setVisible(false, false);
	}

	public native void setVisible(boolean visibility, boolean kioskMode);

	// public void setVisible(boolean visibility) {
	// // Keep original API in-tact. Default kiosk-mode to off.
	// setVisible(visibility, false);
	// }
}
