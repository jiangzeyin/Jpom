package io.jpom.util;

import cn.hutool.core.io.CharsetDetector;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.core.io.file.FileMode;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.jiangzeyin.common.DefaultSystemLog;
import io.jpom.system.ExtConfigBean;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

/**
 * @author bwcx_jzy
 * @date 2019/7/21
 */
public class FileTailWatcherRun implements Runnable {
	/**
	 * 缓存近10条
	 */
	private final LimitQueue<String> limitQueue = new LimitQueue<>(ExtConfigBean.getInstance().getLogInitReadLine());
	private final RandomAccessFile randomFile;
	/**
	 * 是否已经开始执行
	 */
	private boolean start = false;
	private final Charset charset;
	private final LineHandler lineHandler;

	public LimitQueue<String> getLimitQueue() {
		return limitQueue;
	}

	FileTailWatcherRun(File file, LineHandler lineHandler) throws IOException {
		this.lineHandler = lineHandler;
		this.randomFile = new RandomAccessFile(file, FileMode.r.name());
		Charset detSet = ExtConfigBean.getInstance().getLogFileCharset();
		if (detSet == null) {
			//            detSet = CharsetUtil.charset(new CharsetDetector().detectChineseCharset(file));
			//            detSet = (detSet == StandardCharsets.US_ASCII) ? CharsetUtil.CHARSET_UTF_8 : detSet;
			detSet = ObjectUtil.defaultIfNull(CharsetDetector.detect(file), Charset.defaultCharset());
		}
		this.charset = detSet;
		if (file.length() > 0) {
			// 开始读取
			this.startRead();
		}
	}

	private void startRead() throws IOException {
		if (ExtConfigBean.getInstance().getLogInitReadLine() == 0) {
			// 不初始读取
			return;
		}
		long len = randomFile.length();
		long start = randomFile.getFilePointer();
		long nextEnd = start + len - 1;
		randomFile.seek(nextEnd);
		int c;
		while (nextEnd > start) {
			// 满
			if (limitQueue.full()) {
				break;
			}
			c = randomFile.read();
			if (c == CharUtil.LF || c == CharUtil.CR) {
				this.readLine();
				nextEnd--;
			}
			nextEnd--;
			randomFile.seek(nextEnd);
			if (nextEnd == 0) {
				// 当文件指针退至文件开始处，输出第一行
				this.readLine();
				break;
			}
		}
		// 移动到尾部
		randomFile.seek(len);
	}

	private void readLine() throws IOException {
		String line = randomFile.readLine();
		if (line != null) {
			line = CharsetUtil.convert(line, CharsetUtil.CHARSET_ISO_8859_1, charset);
			limitQueue.offerFirst(line);
		}
	}


	/**
	 * 读取文件内容
	 *
	 * @throws IOException IO
	 */
	private void read() throws IOException {
		final long currentLength = randomFile.length();
		final long position = randomFile.getFilePointer();
		if (0 == currentLength || currentLength == position) {
			// 内容长度不变时忽略此次
			return;
		} else if (currentLength < position) {
			// 如果内容变短，说明文件做了删改，回到内容末尾
			randomFile.seek(currentLength);
			return;
		}
		String tmp;
		while ((tmp = randomFile.readLine()) != null) {
			tmp = CharsetUtil.convert(tmp, CharsetUtil.CHARSET_ISO_8859_1, charset);
			limitQueue.offer(tmp);
			lineHandler.handle(tmp);
		}
		// 记录当前读到的位置
		this.randomFile.seek(currentLength);
	}


	/**
	 * 开始监听
	 */
	public void start() {
		if (this.start) {
			return;
		}
		this.start = true;
		ThreadUtil.execute(this);

	}

	@Override
	public void run() {
		while (this.start) {
			try {
				this.read();
			} catch (IOException e) {
				DefaultSystemLog.getLog().error("读取文件发送异常", e);
				lineHandler.handle("读取文件发生异常：" + e.getMessage());
				break;
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException ignored) {
			}
		}
		this.close();
	}

	public void close() {
		this.start = false;
		IoUtil.close(this.randomFile);
	}
}
