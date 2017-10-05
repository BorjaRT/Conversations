package eu.siacs.conversations.ui;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.openintents.openpgp.util.OpenPgpUtils;

import java.util.Set;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.crypto.axolotl.AxolotlService;
import eu.siacs.conversations.crypto.axolotl.XmppAxolotlSession;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.ui.XmppActivity;
import eu.siacs.conversations.ui.adapter.KnownHostsAdapter;
import eu.siacs.conversations.utils.CryptoHelper;
import eu.siacs.conversations.utils.UIHelper;
import eu.siacs.conversations.utils.XmppUri;
import eu.siacs.conversations.xmpp.XmppConnection;
import eu.siacs.conversations.xmpp.jid.InvalidJidException;
import eu.siacs.conversations.xmpp.jid.Jid;

/**
 * Desarrollada a partir de EditAccountActivity.java
 * @see EditAccountActivity
 */
public class LoginActivity extends OmemoActivity {

    private static final int REQUEST_DATA_SAVER = 0x37af244;
    private AutoCompleteTextView accountJid;
    private EditText password;
    private EditText passwordConfirm;
    private CheckBox registerNew;
    private Button cancelButton;
    private Button saveButton;
    private Button disableOsOptimizationsButton;
    private TextView disableOsOptimizationsHeadline;
    private TextView getmDisableOsOptimizationsBody;
    private TableLayout moreTable;

    private LinearLayout stats;
    private RelativeLayout osOptimizations;
    private TextView serverInfoSm;
    private TextView serverInfoRosterVersion;
    private TextView serverInfoCarbons;
    private TextView serverInfoMam;
    private TextView serverInfoCSI;
    private TextView serverInfoBlocking;
    private TextView serverInfoPep;
    private TextView serverInfoHttpUpload;
    private TextView serverInfoPush;
    private TextView sessionEst;
    private TextView otrFingerprint;
    private TextView axolotlFingerprint;
    private TextView pgpFingerprint;
    private TextView ownFingerprintDesc;
    private TextView otrFingerprintDesc;
    private TextView getmPgpFingerprintDesc;
    private TextView accountJidLabel;
    private ImageView avatar;
    private RelativeLayout otrFingerprintBox;
    private RelativeLayout axolotlFingerprintBox;
    private RelativeLayout pgpFingerprintBox;
    private ImageButton otrFingerprintToClipboardButton;
    private ImageButton axolotlFingerprintToClipboardButton;
    private ImageButton pgpDeleteFingerprintButton;
    private LinearLayout keys;
    private LinearLayout keysCard;
    private LinearLayout namePort;
    private EditText hostname;
    private EditText port;
    private AlertDialog captchaDialog = null;

    private Jid jidToEdit;
    private boolean initMode = false;
    private boolean usernameMode = Config.DOMAIN_LOCK != null;
    private boolean showOptions = false;
    private Account account;
    private String messageFingerprint;

    private boolean fetchingAvatar = false;

    private Toast fetchingMamPrefsToast;
    private TableRow pushRow;
    private String savedInstanceAccount;
    private boolean savedInstanceInit = false;
    private Button clearDevicesButton;

    //TODO ??
    private final TextWatcher textWatcher = new TextWatcher() {

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

    @Override
    protected void refreshUiReal() {
        //TODO
    }

    @Override
    void onBackendConnected() {
        //TODO
        boolean init = true;
        if (savedInstanceAccount != null) {
            try {
                this.account = xmppConnectionService.findAccountByJid(Jid.fromString(savedInstanceAccount));
                this.initMode = savedInstanceInit;
                init = false;
            } catch (InvalidJidException e) {
                this.account = null;
            }

        } else if (this.jidToEdit != null) {
            this.account = xmppConnectionService.findAccountByJid(jidToEdit);
        }

        if (account != null) {
            this.initMode |= this.account.isOptionSet(Account.OPTION_REGISTER);
            this.usernameMode |= account.isOptionSet(Account.OPTION_MAGIC_CREATE) && account.isOptionSet(Account.OPTION_REGISTER);
            if (this.account.getPrivateKeyAlias() != null) {
                this.password.setHint(R.string.authenticate_with_certificate);
                if (this.initMode) {
                    this.password.requestFocus();
                }
            }
            if (mPendingFingerprintVerificationUri != null) {
                processFingerprintVerification(mPendingFingerprintVerificationUri);
                mPendingFingerprintVerificationUri = null;
            }
            updateAccountInformation(init);
        }


        if (Config.MAGIC_CREATE_DOMAIN == null && this.xmppConnectionService.getAccounts().size() == 0) {
            this.cancelButton.setEnabled(false);
            this.cancelButton.setTextColor(getSecondaryTextColor());
        }
        if (usernameMode) {
            this.accountJidLabel.setText(R.string.username);
            this.accountJid.setHint(R.string.username_hint);
        } else {
            final KnownHostsAdapter mKnownHostsAdapter = new KnownHostsAdapter(this,
                    R.layout.simple_list_item,
                    xmppConnectionService.getKnownHosts());
            this.accountJid.setAdapter(mKnownHostsAdapter);
        }
        updateSaveButton();
        invalidateOptionsMenu();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        //TODO
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        if (savedInstanceState != null) {
            this.savedInstanceAccount = savedInstanceState.getString("account");
            this.savedInstanceInit = savedInstanceState.getBoolean("initMode", false);//TODO ??
        }

        setContentView(R.layout.activity_edit_account);

        this.accountJid = (AutoCompleteTextView) findViewById(R.id.account_jid);
        this.accountJid.addTextChangedListener(this.textWatcher);
        this.accountJidLabel = (TextView) findViewById(R.id.account_jid_label);
        this.password = (EditText) findViewById(R.id.account_password);
        this.password.addTextChangedListener(this.textWatcher);
        this.passwordConfirm = (EditText) findViewById(R.id.account_password_confirm);
        this.avatar = (ImageView) findViewById(R.id.avater);
        this.avatar.setOnClickListener(this.avatarClickListener);
        this.registerNew = (CheckBox) findViewById(R.id.account_register_new);
        this.stats = (LinearLayout) findViewById(R.id.stats);
        this.osOptimizations = (RelativeLayout) findViewById(R.id.os_optimization);
        this.disableOsOptimizationsButton = (Button) findViewById(R.id.os_optimization_disable);
        this.disableOsOptimizationsHeadline = (TextView) findViewById(R.id.os_optimization_headline);
        this.getmDisableOsOptimizationsBody = (TextView) findViewById(R.id.os_optimization_body);
        this.sessionEst = (TextView) findViewById(R.id.session_est);
        this.serverInfoRosterVersion = (TextView) findViewById(R.id.server_info_roster_version);
        this.serverInfoCarbons = (TextView) findViewById(R.id.server_info_carbons);
        this.serverInfoMam = (TextView) findViewById(R.id.server_info_mam);
        this.serverInfoCSI = (TextView) findViewById(R.id.server_info_csi);
        this.serverInfoBlocking = (TextView) findViewById(R.id.server_info_blocking);
        this.serverInfoSm = (TextView) findViewById(R.id.server_info_sm);
        this.serverInfoPep = (TextView) findViewById(R.id.server_info_pep);
        this.serverInfoHttpUpload = (TextView) findViewById(R.id.server_info_http_upload);
        this.pushRow = (TableRow) findViewById(R.id.push_row);
        this.serverInfoPush = (TextView) findViewById(R.id.server_info_push);
        this.pgpFingerprintBox = (RelativeLayout) findViewById(R.id.pgp_fingerprint_box);
        this.pgpFingerprint = (TextView) findViewById(R.id.pgp_fingerprint);
        this.getmPgpFingerprintDesc = (TextView) findViewById(R.id.pgp_fingerprint_desc);
        this.pgpDeleteFingerprintButton = (ImageButton) findViewById(R.id.action_delete_pgp);
        this.otrFingerprint = (TextView) findViewById(R.id.otr_fingerprint);
        this.otrFingerprintDesc = (TextView) findViewById(R.id.otr_fingerprint_desc);
        this.otrFingerprintBox = (RelativeLayout) findViewById(R.id.otr_fingerprint_box);
        this.otrFingerprintToClipboardButton = (ImageButton) findViewById(R.id.action_copy_to_clipboard);
        this.axolotlFingerprint = (TextView) findViewById(R.id.axolotl_fingerprint);
        this.axolotlFingerprintBox = (RelativeLayout) findViewById(R.id.axolotl_fingerprint_box);
        this.axolotlFingerprintToClipboardButton = (ImageButton) findViewById(R.id.action_copy_axolotl_to_clipboard);
        this.ownFingerprintDesc = (TextView) findViewById(R.id.own_fingerprint_desc);
        this.keysCard = (LinearLayout) findViewById(R.id.other_device_keys_card);
        this.keys = (LinearLayout) findViewById(R.id.other_device_keys);
        this.namePort = (LinearLayout) findViewById(R.id.name_port);
        this.hostname = (EditText) findViewById(R.id.hostname);
        this.hostname.addTextChangedListener(textWatcher);
        this.clearDevicesButton = (Button) findViewById(R.id.clear_devices);
        this.clearDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWipePepDialog();
            }
        });
        this.port = (EditText) findViewById(R.id.port);
        this.port.setText("5222");
        this.port.addTextChangedListener(textWatcher);
        this.saveButton = (Button) findViewById(R.id.save_button);
        this.cancelButton = (Button) findViewById(R.id.cancel_button);
        this.saveButton.setOnClickListener(this.saveButtonClickListener);
        this.cancelButton.setOnClickListener(this.cancelButtonClickListener);
        this.moreTable = (TableLayout) findViewById(R.id.server_info_more);
        if (savedInstanceState != null && savedInstanceState.getBoolean("showMoreTable")) {
            changeMoreTableVisibility(true);
        }
        final CompoundButton.OnCheckedChangeListener OnCheckedShowConfirmPassword = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView,
                                         final boolean isChecked) {
                if (isChecked) {
                    passwordConfirm.setVisibility(View.VISIBLE);
                } else {
                    passwordConfirm.setVisibility(View.GONE);
                }
                updateSaveButton();
            }
        };
        this.registerNew.setOnCheckedChangeListener(OnCheckedShowConfirmPassword);
        if (Config.DISALLOW_REGISTRATION_IN_UI) {
            this.registerNew.setVisibility(View.GONE);
        }
    }

    @Override
    protected void processFingerprintVerification(XmppUri uri) {
        //TODO
    }

    private void updateAccountInformation(boolean init) {
        //todo
//        if (init) {
//            this.accountJid.getEditableText().clear();
//            if (mUsernameMode) {
//                this.accountJid.getEditableText().append(this.account.getJid().getLocalpart());
//            } else {
//                this.accountJid.getEditableText().append(this.account.getJid().toBareJid().toString());
//            }
//            this.mPassword.getEditableText().clear();
//            this.mPassword.getEditableText().append(this.account.getPassword());
//            this.mHostname.setText("");
//            this.mHostname.getEditableText().append(this.account.getHostname());
//            this.mPort.setText("");
//            this.mPort.getEditableText().append(String.valueOf(this.account.getPort()));
//            this.mNamePort.setVisibility(mShowOptions ? View.VISIBLE : View.GONE);
//
//        }
//
//        final boolean editable = !account.isOptionSet(Account.OPTION_LOGGED_IN_SUCCESSFULLY);
//        this.accountJid.setEnabled(editable);
//        this.accountJid.setFocusable(editable);
//        this.accountJid.setFocusableInTouchMode(editable);
//
//        if (!mInitMode) {
//            this.mAvatar.setVisibility(View.VISIBLE);
//            this.mAvatar.setImageBitmap(avatarService().get(this.account, getPixel(72)));
//        } else {
//            this.mAvatar.setVisibility(View.GONE);
//        }
//        if (this.account.isOptionSet(Account.OPTION_REGISTER)) {
//            this.mRegisterNew.setVisibility(View.VISIBLE);
//            this.mRegisterNew.setChecked(true);
//            this.mPasswordConfirm.setText(this.account.getPassword());
//        } else {
//            this.mRegisterNew.setVisibility(View.GONE);
//            this.mRegisterNew.setChecked(false);
//        }
//        if (this.account.isOnlineAndConnected() && !this.mFetchingAvatar) {
//            XmppConnection.Features features = this.account.getXmppConnection().getFeatures();
//            this.mStats.setVisibility(View.VISIBLE);
//            boolean showBatteryWarning = !xmppConnectionService.getPushManagementService().availableAndUseful(account) && isOptimizingBattery();
//            boolean showDataSaverWarning = isAffectedByDataSaver();
//            showOsOptimizationWarning(showBatteryWarning,showDataSaverWarning);
//            this.mSessionEst.setText(UIHelper.readableTimeDifferenceFull(this, this.account.getXmppConnection()
//                    .getLastSessionEstablished()));
//            if (features.rosterVersioning()) {
//                this.serverInfoRosterVersion.setText(R.string.server_info_available);
//            } else {
//                this.serverInfoRosterVersion.setText(R.string.server_info_unavailable);
//            }
//            if (features.carbons()) {
//                this.serverInfoCarbons.setText(R.string.server_info_available);
//            } else {
//                this.serverInfoCarbons
//                        .setText(R.string.server_info_unavailable);
//            }
//            if (features.mam()) {
//                this.serverInfoMam.setText(R.string.server_info_available);
//            } else {
//                this.serverInfoMam.setText(R.string.server_info_unavailable);
//            }
//            if (features.csi()) {
//                this.serverInfoCSI.setText(R.string.server_info_available);
//            } else {
//                this.serverInfoCSI.setText(R.string.server_info_unavailable);
//            }
//            if (features.blocking()) {
//                this.serverInfoBlocking.setText(R.string.server_info_available);
//            } else {
//                this.serverInfoBlocking.setText(R.string.server_info_unavailable);
//            }
//            if (features.sm()) {
//                this.serverInfoSm.setText(R.string.server_info_available);
//            } else {
//                this.serverInfoSm.setText(R.string.server_info_unavailable);
//            }
//            if (features.pep()) {
//                AxolotlService axolotlService = this.account.getAxolotlService();
//                if (axolotlService != null && axolotlService.isPepBroken()) {
//                    this.serverInfoPep.setText(R.string.server_info_broken);
//                } else if (features.pepPublishOptions()) {
//                    this.serverInfoPep.setText(R.string.server_info_available);
//                } else {
//                    this.serverInfoPep.setText(R.string.server_info_partial);
//                }
//            } else {
//                this.serverInfoPep.setText(R.string.server_info_unavailable);
//            }
//            if (features.httpUpload(0)) {
//                this.serverInfoHttpUpload.setText(R.string.server_info_available);
//            } else {
//                this.serverInfoHttpUpload.setText(R.string.server_info_unavailable);
//            }
//
//            this.mPushRow.setVisibility(xmppConnectionService.getPushManagementService().isStub() ? View.GONE : View.VISIBLE);
//
//            if (xmppConnectionService.getPushManagementService().available(account)) {
//                this.serverInfoPush.setText(R.string.server_info_available);
//            } else {
//                this.serverInfoPush.setText(R.string.server_info_unavailable);
//            }
//            final long pgpKeyId = this.account.getPgpId();
//            if (pgpKeyId != 0 && Config.supportOpenPgp()) {
//                View.OnClickListener openPgp = new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        launchOpenKeyChain(pgpKeyId);
//                    }
//                };
//                View.OnClickListener delete = new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        showDeletePgpDialog();
//                    }
//                };
//                this.mPgpFingerprintBox.setVisibility(View.VISIBLE);
//                this.mPgpFingerprint.setText(OpenPgpUtils.convertKeyIdToHex(pgpKeyId));
//                this.mPgpFingerprint.setOnClickListener(openPgp);
//                if ("pgp".equals(messageFingerprint)) {
//                    this.getmPgpFingerprintDesc.setTextColor(ContextCompat.getColor(this, R.color.accent));
//                }
//                this.getmPgpFingerprintDesc.setOnClickListener(openPgp);
//                this.mPgpDeleteFingerprintButton.setOnClickListener(delete);
//            } else {
//                this.mPgpFingerprintBox.setVisibility(View.GONE);
//            }
//            final String otrFingerprint = this.account.getOtrFingerprint();
//            if (otrFingerprint != null && Config.supportOtr()) {
//                if ("otr".equals(messageFingerprint)) {
//                    this.mOtrFingerprintDesc.setTextColor(ContextCompat.getColor(this, R.color.accent));
//                }
//                this.mOtrFingerprintBox.setVisibility(View.VISIBLE);
//                this.mOtrFingerprint.setText(CryptoHelper.prettifyFingerprint(otrFingerprint));
//                this.mOtrFingerprintToClipboardButton
//                        .setVisibility(View.VISIBLE);
//                this.mOtrFingerprintToClipboardButton
//                        .setOnClickListener(new View.OnClickListener() {
//
//                            @Override
//                            public void onClick(final View v) {
//
//                                if (copyTextToClipboard(CryptoHelper.prettifyFingerprint(otrFingerprint), R.string.otr_fingerprint)) {
//                                    Toast.makeText(
//                                            EditAccountActivity.this,
//                                            R.string.toast_message_otr_fingerprint,
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
//            } else {
//                this.mOtrFingerprintBox.setVisibility(View.GONE);
//            }
//            final String ownAxolotlFingerprint = this.account.getAxolotlService().getOwnFingerprint();
//            if (ownAxolotlFingerprint != null && Config.supportOmemo()) {
//                this.mAxolotlFingerprintBox.setVisibility(View.VISIBLE);
//                if (ownAxolotlFingerprint.equals(messageFingerprint)) {
//                    this.mOwnFingerprintDesc.setTextColor(ContextCompat.getColor(this, R.color.accent));
//                    this.mOwnFingerprintDesc.setText(R.string.omemo_fingerprint_selected_message);
//                } else {
//                    this.mOwnFingerprintDesc.setTextColor(getSecondaryTextColor());
//                    this.mOwnFingerprintDesc.setText(R.string.omemo_fingerprint);
//                }
//                this.mAxolotlFingerprint.setText(CryptoHelper.prettifyFingerprint(ownAxolotlFingerprint.substring(2)));
//                this.mAxolotlFingerprintToClipboardButton
//                        .setVisibility(View.VISIBLE);
//                this.mAxolotlFingerprintToClipboardButton
//                        .setOnClickListener(new View.OnClickListener() {
//
//                            @Override
//                            public void onClick(final View v) {
//                                copyOmemoFingerprint(ownAxolotlFingerprint);
//                            }
//                        });
//            } else {
//                this.mAxolotlFingerprintBox.setVisibility(View.GONE);
//            }
//            boolean hasKeys = false;
//            keys.removeAllViews();
//            for(XmppAxolotlSession session : account.getAxolotlService().findOwnSessions()) {
//                if (!session.getTrust().isCompromised()) {
//                    boolean highlight = session.getFingerprint().equals(messageFingerprint);
//                    addFingerprintRow(keys,session,highlight);
//                    hasKeys = true;
//                }
//            }
//            if (hasKeys && Config.supportOmemo()) {
//                keysCard.setVisibility(View.VISIBLE);
//                Set<Integer> otherDevices = account.getAxolotlService().getOwnDeviceIds();
//                if (otherDevices == null || otherDevices.isEmpty()) {
//                    mClearDevicesButton.setVisibility(View.GONE);
//                } else {
//                    mClearDevicesButton.setVisibility(View.VISIBLE);
//                }
//            } else {
//                keysCard.setVisibility(View.GONE);
//            }
//        } else {
//            if (this.account.errorStatus()) {
//                final EditText errorTextField;
//                if (this.account.getStatus() == Account.State.UNAUTHORIZED) {
//                    errorTextField = this.mPassword;
//                } else if (mShowOptions
//                        && this.account.getStatus() == Account.State.SERVER_NOT_FOUND
//                        && this.mHostname.getText().length() > 0) {
//                    errorTextField = this.mHostname;
//                } else {
//                    errorTextField = this.accountJid;
//                }
//                errorTextField.setError(getString(this.account.getStatus().getReadableId()));
//                if (init || !accountInfoEdited()) {
//                    errorTextField.requestFocus();
//                }
//            } else {
//                this.accountJid.setError(null);
//                this.mPassword.setError(null);
//                this.mHostname.setError(null);
//            }
//            this.mStats.setVisibility(View.GONE);
//        }
    }

    protected void updateSaveButton() {
        //TODO

//        boolean accountInfoEdited = accountInfoEdited();
//
//        if (!mInitMode && passwordChangedInMagicCreateMode()) {
//            this.mSaveButton.setText(R.string.change_password);
//            this.mSaveButton.setEnabled(true);
//            this.mSaveButton.setTextColor(getPrimaryTextColor());
//        } else if (accountInfoEdited && !mInitMode) {
//            this.mSaveButton.setText(R.string.save);
//            this.mSaveButton.setEnabled(true);
//            this.mSaveButton.setTextColor(getPrimaryTextColor());
//        } else if (account != null
//                && (account.getStatus() == Account.State.CONNECTING || account.getStatus() == Account.State.REGISTRATION_SUCCESSFUL|| mFetchingAvatar)) {
//            this.mSaveButton.setEnabled(false);
//            this.mSaveButton.setTextColor(getSecondaryTextColor());
//            this.mSaveButton.setText(R.string.account_status_connecting);
//        } else if (account != null && account.getStatus() == Account.State.DISABLED && !mInitMode) {
//            this.mSaveButton.setEnabled(true);
//            this.mSaveButton.setTextColor(getPrimaryTextColor());
//            this.mSaveButton.setText(R.string.enable);
//        } else {
//            this.mSaveButton.setEnabled(true);
//            this.mSaveButton.setTextColor(getPrimaryTextColor());
//            if (!mInitMode) {
//                if (account != null && account.isOnlineAndConnected()) {
//                    this.mSaveButton.setText(R.string.save);
//                    if (!accountInfoEdited) {
//                        this.mSaveButton.setEnabled(false);
//                        this.mSaveButton.setTextColor(getSecondaryTextColor());
//                    }
//                } else {
//                    this.mSaveButton.setText(R.string.connect);
//                }
//            } else {
//                XmppConnection connection = account == null ? null : account.getXmppConnection();
//                String url = connection != null && account.getStatus() == Account.State.REGISTRATION_WEB ? connection.getWebRegistrationUrl() : null;
//                if (url != null && mRegisterNew.isChecked()) {
//                    this.mSaveButton.setText(R.string.open_website);
//                } else {
//                    this.mSaveButton.setText(R.string.next);
//                }
//            }
//        }
    }

    private void changeMoreTableVisibility(boolean visible) {
        //TODO
//        mMoreTable.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private final View.OnClickListener cancelButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            //TODO
            deleteMagicCreatedAccountAndReturnIfNecessary();
            finish();
        }
    };

    //TODO ??
    private void deleteMagicCreatedAccountAndReturnIfNecessary() {
        if (Config.MAGIC_CREATE_DOMAIN != null
                && account != null
                && account.isOptionSet(Account.OPTION_MAGIC_CREATE)
                && account.isOptionSet(Account.OPTION_REGISTER)
                && xmppConnectionService.getAccounts().size() == 1) {
            xmppConnectionService.deleteAccount(account);
            startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
        }
    }

    //TODO ??
    private final View.OnClickListener saveButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {

            //CHECK

            final String passwordStr = password.getText().toString();
            final String passwordConfirmStr = passwordConfirm.getText().toString();
            final boolean wasDisabled = account != null && account.getStatus() == Account.State.DISABLED;

            if (!initMode && passwordChangedInMagicCreateMode()) {
                gotoChangePassword(passwordStr);
                return;
            }
            if (initMode && account != null) {
                account.setOption(Account.OPTION_DISABLED, false);
            }
            if (account != null && account.getStatus() == Account.State.DISABLED && !accountInfoEdited()) {
                account.setOption(Account.OPTION_DISABLED, false);
                if (!xmppConnectionService.updateAccount(account)) {
                    Toast.makeText(LoginActivity.this,R.string.unable_to_update_account,Toast.LENGTH_SHORT).show();
                }
                return;
            }
            final boolean registerNewAccount = registerNew.isChecked() && !Config.DISALLOW_REGISTRATION_IN_UI;
            if (usernameMode && accountJid.getText().toString().contains("@")) {
                accountJid.setError(getString(R.string.invalid_username));
                accountJid.requestFocus();
                return;
            }

            XmppConnection connection = account == null ? null : account.getXmppConnection();
            String url = connection != null && account.getStatus() == Account.State.REGISTRATION_WEB ? connection.getWebRegistrationUrl() : null;
            if (url != null && registerNewAccount && !wasDisabled) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return;
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(LoginActivity.this,R.string.application_found_to_open_website,Toast.LENGTH_SHORT);
                    return;
                }
            }

            final Jid jid;
            try {
                if (usernameMode) {
                    jid = Jid.fromParts(accountJid.getText().toString(), getUserModeDomain(), null);
                } else {
                    jid = Jid.fromString(accountJid.getText().toString());
                }
            } catch (final InvalidJidException e) {
                if (usernameMode) {
                    accountJid.setError(getString(R.string.invalid_username));
                } else {
                    accountJid.setError(getString(R.string.invalid_jid));
                }
                accountJid.requestFocus();
                return;
            }
            String hostnameStr = null;
            int numericPort = 5222;
            if (showOptions) {
                hostnameStr = hostname.getText().toString().replaceAll("\\s","");
                final String portStr = port.getText().toString().replaceAll("\\s","");
                if (hostnameStr.contains(" ")) {
                    hostname.setError(getString(R.string.not_valid_hostname));
                    hostname.requestFocus();
                    return;
                }
                try {
                    numericPort = Integer.parseInt(portStr);
                    if (numericPort < 0 || numericPort > 65535) {
                        port.setError(getString(R.string.not_a_valid_port));
                        port.requestFocus();
                        return;
                    }

                } catch (NumberFormatException e) {
                    port.setError(getString(R.string.not_a_valid_port));
                    port.requestFocus();
                    return;
                }
            }

            if (jid.isDomainJid()) {
                if (usernameMode) {
                    accountJid.setError(getString(R.string.invalid_username));
                } else {
                    accountJid.setError(getString(R.string.invalid_jid));
                }
                accountJid.requestFocus();
                return;
            }
            if (registerNewAccount) {
                if (!passwordStr.equals(passwordConfirmStr)) {
                    passwordConfirm.setError(getString(R.string.passwords_do_not_match));
                    passwordConfirm.requestFocus();
                    return;
                }
            }
            if (account != null) {
                if (initMode && account.isOptionSet(Account.OPTION_MAGIC_CREATE)) {
                    account.setOption(Account.OPTION_MAGIC_CREATE, account.getPassword().contains(passwordStr));
                }
                account.setJid(jid);
                account.setPort(numericPort);
                account.setHostname(hostnameStr);
                accountJid.setError(null);
                passwordConfirm.setError(null);
                account.setPassword(passwordStr);
                account.setOption(Account.OPTION_REGISTER, registerNewAccount);
                if (!xmppConnectionService.updateAccount(account)) {
                    Toast.makeText(LoginActivity.this,R.string.unable_to_update_account,Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (xmppConnectionService.findAccountByJid(jid) != null) {
                    accountJid.setError(getString(R.string.account_already_exists));
                    accountJid.requestFocus();
                    return;
                }
                account = new Account(jid.toBareJid(), passwordStr);
                account.setPort(numericPort);
                account.setHostname(hostnameStr);
                account.setOption(Account.OPTION_USETLS, true);
                account.setOption(Account.OPTION_USECOMPRESSION, true);
                account.setOption(Account.OPTION_REGISTER, registerNewAccount);
                xmppConnectionService.createAccount(account);
            }
            hostname.setError(null);
            port.setError(null);
            if (!account.isOptionSet(Account.OPTION_DISABLED)
                    && !registerNewAccount
                    && !initMode) {
                finish();
            } else {
                updateSaveButton();
                updateAccountInformation(true);
            }

        }
    };

    //TODO ??
    protected boolean accountInfoEdited() {
        if (this.account == null) {
            return false;
        }
        return jidEdited() ||
                !this.account.getPassword().equals(this.password.getText().toString()) ||
                !this.account.getHostname().equals(this.hostname.getText().toString()) ||
                !String.valueOf(this.account.getPort()).equals(this.port.getText().toString());
    }

    //TODO ??
    protected boolean jidEdited() {
        final String unmodified;
        if (usernameMode) {
            unmodified = this.account.getJid().getLocalpart();
        } else {
            unmodified = this.account.getJid().toBareJid().toString();
        }
        return !unmodified.equals(this.accountJid.getText().toString());
    }

    //TODO ??
    private String getUserModeDomain() {
        if (account != null) {
            return account.getJid().getDomainpart();
        } else {
            return Config.DOMAIN_LOCK;
        }
    }

    //TODO ??
    private final View.OnClickListener avatarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            if (account != null) {
                final Intent intent = new Intent(getApplicationContext(), PublishProfilePictureActivity.class);
                intent.putExtra(EXTRA_ACCOUNT, account.getJid().toBareJid().toString());
                startActivity(intent);
            }
        }
    };

    //TODO ??
    protected boolean passwordChangedInMagicCreateMode() {
        return account != null
                && account.isOptionSet(Account.OPTION_MAGIC_CREATE)
                && !this.account.getPassword().equals(this.password.getText().toString())
                && !this.jidEdited()
                && account.isOnlineAndConnected();
    }

    //TODO ??
    private void gotoChangePassword(String newPassword) {
        final Intent changePasswordIntent = new Intent(this, ChangePasswordActivity.class);
        changePasswordIntent.putExtra(EXTRA_ACCOUNT, account.getJid().toString());
        if (newPassword != null) {
            changePasswordIntent.putExtra("password", newPassword);
        }
        startActivity(changePasswordIntent);
    }

    //TODO ??
    public void showWipePepDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.clear_other_devices));
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setMessage(getString(R.string.clear_other_devices_desc));
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.setPositiveButton(getString(R.string.accept),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        account.getAxolotlService().wipeOtherPepDevices();
                    }
                });
        builder.create().show();
    }
}
