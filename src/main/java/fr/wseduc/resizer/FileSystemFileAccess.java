package fr.wseduc.resizer;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.file.FileSystem;

public class FileSystemFileAccess implements FileAccess {

	private final FileSystem fs;
	private final String basePath;

	public FileSystemFileAccess(Vertx vertx, String basePath) {
		this.fs = vertx.fileSystem();
		this.basePath = (basePath != null && !basePath.endsWith("/")) ? basePath + "/" : basePath;
	}

	@Override
	public void read(String src, final Handler<ImageFile> handler) {
		String path = parsePath(src);
		boolean absolutePath = path.startsWith("/");
		if (path == null || (basePath == null && !absolutePath)) {
			handler.handle(null);
			return;
		}
		final String p = absolutePath ? path : basePath + path;
		fs.readFile(p, new Handler<AsyncResult<Buffer>>() {
			@Override
			public void handle(AsyncResult<Buffer> ar) {
				if (ar.succeeded()) {
					handler.handle(new ImageFile(ar.result().getBytes(), getFileName(p), ""));
				} else {
					handler.handle(null);
				}
			}
		});
	}

	@Override
	public void write(String dest, ImageFile img, final Handler<String> handler) {
		final String path = parsePath(dest);
		boolean absolutePath = path.startsWith("/");
		if (path == null || (basePath == null && !absolutePath)) {
			handler.handle(null);
			return;
		}
		final String p = absolutePath ? path : basePath + path;
		fs.writeFile(p, new Buffer(img.getData()), new Handler<AsyncResult<Void>>() {
			@Override
			public void handle(AsyncResult<Void> ar) {
				if (ar.succeeded()) {
					handler.handle(path);
				} else {
					handler.handle(null);
				}
			}
		});
	}

	@Override
	public void close() {
	}

	private String parsePath(String path) {
		String[] p = path.split("://");
		if (p == null || p.length != 2) {
			return null;
		}
		return p[1];
	}

	private String getFileName(String path) {
		if (path != null) {
			int idx = path.lastIndexOf('/');
			if (idx > 0 && path.length() > idx + 1) {
				return path.substring(idx + 1);
			}
		}
		return "";
	}

}