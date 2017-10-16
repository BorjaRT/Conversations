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
//		checkEnabledPreferences();

		// Remove from standard preferences if the flag ONLY_INTERNAL_STORAGE is not true
		if (!Config.ONLY_INTERNAL_STORAGE) {
			PreferenceCategory mCategory = (PreferenceCategory) findPreference("security_options");
			Preference mPref1 = findPreference("clean_cache");
			Preference mPref2 = findPreference("clean_private_storage");
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

        preferenceCategory = (PreferenceCategory) findPreference("general");
        if(!Config.CONFIG_GROUP_GENERAL_ENABLED){
            getPreferenceScreen().removePreference(preferenceCategory);
        }else{
            if(!Config.CONFIG_PRESENCE_UPDATES_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("grant_new_contacts");
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_RESOURCE_ENABLED){
                listPreference = (ListPreference) findPreference("resource");
                preferenceCategory.removePreference(listPreference);
            }
        }

        preferenceCategory = (PreferenceCategory) findPreference("privacy");
        if(!Config.CONFIG_GROUP_PRIVACY_ENABLED){
            getPreferenceScreen().removePreference(preferenceCategory);
        }else{
            if(!Config.CONFIG_CONFIRM_MESSAGES_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("confirm_messages");
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_CHAT_STATES_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("chat_states");
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_BROADCAST_ACTIVITY_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("last_activity");
                preferenceCategory.removePreference(checkBoxPreference);
            }
        }

        preferenceCategory = (PreferenceCategory) findPreference("notifications");
        if(!Config.CONFIG_GROUP_NOTIFICATIONS_ENABLED){
            getPreferenceScreen().removePreference(preferenceCategory);
        }else{
            if(!Config.CONFIG_NOTIFICATIONS_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("show_notification");
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_NOTIFICATIONS_STRANGERS){
                checkBoxPreference = (CheckBoxPreference) findPreference("notifications_from_strangers");
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_NOTIFICATIONS_HEADSUP_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("notification_headsup");
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_VIBRATION_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("vibrate_on_notification");
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_LIGHT_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("led");
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_SOUND_ENABLED){
                ringtonePreference = (RingtonePreference) findPreference("notification_ringtone");
                preferenceCategory.removePreference(ringtonePreference);
            }
            if(!Config.CONFIG_SILENCE_ENABLED){
                preferenceScreenHours = (PreferenceScreen) findPreference("quiet_hours");
                preferenceCategory.removePreference(preferenceScreenHours);
            }
            if(!Config.CONFIG_GRACE_PERIOD_ENABLED){
                listPreference = (ListPreference) findPreference("grace_period_length");
                preferenceCategory.removePreference(listPreference);
            }
        }

        preferenceCategory = (PreferenceCategory) findPreference("attachments");
        if(!Config.CONFIG_GROUP_ATTACHMENTS_ENABLED){
            getPreferenceScreen().removePreference(preferenceCategory);
        }else{
            if(!Config.CONFIG_FILES_ENABLED){
                listPreference = (ListPreference) findPreference("auto_accept_file_size");
                preferenceCategory.removePreference(listPreference);
            }
            if(!Config.CONFIG_COMPRESSION_ENABLED){
                listPreference = (ListPreference) findPreference("picture_compression");
                preferenceCategory.removePreference(listPreference);
            }
            if(!Config.CONFIG_QUICK_SHARE_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("return_to_previous");
                preferenceCategory.removePreference(checkBoxPreference);
            }
        }

        preferenceCategory = (PreferenceCategory) findPreference("ui");
        if(!Config.CONFIG_GROUP_SCREEN_ENABLED){
            getPreferenceScreen().removePreference(preferenceCategory);
        }else{
            if(!Config.CONFIG_THEME_ENABLED){
                listPreference = (ListPreference) findPreference("theme");
                preferenceCategory.removePreference(listPreference);
            }
            if(!Config.CONFIG_NAME_IN_GROUP_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("use_subject");
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_GREEN_BACKGROUND_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("use_green_background");
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_FONT_SIZE_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("use_larger_font");
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_SEND_INDICATE_sTATUS_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("send_button_status");
                preferenceCategory.removePreference(checkBoxPreference);
            }
            if(!Config.CONFIG_QUICK_ACTION_ENABLED){
                listPreference = (ListPreference) findPreference("quick_action");
                preferenceCategory.removePreference(listPreference);
            }
            if(!Config.CONFIG_DINAMIC_TAGS_ENABLED){
                checkBoxPreference = (CheckBoxPreference) findPreference("show_dynamic_tags");
                preferenceCategory.removePreference(checkBoxPreference);
            }
        }

        //TODO Avanzadas

        preferenceCategory = (PreferenceCategory) findPreference("advanced");
        if(!Config.CONFIG_GROUP_ADVANCED_ENABLED){
            getPreferenceScreen().removePreference(preferenceCategory);
        }else {
//            if (!Config.CONFIG_EXPERT_OPTIONS_ENABLED) {
//                checkBoxPreference = (CheckBoxPreference) findPreference("grant_new_contacts");
//                preferenceCategory.removePreference(checkBoxPreference);
//            }
            if (!Config.CONFIG_SEND_ERRORS_ENABLED) {
                checkBoxPreference = (CheckBoxPreference) findPreference("never_send");
                preferenceCategory.removePreference(checkBoxPreference);
            }
        }

        if(!Config.CONFIG_ABOUT_ENABLED){
            getPreferenceScreen().removePreference(findPreference("about"));
        }
    }

}
