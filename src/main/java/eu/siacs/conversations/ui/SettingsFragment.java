package eu.siacs.conversations.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;

public class SettingsFragment extends PreferenceFragment {

	//http://stackoverflow.com/questions/16374820/action-bar-home-button-not-functional-with-nested-preferencescreen/16800527#16800527
	private void initializeActionBar(PreferenceScreen preferenceScreen) {
		final Dialog dialog = preferenceScreen.getDialog();

		if (dialog != null) {
			View homeBtn = dialog.findViewById(android.R.id.home);

			if (homeBtn != null) {
				View.OnClickListener dismissDialogClickListener = new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				};

				ViewParent homeBtnContainer = homeBtn.getParent();

				if (homeBtnContainer instanceof FrameLayout) {
					ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();
					if (containerParent instanceof LinearLayout) {
						((LinearLayout) containerParent).setOnClickListener(dismissDialogClickListener);
					} else {
						((FrameLayout) homeBtnContainer).setOnClickListener(dismissDialogClickListener);
					}
				} else {
					homeBtn.setOnClickListener(dismissDialogClickListener);
				}
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		// Remove from standard preferences if the flag ONLY_INTERNAL_STORAGE is not true
		if (!Config.ONLY_INTERNAL_STORAGE) {
			PreferenceCategory mCategory = (PreferenceCategory) findPreference(getString(R.string.key_security_options));
			Preference mPref1 = findPreference(getString(R.string.key_clean_cache));
			Preference mPref2 = findPreference(getString(R.string.key_clean_private_storage));
			mCategory.removePreference(mPref1);
			mCategory.removePreference(mPref2);
		}

	}

    @Override
    public void onResume(){
        super.onResume();
        checkEnabledPreferences();
    }

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		super.onPreferenceTreeClick(preferenceScreen, preference);
		if (preference instanceof PreferenceScreen) {
			initializeActionBar((PreferenceScreen) preference);
		}
		return false;
	}

    private void checkEnabledPreferences(){

        PreferenceCategory preferenceCategory;
        CheckBoxPreference checkBoxPreference;
        ListPreference listPreference;
        Preference preference;
        AboutPreference aboutPreference;
        PreferenceScreen preferenceScreenHours;
        RingtonePreference ringtonePreference;

        preferenceCategory = (PreferenceCategory) findPreference(getString(R.string.key_general));
        if(!Config.CONFIG_GROUP_GENERAL_ENABLED){
            getPreferenceScreen().removePreference(preferenceCategory);
        }else{
            if(!Config.CONFIG_PRESENCE_UPDATES_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_grant_new_contacts));
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_RESOURCE_ENABLED){
                listPreference = (ListPreference) findPreference(getString(R.string.key_resource));
                preferenceCategory.removePreference(listPreference);
            }
        }

        preferenceCategory = (PreferenceCategory) findPreference(getString(R.string.key_privacy));
        if(!Config.CONFIG_GROUP_PRIVACY_ENABLED){
            getPreferenceScreen().removePreference(preferenceCategory);
        }else{
            if(!Config.CONFIG_CONFIRM_MESSAGES_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_confirm_messages));
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_CHAT_STATES_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_chat_states));
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_BROADCAST_ACTIVITY_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_last_activity));
                preferenceCategory.removePreference(checkBoxPreference);
            }
        }

        preferenceCategory = (PreferenceCategory) findPreference(getString(R.string.key_notifications));
        if(!Config.CONFIG_GROUP_NOTIFICATIONS_ENABLED){
            getPreferenceScreen().removePreference(preferenceCategory);
        }else{
            if(!Config.CONFIG_NOTIFICATIONS_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_show_notification));
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_NOTIFICATIONS_STRANGERS_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_notifications_from_strangers));
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_NOTIFICATIONS_HEADSUP_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_notification_headsup));
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_VIBRATION_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_vibrate_on_notification));
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_LIGHT_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_led));
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_SOUND_ENABLED){
                ringtonePreference = (RingtonePreference) findPreference(getString(R.string.key_notification_ringtone));
                preferenceCategory.removePreference(ringtonePreference);
            }
            if(!Config.CONFIG_GRACE_PERIOD_ENABLED){
                listPreference = (ListPreference) findPreference(getString(R.string.key_grace_period_length));
                preferenceCategory.removePreference(listPreference);
            }
        }

        preferenceCategory = (PreferenceCategory) findPreference(getString(R.string.key_attachments));
        if(!Config.CONFIG_GROUP_ATTACHMENTS_ENABLED){
            getPreferenceScreen().removePreference(preferenceCategory);
        }else{
            if(!Config.CONFIG_FILES_ENABLED){
                listPreference = (ListPreference) findPreference(getString(R.string.key_auto_accept_file_size));
                preferenceCategory.removePreference(listPreference);
            }
            if(!Config.CONFIG_COMPRESSION_ENABLED){
                listPreference = (ListPreference) findPreference(getString(R.string.key_picture_compression));
                preferenceCategory.removePreference(listPreference);
            }
        }

        preferenceCategory = (PreferenceCategory) findPreference(getString(R.string.key_ui));
        if(!Config.CONFIG_GROUP_SCREEN_ENABLED){
            getPreferenceScreen().removePreference(preferenceCategory);
        }else{
            if(!Config.CONFIG_THEME_ENABLED){
                listPreference = (ListPreference) findPreference(getString(R.string.key_theme));
                preferenceCategory.removePreference(listPreference);
            }
            if(!Config.CONFIG_GREEN_BACKGROUND_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_use_green_background));
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_FONT_SIZE_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_use_larger_font));
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_SEND_INDICATE_STATUS_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_send_button_status));
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_QUICK_ACTION_ENABLED){
                listPreference = (ListPreference) findPreference(getString(R.string.key_quick_action));
                preferenceCategory.removePreference(listPreference);
            }
            if(!Config.CONFIG_DINAMIC_TAGS_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_show_dynamic_tags));
                preferenceCategory.removePreference(checkBoxPreference);
            }
        }

        preferenceCategory = (PreferenceCategory) findPreference(getString(R.string.key_advanced));
        if(!Config.CONFIG_GROUP_ADVANCED_ENABLED){
            getPreferenceScreen().removePreference(preferenceCategory);
        }else {
            PreferenceScreen advancedPreferences = (PreferenceScreen) preferenceCategory.findPreference(getString(R.string.key_expert));
            PreferenceCategory prefSecurity = (PreferenceCategory) advancedPreferences.findPreference(getString(R.string.key_security_options)),
                    prefConnection = (PreferenceCategory)advancedPreferences.findPreference(getString(R.string.key_connection_options)),
                    prefInput = (PreferenceCategory)advancedPreferences.findPreference(getString(R.string.key_input_options)),
                    prefPresence = (PreferenceCategory)advancedPreferences.findPreference(getString(R.string.key_presence_options)),
                    prefOther = (PreferenceCategory)advancedPreferences.findPreference(getString(R.string.key_other_options));

            if(!Config.CONFIG_SUB_SECURITY_ENABLED){
                advancedPreferences.removePreference(prefSecurity);
            }else{
                if(!Config.CONFIG_BLIND_TRUST_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_btbv));
                    prefSecurity.removePreference(checkBoxPreference);
                }
                if(!Config.CONFIG_AUTO_MESSAGE_DELETION_ENABLED){
                    listPreference = (ListPreference) advancedPreferences.findPreference(getString(R.string.key_automatic_message_deletion));
                    prefSecurity.removePreference(listPreference);
                }
                if(!Config.CONFIG_DONT_TRUST_CAS_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_dont_trust_system_cas));
                    prefSecurity.removePreference(checkBoxPreference);
                }
                if(!Config.CONFIG_VALIDATE_HOSTNAME_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_validate_hostname));
                    prefSecurity.removePreference(checkBoxPreference);
                }
                if(!Config.CONFIG_REMOVE_CERT_ENABLED){
                    preference = advancedPreferences.findPreference(getString(R.string.key_remove_trusted_certificates));
                    prefSecurity.removePreference(preference);
                }
                if(!Config.CONFIG_MESSAGE_CORRECTION_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_allow_message_correction));
                    prefSecurity.removePreference(checkBoxPreference);
                }
                if(!Config.CONFIG_CLEAN_CACHE_ENABLED){
                    preference = advancedPreferences.findPreference(getString(R.string.key_clean_cache));
                    prefSecurity.removePreference(preference);
                }
                if(!Config.CONFIG_PRIVATE_STORAGE_ENABLED){
                    preference = advancedPreferences.findPreference(getString(R.string.key_clean_private_storage));
                    prefSecurity.removePreference(preference);
                }
                if(!Config.CONFIG_DELETE_OMEMO_ENABLED){
                    preference = advancedPreferences.findPreference(getString(R.string.key_delete_omemo_identities));
                    prefSecurity.removePreference(preference);
                }
            }

            if(!Config.CONFIG_SUB_CONNECTION_ENABLED){
                advancedPreferences.removePreference(prefConnection);
            }else{
                if(!Config.CONFIG_USE_TOR_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_use_tor));
                    prefSecurity.removePreference(checkBoxPreference);
                }
                if(!Config.CONFIG_CONNECTION_OPTIONS_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_show_connection_options));
                    prefSecurity.removePreference(checkBoxPreference);
                }
            }

            if(!Config.CONFIG_SUB_INPUT_ENABLED){
                advancedPreferences.removePreference(prefInput);
            }else{
                if(!Config.CONFIG_ENTER_SEND_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_enter_is_send));
                    prefSecurity.removePreference(checkBoxPreference);
                }
                if(!Config.CONFIG_DISPLAY_ENTER_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_display_enter_key));
                    prefSecurity.removePreference(checkBoxPreference);
                }
            }

            if(!Config.CONFIG_SUB_PRESENCE_ENABLED){
                advancedPreferences.removePreference(prefPresence);
            }else{
                if(!Config.CONFIG_MANUAL_PRESENCE_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_manually_change_presence));
                    prefSecurity.removePreference(checkBoxPreference);
                }
                if(!Config.CONFIG_AWAY_SCREEN_OFF_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_away_when_screen_off));
                    prefSecurity.removePreference(checkBoxPreference);
                }
                if(!Config.CONFIG_DND_SILENT_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_dnd_on_silent_mode));
                    prefSecurity.removePreference(checkBoxPreference);
                }
                if(!Config.CONFIG_VIBRATE_SILENT_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_treat_vibrate_as_silent));
                    prefSecurity.removePreference(checkBoxPreference);
                }
            }

            if(!Config.CONFIG_SUB_OTHER_ENABLED ){
                advancedPreferences.removePreference(prefOther);
            }else{
                if(!Config.CONFIG_AUTOJOIN_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_autojoin));
                    prefSecurity.removePreference(checkBoxPreference);
                }
                if(!Config.CONFIG_INDICATE_RECEIVED_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_indicate_received));
                    prefSecurity.removePreference(checkBoxPreference);
                }
                if(!Config.CONFIG_FOREGROUND_SERVICE_ENABLED){
                    checkBoxPreference = (CheckBoxPreference) advancedPreferences.findPreference(getString(R.string.key_enable_foreground_service));
                    prefSecurity.removePreference(checkBoxPreference);
                }
            }

            if (!Config.CONFIG_SEND_ERRORS_ENABLED) {
                checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_never_send));
                preferenceCategory.removePreference(checkBoxPreference);
            }
        }

        if(!Config.CONFIG_ABOUT_ENABLED){
            getPreferenceScreen().removePreference(findPreference(getString(R.string.key_about)));
        }
    }

}
