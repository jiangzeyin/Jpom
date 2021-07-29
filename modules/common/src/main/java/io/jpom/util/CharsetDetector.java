package io.jpom.util;

import cn.hutool.core.io.IoUtil;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;
import org.mozilla.universalchardet.CharsetListener;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.*;
import java.util.Arrays;

/**
 * 文件编码识别器
 *
 * @author Administrator
 */
public class CharsetDetector implements nsICharsetDetectionObserver {

	private boolean found = false;
	private String result;

	public String detectChineseCharset(File file) throws IOException {
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		String[] val = detectChineseCharset(new FileInputStream(file));
		if (val == null || val.length <= 0) {
			return null;
		}
		System.out.println(Arrays.toString(val));
		return val[0];
	}

	/**
	 * juniversalchardet is a Java port of 'universalchardet',
	 * that is the encoding detector library of Mozilla.
	 * (https://code.google.com/p/juniversalchardet/)
	 */
	public void universalDetect(FileInputStream fileInputStream) {
		byte[] buf = new byte[4096];

		// (1)
		UniversalDetector detector = new UniversalDetector(new CharsetListener() {
			@Override
			public void report(String s) {
				System.out.println(s);
			}
		});

		// (2)
		int n;
		BufferedInputStream imp = null;
		try {
			imp = new BufferedInputStream(fileInputStream);

			while ((n = imp.read(buf)) > 0 && !detector.isDone()) {
				detector.handleData(buf, 0, n);
			}

		} catch (IOException e) {
			e.printStackTrace();
			return;

		} finally {
			if (imp != null) {
				try {
					imp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// (3)
		detector.dataEnd();

		// (4)
		String charset = detector.getDetectedCharset();

		if (charset != null) {
			System.out.println(charset);
		}

		// (5)
		detector.reset();
	}

	private String[] detectChineseCharset(InputStream in) throws IOException {
		// Initalize the nsDetector() ;
		nsDetector det = new nsDetector();
		// Set an observer...
		// The Notify() will be called when a matching charset is found.
		det.Init(this);

		InputStream imp = IoUtil.toMarkSupportStream(in);
		byte[] buf = new byte[1024];
		int len;
		boolean isAscii = true;
		while ((len = imp.read(buf, 0, buf.length)) != -1) {
			// Check if the stream is only ascii.
			if (isAscii) {
				isAscii = det.isAscii(buf, len);
			}
			// DoIt if non-ascii and not done yet.
			if (!isAscii) {
				if (det.DoIt(buf, len, false)) {
					break;
				}
			}
		}
		IoUtil.close(imp);
		IoUtil.close(in);
		det.DataEnd();
		String[] prob;
		if (isAscii) {
			found = true;
			prob = new String[]{"ASCII"};
		} else if (found) {
			prob = new String[]{result};
		} else {
			prob = det.getProbableCharsets();
		}
		return prob;
	}

	@Override
	public void Notify(String charset) {
		found = true;
		result = charset;
	}
}
