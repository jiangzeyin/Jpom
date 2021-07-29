

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ruoyi.parser;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import io.jpom.util.CharsetDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.BitSet;

/**
 * @desc:
 * @name: CodeDetector.java
 * @author: tompai
 * @email：liinux@qq.com
 * @createTime: 2020年3月10日 下午11:28:51
 * @history:
 * @version: v1.0
 */

public class CodeDetector {
	private static Logger logger = LoggerFactory.getLogger(CodeDetector.class);


	public static String CODE_UTF8 = "UTF-8";
	public static String CODE_UTF8_BOM = "UTF-8_BOM";
	public static String CODE_GBK = "GBK";

	public static void main(String[] args) throws Exception {
		//File file = new File("D:\\SystemDir\\桌面\\jpom-2.5.0.tar.gz");
		//System.out.println(getEncode(file.getAbsolutePath(), true));
		//System.out.println(detect(new FileInputStream("D:\\Idea\\Jpom\\modules\\common\\src\\test\\resources\\test.bat")));
		//System.out.println(getEncode("D:\\SystemDir\\Desktop\\新建文本文档 (2).zip", true));
		CharsetDetector charsetDetector = new CharsetDetector();
		//System.out.println(charsetDetector.detectChineseCharset(new File("D:\\\\SystemDir\\\\Desktop\\\\新建文本文档 (2).zip")));

		charsetDetector.universalDetect(new FileInputStream("D:\\\\SystemDir\\\\Desktop\\\\新建文本文档 (2).zip"));
		System.out.println("sss");
		charsetDetector.universalDetect(new FileInputStream("D:\\Idea\\Jpom\\modules\\common\\src\\test\\resources\\test.bat"));

		//CharsetDetector charsetDetector = new CharsetDetector();
//		File file = new File("D:\\Idea\\Jpom\\modules\\common\\src\\test\\resources\\test.bat");
//		System.out.println(charsetDetector.detectChineseCharset(file));
//		System.out.println(FileUtil.readString(file,CharsetUtil.charset("GB18030")));
	}

	private static final Charset[] DEFAULT_CHARSETS;

	private static final int BYTE_SIZE = 8;

	static {
		String[] names = {
				"UTF-8",
				"GBK",
				"GB2312",
				"GB18030",
				"UTF-16BE",
				"UTF-16LE",
				"UTF-16",
				"BIG5",
				"UNICODE",
				"US-ASCII"};
		DEFAULT_CHARSETS = Convert.convert(Charset[].class, names);
	}

	/**
	 * 探测编码<br>
	 * 注意：此方法会读取流的一部分，然后关闭流，如重复使用流，请使用使用支持reset方法的流
	 *
	 * @param in       流，使用后关闭此流
	 * @param charsets 需要测试用的编码，null或空使用默认的编码数组
	 * @return 编码
	 */
	public static Charset detect(InputStream in, Charset... charsets) {
		if (ArrayUtil.isEmpty(charsets)) {
			charsets = DEFAULT_CHARSETS;
		}

		final byte[] buffer = new byte[512];
		try {
			int len;
			while ((len = in.read(buffer)) > -1) {
				byte[] bufferBytes = (byte[]) ArrayUtil.copy(buffer, 0, new byte[len], 0, len);
				for (Charset charset : charsets) {
					System.out.println(charset + "   -----------------");
					final CharsetDecoder decoder = charset.newDecoder();
					if (identify(bufferBytes, decoder, charset)) {
						return charset;
					}
				}
			}
		} catch (IOException e) {
			throw new IORuntimeException(e);
		} finally {
			IoUtil.close(in);
		}
		return null;
	}

	/**
	 * 通过try的方式测试指定bytes是否可以被解码，从而判断是否为指定编码
	 *
	 * @param bytes   测试的bytes
	 * @param decoder 解码器
	 * @return 是否是指定编码
	 */
	private static boolean identify(byte[] bytes, CharsetDecoder decoder, Charset charset) {
		try {
			CharBuffer decode = decoder.decode(ByteBuffer.wrap(bytes));
			char[] array = decode.array();
			String toString = decode.toString();
			String s = new String(bytes, charset);
			System.out.println(bytes.length + "  " + decode.length() + "  " + toString + "  " + s);

			return StrUtil.equals(toString, s);
		} catch (CharacterCodingException e) {
			//e.printStackTrace();
			return false;
		}
//		return true;
	}

	/**
	 * 通过文件全名称获取编码集名称
	 *
	 * @param fullFileName
	 * @param ignoreBom
	 * @return
	 * @throws Exception
	 */
	public static String getEncode(String fullFileName, boolean ignoreBom) throws Exception {
		logger.debug("fullFileName ; {}", fullFileName);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fullFileName));
		return getEncode(bis, ignoreBom);
	}

	/**
	 * 通过文件缓存流获取编码集名称，文件流必须为未曾
	 *
	 * @param bis
	 * @param ignoreBom 是否忽略utf-8 bom
	 * @return
	 * @throws Exception
	 */
	public static String getEncode(BufferedInputStream bis, boolean ignoreBom) {
		String encodeType = "未识别";
		try {
			bis.mark(0);
			byte[] head = new byte[3];
			bis.read(head);
			if (head[0] == -1 && head[1] == -2) {
				encodeType = "UTF-16";
			} else if (head[0] == -2 && head[1] == -1) {
				encodeType = "Unicode";
			} else if (head[0] == -17 && head[1] == -69 && head[2] == -65) { //带BOM
				if (ignoreBom) {
					encodeType = CODE_UTF8;
				} else {
					encodeType = CODE_UTF8_BOM;
				}
			} else if (isUTF8(bis)) {
				encodeType = CODE_UTF8;
			} else {
				encodeType = CODE_GBK;
			}
			System.out.println(Arrays.toString(head));
			logger.info("result encode type : " + encodeType);
		} catch (Exception e) {
			// TODO: handle exception
		}

		return encodeType;
	}

	/**
	 * 是否是无BOM的UTF8格式，不判断常规场景，只区分无BOM UTF8和GBK
	 *
	 * @param bis
	 * @return
	 */
	private static boolean isUTF8(BufferedInputStream bis) throws Exception {
		bis.reset();

		//读取第一个字节
		int code = bis.read();
		do {
			BitSet bitSet = convert2BitSet(code);
			//判断是否为单字节
			if (bitSet.get(0)) {//多字节时，再读取N个字节
				if (!checkMultiByte(bis, bitSet)) {//未检测通过,直接返回
					return false;
				}
			} else {
				//单字节时什么都不用做，再次读取字节
			}
			code = bis.read();
		} while (code != -1);
		return true;
	}

	/**
	 * 检测多字节，判断是否为utf8，已经读取了一个字节
	 *
	 * @param bis
	 * @param bitSet
	 * @return
	 */
	private static boolean checkMultiByte(BufferedInputStream bis, BitSet bitSet) throws Exception {
		int count = getCountOfSequential(bitSet);
		byte[] bytes = new byte[count - 1];//已经读取了一个字节，不能再读取
		bis.read(bytes);
		for (byte b : bytes) {
			if (!checkUtf8Byte(b)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 检测单字节，判断是否为utf8
	 *
	 * @param b
	 * @return
	 */
	private static boolean checkUtf8Byte(byte b) throws Exception {
		BitSet bitSet = convert2BitSet(b);
		return bitSet.get(0) && !bitSet.get(1);
	}

	/**
	 * 检测bitSet中从开始有多少个连续的1
	 *
	 * @param bitSet
	 * @return
	 */
	private static int getCountOfSequential(BitSet bitSet) {
		int count = 0;
		for (int i = 0; i < BYTE_SIZE; i++) {
			if (bitSet.get(i)) {
				count++;
			} else {
				break;
			}
		}
		return count;
	}


	/**
	 * 将整形转为BitSet
	 *
	 * @param code
	 * @return
	 */
	private static BitSet convert2BitSet(int code) {
		BitSet bitSet = new BitSet(BYTE_SIZE);

		for (int i = 0; i < BYTE_SIZE; i++) {
			int tmp3 = code >> (BYTE_SIZE - i - 1);
			int tmp2 = 0x1 & tmp3;
			if (tmp2 == 1) {
				bitSet.set(i);
			}
		}
		return bitSet;
	}

	/**
	 * 将一指定编码的文件转换为另一编码的文件
	 *
	 * @param oldFullFileName
	 * @param oldCharsetName
	 * @param newFullFileName
	 * @param newCharsetName
	 */
	public static void convert(String oldFullFileName, String oldCharsetName, String newFullFileName, String newCharsetName) throws Exception {
		logger.info("the old file name is : {}, The oldCharsetName is : {}", oldFullFileName, oldCharsetName);
		logger.info("the new file name is : {}, The newCharsetName is : {}", newFullFileName, newCharsetName);

		StringBuffer content = new StringBuffer();

		BufferedReader bin = new BufferedReader(new InputStreamReader(new FileInputStream(oldFullFileName), oldCharsetName));
		String line;
		while ((line = bin.readLine()) != null) {
			content.append(line);
			content.append(System.getProperty("line.separator"));
		}
		newFullFileName = newFullFileName.replace("\\", "/");
		File dir = new File(newFullFileName.substring(0, newFullFileName.lastIndexOf("/")));
		if (!dir.exists()) {
			dir.mkdirs();
		}
		Writer out = new OutputStreamWriter(new FileOutputStream(newFullFileName), newCharsetName);
		out.write(content.toString());
	}
}

