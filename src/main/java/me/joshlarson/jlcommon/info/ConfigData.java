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

import me.joshlarson.jlcommon.log.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

class ConfigData {
	
	private final DateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss zzz");
	private final Map<String, String> data;
	private final File file;
	
	public ConfigData(File file) {
		this.data = new TreeMap<>();
		this.file = file;
	}
	
	public boolean containsKey(String key) {
		synchronized (data) {
			return data.containsKey(key);
		}
	}
	
	public String get(String key) {
		synchronized (data) {
			return data.get(key);
		}
	}
	
	public String put(String key, String value) {
		synchronized (data) {
			return data.put(key, value);
		}
	}
	
	public String remove(String key) {
		synchronized (data) {
			return data.remove(key);
		}
	}
	
	public void clear() {
		synchronized (data) {
			data.clear();
		}
	}
	
	/**
	 * @return null on an I/O failure, an empty {@code Map} on the first load and a populated {@code Map} when called afterwards
	 */
	public Map<String, String> load() {
		
		synchronized (data) {
			Map<String, String> delta = new HashMap<>(data);
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
				String line = reader.readLine();
				while (line != null) {
					loadLine(line);
					line = reader.readLine();
				}
			} catch (IOException e) {
				Log.e(e);
				return null;
			}
			for (Entry<String, String> entry : data.entrySet())
				delta.remove(entry.getKey(), entry.getValue());
			
			return delta;
		}
	}
	
	public boolean save() {
		synchronized (data) {
			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
				writer.write("# " + FORMAT.format(System.currentTimeMillis()));
				writer.newLine();
				for (Entry<String, String> e : data.entrySet()) {
					writer.write(e.getKey() + "=" + e.getValue());
					writer.newLine();
				}
				return true;
			} catch (IOException e) {
				Log.e(e);
				return false;
			}
		}
	}
	
	private void loadLine(String line) {
		String beforeComment = line;
		if (line.contains("#"))
			beforeComment = line.substring(0, line.indexOf('#'));
		if (!beforeComment.contains("="))
			return;
		String key = beforeComment.substring(0, beforeComment.indexOf('='));
		String val = beforeComment.substring(key.length() + 1);
		synchronized (data) {
			data.put(key, val);
		}
	}
	
}
