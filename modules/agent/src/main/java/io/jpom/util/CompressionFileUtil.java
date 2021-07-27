package io.jpom.util;

import cn.hutool.core.io.CharsetDetector;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.compress.CompressUtil;
import cn.hutool.extra.compress.extractor.Extractor;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;

/**
 * 压缩文件工具
 *
 * @author bwcx_jzy
 */
public class CompressionFileUtil {

//	private static int BUFFER_SIZE = 2048;
//
//	private static List<String> unTar(File tarFile, File destDir) throws Exception {
//		return unTar(new FileInputStream(tarFile), destDir);
//	}
//
//	private static List<String> unTarBZip2(File tarFile, File destDir) throws Exception {
//		return unTar(new BZip2CompressorInputStream(new FileInputStream(tarFile)), destDir);
//	}
//
//	private static List<String> unTarGZ(File tarFile, File destDir) throws Exception {
//		return unTar(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(tarFile))), destDir);
//	}
//
//	private static List<String> unTar(InputStream inputStream, File destDir) throws Exception {
//		List<String> fileNames = new ArrayList<>();
//		TarArchiveInputStream tarIn = new TarArchiveInputStream(inputStream, BUFFER_SIZE, CharsetUtil.GBK);
//		TarArchiveEntry entry;
//		try {
//			while ((entry = tarIn.getNextTarEntry()) != null) {
//				fileNames.add(entry.getName());
//				if (entry.isDirectory()) {
//					//是目录
//					FileUtil.mkdir(new File(destDir, entry.getName()));
//					//创建空目录
//				} else {
//					//是文件
//					File tmpFile = new File(destDir, entry.getName());
//					//创建输出目录
//					FileUtil.mkParentDirs(tmpFile);
//					OutputStream out = null;
//					try {
//						out = new FileOutputStream(tmpFile);
//						int length;
//						byte[] b = new byte[2048];
//						while ((length = tarIn.read(b)) != -1) {
//							out.write(b, 0, length);
//						}
//					} finally {
//						IOUtils.closeQuietly(out);
//					}
//				}
//			}
//		} finally {
//			IOUtils.closeQuietly(tarIn);
//		}
//		return fileNames;
//	}
//
//	private static List<String> unBZip2(File srcFile, File destDir) throws IOException {
//		List<String> fileNames = new ArrayList<>();
//		InputStream is = null;
//		OutputStream os = null;
//		try {
//			String name = srcFile.getName().replace(".bz2", "");
//			File destFile = new File(destDir, name);
//			fileNames.add(srcFile.getName());
//			is = new BZip2CompressorInputStream(new BufferedInputStream(new FileInputStream(srcFile), BUFFER_SIZE));
//			os = new BufferedOutputStream(new FileOutputStream(destFile), BUFFER_SIZE);
//			IOUtils.copy(is, os);
//		} finally {
//			IOUtils.closeQuietly(os);
//			IOUtils.closeQuietly(is);
//		}
//		return fileNames;
//	}
//
//	private static List<String> unGZ(File srcFile, File destDir) throws IOException {
//		List<String> fileNames = new ArrayList<>();
//		InputStream is = null;
//		OutputStream os = null;
//		try {
//			String name = srcFile.getName().replace(".gz", "");
//			File destFile = new File(destDir, name);
//			fileNames.add(srcFile.getName());
//			os = new BufferedOutputStream(new FileOutputStream(destFile), BUFFER_SIZE);
//			is = new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(srcFile), BUFFER_SIZE));
//			IOUtils.copy(is, os);
//		} finally {
//			IOUtils.closeQuietly(os);
//			IOUtils.closeQuietly(is);
//		}
//		return fileNames;
//	}
//
//	private static List<String> unZip(File zipfile, File destDir) throws Exception {
//		ZipArchiveInputStream is = null;
//		List<String> fileNames = new ArrayList<>();
//		try {
//			is = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipfile), BUFFER_SIZE), CharsetUtil.GBK);
//			ZipArchiveEntry entry;
//			while ((entry = is.getNextZipEntry()) != null) {
//				fileNames.add(entry.getName());
//				if (entry.isDirectory()) {
//					FileUtil.mkdir(new File(destDir, entry.getName()));
//				} else {
//					File file = new File(destDir, entry.getName());
//					FileUtil.mkParentDirs(file);
//					OutputStream os = null;
//					try {
//						os = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
//						IOUtils.copy(is, os);
//					} finally {
//						IOUtils.closeQuietly(os);
//					}
//				}
//			}
//		} finally {
//			IOUtils.closeQuietly(is);
//		}
//		return fileNames;
//	}

	public static void unCompress(File compressFile, File destDir) throws Exception {
		Charset charset = CharsetDetector.detect(compressFile);
		charset = ObjectUtil.defaultIfNull(charset, CharsetUtil.defaultCharset());
		try {
			try (Extractor extractor = CompressUtil.createExtractor(charset, compressFile)) {
				extractor.extract(destDir);
			}
		} catch (Exception e) {
			CompressorInputStream compressUtilIn = null;
			try {
				CompressUtil compressUtil = new CompressUtil();
				compressUtilIn = compressUtil.getIn(null, new FileInputStream(compressFile));
				try (Extractor extractor = CompressUtil.createExtractor(charset, compressUtilIn)) {
					extractor.extract(destDir);
				}
			} catch (Exception e2) {
				//
				e2.addSuppressed(e);
				//
				e2.printStackTrace();
			} finally {
				IOUtils.closeQuietly(compressUtilIn);
			}
		}
//		String upperName = FileUtil.getName(compressFile).toUpperCase();
//		List<String> ret = null;
//		if (upperName.endsWith(".ZIP")) {
//			ret = unZip(compressFile, destDir);
//		} else if (upperName.endsWith(".TAR")) {
//			ret = unTar(compressFile, destDir);
//		} else if (upperName.endsWith(".TAR.BZ2")) {
//			ret = unTarBZip2(compressFile, destDir);
//		} else if (upperName.endsWith(".BZ2")) {
//			ret = unBZip2(compressFile, destDir);
//		} else if (upperName.endsWith(".TAR.GZ")) {
//			ret = unTarGZ(compressFile, destDir);
//		} else if (upperName.endsWith(".GZ")) {
//			ret = unGZ(compressFile, destDir);
//		}
//		return ret;
	}
}
