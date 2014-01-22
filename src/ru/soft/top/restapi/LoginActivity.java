package ru.soft.top.restapi;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import ru.soft.top.restapi.loader.RESTLoader;
import android.accounts.AccountAuthenticatorActivity;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 * 
 * @param <D>
 */
public class LoginActivity extends AccountAuthenticatorActivity implements
		LoaderCallbacks<RESTLoader.RESTResponse> {
	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private static final int LOADER_TWITTER_SEARCH = 0x1;

	private static final String ARGS_URI = "ru.soft.top.restapi.ARGS_URI";
	private static final String ARGS_PARAMS = "ru.soft.top.restapi.ARGS_PARAMS";
	public static final String ACCESS_TOKEN = "ru.soft.top.restapi.ACCESS_TOKEN";
	private static Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		setupActionBar();

		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (mAuthTask != null) {
							return;
						}

						// Reset errors.
						mEmailView.setError(null);
						mPasswordView.setError(null);

						// Store values at the time of the login attempt.
						mEmail = mEmailView.getText().toString();
						mPassword = mPasswordView.getText().toString();

						boolean cancel = false;
						View focusView = null;

						// Check for a valid password.
						if (TextUtils.isEmpty(mPassword)) {
							mPasswordView
									.setError(getString(R.string.error_field_required));
							focusView = mPasswordView;
							cancel = true;
						} else if (mPassword.length() < 4) {
							mPasswordView
									.setError(getString(R.string.error_invalid_password));
							focusView = mPasswordView;
							cancel = true;
						}

						// Check for a valid email address.
						if (TextUtils.isEmpty(mEmail)) {
							mEmailView
									.setError(getString(R.string.error_field_required));
							focusView = mEmailView;
							cancel = true;
						} else if (!mEmail.contains("@")) {
							mEmailView
									.setError(getString(R.string.error_invalid_email));
							focusView = mEmailView;
							cancel = true;
						}

						if (cancel) {
							// There was an error; don't attempt login and focus
							// the first
							// form field with an error.
							focusView.requestFocus();
						} else {
							// Show a progress spinner, and kick off a
							// background task to
							// perform the user login attempt.
							mLoginStatusMessageView
									.setText(R.string.login_progress_signing_in);
							showProgress(true);
							// mAuthTask = new UserLoginTask();
							// mAuthTask.execute((Void) null);
							attemptLogin();

						}
					}
				});
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		// This is our REST action.
		Uri twitterSearchUri = Uri
				.parse("http://wall.wall.cloudbees.net/api/v1/oauth/token");

		// Here we are going to place our REST call parameters. Note that
		// we could have just used Uri.Builder and appendQueryParameter()
		// here, but I wanted to illustrate how to use the Bundle params.
		Bundle params = new Bundle();
		params.putString("grant_type", "password");
		params.putString("client_id", "mobileV1");
		params.putString("client_secret", "abc123456");
		params.putString("username", mEmail);
		params.putString("password", mPassword);

		// These are the loader arguments. They are stored in a Bundle
		// because
		// LoaderManager will maintain the state of our Loaders for us and
		// reload the Loader if necessary. This is the whole reason why
		// we have even bothered to implement RESTLoader.
		Bundle args = new Bundle();
		args.putParcelable(ARGS_URI, twitterSearchUri);
		args.putParcelable(ARGS_PARAMS, params);

		// Initialize the Loader.
		getLoaderManager().initLoader(LOADER_TWITTER_SEARCH, args, this);

	}

	@Override
	public Loader<RESTLoader.RESTResponse> onCreateLoader(int id, Bundle args) {
		if (args != null && args.containsKey(ARGS_URI)
				&& args.containsKey(ARGS_PARAMS)) {
			Uri action = args.getParcelable(ARGS_URI);
			Bundle params = args.getParcelable(ARGS_PARAMS);

			return new RESTLoader(this, RESTLoader.HTTPVerb.POST, action,
					params);
		}

		return null;
	}

	@Override
	public void onLoadFinished(Loader<RESTLoader.RESTResponse> loader,
			RESTLoader.RESTResponse data) {
		showProgress(false);
		mAuthTask = null;
		int code = data.getCode();
		String json = data.getData();

		// Check to see if we got an HTTP 200 code and have some data.
		if (code == 200 && !json.equals("")) {
			// For really complicated JSON decoding I usually do my heavy
			// lifting
			// Gson and proper model classes, but for now let's keep it simple
			// and use a utility method that relies on some of the built in
			// JSON utilities on Android.

			// Load our list adapter with our Tweets.
			intent = new Intent(this, RESTLoaderActivity.class);

			try {
				JSONObject accessTokenWrapper = (JSONObject) new JSONTokener(
						json).nextValue();
				String accessToken = accessTokenWrapper
						.getString("access_token");
				if (accessToken != null && !accessToken.isEmpty()) {
					intent.putExtra(ACCESS_TOKEN, accessToken);
					startActivity(intent);
				} else {
					Log.e("REST API Boxes Failed", "Empty access token");
				}
			} catch (JSONException e) {
				Log.e("REST API Boxes Failed", "Failed to parse JSON.", e);
			}

			finish();

		} else {
			mPasswordView
					.setError(getString(R.string.error_incorrect_password));
			mPasswordView.requestFocus();
		}
	}

	@Override
	public void onLoaderReset(Loader<RESTLoader.RESTResponse> loader) {

	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			try {
				// Simulate network access.
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return false;
			}

			for (String credential : DUMMY_CREDENTIALS) {
				String[] pieces = credential.split(":");
				if (pieces[0].equals(mEmail)) {
					// Account exists, return true if the password matches.
					return pieces[1].equals(mPassword);
				}
			}

			// TODO: register the new account here.
			return true;
		}

	}
}
