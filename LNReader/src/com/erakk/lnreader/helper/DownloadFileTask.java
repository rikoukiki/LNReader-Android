package com.erakk.lnreader.helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Date;

import com.erakk.lnreader.Constants;
import com.erakk.lnreader.dao.NovelsDao;
import com.erakk.lnreader.model.ImageModel;

import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract.Directory;
import android.text.Html;
import android.util.Log;

public class DownloadFileTask extends AsyncTask<URL, Integer, AsyncTaskResult<ImageModel>> {
	private static final String TAG = DownloadFileTask.class.toString();

	@Override
	protected AsyncTaskResult<ImageModel> doInBackground(URL... urls) {
		try{
			ImageModel image = downloadImage(urls[0]);
			return new AsyncTaskResult<ImageModel>(image);	         
		} catch (Exception e) {
			return new AsyncTaskResult<ImageModel>(e);
		}
	}

	public ImageModel downloadImage(URL url) throws Exception{
		Log.d(TAG, "Start Downloading: " + url.toString());
		InputStream input = null;
		OutputStream output = null;
		String filepath = Constants.IMAGE_ROOT + url.getFile();
		@SuppressWarnings("deprecation")
		String decodedUrl = URLDecoder.decode(filepath);
		Log.d(TAG, "Saving to: " + decodedUrl);

		// create dir if not exist
		String path = decodedUrl.substring(0, decodedUrl.lastIndexOf("/"));
		File cacheDir = new File(path);
		cacheDir.mkdirs();
		Log.d(TAG, "Path to: " + path);
		
		
		URLConnection connection = url.openConnection();
		connection.connect();
		// this will be useful so that you can show a typical 0-100% progress bar
		// I'm not using it AT them moment, but don't remove, might be useful for real.
		int fileLength = connection.getContentLength();

		// download the file
		input = new BufferedInputStream(url.openStream());
		output = new FileOutputStream(decodedUrl);

		byte data[] = new byte[1024];
		long total = 0;
		int count;
		while ((count = input.read(data)) != -1) {
			total += count;
			// publishing the progress....
			publishProgress((int) (total * 100 / fileLength));
			output.write(data, 0, count);
		}
		output.flush();
		output.close();
		input.close();
		
		ImageModel image = new ImageModel();
		image.setName(url.getFile());
		image.setUrl(url);
		image.setPath(filepath);
		image.setLastCheck(new Date());
		image.setLastUpdate(new Date());        
		Log.d(TAG, "Complete Downloading: " + url.toString());
		return image;
	}
}

