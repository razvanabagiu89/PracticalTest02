package ro.pub.cs.systems.eim.practicaltest02.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;

public class ClientThread extends Thread {

    private String address;
    private int port;
    private String client_field;
    private TextView client_text_view;
    private TextView client_text_view2;
    private ImageView client_image_view;

    private Socket socket;

    public ClientThread(String address, int port, String client_field, TextView client_text_view, TextView client_text_view2, ImageView client_image_view) {
        this.address = address;
        this.port = port;
        this.client_field = client_field;
        this.client_text_view = client_text_view;
        this.client_text_view2 = client_text_view2;
        this.client_image_view = client_image_view;
    }

    private Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            // send data to server
            printWriter.println(client_field);
            printWriter.flush();
            // receive data from server
            String serverInformation;
            serverInformation = bufferedReader.readLine();
            final String finalizedServerInformation = serverInformation;
            client_text_view.post(new Runnable() {
                @Override
                public void run() {
                    client_text_view.setText(finalizedServerInformation);
                }
            });
            String serverInformation2 = bufferedReader.readLine();
            final String finalizedServerInformation2 = serverInformation2;
            client_text_view2.post(new Runnable() {
                @Override
                public void run() {
                    client_text_view2.setText(finalizedServerInformation2);
                }
            });
            String image_url;
            image_url = bufferedReader.readLine();
            final String finalizedimage_url = image_url;
            new AsyncTask<String, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(String... params) {
                    return getBitmapFromURL(params[0]);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if (bitmap != null) {
                        client_image_view.setImageBitmap(bitmap);
                    }
                }
            }.execute(finalizedimage_url);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}
