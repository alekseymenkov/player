package knaapo.player;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;


public class NetworkPreferences {

    final static String SERVER_ADDRESS = "SERVER_ADDRESS";
    final static String PORT = "PORT";
    final static int DEFAULT_PORT = 5525;

    private String mServerAddress;
    private int mPort = 5525;

    static FragmentActivity mActivity;
    SharedPreferences mSharedPreferences;

    public NetworkPreferences(FragmentActivity activity) {
        mActivity = activity;
        mSharedPreferences = mActivity.getPreferences(Context.MODE_PRIVATE);
    }


    public void loadPreferences() {
        mServerAddress = mSharedPreferences.getString(SERVER_ADDRESS, mActivity.getString(R.string.empty_string));
        mPort = mSharedPreferences.getInt(PORT, DEFAULT_PORT);
    }

    public void savePreferences() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(SERVER_ADDRESS, mServerAddress);
        editor.putInt(PORT, mPort);
        editor.apply();
    }


    public void setServerAddress(String serverAddress) {
        mServerAddress = serverAddress;
        return;
    }


    public void setPort(int port) {
        mPort = port;
        return;
    }


    public String getServerAddress() {
        return mServerAddress;
    }


    public int getPort() {
        return mPort;
    }


    public boolean isPreferencesCorrect() {
        final int MIN_PORT = 1024;
        final int MAX_PORT = 65535;

        boolean exp1 = mServerAddress.isEmpty();
        boolean exp2 = mPort < MIN_PORT || mPort > MAX_PORT;

        return !(exp1 || exp2);
    }
}
