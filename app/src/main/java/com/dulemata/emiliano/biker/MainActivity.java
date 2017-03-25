package com.dulemata.emiliano.biker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.dulemata.emiliano.biker.connectivity.AsyncResponse;
import com.dulemata.emiliano.biker.connectivity.BackgroundHTTPRequestGet;
import com.dulemata.emiliano.biker.data.Negozio;
import com.dulemata.emiliano.biker.fragment.AcquistiFragment;
import com.dulemata.emiliano.biker.fragment.NegozioFragment;
import com.dulemata.emiliano.biker.data.Percorso;
import com.dulemata.emiliano.biker.fragment.FragmentInt;
import com.dulemata.emiliano.biker.fragment.HistoryFragment;
import com.dulemata.emiliano.biker.fragment.ProfileFragment;
import com.dulemata.emiliano.biker.fragment.TrackerFragment;
import com.dulemata.emiliano.biker.util.Keys;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;

import static com.dulemata.emiliano.biker.util.Dialog.showAlert;
import static com.dulemata.emiliano.biker.util.Keys.A_FRAGMENT;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HistoryFragment.OnListPercorsoInteractionListener, NegozioFragment.OnListFragmentInteractionListener {

    public static boolean isRunning;
    private static final int LOCATION_PERMISSIONS = 4;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 8;
    NavigationView navigationView;
    Toolbar toolbar;
    private int idContainer;
    private AlertDialog dialog;
    private WeakReference<FragmentInt> reference;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private AsyncResponse logoutResponse = new AsyncResponse() {
        @Override
        public void processResult(JSONArray result) {
            try {
                if (result != null && result.getJSONObject(0).getString(Keys.JSON_RESULT).equals(Keys.JSON_OK)) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    dialog = showAlert(MainActivity.this, dialog, "Errore", "Errore logout.", true).setPositiveButton("Riprova", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logout();
                        }
                    }).create();
                    dialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    private Negozio negozio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = getSharedPreferences(Keys.SHARED_PREFERENCIES, MODE_PRIVATE);
        checkPlayServices();
        if (savedInstanceState != null) {
            reference = new WeakReference<>((FragmentInt) getSupportFragmentManager().getFragment(savedInstanceState, A_FRAGMENT));
        } else {
            setFragment(R.id.tracker);
        }
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        setHeaderValues();
        navigationView.setNavigationItemSelectedListener(this);
        isRunning = true;
        runtimePermissions();
    }

    private void setHeaderValues() {
        View v = navigationView.getHeaderView(0);
        TextView email_main = (TextView) v.findViewById(R.id.email_main);
        TextView punti_main = (TextView) v.findViewById(R.id.punti_main);
        String email_string = email_main.getText().toString() + " " + preferences.getString(Keys.EMAIL, "");
        String punti_string = punti_main.getText().toString() + " " + preferences.getInt(Keys.PUNTEGGIO, -1);
        email_main.setText(email_string);
        punti_main.setText(punti_string);

    }

    private boolean runtimePermissions() {
        int fine_location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarse_location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (Build.VERSION.SDK_INT >= 23 && fine_location != PackageManager.PERMISSION_GRANTED && coarse_location != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSIONS);
            return true;
        }
        return false;
    }

    private void setFragment(int idContainer) {
        switch (idContainer) {
            case R.id.tracker:
                reference = new WeakReference<FragmentInt>(new TrackerFragment());
                break;
            case R.id.history:
                reference = new WeakReference<FragmentInt>(new HistoryFragment());
                break;
            case R.id.profile:
                reference = new WeakReference<FragmentInt>(new ProfileFragment());
                break;
            case R.id.negozi:
                reference = new WeakReference<FragmentInt>(new NegozioFragment());
                break;
            case R.id.acquisti:
                reference = new WeakReference<FragmentInt>(new AcquistiFragment());
                break;
        }
        if (reference.get() != null) {
            this.idContainer = idContainer;
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, (Fragment) reference.get());
            fragmentTransaction.commit();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Keys.MENU_ITEM, idContainer);
        getSupportFragmentManager().putFragment(outState, A_FRAGMENT, (Fragment) reference.get());
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                dialog = showAlert(this, dialog,
                        "Impossibile continuare",
                        "Per usare Biker sono necessari i google play services",
                        false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.this.finish();
                            }
                        }).create();
                dialog.show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    dialog = showAlert(this, dialog, "Permessi necessari", "I permessi per accedere alla posizione sono necessari per l'utilizzo del servizio di tracking", false)
                            .setPositiveButton("Attiva", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    runtimePermissions();
                                }
                            })
                            .setNegativeButton("Esci", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.this.finish();
                                }
                            })
                            .create();
                    dialog.show();
                }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            dialog = showAlert(this, dialog, "Chiusura", "Chiudere Biker?", false).setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (TrackerFragment.isTracking) {
                        Intent intent = new Intent(Keys.SHOW_NOTIFICA);
                        MainActivity.this.sendBroadcast(intent);
                    }
                    if (MainActivity.this.dialog != null)
                        MainActivity.this.dialog.dismiss();
                    MainActivity.super.onBackPressed();
                }
            })
                    .setNegativeButton(android.R.string.no, null).create();
            dialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.logout) {
            dialog = showAlert(this, dialog, getString(R.string.logout), getString(R.string.logout_message), false).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getSharedPreferences(Keys.SHARED_PREFERENCIES, MODE_PRIVATE).edit().putBoolean(Keys.AUTO_LOGIN, false).apply();
                    logout();
                }
            }).setNegativeButton(R.string.cancel, null).create();
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        BackgroundHTTPRequestGet request = new BackgroundHTTPRequestGet(logoutResponse);
        request.execute(Keys.URL_SERVER + "logout.php" + "?id_utente=" + preferences.getInt(Keys.ID, -1), Keys.JSON_RESULT);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id != idContainer) {
            setFragment(id);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onListPercorsoInteraction(Percorso percorso) {
        Intent intent = new Intent(this, PercorsoActivity.class);
        intent.putExtra(Keys.PERCORSO, percorso);
        startActivity(intent);
    }

    @Override
    public void onListFragmentInteraction(Negozio negozio) {
        Intent intent = new Intent(this, PremiActivity.class);
        intent.putExtra(Keys.NEGOZIO, negozio);
        startActivity(intent);
    }
}
