package name.gaudat.panda.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by anon on 11/1/2015.
 */
public class Account implements Parcelable {
    // Stores account data for panda accounts
    // Come on, no one is going to steal your virtual fap points
    // If they use this app to download images

    // the whole account will be passed to the worker when doing anything past the panda

    public String username; // self explanatory
    public String password; // ditto
    public HashMap<String, String> cookie; // cookie for getting through the panda
    public String siteRoot = Constants.NORMAL_SITEROOT; // changes to sad panda if logined and verified
    public boolean enableFjords = false; // changes to true if it really can

    // no-argument constructor
    public Account() {
    }

    ;

    // For parcel

    public class LoginRequest {
        // TODO: Login request, to be sent to workers

        String username;
        String password;

        // Constructs a request
        // Package local
        LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    // getting a request from this account
    public LoginRequest getLoginRequest() {
        return new LoginRequest(username, password);
    }

    public void writeCookie(HashMap<String, String> cookie) {
        // for writing back the cookie
        // used by workers only
        this.cookie = cookie;
    }

    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", cookie=" + cookie +
                ", siteRoot='" + siteRoot + '\'' +
                ", enableFjords=" + enableFjords +
                '}';
    }

    // parcelable

    protected Account(Parcel in) {
        username = in.readString();
        password = in.readString();
        cookie = (HashMap) in.readValue(HashMap.class.getClassLoader());
        siteRoot = in.readString();
        boolean[] temp = new boolean[1];
        in.readBooleanArray(temp);
        enableFjords = temp[0];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(password);
        dest.writeValue(cookie);
        dest.writeString(siteRoot);
        boolean[] temp = new boolean[1];
        temp[0] = enableFjords;
        dest.writeBooleanArray(temp);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };
}