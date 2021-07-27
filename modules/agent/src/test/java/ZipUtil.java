import cn.hutool.core.io.CharsetDetector;
import io.jpom.util.CompressionFileUtil;

import java.io.File;
import java.io.FileInputStream;

public class ZipUtil {

	private static final int BUFFER_SIZE = 1024 * 100;

	public static void main(String[] args) throws Exception {

		File file = new File("D:\\SystemDir\\桌面\\jpom-2.5.0.tar.bz2");
		File file1 = new File("D:\\SystemDir\\桌面\\ttt\\");


		//	CompressionFileUtil.unCompress(file, file1);
		file = new File("D:\\SystemDir\\桌面\\jpom-2.5.0.tar.gz");


		System.out.println(CharsetDetector.detect(file));

		System.out.println(CharsetDetector.detect(new FileInputStream(file)));


		System.out.println(CharsetDetector.detect(new File("D:\\SystemDir\\桌面\\api.conf")));
//		System.out.println(CharsetUtil.charset(new CharsetDetector().detectChineseCharset(file)));

		CompressionFileUtil.unCompress(file, file1);
	}
}
