package eu.siacs.conversations;

import android.graphics.Bitmap;

import eu.siacs.conversations.xmpp.chatstate.ChatState;

public final class Config {


	private static final int UNENCRYPTED = 1;
	private static final int OPENPGP = 2;
	private static final int OTR = 4;
	private static final int OMEMO = 8;

	private static final int ENCRYPTION_MASK = UNENCRYPTED | OPENPGP | OTR | OMEMO;

	public static boolean supportUnencrypted() {
		return (ENCRYPTION_MASK & UNENCRYPTED) != 0;
	}

	public static boolean supportOpenPgp() {
		return (ENCRYPTION_MASK & OPENPGP) != 0;
	}

	public static boolean supportOtr() {
		return (ENCRYPTION_MASK & OTR) != 0;
	}

	public static boolean supportOmemo() {
		return (ENCRYPTION_MASK & OMEMO) != 0;
	}

	public static boolean multipleEncryptionChoices() {
		return (ENCRYPTION_MASK & (ENCRYPTION_MASK - 1)) != 0;
	}

	public static final String LOGTAG = "conversations";

	public static final String BUG_REPORTS = "bugs@conversations.im";


	public static final String DOMAIN_LOCK = null; //only allow account creation for this domain
	public static final String MAGIC_CREATE_DOMAIN = "conversations.im";
	public static final boolean DISALLOW_REGISTRATION_IN_UI = false; //hide the register checkbox

	public static final boolean ALLOW_NON_TLS_CONNECTIONS = false; //very dangerous. you should have a good reason to set this to true
	public static final boolean FORCE_ORBOT = false; // always use TOR
	public static final boolean HIDE_MESSAGE_TEXT_IN_NOTIFICATION = false;
	public static final boolean SHOW_CONNECTED_ACCOUNTS = false; //show number of connected accounts in foreground notification

	public static final boolean ALWAYS_NOTIFY_BY_DEFAULT = false;

	public static final int PING_MAX_INTERVAL = 300;
	public static final int IDLE_PING_INTERVAL = 600; //540 is minimum according to docs;
	public static final int PING_MIN_INTERVAL = 30;
	public static final int LOW_PING_TIMEOUT = 1; // used after push received
	public static final int PING_TIMEOUT = 15;
	public static final int SOCKET_TIMEOUT = 15;
	public static final int CONNECT_TIMEOUT = 90;
	public static final int CONNECT_DISCO_TIMEOUT = 20;
	public static final int MINI_GRACE_PERIOD = 750;

	public static final int AVATAR_SIZE = 192;
	public static final Bitmap.CompressFormat AVATAR_FORMAT = Bitmap.CompressFormat.WEBP;

	public static final int IMAGE_SIZE = 1920;
	public static final Bitmap.CompressFormat IMAGE_FORMAT = Bitmap.CompressFormat.JPEG;
	public static final int IMAGE_QUALITY = 75;
	public static final int IMAGE_MAX_SIZE = 524288; //512KiB

	public static final int MESSAGE_MERGE_WINDOW = 20;

	public static final int PAGE_SIZE = 50;
	public static final int MAX_NUM_PAGES = 3;

	public static final int REFRESH_UI_INTERVAL = 500;

	public static final int MAX_DISPLAY_MESSAGE_CHARS = 4096;
	public static final int MAX_STORAGE_MESSAGE_CHARS = 1024 * 1024 * 1024;

	public static final long MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000;

	public static final long OMEMO_AUTO_EXPIRY = 7 * MILLISECONDS_IN_DAY;
	public static final boolean REMOVE_BROKEN_DEVICES = false;
	public static final boolean OMEMO_PADDING = false;
	public static boolean PUT_AUTH_TAG_INTO_KEY = true;


	public static final boolean DISABLE_PROXY_LOOKUP = false; //useful to debug ibb
	public static final boolean DISABLE_HTTP_UPLOAD = false;
	public static final boolean DISABLE_STRING_PREP = false; // setting to true might increase startup performance
	public static final boolean EXTENDED_SM_LOGGING = false; // log stanza counts
	public static final boolean BACKGROUND_STANZA_LOGGING = false; //log all stanzas that were received while the app is in background
	public static final boolean RESET_ATTEMPT_COUNT_ON_NETWORK_CHANGE = true; //setting to true might increase power consumption

	public static final boolean ENCRYPT_ON_HTTP_UPLOADED = false;

	public static final boolean X509_VERIFICATION = false; //use x509 certificates to verify OMEMO keys

	public static final boolean ONLY_INTERNAL_STORAGE = false; //use internal storage instead of sdcard to save attachments

	public static final boolean IGNORE_ID_REWRITE_IN_MUC = true;

	public static final boolean PARSE_REAL_JID_FROM_MUC_MAM = false; //dangerous if server doesn’t filter

	public static final long MAM_MAX_CATCHUP =  MILLISECONDS_IN_DAY * 5;
	public static final int MAM_MAX_MESSAGES = 750;

	public static final long FREQUENT_RESTARTS_DETECTION_WINDOW = 12 * 60 * 60 * 1000; // 10 hours
	public static final long FREQUENT_RESTARTS_THRESHOLD = 0; // previous value was 16;

	public static final ChatState DEFAULT_CHATSTATE = ChatState.ACTIVE;
	public static final int TYPING_TIMEOUT = 8;

	public static final int EXPIRY_INTERVAL = 30 * 60 * 1000; // 30 minutes

	public static final String ENABLED_CIPHERS[] = {
		"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
		"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA384",
		"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA256",
		"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
		"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
		"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",

		"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
		"TLS_DHE_RSA_WITH_AES_128_GCM_SHA384",
		"TLS_DHE_RSA_WITH_AES_256_GCM_SHA256",
		"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",

		"TLS_DHE_RSA_WITH_CAMELLIA_256_SHA",

		// Fallback.
		"TLS_RSA_WITH_AES_128_GCM_SHA256",
		"TLS_RSA_WITH_AES_128_GCM_SHA384",
		"TLS_RSA_WITH_AES_256_GCM_SHA256",
		"TLS_RSA_WITH_AES_256_GCM_SHA384",
		"TLS_RSA_WITH_AES_128_CBC_SHA256",
		"TLS_RSA_WITH_AES_128_CBC_SHA384",
		"TLS_RSA_WITH_AES_256_CBC_SHA256",
		"TLS_RSA_WITH_AES_256_CBC_SHA384",
		"TLS_RSA_WITH_AES_128_CBC_SHA",
		"TLS_RSA_WITH_AES_256_CBC_SHA",
	};

	public static final String WEAK_CIPHER_PATTERNS[] = {
		"_NULL_",
		"_EXPORT_",
		"_anon_",
		"_RC4_",
		"_DES_",
		"_MD5",
	};

	public static final boolean ADD_ACCOUNT_ENABLED = false;
    public static final boolean ACCESS_ACCOUNT_MANAGEMENT = true;

    //AJUSTES
    public static final boolean CONFIG_GROUP_GENERAL_ENABLED = false;//OK
    public static final boolean CONFIG_GROUP_PRIVACY_ENABLED = false;//OK
    public static final boolean CONFIG_GROUP_NOTIFICATIONS_ENABLED = true;
    public static final boolean CONFIG_GROUP_ATTACHMENTS_ENABLED = true;
    public static final boolean CONFIG_GROUP_SCREEN_ENABLED = true;
    public static final boolean CONFIG_GROUP_ADVANCED_ENABLED = true;
    public static final boolean CONFIG_ABOUT_ENABLED = true;//OK

    //AJUSTES GENERAL
    public static final boolean CONFIG_PRESENCE_UPDATES_ENABLED = false;//OK
    public static final boolean CONFIG_RESOURCE_ENABLED = false;//OK

    //AJUSTES PRIVACIDAD
    public static final boolean CONFIG_CONFIRM_MESSAGES_ENABLED = false;//OK
    public static final boolean CONFIG_CHAT_STATES_ENABLED = false;//OK
    public static final boolean CONFIG_BROADCAST_ACTIVITY_ENABLED = false;//OK

    //AJUSTES NOTIFICACIONES
    public static final boolean CONFIG_NOTIFICATIONS_ENABLED = false;//OK
    public static final boolean CONFIG_NOTIFICATIONS_STRANGERS_ENABLED = false;//OK
    public static final boolean CONFIG_NOTIFICATIONS_HEADSUP_ENABLED = false;//OK
    public static final boolean CONFIG_VIBRATION_ENABLED = true;
    public static final boolean CONFIG_LIGHT_ENABLED = true;
    public static final boolean CONFIG_SOUND_ENABLED = true;
    public static final boolean CONFIG_SILENCE_ENABLED = true;
    public static final boolean CONFIG_GRACE_PERIOD_ENABLED = true;

    //AJUSTES ADJUNTOS
    public static final boolean CONFIG_FILES_ENABLED = true;
    public static final boolean CONFIG_COMPRESSION_ENABLED = true;
    public static final boolean CONFIG_QUICK_SHARE_ENABLED = true;

    //AJUSTES PANTALLA
    public static final boolean CONFIG_THEME_ENABLED = false;
    public static final boolean CONFIG_NAME_IN_GROUP_ENABLED = true;
    public static final boolean CONFIG_GREEN_BACKGROUND_ENABLED = true;
    public static final boolean CONFIG_FONT_SIZE_ENABLED = true;
    public static final boolean CONFIG_SEND_INDICATE_STATUS_ENABLED = false;//OK
    public static final boolean CONFIG_QUICK_ACTION_ENABLED = true;
    public static final boolean CONFIG_DINAMIC_TAGS_ENABLED = false;//OK

    //AJUSTES AVANZADO
	public static final boolean CONFIG_SUB_SECURITY_ENABLED = true;
	public static final boolean CONFIG_BLIND_TRUST_ENABLED = false;//OK
	public static final boolean CONFIG_AUTO_MESSAGE_DELETION_ENABLED = true;
	public static final boolean CONFIG_DONT_TRUST_CAS_ENABLED = false;//OK
	public static final boolean CONFIG_VALIDATE_HOSTNAME_ENABLED = false;//OK
	public static final boolean CONFIG_REMOVE_CERT_ENABLED = true;
	public static final boolean CONFIG_MESSAGE_CORRECTION_ENABLED = false;//OK
	public static final boolean CONFIG_CLEAN_CACHE_ENABLED = true;
	public static final boolean CONFIG_PRIVATE_STORAGE_ENABLED = true;
	public static final boolean CONFIG_DELETE_OMEMO_ENABLED = false;

	public static final boolean CONFIG_SUB_CONNECTION_ENABLED = true;
	public static final boolean CONFIG_USE_TOR_ENABLED = true;
	public static final boolean CONFIG_CONNECTION_OPTIONS_ENABLED = false;//OK

	public static final boolean CONFIG_SUB_INPUT_ENABLED = false;//OK
	public static final boolean CONFIG_ENTER_SEND_ENABLED = false;//OK
	public static final boolean CONFIG_DISPLAY_ENTER_ENABLED = false;//OK

	public static final boolean CONFIG_SUB_PRESENCE_ENABLED = false;//OK
	public static final boolean CONFIG_MANUAL_PRESENCE_ENABLED = false;//OK
	public static final boolean CONFIG_AWAY_SCREEN_OFF_ENABLED = false;//OK
	public static final boolean CONFIG_DND_SILENT_ENABLED = false;//OK
	public static final boolean CONFIG_VIBRATE_SILENT_ENABLED = false;//OK

	public static final boolean CONFIG_SUB_OTHER_ENABLED = true;
	public static final boolean CONFIG_AUTOJOIN_ENABLED = true;
	public static final boolean CONFIG_INDICATE_RECEIVED_ENABLED = true;
	public static final boolean CONFIG_FOREGROUND_SERVICE_ENABLED = true;
	public static final boolean CONFIG_EXPORT_LOGS_ENABLED = false;

    public static final boolean CONFIG_SEND_ERRORS_ENABLED = true;

	private Config() {
	}
}
