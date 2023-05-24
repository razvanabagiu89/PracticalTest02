package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.model.ServerInformation;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;
    private String get20 = "false";

    public CommunicationThread(ServerThread serverThread, Socket socket, String get20) {
        this.serverThread = serverThread;
        this.socket = socket;
        this.get20 = get20;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        if(get20.equals("") || get20.equals("false")) {
            try {
                BufferedReader bufferedReader = Utilities.getReader(socket);
                PrintWriter printWriter = Utilities.getWriter(socket);
                if (bufferedReader == null || printWriter == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                    return;
                }
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client");
                String client_field = bufferedReader.readLine();
                if (client_field == null || client_field.isEmpty()) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client");
                    return;
                }
                HashMap<String, ServerInformation> data = serverThread.getData();
                ServerInformation serverInformation = null;
                if (data.containsKey(client_field)) {
                    Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                    serverInformation = data.get(client_field);
                } else {
                    Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                    HttpClient httpClient = new DefaultHttpClient();
                    String pageSourceCode = "";

                    HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + client_field);
                    HttpResponse httpGetResponse = httpClient.execute(httpGet);
                    HttpEntity httpGetEntity = httpGetResponse.getEntity();
                    if (httpGetEntity != null) {
                        pageSourceCode = EntityUtils.toString(httpGetEntity);
                    }

                    if (pageSourceCode == null) {
                        Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                        return;
                    } else
                        Log.i(Constants.TAG, pageSourceCode);

                    JSONObject content = new JSONObject(pageSourceCode);
                    JSONArray jsonArray_abilities = content.getJSONArray("abilities");
                    String abilities = "";
                    for (int i = 0; i < jsonArray_abilities.length(); i++) {
                        JSONObject abilityObject = jsonArray_abilities.getJSONObject(i);
                        JSONObject innerAbilityObject = abilityObject.getJSONObject("ability");
                        String name = innerAbilityObject.getString("name");
                        abilities += name + " AND ";

                        if (i < jsonArray_abilities.length() - 1) {
                            abilities += ";";
                        }
                    }

                    JSONArray jsonArray_types = content.getJSONArray("types");
                    JSONObject jsonObject_type;
                    String types = "";
                    for (int i = 0; i < jsonArray_types.length(); i++) {
                        jsonObject_type = jsonArray_types.getJSONObject(i);
                        JSONObject innerTypeObject = jsonObject_type.getJSONObject("type");
                        String name = innerTypeObject.getString("name");
                        types += name + " AND ";

                        if (i < jsonArray_types.length() - 1) {
                            types += ";";
                        }
                    }

                    JSONObject image_url = content.getJSONObject("sprites");
                    String image_url_string = image_url.getString("front_default");

                    serverInformation = new ServerInformation(
                            abilities, types, image_url_string);
                    serverThread.setData(client_field, serverInformation);
                }
                if (serverInformation == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Information is null!");
                    return;
                }

                // abilities
                printWriter.println(serverInformation.getAbilities());
                printWriter.flush();
                // types
                printWriter.println(serverInformation.getTypes());
                printWriter.flush();
                printWriter.println(serverInformation.getImage_url());
                printWriter.flush();
            } catch (IOException | JSONException ioException) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ioException) {
                        Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                        if (Constants.DEBUG) {
                            ioException.printStackTrace();
                        }
                    }
                }
            }
        }
        else {
            try {
                Log.e(Constants.TAG, "AICI");
                PrintWriter printWriter = Utilities.getWriter(socket);
                HttpClient httpClient = new DefaultHttpClient();
                String pageSourceCode = "";

                HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS);
                HttpResponse httpGetResponse = httpClient.execute(httpGet);
                HttpEntity httpGetEntity = httpGetResponse.getEntity();
                if (httpGetEntity != null) {
                    pageSourceCode = EntityUtils.toString(httpGetEntity);
                }

                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                } else
                    Log.i(Constants.TAG, pageSourceCode);

                JSONObject content = new JSONObject(pageSourceCode);
                JSONArray jsonArray_names = content.getJSONArray("results");
                String names = "";
                for (int i = 0; i < jsonArray_names.length(); i++) {
                    JSONObject nameObject = jsonArray_names.getJSONObject(i);
                    String name = nameObject.getString("name");
                    names += name + " AND ";

                    if (i < jsonArray_names.length() - 1) {
                        names += ";";
                    }
                }
                printWriter.println(names);
                printWriter.flush();
            }
            catch(IOException | JSONException ioException) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            }
            finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ioException) {
                        Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                        if (Constants.DEBUG) {
                            ioException.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
