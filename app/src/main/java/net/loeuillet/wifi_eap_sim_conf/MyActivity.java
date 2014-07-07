package net.loeuillet.wifi_eap_sim_conf;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import android.content.Context;
import android.telephony.TelephonyManager;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiConfiguration;

public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String ssid = new String("");
        TelephonyManager tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String simOperator = tel.getSimOperator(); // Not getNetworkOperator wrt Roaming

        if (simOperator != null) {
            int mcc = Integer.parseInt(simOperator.substring(0, 3));
            int mnc = Integer.parseInt(simOperator.substring(3));
            if ( mcc == 208 )
            {
                if ( ( mnc >=  9 ) && ( mnc <= 13 ) ) { ssid = new String("SFR WiFi Mobile"); }
                if ( ( mnc >= 15 ) && ( mnc <= 16 ) ) { ssid = new String("FreeWifi_secure"); }
            }

            if ( !ssid.isEmpty() ) {
                WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
                enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.SIM); // EAP SIM / AKA for Mobile Phones

                // IMSI : 208(mcc) + 15(mnc) + 0000XXXXXX
                //enterpriseConfig.setIdentity("1"+tel.getSubscriberId()); // Use 1 + IMSI (See RFC4186)

                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = ssid;
                //wifiConfig.priority = 0; // Use lower priority than known APs
                wifiConfig.status = WifiConfiguration.Status.ENABLED;
                wifiConfig.allowedKeyManagement.clear();
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                wifiConfig.enterpriseConfig = enterpriseConfig;

                WifiManager wfMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                wfMgr.setWifiEnabled(true);
                wfMgr.disconnect();

                List<WifiConfiguration> list = wfMgr.getConfiguredNetworks();
                for( WifiConfiguration i : list ) {
                    if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                        wfMgr.removeNetwork(i.networkId);
                    }
                }
                int networkId = wfMgr.addNetwork(wifiConfig);
                if (networkId != -1) {
                    wfMgr.reconnect();
                    wfMgr.enableNetwork(networkId, true);
                }
            }
        }

        setContentView(R.layout.activity_my);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
