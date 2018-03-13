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
