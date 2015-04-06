package name.gaudat.panda.worker;

import android.os.AsyncTask;
import android.util.Log;
import name.gaudat.panda.data.Account;
import name.gaudat.panda.data.Constants;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.helper.HttpConnection;

import java.io.IOException;
import java.net.ConnectException;
import java.util.HashMap;

/**
 * Created by anon on 11/1/2015.
 */
public class AccountLoginer extends AsyncTask<String, Void, Account> {
    //TODO
    // Try to login account and set fjord flag according to options
    // with shared pref options (go into fjord/ auto fjord if given
    // a sad panda link?)

    Account acc; // the account to be returned

    public AccountLoginer(Account acc) {
        this.acc = acc;
        Log.d(getClass().toString(), "Made with account " + acc);
    }

    @Override
    protected Account doInBackground(String... params) {
        Log.d(getClass().toString(), "Started with account " + acc);
        // params[0] is username
        // params[1] is password
        acc.username = params[0];
        acc.password = params[1];

        // use Guest for username as a placeholder
        if (acc.username.equals(Constants.GUEST_ACC)) {
            Log.d(getClass().toString(), "Not really logging in because we are Guest...");
            Connection.Response r = null;
            try {
                r = Jsoup.connect(acc.siteRoot).method(Connection.Method.GET).execute();
                Log.d(getClass().toString(), "Got login response");

                acc.cookie = (java.util.HashMap<String, String>) r.cookies();
                Log.d(getClass().toString(), "Set guest cookies");
            } catch (ConnectException e) {
                Log.e(getClass().toString(), "Network error");
                return null; // sanity check

            } catch (IOException e) {
                e.printStackTrace();
            }
            return acc;
        }

        // assume this user has access to panda
        acc.enableFjords = true;

        HttpConnection.Response r = null;
        try {
            HashMap<String, String> postdata = new HashMap<>();
            //http://e-hentai.org/bounce_login.php?CookieDate=1&b=d&bt=1-1&UserName=dtwai&PassWord=dontaskwhoami&ipb_login_submit=Login%21
            postdata.put("CookieDate", "1");
            postdata.put("b", "d");
            postdata.put("bt", "1-1");
            postdata.put("UserName", acc.username);
            postdata.put("PassWord", acc.password);
            postdata.put("ipb_login_submit", "Login!");
            r = (HttpConnection.Response) Jsoup.connect(Constants.LOGIN_SITE)
                    .method(Connection.Method.POST)
                    .data(postdata)
                    .execute();

            if (r == null) {
                Log.e(getClass().toString(), "Network error");
                return null; // sanity check
            }
            Log.d(getClass().toString(), "Got login response");
            acc.cookie = (java.util.HashMap<String, String>) r.cookies();
            Log.d(getClass().toString(), "Set login cookies");
            if (acc.enableFjords) {
                try {
                    r = (HttpConnection.Response) Jsoup.connect(Constants.FJORD_SITEROOT)
                            .cookies(acc.cookie).method(Connection.Method.GET).execute();
                } catch (UnsupportedMimeTypeException e) {
                    Log.w(getClass().toString(), "This account cannot get past the panda!");
                    Log.i(getClass().toString(), "Disabling Fjords");
                    acc.enableFjords = false;
                    return acc;
                }
                acc.siteRoot = Constants.FJORD_SITEROOT;
                return acc;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

