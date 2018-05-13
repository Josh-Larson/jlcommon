/***********************************************************************************
 * MIT License                                                                     *
 *                                                                                 *
 * Copyright (c) 2018 Josh Larson                                                  *
 *                                                                                 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy    *
 * of this software and associated documentation files (the "Software"), to deal   *
 * in the Software without restriction, including without limitation the rights    *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell       *
 * copies of the Software, and to permit persons to whom the Software is           *
 * furnished to do so, subject to the following conditions:                        *
 *                                                                                 *
 * The above copyright notice and this permission notice shall be included in all  *
 * copies or substantial portions of the Software.                                 *
 *                                                                                 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR      *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,        *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE     *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER          *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,   *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE   *
 * SOFTWARE.                                                                       *
 ***********************************************************************************/
package me.joshlarson.jlcommon.info;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Reads and stores configuration data from a file
 */
public class Config {
	
	private final ConfigData configData;
	
	/**
	 * Initilizes the Config and loads the data in the file
	 *
	 * @param filename the file to load
	 */
	public Config(@NotNull String filename) {
		this(new File(filename));
	}
	
	/**
	 * Initilizes the Config and loads the data in the file
	 *
	 * @param file the file to load
	 */
	public Config(@NotNull File file) {
		if (!file.exists() || !file.isFile())
			throw new IllegalArgumentException("Filepath does not point to a valid file!");
		configData = new ConfigData(file);
		load();
		save();
	}
	
	/**
	 * Determines whether or not the key-value pair exists in the config
	 *
	 * @param key the key to check
	 * @return TRUE if the key-value pair exists, FALSE otherwise
	 */
	public boolean containsKey(@NotNull String key) {
		return configData.containsKey(key);
	}
	
	/**
	 * Gets the parameter with the specified key. If no such parameter exists, it returns the default
	 *
	 * @param key the key to get the value for
	 * @param def the default value
	 * @return the value represented by the key, or the default value
	 */
	public String getString(@NotNull String key, String def) {
		if (!containsKey(key)) {
			setProperty(key, def);
			return def;
		}
		return configData.get(key);
	}
	
	/**
	 * Gets the parameter with the specified key. If no such parameter exists, or if the value isn't an integer then it returns the default
	 *
	 * @param key the key to get the value for
	 * @param def the default value
	 * @return the value represented by the key, or the default value
	 */
	public int getInt(@NotNull String key, int def) {
		try {
			return Integer.parseInt(getString(key, Integer.toString(def)));
		} catch (NumberFormatException e) {
			return def;
		}
	}
	
	/**
	 * Gets the parameter with the specified key. If no such parameter exists, or if the value isn't a double then it returns the default
	 *
	 * @param key the key to get the value for
	 * @param def the default value
	 * @return the value represented by the key, or the default value
	 */
	public double getDouble(@NotNull String key, double def) {
		try {
			return Double.parseDouble(getString(key, Double.toString(def)));
		} catch (NumberFormatException e) {
			return def;
		}
	}
	
	/**
	 * Gets the parameter with the specified key. If no such parameter exists, or if the value isn't a boolean then it returns the default
	 *
	 * @param key the key to get the value for
	 * @param def the default value
	 * @return the value represented by the key, or the default value
	 */
	public boolean getBoolean(@NotNull String key, boolean def) {
		String val = getString(key, def ? "true" : "false");
		return (val.equalsIgnoreCase("true") || val.equals("1")) || !val.equalsIgnoreCase("false") && !val.equals("0") && def;
	}
	
	/**
	 * Sets the property value for the specified key
	 *
	 * @param key   the key of the value to set
	 * @param value the value to set
	 */
	public void setProperty(@NotNull String key, @NotNull String value) {
		configData.put(key, value);
		save();
	}
	
	/**
	 * Sets the property value for the specified key
	 *
	 * @param key   the key of the value to set
	 * @param value the value to set
	 */
	public void setProperty(@NotNull String key, int value) {
		setProperty(key, Integer.toString(value));
	}
	
	/**
	 * Sets the property value for the specified key
	 *
	 * @param key   the key of the value to set
	 * @param value the value to set
	 */
	public void setProperty(@NotNull String key, boolean value) {
		setProperty(key, value ? "true" : "false");
	}
	
	/**
	 * Sets the property value for the specified key
	 *
	 * @param key   the key of the value to set
	 * @param value the value to set
	 */
	public void setProperty(@NotNull String key, double value) {
		setProperty(key, Double.toString(value));
	}
	
	/**
	 * Removes the property for the specified key
	 *
	 * @param key the key to remove
	 */
	public void removeProperty(@NotNull String key) {
		configData.put(key, null);
	}
	
	/**
	 * Clears all properties
	 */
	public void clearProperties() {
		configData.clear();
	}
	
	/**
	 * Reloads the config data from the file
	 *
	 * @return TRUE if successful load, FALSE on IO failure (file not found, read error, etc)
	 */
	public boolean load() {
		return configData.load() != null;
	}
	
	/**
	 * Saves the config data to the file
	 */
	public void save() {
		configData.save();
	}
	
}
