package com.XACS.Assist.Util;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class Logger {
	public static void debug(String msg) {
		System.out.println(msg);
	}

	public static void info(String msg) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		(new Throwable()).printStackTrace(pw);
		pw.flush();
		String stackTrace = baos.toString();
		pw.close();
		StringTokenizer tok = new StringTokenizer(stackTrace, "\n");
		String l = tok.nextToken();
		l = tok.nextToken();
		l = tok.nextToken();
		l = l.trim();
		String outP = l.replaceAll("^at", "INFO:");
		outP = outP.trim();
		System.out.println(outP + "::" + msg);
	}

	public static void info() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(baos);
		(new Throwable()).printStackTrace(pw);
		pw.flush();
		String stackTrace = baos.toString();
		pw.close();
		StringTokenizer tok = new StringTokenizer(stackTrace, "\n");
		String l = tok.nextToken();
		l = tok.nextToken();
		l = tok.nextToken();
		l = l.trim();
		String outP = l.replaceAll("^at", "INFO:");
		outP = outP.trim();
		System.out.println(outP + ".");
	}

	public Logger() {
	}
}
