package com.maxleap.platform;

/**
 * desc this class
 *
 * @author jing zhao
 * @date 4/28/14
 * @see
 * @since 2.0
 */
public class LASTables {
    public final static String SYSTEM = "zcloud_system";

    public final static String DEVICE = "device";

    public final static String SCHEMA = "schema";

    public final static String PASSPORT = "passport";

    public final static String PUSH_DATA = "push_data";
    public final static String CAMPAIGN_MESSAGE = "campaign_message";

    public final static String PLATFORM_DATA = "platform_data";

    public final static String AD_SCHEMA = "ad_schema";

    public final static String WEB_CONSOLE = "web_console";

    public final static String MAPPING_DATA = "mapping_data";

    public final static String HELP_CENTER = "help_center";

    public final static String USER_DATA = "user_data";

    public final static String SMS_SCHEMA = "sms_schema";

    public final static String MESSAGE_SCHEMA = "message_schema";

    public final static String[] dbs = new String[]{CAMPAIGN_MESSAGE, USER_DATA, HELP_CENTER, SYSTEM, DEVICE, SCHEMA, PASSPORT, PUSH_DATA, PLATFORM_DATA, AD_SCHEMA, WEB_CONSOLE, MAPPING_DATA, SMS_SCHEMA};

    public final static String[][] collections = new String[][]
            {
                    {MarketCampaign.CAMPAIGN_MESSAGE},
                    {UserData.APP_USER_DATA, UserData.APP_USER_EVENT, UserData.SEGMENT, UserData.ATOM_SEGMENT, UserData.LINK_TAG},
                    {HelpCenter.FAQ_BOARD, HelpCenter.FAQ_SECTION, HelpCenter.FAQ_SECTION_GEO, HelpCenter.FAQ_ITEM, HelpCenter.FAQ_TAG, HelpCenter.FAQ_ITEM_GEO, HelpCenter.ISSUE, HelpCenter.ISSUE_MSG, HelpCenter.ISSUE_SMART_VIEW, HelpCenter.ISSUE_NOTIFICATION, HelpCenter.ISSUE_DEVICE_LOG},
                    {System.GEO_BLOCKS, System.GEO_LOCATION, System.GEO_COUNTRY, System.GEO_COUNTRY_SUB, System.GEO_LANGUAGE},
                    {Device.device},
                    {Schema.CLASS_SCHEMA, Schema.SYSTEM_SCHEMA},
                    {Passport.SESSIONTOKEN, Passport.PASSPORT, Passport.PASSPORT_APP},
                    {"*"},
                    {PlatformData.ORGANIZATION, PlatformData.ORGANIZATION_USER, PlatformData.ORGANIZATION_ROLE, PlatformData.APPLICATION, PlatformData.EMAIL_TEMPLATE, PlatformData.ENTITY_HOOK, PlatformData.CLOUD_CONFIG},
                    {AdSchema.CAMPAIGN, AdSchema.SCHEDULE, AdSchema.CREATIVE, AdSchema.TRACE},
                    {"*"},
                    {"*"},
                    {"*"}
            };

    public static class System {
        public static final String GEO_BLOCKS = "geo_blocks";
        public static final String GEO_LOCATION = "geo_location";
        public static final String GEO_COUNTRY = "geo_country";
        public static final String GEO_COUNTRY_SUB = "geo_country_sub";
        public static final String GEO_LANGUAGE = "geo_language";
    }

    public static class HelpCenter {
        public static final String FAQ_BOARD = "faq_board";
        public static final String FAQ_SECTION = "faq_section";
        public static final String FAQ_SECTION_GEO = "faq_section_geo";
        public static final String FAQ_ITEM = "faq_item";
        public static final String FAQ_TAG = "faq_tag";
        public static final String FAQ_ITEM_GEO = "faq_item_geo";
        public static final String ISSUE = "issue";
        public static final String ISSUE_MSG = "issue_msg";
        public static final String ISSUE_NOTIFICATION = "issue_notification";
        public static final String ISSUE_SMART_VIEW = "issue_smart_view";
        public static final String ISSUE_DEVICE_LOG = "issue_device_log";


    }

    public static class UserData {
        public static final String APP_USER_DATA = "app_user_data";
        public static final String SEGMENT = "segment";
        public static final String ATOM_SEGMENT = "atom_segment";
        public static final String APP_USER_EVENT = "app_user_event";
        public static final String PAYMENT = "payment";
        public static final String LINK_TAG = "link_tag";
        public static final String APP_LINK_TAG = "app_link_tag";

    }

    public static class Device {
        public static final String device = "device";
    }

    public static class Schema {

        public static final String CLASS_SCHEMA = "zcloud_class_schema";

        public static final String SYSTEM_SCHEMA = "zcloud_system_schema";

    }

    public static class Passport {

        public static final String PASSPORT = "zcloud_passport";

        public static final String PASSPORT_APP = "zcloud_passport_app";

        public static final String SESSIONTOKEN = "zcloud_session_token";

    }

    public static class PlatformData {

        public static final String ORGANIZATION = "zcloud_organization";

        public static final String ORGANIZATION_USER = "zcloud_organization_user";

        public static final String ORGANIZATION_ROLE = "zcloud_organization_role";

        public static final String APPLICATION = "zcloud_application";

        public static final String EMAIL_TEMPLATE = "zcloud_email_template";

        public static final String ENTITY_HOOK = "zcloud_entity_hook";

        public static final String CLOUD_CONFIG = "zcloud_config";

        public static final String BLACK_LIST = "zcloud_black_list";

        public static final String ORDER_LIST = "zcloud_order_list";
    }

    public static class AdSchema {

        public static final String CAMPAIGN = "ad_campaign";

        public static final String SCHEDULE = "ad_schedule";

        public static final String CREATIVE = "ad_creative";

        public static final String TRACE = "ad_trace";
    }

    public static class PushData {

        public static final String PUSH_TASK = "zcloud_push_task";

    }

    public static class MarketCampaign {
        public static final String CAMPAIGN_MESSAGE = "campaign_message";
    }

    public static class AppData {

        public static final String USER = "_User";

        public static final String ROLE = "_Role";

        public static final String INSTALLATION = "_Installation";

        public static final String PUSH_TASK = "_zcloud_push_task";

        public static final String PRODUCT = "_Product";

        public static final String PARAMETER = "_Parameter";


    }

    public static class MappingData {

        public static final String ORG_ID_COM_ID = "org_id_com_id";

        public static final String ORG_USER_ID_COM_USER_ID = "o_user_id_c_user_id";

        public static final String APP_ID_APP_ID = "app_id_app_id";

    }

    public static class SmsSchema {
        public static final String SMS_LOG = "sms_log";
        public static final String SMS_CFG = "sms_cfg";
        public static final String SMS_TOP_UP = "sms_top_up";
    }

    public static class MessageSchema {
        public static final String MESSAGE = "message";
    }
}
