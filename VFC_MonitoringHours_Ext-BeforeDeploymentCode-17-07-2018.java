public with sharing class VFC_MonitoringHours_Ext {
    public Installation__c i { get; set; }
    public List<Installation__c> installList { get; set; }
    public Map<String, Boolean> installSelectionMap { get; set; }
    public Boolean selectAll { get; set; }
    public List<DayOfWeek> weekdayList { get; set; }
    public String objectType { get; set; }

    public static Set<String> dayOfWeekNames = new Set<String> {
        'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'
    };

    public class DayOfWeek {
        public String day { get; set; }
        public Boolean daySelected { get; set; }
        public String startAM { get; set; }
        public String startPM { get; set; }
        public String stopAM { get; set; }
        public String stopPM { get; set; }
        public String startMinuteAM { get; set; }
        public String startMinutePM { get; set; }
        public String stopMinuteAM { get; set; }
        public String stopMinutePM { get; set; }
        public Boolean twentyFourHour { get; set; }
    }

    public List<SelectOption> hourAMList { 
        get {
            if( hourAMList == null ) {
                hourAMList = getPicklist( Installation__c.AM_Start_Mon_Hour__c );
            }
            return hourAMList;
        } 
        set;
    }

    public List<SelectOption> hourPMList { 
        get {
            if( hourPMList == null ) {
                hourPMList = getPicklist( Installation__c.PM_Start_Mon_Hour__c );
            }
            return hourPMList;
        } 
        set;
    }

    public List<SelectOption> minuteList { 
        get {
            if( minuteList == null ) {
                minuteList = getPicklist( Installation__c.AM_Start_Mon_Min__c );
            }
            return minuteList;
        } 
        set;
    }

    public static List<SelectOption> getPicklist( Schema.SObjectField f ) {
        List<SelectOption> theList = new List<SelectOption>();
        Schema.DescribeFieldResult fr = f.getDescribe();
        List<Schema.PicklistEntry> pleList = fr.getPicklistValues();
        theList.add( new SelectOption( '', '' ) );
        for( Schema.PicklistEntry p : pleList ) {
            theList.add( new SelectOption( p.getValue(), p.getLabel() ) );
        }
        system.debug( theList );
        return theList;
    }

    public VFC_MonitoringHours_Ext( ApexPages.StandardController theController ) {
        SObject o = theController.getRecord();
        objectType = o.getSObjectType().getDescribe().getName() + '';

        installSelectionMap = new Map<String, Boolean>();

        if( objectType == 'Installation__c' ) {
            installationInitialization( o );
            return;
        }

        if( objectType == 'Account' ) {
            accountInitialization( o );
            return;
        }

        if( objectType == 'Case' ) {
            caseInitialization( o );
            return;
        }
    }

    public void accountInitialization( SObject o ) {
        // retrieve the installations for the account and use the first record
        String acctID = o.ID;
        installList = [ 
            SELECT ID, Name, Monitoring_Service__c, Time_Zone__c, Status__c, Unit__r.Name
                , Product__r.Name, Installed_Date__c, Site__c, Install_Case__c
                , Monday__c, Tuesday__c, Wednesday__c, Thursday__c, Friday__c, Saturday__c, Sunday__c
                , X24_Hour_Mon__c, X24_Hour_Tue__c, X24_Hour_Wed__c, X24_Hour_Thu__c
                , X24_Hour_Fri__c, X24_Hour_Sat__c, X24_Hour_Sun__c
                , AM_Start_Mon_Hour__c, AM_Start_Mon_Min__c, AM_Stop_Mon_Hour__c, AM_Stop_Mon_Min__c
                , PM_Start_Mon_Hour__c, PM_Start_Mon_Min__c, PM_Stop_Mon_Hour__c, PM_Stop_Mon_Min__c
                , AM_Start_Tue_Hour__c, AM_Start_Tue_Min__c, AM_Stop_Tue_Hour__c, AM_Stop_Tue_Min__c
                , PM_Start_Tue_Hour__c, PM_Start_Tue_Min__c, PM_Stop_Tue_Hour__c, PM_Stop_Tue_Min__c
                , AM_Start_Wed_Hour__c, AM_Start_Wed_Min__c, AM_Stop_Wed_Hour__c, AM_Stop_Wed_Min__c
                , PM_Start_Wed_Hour__c, PM_Start_Wed_Min__c, PM_Stop_Wed_Hour__c, PM_Stop_Wed_Min__c
                , AM_Start_Thu_Hour__c, AM_Start_Thu_Min__c, AM_Stop_Thu_Hour__c, AM_Stop_Thu_Min__c
                , PM_Start_Thu_Hour__c, PM_Start_Thu_Min__c, PM_Stop_Thu_Hour__c, PM_Stop_Thu_Min__c
                , AM_Start_Fri_Hour__c, AM_Start_Fri_Min__c, AM_Stop_Fri_Hour__c, AM_Stop_Fri_Min__c
                , PM_Start_Fri_Hour__c, PM_Start_Fri_Min__c, PM_Stop_Fri_Hour__c, PM_Stop_Fri_Min__c
                , AM_Start_Sat_Hour__c, AM_Start_Sat_Min__c, AM_Stop_Sat_Hour__c, AM_Stop_Sat_Min__c
                , PM_Start_Sat_Hour__c, PM_Start_Sat_Min__c, PM_Stop_Sat_Hour__c, PM_Stop_Sat_Min__c
                , AM_Start_Sun_Hour__c, AM_Start_Sun_Min__c, AM_Stop_Sun_Hour__c, AM_Stop_Sun_Min__c
                , PM_Start_Sun_Hour__c, PM_Start_Sun_Min__c, PM_Stop_Sun_Hour__c, PM_Stop_Sun_Min__c
                , Monitoring_Hours__c, Monitoring_Hours_Every_Week__c, Additional_Monitoring_Hours__c
                , Guard_Tour_Duration_Mins__c, Guard_Tour_Interval_Mins__c, Guard_Tour_Times__c
                , Opportunity__c 
            FROM Installation__c 
            WHERE Site__c = :acctID 
                AND Status__c != '5-Removed'
            ORDER BY Name ];
        if( installList.size() <= 0 ) {
            ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                , 'There are no installations for this site.' ) );
            return;
        }

        i = installList[ 0 ];

        for( Installation__c inst : installList ) {
            installSelectionMap.put( inst.ID, true );
        }
        selectAll = true;

        // populate the schedule
        initialization();
    }

    public void caseInitialization( SObject o ) {
        // retrieve the installations for the case and use the first record
        String caseID = o.ID;
        installList = [ 
            SELECT ID, Name, Monitoring_Service__c, Time_Zone__c, Status__c, Unit__r.Name
                , Product__r.Name, Installed_Date__c, Site__c, Install_Case__c
                , Monday__c, Tuesday__c, Wednesday__c, Thursday__c, Friday__c, Saturday__c, Sunday__c
                , X24_Hour_Mon__c, X24_Hour_Tue__c, X24_Hour_Wed__c, X24_Hour_Thu__c
                , X24_Hour_Fri__c, X24_Hour_Sat__c, X24_Hour_Sun__c
                , AM_Start_Mon_Hour__c, AM_Start_Mon_Min__c, AM_Stop_Mon_Hour__c, AM_Stop_Mon_Min__c
                , PM_Start_Mon_Hour__c, PM_Start_Mon_Min__c, PM_Stop_Mon_Hour__c, PM_Stop_Mon_Min__c
                , AM_Start_Tue_Hour__c, AM_Start_Tue_Min__c, AM_Stop_Tue_Hour__c, AM_Stop_Tue_Min__c
                , PM_Start_Tue_Hour__c, PM_Start_Tue_Min__c, PM_Stop_Tue_Hour__c, PM_Stop_Tue_Min__c
                , AM_Start_Wed_Hour__c, AM_Start_Wed_Min__c, AM_Stop_Wed_Hour__c, AM_Stop_Wed_Min__c
                , PM_Start_Wed_Hour__c, PM_Start_Wed_Min__c, PM_Stop_Wed_Hour__c, PM_Stop_Wed_Min__c
                , AM_Start_Thu_Hour__c, AM_Start_Thu_Min__c, AM_Stop_Thu_Hour__c, AM_Stop_Thu_Min__c
                , PM_Start_Thu_Hour__c, PM_Start_Thu_Min__c, PM_Stop_Thu_Hour__c, PM_Stop_Thu_Min__c
                , AM_Start_Fri_Hour__c, AM_Start_Fri_Min__c, AM_Stop_Fri_Hour__c, AM_Stop_Fri_Min__c
                , PM_Start_Fri_Hour__c, PM_Start_Fri_Min__c, PM_Stop_Fri_Hour__c, PM_Stop_Fri_Min__c
                , AM_Start_Sat_Hour__c, AM_Start_Sat_Min__c, AM_Stop_Sat_Hour__c, AM_Stop_Sat_Min__c
                , PM_Start_Sat_Hour__c, PM_Start_Sat_Min__c, PM_Stop_Sat_Hour__c, PM_Stop_Sat_Min__c
                , AM_Start_Sun_Hour__c, AM_Start_Sun_Min__c, AM_Stop_Sun_Hour__c, AM_Stop_Sun_Min__c
                , PM_Start_Sun_Hour__c, PM_Start_Sun_Min__c, PM_Stop_Sun_Hour__c, PM_Stop_Sun_Min__c
                , Monitoring_Hours__c, Monitoring_Hours_Every_Week__c, Additional_Monitoring_Hours__c
                , Guard_Tour_Duration_Mins__c, Guard_Tour_Interval_Mins__c, Guard_Tour_Times__c
            FROM Installation__c 
            WHERE Install_Case__c = :caseID 
                AND Status__c != '5-Removed'
            ORDER BY Name ];
        if( installList.size() <= 0 ) {
            ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                , 'There are no installations for this case.' ) );
            return;
        }

        i = installList[ 0 ];

        for( Installation__c inst : installList ) {
            installSelectionMap.put( inst.ID, true );
        }
        selectAll = true;

        // populate the schedule
        initialization();
    }

    public void installationInitialization( SObject o ) {
        // retrieve the record
        installList = [ 
            SELECT ID, Name, Monitoring_Service__c, Time_Zone__c, Status__c, Unit__r.Name
                , Product__r.Name, Installed_Date__c, Site__c, Install_Case__c
                , Monday__c, Tuesday__c, Wednesday__c, Thursday__c, Friday__c, Saturday__c, Sunday__c
                , X24_Hour_Mon__c, X24_Hour_Tue__c, X24_Hour_Wed__c, X24_Hour_Thu__c
                , X24_Hour_Fri__c, X24_Hour_Sat__c, X24_Hour_Sun__c
                , AM_Start_Mon_Hour__c, AM_Start_Mon_Min__c, AM_Stop_Mon_Hour__c, AM_Stop_Mon_Min__c
                , PM_Start_Mon_Hour__c, PM_Start_Mon_Min__c, PM_Stop_Mon_Hour__c, PM_Stop_Mon_Min__c
                , AM_Start_Tue_Hour__c, AM_Start_Tue_Min__c, AM_Stop_Tue_Hour__c, AM_Stop_Tue_Min__c
                , PM_Start_Tue_Hour__c, PM_Start_Tue_Min__c, PM_Stop_Tue_Hour__c, PM_Stop_Tue_Min__c
                , AM_Start_Wed_Hour__c, AM_Start_Wed_Min__c, AM_Stop_Wed_Hour__c, AM_Stop_Wed_Min__c
                , PM_Start_Wed_Hour__c, PM_Start_Wed_Min__c, PM_Stop_Wed_Hour__c, PM_Stop_Wed_Min__c
                , AM_Start_Thu_Hour__c, AM_Start_Thu_Min__c, AM_Stop_Thu_Hour__c, AM_Stop_Thu_Min__c
                , PM_Start_Thu_Hour__c, PM_Start_Thu_Min__c, PM_Stop_Thu_Hour__c, PM_Stop_Thu_Min__c
                , AM_Start_Fri_Hour__c, AM_Start_Fri_Min__c, AM_Stop_Fri_Hour__c, AM_Stop_Fri_Min__c
                , PM_Start_Fri_Hour__c, PM_Start_Fri_Min__c, PM_Stop_Fri_Hour__c, PM_Stop_Fri_Min__c
                , AM_Start_Sat_Hour__c, AM_Start_Sat_Min__c, AM_Stop_Sat_Hour__c, AM_Stop_Sat_Min__c
                , PM_Start_Sat_Hour__c, PM_Start_Sat_Min__c, PM_Stop_Sat_Hour__c, PM_Stop_Sat_Min__c
                , AM_Start_Sun_Hour__c, AM_Start_Sun_Min__c, AM_Stop_Sun_Hour__c, AM_Stop_Sun_Min__c
                , PM_Start_Sun_Hour__c, PM_Start_Sun_Min__c, PM_Stop_Sun_Hour__c, PM_Stop_Sun_Min__c
                , Monitoring_Hours__c, Monitoring_Hours_Every_Week__c, Additional_Monitoring_Hours__c
                , Guard_Tour_Duration_Mins__c, Guard_Tour_Interval_Mins__c, Guard_Tour_Times__c
            FROM Installation__c 
            WHERE ID = :o.ID ];
        if( installList.size() <= 0 ) {
            ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                , 'There are no installations with this ID.' ) );
            return;
        }

        i = installList[ 0 ];

        // populate the schedule
        initialization();
    }

    public void initialization() {
        weekdayList = new List<DayOfWeek>();
        for( String dayName : dayOfWeekNames ) {
            String abbrev = dayName.left( 3 ).capitalize();
            DayOfWeek n = new DayOfWeek();
            n.day = dayName;
            n.daySelected = (Boolean) i.get( dayName.capitalize() + '__c' );
            n.startAM = (String) i.get( 'AM_Start_' + abbrev + '_Hour__c' );
            n.stopAM = (String) i.get( 'AM_Stop_' + abbrev + '_Hour__c' );
            n.startPM = (String) i.get( 'PM_Start_' + abbrev + '_Hour__c' );
            n.stopPM = (String) i.get( 'PM_Stop_' + abbrev + '_Hour__c' );
            n.startMinuteAM = (String) i.get( 'AM_Start_' + abbrev + '_Min__c' );
            n.stopMinuteAM = (String) i.get( 'AM_Stop_' + abbrev + '_Min__c' );
            n.startMinutePM = (String) i.get( 'PM_Start_' + abbrev + '_Min__c' );
            n.stopMinutePM = (String) i.get( 'PM_Stop_' + abbrev + '_Min__c' );
            n.twentyFourHour = (Boolean) i.get( 'X24_Hour_' + abbrev + '__c' );

            // populate the schedule with default values if empty
            if( n.daySelected == null ) {
                n.daySelected = ( dayName != 'SATURDAY' && dayName != 'SUNDAY' );
            }
            if( n.startAM == null ) {
                n.startAM = '00';
            }
            if( n.stopAM == null ) {
                n.stopAM = '06';
            }
            if( n.startPM == null ) {
                n.startPM = '19';
            }
            if( n.stopPM == null ) {
                n.stopPM = '23';
            }
            if( n.startMinuteAM == null ) {
                n.startMinuteAM = '00';
            }
            if( n.stopMinuteAM == null ) {
                n.stopMinuteAM = '00';
            }
            if( n.startMinutePM == null ) {
                n.startMinutePM = '00';
            }
            if( n.stopMinutePM == null ) {
                n.stopMinutePM = '59';
            }
            if( n.twentyFourHour == null ) {
                n.twentyFourHour = false;
            }
            weekdayList.add( n );
        }

    }

    public void changedMonitoringService() {
         initialization();
        if(i.Monitoring_Service__c == 'Client'){
            resetMonitoringIfClient();
        }
        
        system.debug( i.Monitoring_Service__c );

        if( i.Monitoring_Service__c != 'Virtual Guard Tour' ) {
            // NOTE:  this works but since the fields are bound inside of a unrendered panel
            // the old values will still show up instead of these zeroes
            i.Guard_Tour_Duration_Mins__c = 0;
            i.Guard_Tour_Interval_Mins__c = 0;
        }
        system.debug( i.Guard_Tour_Duration_Mins__c );
    }

    public void resetMonitoringIfClient() {
        if( i.Monitoring_Service__c == 'Client' ) {
            // unselect all days and check No Monitoring
            i.No_Monitoring__c = true;
            for( DayOfWeek n : weekdayList ) {
                system.debug( n.daySelected );
                n.daySelected = false;
                n.twentyFourHour = false;
            }
        }
    }

    public void clicked24Hour() {
        for( DayOfWeek n : weekdayList ) {
            system.debug( n.twentyFourHour );
            if( n.twentyFourHour ) {
                n.daySelected = true;
                i.No_Monitoring__c = false;
            }
        }
    }

    //public void clickedNoMonitoring() {
    //  if( i.No_Monitoring__c == false ) {
    //      return;
    //  }
    //  for( DayOfWeek n : weekdayList ) {
    //      system.debug( n.daySelected );
    //      n.daySelected = false;
    //      n.twentyFourHour = false;
    //  }
    //}

    public void clickedDay() {
        for( DayOfWeek n : weekdayList ) {
            system.debug( n.day + ' selected =' + n.daySelected );
            if( n.daySelected ) {
                i.No_Monitoring__c = false;
            } else {
                n.twentyFourHour = false;
            }
        }

    }

    public PageReference resetSelection() {
        for( Installation__c inst : installList ) {
            installSelectionMap.put( inst.ID, selectAll );
        }

        return null;
    }
    public PageReference syncSelection() {
        selectAll = true;
        for( Installation__c inst : installList ) {
            if( installSelectionMap.get( inst.ID ) == false ) {
                selectAll = false;
                break;
            }
        }

        return null;
    }

    public PageReference save() {
        // validate
        if( i.Monitoring_Service__c == null ) {
            ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                    , 'Monitoring Service is required' ) );
        }

        if( i.Time_Zone__c == null ) {
            ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                    , 'Time Zone is required' ) );
        }

        if( i.Monitoring_Service__c == 'Virtual Guard Tour' ) {
            if( i.Guard_Tour_Interval_Mins__c == null || i.Guard_Tour_Interval_Mins__c == 0 ) {
                ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                    , 'Interval in minutes is required for Virtual Guard Tour monitoring service' ) );
            }
            if( i.Guard_Tour_Duration_Mins__c == null || i.Guard_Tour_Duration_Mins__c == 0 ) {
                ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                    , 'Duration in minutes is required for Virtual Guard Tour monitoring service' ) );
            }
        }

        // if page is open for account or case, validate selection of installations
        if( objectType == 'Account' || objectType == 'Case' ) {
            Boolean noInstallationsSelected = true;
            for( Installation__c inst : installList ) {
                if( installSelectionMap.get( inst.ID ) == true ) {
                    noInstallationsSelected = false;
                    break;
                }
            }

            if( noInstallationsSelected ) {
                ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                    , 'Please select at least one installation' ) );
            }
        }
        
        //resetMonitoringIfClient();

        Decimal hourCount = 0;
        String addtlHours = '';
        Map<String, String> scheduleMap = new Map<String, String>();
        for( DayOfWeek n : weekdayList ) {
            // validate schedule
            if( n.startAM > n.stopAM ) {
                ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                    , n.day + ':  AM Stop Hour must be after AM Start Hour.' ) );
            }
            if( n.startPM > n.stopPM ) {
                ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                    , n.day + ':  PM Stop Hour must be after PM Start Hour.' ) );
            }
            if( n.startAM == n.stopAM && n.startMinuteAM > n.stopMinuteAM ) {
                ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                    , n.day + ':  AM Stop Hour/Minute must be after AM Start Hour/Minute.' ) );
            }
            if( n.startPM == n.stopPM && n.startMinutePM > n.stopMinutePM ) {
                ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                    , n.day + ':  PM Stop Hour/Minute must be after PM Start Hour/Minute.' ) );
            }

            // copy fields to the installation record
            String abbrev = n.day.left( 3 ).toLowercase().capitalize();
            i.put( n.day.capitalize() + '__c', n.daySelected );
            i.put( 'AM_Start_' + abbrev + '_Hour__c', n.startAM );
            i.put( 'AM_Stop_' + abbrev + '_Hour__c', n.stopAM );
            i.put( 'PM_Start_' + abbrev + '_Hour__c', n.startPM );
            i.put( 'PM_Stop_' + abbrev + '_Hour__c', n.stopPM );
            i.put( 'AM_Start_' + abbrev + '_Min__c', n.startMinuteAM );
            i.put( 'AM_Stop_' + abbrev + '_Min__c', n.stopMinuteAM );
            i.put( 'PM_Start_' + abbrev + '_Min__c', n.startMinutePM );
            i.put( 'PM_Stop_' + abbrev + '_Min__c', n.stopMinutePM );
            i.put( 'X24_Hour_' + abbrev + '__c', n.twentyFourHour );
        }

        if( ApexPages.getMessages().size() > 0 ) {
            return null;
        }

        setMonitoringDescriptions( i );

        List<Installation__c> installToUpdateList = new List<Installation__c>();

        // if page is open for account or case, copy selected installations
        if( objectType == 'Account' || objectType == 'Case' ) {
            // copy only the selected installations
            for( Installation__c inst : installList ) {
                if( installSelectionMap.get( inst.ID ) == true ) {
                    installToUpdateList.add( inst );
                }
            }

            // copy the schedule from the template installation to the selected installations
            List<String> fieldList = new List<String> {
                'Monitoring_Service__c', 'Time_Zone__c'
                , 'Monday__c', 'Tuesday__c', 'Wednesday__c', 'Thursday__c', 'Friday__c', 'Saturday__c', 'Sunday__c'
                , 'X24_Hour_Mon__c', 'X24_Hour_Tue__c', 'X24_Hour_Wed__c', 'X24_Hour_Thu__c'
                , 'X24_Hour_Fri__c', 'X24_Hour_Sat__c', 'X24_Hour_Sun__c'
                , 'AM_Start_Mon_Hour__c', 'AM_Start_Mon_Min__c', 'AM_Stop_Mon_Hour__c', 'AM_Stop_Mon_Min__c'
                , 'PM_Start_Mon_Hour__c', 'PM_Start_Mon_Min__c', 'PM_Stop_Mon_Hour__c', 'PM_Stop_Mon_Min__c'
                , 'AM_Start_Tue_Hour__c', 'AM_Start_Tue_Min__c', 'AM_Stop_Tue_Hour__c', 'AM_Stop_Tue_Min__c'
                , 'PM_Start_Tue_Hour__c', 'PM_Start_Tue_Min__c', 'PM_Stop_Tue_Hour__c', 'PM_Stop_Tue_Min__c'
                , 'AM_Start_Wed_Hour__c', 'AM_Start_Wed_Min__c', 'AM_Stop_Wed_Hour__c', 'AM_Stop_Wed_Min__c'
                , 'PM_Start_Wed_Hour__c', 'PM_Start_Wed_Min__c', 'PM_Stop_Wed_Hour__c', 'PM_Stop_Wed_Min__c'
                , 'AM_Start_Thu_Hour__c', 'AM_Start_Thu_Min__c', 'AM_Stop_Thu_Hour__c', 'AM_Stop_Thu_Min__c'
                , 'PM_Start_Thu_Hour__c', 'PM_Start_Thu_Min__c', 'PM_Stop_Thu_Hour__c', 'PM_Stop_Thu_Min__c'
                , 'AM_Start_Fri_Hour__c', 'AM_Start_Fri_Min__c', 'AM_Stop_Fri_Hour__c', 'AM_Stop_Fri_Min__c'
                , 'PM_Start_Fri_Hour__c', 'PM_Start_Fri_Min__c', 'PM_Stop_Fri_Hour__c', 'PM_Stop_Fri_Min__c'
                , 'AM_Start_Sat_Hour__c', 'AM_Start_Sat_Min__c', 'AM_Stop_Sat_Hour__c', 'AM_Stop_Sat_Min__c'
                , 'PM_Start_Sat_Hour__c', 'PM_Start_Sat_Min__c', 'PM_Stop_Sat_Hour__c', 'PM_Stop_Sat_Min__c'
                , 'AM_Start_Sun_Hour__c', 'AM_Start_Sun_Min__c', 'AM_Stop_Sun_Hour__c', 'AM_Stop_Sun_Min__c'
                , 'PM_Start_Sun_Hour__c', 'PM_Start_Sun_Min__c', 'PM_Stop_Sun_Hour__c', 'PM_Stop_Sun_Min__c'
                , 'Monitoring_Hours__c', 'Monitoring_Hours_Every_Week__c', 'Additional_Monitoring_Hours__c'
                , 'Guard_Tour_Duration_Mins__c', 'Guard_Tour_Interval_Mins__c', 'Guard_Tour_Times__c'
            };
            for( Installation__c inst : installToUpdateList ) {
                copyFields( i, inst, fieldList );
            }

        } else {
            installToUpdateList.add( i );
        }

        // save
        try {
            update installToUpdateList;
            /*if(i.Monitoring_Service__c == 'Live')
            {
                map<id,list<Installation__c>> instMap = new map<id,list<Installation__c>>();
                //set<string> installationids = new set<string>();
                set<string> oppIds = new set<string>();
                boolean isSatSelected = false;
                boolean isSunSelected = false;
                boolean isWeekEndSelected = false;
                
                for( DayOfWeek n : weekdayList ) 
                {
                    if(n.day == 'SATURDAY' && n.twentyFourHour == true)
                        isSatSelected = true;
                    if(n.day == 'SUNDAY' && n.twentyFourHour == true)
                        isSunSelected = true;
                }
                if(isSatSelected == true && isSunSelected == true)
                    isWeekEndSelected = true;
                
                system.debug('----isSatSelected -----'+isSatSelected +'--isSunSelected--'+ isSunSelected+'-isWeekEndSelected----'+isWeekEndSelected);                           
                for( Installation__c inst : installList ) 
                {
                    system.debug('---istrue----'+installSelectionMap.get( inst.ID ));
                   // installationids.add(string.valueof(inst.id));
                    oppIds.add(inst.Opportunity__c);
                    
                    
                }
                //map<id,string> OpIdWithInstallationId               = new map<id,string>();
                map<string,OpportunityLineItem> installationsmap    = new map<string,OpportunityLineItem>();
                set<string> existingInstallations                   = new set<string>();
                map<id,string> opportunityRecordType                = new map<id,string>();
                list<OpportunityLineItem> insertList                = new list<OpportunityLineItem>();
                list<OpportunityLineItem> updateLineItems           = new list<OpportunityLineItem>();
                
                for(OpportunityLineItem oli : [SELECT PriceBookEntry.Product2.Name,OpportunityId,Installation_Id__c,Remove_Item_From_NetSuite__c,Opportunity.RecordType.Name  
                                                FROM OpportunityLineItem WHERE OpportunityId IN:oppIds])
                {
                    //OpIdWithInstallationId.put(oli.Opportunity,oli.Installation_Id__c);
                    if(oli.Installation_Id__c != null && oli.Installation_Id__c != '' && oli.Remove_Item_From_NetSuite__c == false)
                        installationsmap.put(oli.Installation_Id__c,oli);
                    opportunityRecordType.put(oli.OpportunityId,oli.Opportunity.RecordType.Name );
                }
                set<string> pbeids = new set<string>{system.label.X24_Mobile_Weekend_Product,system.label.X24_Mobile_Sat_Product,system.label.X24_Mobile_Sun_Product,system.label.X24_Fixed_Weekend_Product,system.label.X24_Fixed_Sat_Product,system.label.X24_Fixed_Sun_Product};
                 map<id,Decimal> listprices = new map<id,Decimal>();
                 for(PriceBookEntry pe : [Select UnitPrice from PriceBookEntry where id In:pbeids])
                 {
                    listprices.put(pe.id,pe.UnitPrice);
                 }
                 system.debug('---installationsmap----'+installationsmap.size());
                for( Installation__c inst : installList ) 
                {
                    if( installSelectionMap.get( inst.ID ) == true ) 
                    {
                        
                         system.debug('---treu----'+installSelectionMap.get( inst.ID ));
                        boolean CancreateInstallationProd = true;
                        if(installationsmap.containsKey(string.valueof(inst.id)))
                        {
                            CancreateInstallationProd = false;
                              
                            if(isWeekEndSelected == true && (installationsmap.get(string.valueof(inst.ID)).PriceBookEntry.Product2.Name.CONTAINS('Saturday') || installationsmap.get(string.valueof(inst.ID)).PriceBookEntry.Product2.Name.CONTAINS('Sunday')) )
                            {
                                Opportunitylineitem oi = installationsmap.get(string.valueof(inst.ID));
                                oi.Remove_Item_From_NetSuite__c = true;
                                updatelineitems.add(oi);
                                CancreateInstallationProd = true;
                            }
                            else if(isSatSelected == true && (installationsmap.get(string.valueof(inst.ID)).PriceBookEntry.Product2.Name.CONTAINS('Weekend') || installationsmap.get(string.valueof(inst.ID)).PriceBookEntry.Product2.Name.CONTAINS('Sunday')) )
                            {
                                Opportunitylineitem oi = installationsmap.get(string.valueof(inst.ID));
                                oi.Remove_Item_From_NetSuite__c = true;
                                updatelineitems.add(oi);
                                CancreateInstallationProd = true;
                                
                            }
                            else if(isSunSelected == true && (installationsmap.get(string.valueof(inst.ID)).PriceBookEntry.Product2.Name.CONTAINS('Saturday') || installationsmap.get(string.valueof(inst.ID)).PriceBookEntry.Product2.Name.CONTAINS('Weekend')) )
                            {
                                Opportunitylineitem oi = installationsmap.get(string.valueof(inst.ID));
                                oi.Remove_Item_From_NetSuite__c = true;
                                updatelineitems.add(oi);
                                CancreateInstallationProd = true;
                            }
                            
                        }
                         system.debug('---CancreateInstallationProd ----'+CancreateInstallationProd );
                        if(CancreateInstallationProd)
                        {
                             if(isSatSelected == true || isSunSelected == true)
                             {
                                OpportunityLineItem i = new OpportunityLineItem();
                                if(opportunityRecordType.get(inst.Opportunity__c) == 'Mobile')
                                {
                                    if(isWeekEndSelected){
                                        i.PriceBookEntryId=system.label.X24_Mobile_Weekend_Product;
                                        i.UnitPrice = listprices.get(system.label.X24_Mobile_Weekend_Product);
                                    }
                                    else if(isSatSelected){
                                        i.PriceBookEntryId=system.label.X24_Mobile_Sat_Product;
                                        i.UnitPrice = listprices.get(system.label.X24_Mobile_Sat_Product);
                                    }
                                    else if(isSunSelected){
                                        i.PriceBookEntryId=system.label.X24_Mobile_Sun_Product;
                                        i.UnitPrice = listprices.get(system.label.X24_Mobile_Sun_Product);
                                    }
                                }
                                else
                                {
                                    if(isWeekEndSelected){
                                        i.PriceBookEntryId=system.label.X24_Fixed_Weekend_Product;
                                        i.UnitPrice = listprices.get(system.label.X24_Fixed_Weekend_Product);
                                    }
                                    else if(isSatSelected){
                                        i.PriceBookEntryId=system.label.X24_Fixed_Sat_Product;
                                        i.UnitPrice = listprices.get(system.label.X24_Fixed_Sat_Product);
                                    }
                                    else if(isSunSelected){
                                        i.PriceBookEntryId=system.label.X24_Fixed_Sun_Product;
                                        i.UnitPrice = listprices.get(system.label.X24_Fixed_Sun_Product);
                                    }
                                }
                                i.Quantity  =1;
                                //system.debug('----count is-----'+instCount.get(opid));
                                
                                
                                //i.UnitPrice = decimal.valueof(instCount.get(opid));
                                i.OpportunityId=inst.Opportunity__c;
                                i.Installation_Id__c=string.valueof(inst.id);
                                
                                insertList.add(i);
                             }
                        }
                    }
                    else{
                        if(installationsmap.containsKey(string.valueof(inst.id)))
                        {
                            OpportunityLineItem ol = installationsmap.get(string.valueof(inst.id));
                            ol.Remove_Item_From_NetSuite__c = true;
                            updateLineItems.add(ol);
                        }
                    }
                }
                if(!insertList.isEmpty())
                    insert insertList;
                if(!updateLineItems.isEmpty())
                    update updateLineItems;
            }*/
            
        } catch( Exception e ) {
            ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                , 'Problem found when updating the installation.' ) );
            ApexPages.addMessages( e );
            return null;
        }

        // notify about the installation
        EmailNotificationHelper notifyHelper = new EmailNotificationHelper();
        String errorMessage = notifyHelper.notifyOnInstallation( i.ID );
        if( errorMessage != null && errorMessage.length() > 2 ) {
            ApexPages.addMessage( new ApexPages.Message( ApexPages.Severity.ERROR
                    , 'The installation(s) has been saved but there was a problem found when sending notification.\n'
                    + 'Please notify the customer manually.\n\n'
                    + errorMessage ) );
            return null;
        }

        // return to record
        String theID = i.ID;
        if( objectType == 'Account' ) {
            theID = i.Site__c;
        }
        if( objectType == 'Case' ) {
            theID = i.Install_Case__c;
        }

        return new PageReference( '/' + theID );
    }

    public static void copyFields( SObject source, SObject target, List<String> fieldList ) {
        // copy all mapped fields to the target object
        for( String acctField : fieldList ) {
            target.put( acctField, source.get( acctField ) );
        }
    }

    public static Decimal getDecimalValueOf( String theValue ) {
        return ( theValue != null ? Decimal.valueOf( theValue ) : 0 );
    }

    public static void setMonitoringDescriptions( Installation__c i ) {

        Decimal hourCount = 0;
        String addtlHours = '';
        Map<String, String> scheduleMap = new Map<String, String>();
        for( String dayName : dayOfWeekNames ) {
            String abbrev = dayName.left( 3 ).toLowercase().capitalize();

            object objDaySelected = i.get( dayName.capitalize() + '__c' );
            Boolean daySelected = objDaySelected != null ? (Boolean) objDaySelected : false;
            System.debug('---> daySelected: ' + daySelected);
            System.debug('---> dayName: ' + dayName);
            if( ! daySelected ) {
                continue;
            }

            String startAM = (String) i.get( 'AM_Start_' + abbrev + '_Hour__c' );
            String stopAM = (String) i.get( 'AM_Stop_' + abbrev + '_Hour__c' );
            String startPM = (String) i.get( 'PM_Start_' + abbrev + '_Hour__c' );
            String stopPM = (String) i.get( 'PM_Stop_' + abbrev + '_Hour__c' );
            String startMinuteAM = (String) i.get( 'AM_Start_' + abbrev + '_Min__c' );
            String stopMinuteAM = (String) i.get( 'AM_Stop_' + abbrev + '_Min__c' );
            String startMinutePM = (String) i.get( 'PM_Start_' + abbrev + '_Min__c' );
            String stopMinutePM = (String) i.get( 'PM_Stop_' + abbrev + '_Min__c' );

            object obj24Hour = i.get( 'X24_Hour_' + abbrev + '__c' );
            Boolean twentyFourHour = obj24Hour != null ? (Boolean) obj24Hour : false;

            // compute how many hours in the day schedule and describe it
            Decimal duration = 0;
            if( twentyFourHour ) {
                duration = 24;

                // describe additional monitoring hours
                addtlHours = addtlHours + ', ' + abbrev;
            } else {

                // concatenate description of schedule and use it as a map key 
                // to see what days have the same schedule
                String thisDayHours = String.format( 
                    '{0}:{1} AM - {2}:{3} AM and {4}:{5} PM - {6}:{7} PM '
                    , new List<String> { startAM, startMinuteAM, stopAM, stopMinuteAM
                        , startPM, startMinutePM, stopPM, stopMinutePM } );
                String days = scheduleMap.get( thisDayHours );
                if( days == null ) {
                    days = '';
                }

                days += ',' + dayName.left( 3 ).toLowercase().capitalize();
                scheduleMap.put( thisDayHours, days );

                // compute how many hours in the day schedule
                duration = getDecimalValueOf( stopAM ) + getDecimalValueOf( stopMinuteAM ) / 60 
                        - ( getDecimalValueOf( startAM ) + getDecimalValueOf( startMinuteAM ) / 60 );
                if( startMinuteAM == '59' ) {
                    duration -= 1 / 60;
                }
                if( stopMinuteAM == '59' ) {
                    duration += 1 / 60;
                }
                system.debug( dayName + ' AM = ' + duration );
                duration += getDecimalValueOf( stopPM ) + getDecimalValueOf( stopMinutePM ) / 60 
                        - ( getDecimalValueOf( startPM ) + getDecimalValueOf( startMinutePM ) / 60 );
                if( startMinutePM == '59' ) {
                    duration -= 1 / 60;
                }
                if( stopMinutePM == '59' ) {
                    duration += 1 / 60;
                }
            }

            system.debug( dayName + ' AM+PM = ' + duration );
            hourCount += duration;
        }

        // prevent empty description
        if( i.Time_Zone__c == null ) {
            i.Time_Zone__c = 'CT';
        }

        // get distinct day schedules to compose a description
        String monHours = '';
        for( String aSchedule : scheduleMap.keySet() ) {
            String days = scheduleMap.get( aSchedule );
            days = days.substring( 1 ); // remove initial comma
            monHours += '; ' + aSchedule + days;
        }
        if( monHours.length() > 2 ) {
            monHours = monHours.substring( 2 );
        }
        monHours = monHours.replace( 'Mon,Tue,Wed,Thu,Fri,Sat,Sun', 'Mon through Sun' )
                        .replace( 'Mon,Tue,Wed,Thu,Fri,Sat', 'Mon through Sat' )
                        .replace( 'Mon,Tue,Wed,Thu,Fri', 'Mon through Fri' )
                        .replace( 'Mon,Tue,Wed,Thu', 'Mon through Thu' )
                        .replace( 'Mon,Tue,Wed', 'Mon through Wed' ) 
                        + ' (' + i.Time_Zone__c + ')';

        i.Monitoring_Hours__c = monHours;

        // store calculated # hours
        i.Monitoring_Hours_Every_Week__c = hourCount;

        if( addtlHours != '' ) {
            i.Additional_Monitoring_Hours__c = '24 hours on ' 
                + addtlHours.substring( 1 ).trim() + ' (' + i.Time_Zone__c + ')';
        } else {
            i.Additional_Monitoring_Hours__c = '';
        }

        // concatenate descriptions of schedule
        if( i.Monitoring_Service__c == 'Virtual Guard Tour' ) {
            i.Guard_Tour_Times__c = 'Every ' + i.Guard_Tour_Interval_Mins__c 
                    + ' minutes for ' + i.Guard_Tour_Duration_Mins__c + ' minutes';
        } else {
            i.Guard_Tour_Duration_Mins__c = 0;
            i.Guard_Tour_Interval_Mins__c = 0;
            i.Guard_Tour_Times__c = '';
        }
    }

    public PageReference cancel() {
        // return to record
        return new PageReference( '/' + i.ID );
    }
}