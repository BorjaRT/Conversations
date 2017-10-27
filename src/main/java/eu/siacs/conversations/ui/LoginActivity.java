package eu.siacs.conversations.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.crypto.axolotl.AxolotlService;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.services.BarcodeProvider;
import eu.siacs.conversations.services.XmppConnectionService;
import eu.siacs.conversations.ui.adapter.KnownHostsAdapter;
import eu.siacs.conversations.utils.XmppUri;
import eu.siacs.conversations.xml.Element;
import eu.siacs.conversations.xmpp.OnKeyStatusUpdated;
import eu.siacs.conversations.xmpp.OnUpdateBlocklist;
import eu.siacs.conversations.xmpp.XmppConnection;
import eu.siacs.conversations.xmpp.forms.Data;
import eu.siacs.conversations.xmpp.jid.InvalidJidException;
import eu.siacs.conversations.xmpp.jid.Jid;
import eu.siacs.conversations.xmpp.pep.Avatar;

/**
 * Desarrollada a partir de EditAccountActivity.java
 * @see EditAccountActivity
 */
public class LoginActivity extends OmemoActivity implements XmppConnectionService.OnAccountUpdate, OnUpdateBlocklist,
        OnKeyStatusUpdated, XmppConnectionService.OnCaptchaRequested, KeyChainAliasCallback, XmppConnectionService.OnShowErrorToast, XmppConnectionService.OnMamPreferencesFetched{

    private static final int REQUEST_DATA_SAVER = 0x37af244;
    private EditText etAccountJid;
    private EditText etPassword;
    private Button btnLogin;

    private TextView mAccountJidLabel;
    private AlertDialog mCaptchaDialog = null;

    private Jid jidToEdit;
    private boolean mInitMode = false;
    private boolean mUsernameMode = Config.DOMAIN_LOCK != null;
    private Account mAccount;

    private boolean mFetchingAvatar = false;

    private Toast mFetchingMamPrefsToast;
    private String mSavedInstanceAccount;
    private boolean mSavedInstanceInit = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mSavedInstanceAccount = savedInstanceState.getString("account");
            this.mSavedInstanceInit = savedInstanceState.getBoolean("initMode", false);
        }

        setContentView(R.layout.activity_login_layout);

        this.etAccountJid = (EditText) findViewById(R.id.account_jid);
        this.etAccountJid.addTextChangedListener(this.mTextWatcher);
        this.mAccountJidLabel = (TextView) findViewById(R.id.account_jid_label);
        this.etPassword = (EditText) findViewById(R.id.account_password);
        this.etPassword.addTextChangedListener(this.mTextWatcher);
        this.btnLogin = (Button) findViewById(R.id.btn_login);
        this.btnLogin.setOnClickListener(this.onLoginButtonClickListener);
    }

    private final View.OnClickListener onLoginButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final String password = etPassword.getText().toString();

            if (mInitMode && mAccount != null) {
                mAccount.setOption(Account.OPTION_DISABLED, false);
            }
            if (mAccount != null && mAccount.getStatus() == Account.State.DISABLED && !accountInfoEdited()) {
                mAccount.setOption(Account.OPTION_DISABLED, false);
                if (!xmppConnectionService.updateAccount(mAccount)) {
                    Toast.makeText(LoginActivity.this, R.string.unable_to_update_account,Toast.LENGTH_SHORT).show();
                }else{
                    xmppConnectionService.getCustomApplication().setUserAccount(mAccount);
                }
                return;
            }

            if (mUsernameMode && etAccountJid.getText().toString().contains("@")) {
                etAccountJid.setError(getString(R.string.invalid_username));
                etAccountJid.requestFocus();
                return;
            }

            final Jid jid;
            try {
                if (mUsernameMode) {
                    jid = Jid.fromParts(etAccountJid.getText().toString(), getUserModeDomain(), null);
                } else {
                    jid = Jid.fromString(etAccountJid.getText().toString());
                }
            } catch (final InvalidJidException e) {
                if (mUsernameMode) {
                    etAccountJid.setError(getString(R.string.invalid_username));
                } else {
                    etAccountJid.setError(getString(R.string.invalid_jid));
                }
                etAccountJid.requestFocus();
                return;
            }

            String hostname = null;
            int numericPort = 5222;

            if (jid.isDomainJid()) {
                if (mUsernameMode) {
                    etAccountJid.setError(getString(R.string.invalid_username));
                } else {
                    etAccountJid.setError(getString(R.string.invalid_jid));
                }
                etAccountJid.requestFocus();
                return;
            }

            if (mAccount != null) {
                if (mInitMode && mAccount.isOptionSet(Account.OPTION_MAGIC_CREATE)) {
                    mAccount.setOption(Account.OPTION_MAGIC_CREATE, mAccount.getPassword().contains(password));
                }
                mAccount.setJid(jid);
                mAccount.setPort(numericPort);
                mAccount.setHostname(hostname);
                etAccountJid.setError(null);
                mAccount.setPassword(password);
                //CHECK
                mAccount.setOption(Account.OPTION_REGISTER, false);

                if (!xmppConnectionService.updateAccount(mAccount)) {
                    Toast.makeText(LoginActivity.this,R.string.unable_to_update_account,Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    xmppConnectionService.getCustomApplication().setUserAccount(mAccount);
                }
            } else {
                if (xmppConnectionService.findAccountByJid(jid) != null) {
                    etAccountJid.setError(getString(R.string.account_already_exists));
                    etAccountJid.requestFocus();
                    return;
                }
                //CHECK
                mAccount = new Account(jid.toBareJid(), password);
                mAccount.setPort(numericPort);
                mAccount.setHostname(hostname);
                mAccount.setOption(Account.OPTION_USETLS, true);
                mAccount.setOption(Account.OPTION_USECOMPRESSION, true);
                //CHECK
                mAccount.setOption(Account.OPTION_REGISTER, false);

                xmppConnectionService.createAccount(mAccount);
                xmppConnectionService.getCustomApplication().setUserAccount(mAccount);
            }

            if (!mAccount.isOptionSet(Account.OPTION_DISABLED)
                    && !mInitMode) {
                finish();
            } else {
                updateSaveButton();
                updateAccountInformation(true);
            }
        }
    };

    public void refreshUiReal() {
        invalidateOptionsMenu();
        if (mAccount != null
                && mAccount.getStatus() != Account.State.ONLINE
                && mFetchingAvatar) {
            startActivity(new Intent(getApplicationContext(),
                    ManageAccountActivity.class));
            finish();
        } else if (mInitMode && mAccount != null && mAccount.getStatus() == Account.State.ONLINE) {
            if (!mFetchingAvatar) {
                mFetchingAvatar = true;
                xmppConnectionService.checkForAvatar(mAccount, mAvatarFetchCallback);
            }
        }
        if (mAccount != null) {
            updateAccountInformation(false);
        }
        updateSaveButton();
    }

    @Override
    public boolean onNavigateUp() {
        deleteMagicCreatedAccountAndReturnIfNecessary();
        return super.onNavigateUp();
    }

    @Override
    public void onBackPressed() {
        deleteMagicCreatedAccountAndReturnIfNecessary();
        super.onBackPressed();
    }

    private void deleteMagicCreatedAccountAndReturnIfNecessary() {
        if (Config.MAGIC_CREATE_DOMAIN != null
                && mAccount != null
                && mAccount.isOptionSet(Account.OPTION_MAGIC_CREATE)
                && mAccount.isOptionSet(Account.OPTION_REGISTER)
                && xmppConnectionService.getAccounts().size() == 1) {
            xmppConnectionService.deleteAccount(mAccount);
            startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
        }
    }

    @Override
    public void onAccountUpdate() {
        refreshUi();
    }

    private final UiCallback<Avatar> mAvatarFetchCallback = new UiCallback<Avatar>() {

        @Override
        public void userInputRequried(final PendingIntent pi, final Avatar avatar) {
            finishInitialSetup(avatar);
        }

        @Override
        public void success(final Avatar avatar) {
            finishInitialSetup(avatar);
        }

        @Override
        public void error(final int errorCode, final Avatar avatar) {
            finishInitialSetup(avatar);
        }
    };
    private final TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
            updateSaveButton();
        }

        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
        }

        @Override
        public void afterTextChanged(final Editable s) {

        }
    };

    protected void finishInitialSetup(final Avatar avatar) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                hideKeyboard();
                final Intent intent;
                final XmppConnection connection = mAccount.getXmppConnection();
                final boolean wasFirstAccount = xmppConnectionService != null && xmppConnectionService.getAccounts().size() == 1;
                if (avatar != null || (connection != null && !connection.getFeatures().pep())) {
                    intent = new Intent(getApplicationContext(), StartConversationActivity.class);
                    if (wasFirstAccount) {
                        intent.putExtra(ConversationActivity.INTENT_EXTRA_INIT_MODE, true);
                    }
                } else {
                    intent = new Intent(getApplicationContext(), PublishProfilePictureActivity.class);
                    intent.putExtra(EXTRA_ACCOUNT, mAccount.getJid().toBareJid().toString());
                    intent.putExtra("setup", true);
                }
                if (wasFirstAccount) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_BATTERY_OP || requestCode == REQUEST_DATA_SAVER) {
            updateAccountInformation(mAccount == null);
        }
    }

    @Override
    protected void processFingerprintVerification(XmppUri uri) {
        if (mAccount != null && mAccount.getJid().toBareJid().equals(uri.getJid()) && uri.hasFingerprints()) {
            if (xmppConnectionService.verifyFingerprints(mAccount,uri.getFingerprints())) {
                Toast.makeText(this,R.string.verified_fingerprints,Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,R.string.invalid_barcode,Toast.LENGTH_SHORT).show();
        }
    }

    protected void updateSaveButton() {
        boolean accountInfoEdited = accountInfoEdited();

        if (!mInitMode && passwordChangedInMagicCreateMode()) {
            this.btnLogin.setText(R.string.change_password);
            this.btnLogin.setEnabled(true);
            this.btnLogin.setTextColor(getPrimaryTextColor());
        } else if (accountInfoEdited && !mInitMode) {
            this.btnLogin.setText(R.string.save);
            this.btnLogin.setEnabled(true);
            this.btnLogin.setTextColor(getPrimaryTextColor());
        } else if (mAccount != null
                && (mAccount.getStatus() == Account.State.CONNECTING || mAccount.getStatus() == Account.State.REGISTRATION_SUCCESSFUL|| mFetchingAvatar)) {
            this.btnLogin.setEnabled(false);
            this.btnLogin.setTextColor(getSecondaryTextColor());
            this.btnLogin.setText(R.string.account_status_connecting);
        } else if (mAccount != null && mAccount.getStatus() == Account.State.DISABLED && !mInitMode) {
            this.btnLogin.setEnabled(true);
            this.btnLogin.setTextColor(getPrimaryTextColor());
            this.btnLogin.setText(R.string.enable);
        } else {
            this.btnLogin.setEnabled(true);
            this.btnLogin.setTextColor(getPrimaryTextColor());
            if (!mInitMode) {
                if (mAccount != null && mAccount.isOnlineAndConnected()) {
                    this.btnLogin.setText(R.string.save);
                    if (!accountInfoEdited) {
                        this.btnLogin.setEnabled(false);
                        this.btnLogin.setTextColor(getSecondaryTextColor());
                    }
                } else {
                    this.btnLogin.setText(R.string.connect);
                }
            }
        }
    }

    protected boolean accountInfoEdited() {
        if (this.mAccount == null) {
            return false;
        }
        return jidEdited() ||
                !this.mAccount.getPassword().equals(this.etPassword.getText().toString());
    }

    protected boolean jidEdited() {
        final String unmodified;
        if (mUsernameMode) {
            unmodified = this.mAccount.getJid().getLocalpart();
        } else {
            unmodified = this.mAccount.getJid().toBareJid().toString();
        }
        return !unmodified.equals(this.etAccountJid.getText().toString());
    }

    protected boolean passwordChangedInMagicCreateMode() {
        return mAccount != null
                && mAccount.isOptionSet(Account.OPTION_MAGIC_CREATE)
                && !this.mAccount.getPassword().equals(this.etPassword.getText().toString())
                && !this.jidEdited()
                && mAccount.isOnlineAndConnected();
    }

    @Override
    protected String getShareableUri(boolean http) {
        if (mAccount != null) {
            return http ? mAccount.getShareableLink() : mAccount.getShareableUri();
        } else {
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.editaccount, menu);
        final MenuItem showBlocklist = menu.findItem(R.id.action_show_block_list);
        final MenuItem showMoreInfo = menu.findItem(R.id.action_server_info_show_more);
        final MenuItem changePassword = menu.findItem(R.id.action_change_password_on_server);
        final MenuItem showPassword = menu.findItem(R.id.action_show_password);
        final MenuItem renewCertificate = menu.findItem(R.id.action_renew_certificate);
        final MenuItem mamPrefs = menu.findItem(R.id.action_mam_prefs);
        final MenuItem changePresence = menu.findItem(R.id.action_change_presence);
        final MenuItem share = menu.findItem(R.id.action_share);
        renewCertificate.setVisible(mAccount != null && mAccount.getPrivateKeyAlias() != null);

        share.setVisible(mAccount != null && !mInitMode);

        if (mAccount != null && mAccount.isOnlineAndConnected()) {
            if (!mAccount.getXmppConnection().getFeatures().blocking()) {
                showBlocklist.setVisible(false);
            }

            if (!mAccount.getXmppConnection().getFeatures().register()) {
                changePassword.setVisible(false);
            }
            mamPrefs.setVisible(mAccount.getXmppConnection().getFeatures().mam());
            changePresence.setVisible(manuallyChangePresence());
        } else {
            showBlocklist.setVisible(false);
            showMoreInfo.setVisible(false);
            changePassword.setVisible(false);
            mamPrefs.setVisible(false);
            changePresence.setVisible(false);
        }

        if (mAccount != null) {
            showPassword.setVisible(mAccount.isOptionSet(Account.OPTION_MAGIC_CREATE)
                    && !mAccount.isOptionSet(Account.OPTION_REGISTER));
        } else {
            showPassword.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem showMoreInfo = menu.findItem(R.id.action_server_info_show_more);
        if (showMoreInfo.isVisible()) {
            showMoreInfo.setChecked(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final int theme = findTheme();
        if (this.mTheme != theme) {
            recreate();
        } else if (getIntent() != null) {
            try {
                this.jidToEdit = Jid.fromString(getIntent().getStringExtra("jid"));
            } catch (final InvalidJidException | NullPointerException ignored) {
                this.jidToEdit = null;
            }
            boolean init = getIntent().getBooleanExtra(ConversationActivity.INTENT_EXTRA_INIT_MODE, false);
            this.mInitMode = init || this.jidToEdit == null;

            ActionBar ab = getActionBar();
            if (ab != null) {
                ab.setDisplayShowHomeEnabled(false);
                ab.setDisplayHomeAsUpEnabled(false);
                ab.setTitle(R.string.title_login);
            }
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        if (mAccount != null) {
            savedInstanceState.putString("account", mAccount.getJid().toBareJid().toString());
            savedInstanceState.putBoolean("initMode", mInitMode);
            savedInstanceState.putBoolean("showMoreTable",false);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void onBackendConnected() {
        boolean init = true;
        if (mSavedInstanceAccount != null) {
            try {
                this.mAccount = xmppConnectionService.findAccountByJid(Jid.fromString(mSavedInstanceAccount));
                this.mInitMode = mSavedInstanceInit;
                init = false;
            } catch (InvalidJidException e) {
                this.mAccount = null;
            }

        } else if (this.jidToEdit != null) {
            this.mAccount = xmppConnectionService.findAccountByJid(jidToEdit);
        }

        if (mAccount != null) {
            this.mInitMode |= this.mAccount.isOptionSet(Account.OPTION_REGISTER);
            this.mUsernameMode |= mAccount.isOptionSet(Account.OPTION_MAGIC_CREATE) && mAccount.isOptionSet(Account.OPTION_REGISTER);
            if (this.mAccount.getPrivateKeyAlias() != null) {
                this.etPassword.setHint(R.string.authenticate_with_certificate);
                if (this.mInitMode) {
                    this.etPassword.requestFocus();
                }
            }
            if (mPendingFingerprintVerificationUri != null) {
                processFingerprintVerification(mPendingFingerprintVerificationUri);
                mPendingFingerprintVerificationUri = null;
            }
            updateAccountInformation(init);
        }

        if (mUsernameMode) {
            this.mAccountJidLabel.setText(R.string.username);
            this.etAccountJid.setHint(R.string.username_hint);
        }

        updateSaveButton();
        invalidateOptionsMenu();
    }

    private String getUserModeDomain() {
        if (mAccount != null) {
            return mAccount.getJid().getDomainpart();
        } else {
            return Config.DOMAIN_LOCK;
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_block_list:
                final Intent showBlocklistIntent = new Intent(this, BlocklistActivity.class);
                showBlocklistIntent.putExtra(EXTRA_ACCOUNT, mAccount.getJid().toString());
                startActivity(showBlocklistIntent);
                break;
            case R.id.action_server_info_show_more:

                break;
            case R.id.action_share_barcode:
                shareBarcode();
                break;
            case R.id.action_share_http:
                shareLink(true);
                break;
            case R.id.action_share_uri:
                shareLink(false);
                break;
            case R.id.action_change_password_on_server:
                gotoChangePassword(null);
                break;
            case R.id.action_mam_prefs:
                editMamPrefs();
                break;
            case R.id.action_renew_certificate:
                renewCertificate();
                break;
            case R.id.action_change_presence:
                changePresence();
                break;
            case R.id.action_show_password:
                showPassword();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareBarcode() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, BarcodeProvider.getUriForAccount(this,mAccount));
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent, getText(R.string.share_with)));
    }

    private void gotoChangePassword(String newPassword) {
        final Intent changePasswordIntent = new Intent(this, ChangePasswordActivity.class);
        changePasswordIntent.putExtra(EXTRA_ACCOUNT, mAccount.getJid().toString());
        if (newPassword != null) {
            changePasswordIntent.putExtra("password", newPassword);
        }
        startActivity(changePasswordIntent);
    }

    private void renewCertificate() {
        KeyChain.choosePrivateKeyAlias(this, this, null, null, null, -1, null);
    }

    private void changePresence() {
        Intent intent = new Intent(this, SetPresenceActivity.class);
        intent.putExtra(SetPresenceActivity.EXTRA_ACCOUNT,mAccount.getJid().toBareJid().toString());
        startActivity(intent);
    }

    @Override
    public void alias(String alias) {
        if (alias != null) {
            xmppConnectionService.updateKeyInAccount(mAccount, alias);
        }
    }

    private void updateAccountInformation(boolean init) {
        if (init) {
            this.etAccountJid.getEditableText().clear();
            if (mUsernameMode) {
                this.etAccountJid.getEditableText().append(this.mAccount.getJid().getLocalpart());
            } else {
                this.etAccountJid.getEditableText().append(this.mAccount.getJid().toBareJid().toString());
            }
            this.etPassword.getEditableText().clear();
            this.etPassword.getEditableText().append(this.mAccount.getPassword());
        }

        final boolean editable = !mAccount.isOptionSet(Account.OPTION_LOGGED_IN_SUCCESSFULLY);
        this.etAccountJid.setEnabled(editable);
        this.etAccountJid.setFocusable(editable);
        this.etAccountJid.setFocusableInTouchMode(editable);

        if (this.mAccount.isOnlineAndConnected() && !this.mFetchingAvatar) {

        } else {
            if (this.mAccount.errorStatus()) {
                final EditText errorTextField;
                if (this.mAccount.getStatus() == Account.State.UNAUTHORIZED) {
                    errorTextField = this.etPassword;
                } else {
                    errorTextField = this.etAccountJid;
                }
                errorTextField.setError(getString(this.mAccount.getStatus().getReadableId()));
                if (init || !accountInfoEdited()) {
                    errorTextField.requestFocus();
                }
            } else {
                this.etAccountJid.setError(null);
                this.etPassword.setError(null);
            }
        }
    }

    private void editMamPrefs() {
        this.mFetchingMamPrefsToast = Toast.makeText(this, R.string.fetching_mam_prefs, Toast.LENGTH_LONG);
        this.mFetchingMamPrefsToast.show();
        xmppConnectionService.fetchMamPreferences(mAccount, this);
    }

    private void showPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_show_password, null);
        TextView password = (TextView) view.findViewById(R.id.password);
        password.setText(mAccount.getPassword());
        builder.setTitle(R.string.password);
        builder.setView(view);
        builder.setPositiveButton(R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public void onKeyStatusUpdated(AxolotlService.FetchStatus report) {
        refreshUi();
    }

    @Override
    public void onCaptchaRequested(final Account account, final String id, final Data data, final Bitmap captcha) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((mCaptchaDialog != null) && mCaptchaDialog.isShowing()) {
                    mCaptchaDialog.dismiss();
                }
                final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                final View view = getLayoutInflater().inflate(R.layout.captcha, null);
                final ImageView imageView = (ImageView) view.findViewById(R.id.captcha);
                final EditText input = (EditText) view.findViewById(R.id.input);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                imageView.setImageBitmap(captcha);

                builder.setTitle(getString(R.string.captcha_required));
                builder.setView(view);

                builder.setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String rc = input.getText().toString();
                                data.put("username", account.getUsername());
                                data.put("password", account.getPassword());
                                data.put("ocr", rc);
                                data.submit();

                                if (xmppConnectionServiceBound) {
                                    xmppConnectionService.sendCreateAccountWithCaptchaPacket(
                                            account, id, data);
                                }
                            }
                        });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (xmppConnectionService != null) {
                            xmppConnectionService.sendCreateAccountWithCaptchaPacket(account, null, null);
                        }
                    }
                });

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (xmppConnectionService != null) {
                            xmppConnectionService.sendCreateAccountWithCaptchaPacket(account, null, null);
                        }
                    }
                });
                mCaptchaDialog = builder.create();
                mCaptchaDialog.show();
                input.requestFocus();
            }
        });
    }

    public void onShowErrorToast(final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, resId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPreferencesFetched(final Element prefs) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mFetchingMamPrefsToast != null) {
                    mFetchingMamPrefsToast.cancel();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle(R.string.server_side_mam_prefs);
                String defaultAttr = prefs.getAttribute("default");
                final List<String> defaults = Arrays.asList("never", "roster", "always");
                final AtomicInteger choice = new AtomicInteger(Math.max(0,defaults.indexOf(defaultAttr)));
                builder.setSingleChoiceItems(R.array.mam_prefs, choice.get(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        choice.set(which);
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.setAttribute("default",defaults.get(choice.get()));
                        xmppConnectionService.pushMamPreferences(mAccount, prefs);
                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    public void onPreferencesFetchFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mFetchingMamPrefsToast != null) {
                    mFetchingMamPrefsToast.cancel();
                }
                Toast.makeText(LoginActivity.this,R.string.unable_to_fetch_mam_prefs,Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void OnUpdateBlocklist(OnUpdateBlocklist.Status status) {
        refreshUi();
    }
}
